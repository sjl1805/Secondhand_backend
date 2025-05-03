package com.example.secondhand_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.secondhand_backend.exception.BusinessException;
import com.example.secondhand_backend.mapper.UserMapper;
import com.example.secondhand_backend.model.dto.LoginDTO;
import com.example.secondhand_backend.model.dto.PasswordUpdateDTO;
import com.example.secondhand_backend.model.dto.RegisterDTO;
import com.example.secondhand_backend.model.dto.UserInfoDTO;
import com.example.secondhand_backend.model.entity.User;
import com.example.secondhand_backend.service.UserService;
import com.example.secondhand_backend.utils.CaptchaUtils;
import com.example.secondhand_backend.utils.MD5Utils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author 28619
 * @description 针对表【user(用户表)】的数据库操作Service实现
 * @createDate 2025-04-29 13:42:42
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    // 缓存相关常量
    private static final String USER_CACHE_PREFIX = "user:info:";
    private static final String USER_BY_USERNAME_CACHE_PREFIX = "user:username:";
    private static final String USER_LIST_CACHE_PREFIX = "user:list:";
    private static final String ADMIN_LIST_CACHE_KEY = "user:admin:list";
    private static final long CACHE_EXPIRE_TIME = 24; // 缓存过期时间（小时）
    @Autowired
    private CaptchaUtils captchaUtils;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public User getByUsername(String username) {
        // 尝试从缓存获取
        String cacheKey = USER_BY_USERNAME_CACHE_PREFIX + username;
        User user = (User) redisTemplate.opsForValue().get(cacheKey);

        if (user != null) {
            return user;
        }

        // 缓存未命中，查询数据库
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        user = getOne(wrapper);

        // 存入缓存
        if (user != null) {
            redisTemplate.opsForValue().set(cacheKey, user, CACHE_EXPIRE_TIME, TimeUnit.HOURS);
            // 同时更新用户ID缓存
            redisTemplate.opsForValue().set(USER_CACHE_PREFIX + user.getId(), user, CACHE_EXPIRE_TIME, TimeUnit.HOURS);
        }

        return user;
    }

    @Override
    public User login(LoginDTO loginDTO) {
        // 验证验证码
        if (!captchaUtils.verifyCaptcha(loginDTO.getCaptchaKey(), loginDTO.getCaptcha())) {
            throw BusinessException.captchaError();
        }

        User user = getByUsername(loginDTO.getUsername());
        if (user == null) {
            throw BusinessException.usernameOrPasswordError();
        }
        if (!MD5Utils.verify(loginDTO.getPassword(), user.getPassword())) {
            throw BusinessException.usernameOrPasswordError();
        }
        return user;
    }

    @Override
    @Transactional
    public User register(RegisterDTO registerDTO) {
        // 验证验证码
        if (!captchaUtils.verifyCaptcha(registerDTO.getCaptchaKey(), registerDTO.getCaptcha())) {
            throw BusinessException.captchaError();
        }

        // 检查用户名是否已存在
        if (checkUsernameExists(registerDTO.getUsername())) {
            throw BusinessException.usernameExists();
        }

        // 创建新用户
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(MD5Utils.encrypt(registerDTO.getPassword()));
        user.setNickname(registerDTO.getNickname());
        user.setPhone(registerDTO.getPhone());
        user.setEmail(registerDTO.getEmail());
        user.setRole(0); // 默认普通用户
        user.setCreditScore(100); // 默认信用分

        // 保存用户
        save(user);

        // 清除用户列表缓存
        clearUserListCache();

        return user;
    }

    @Override
    public boolean checkUsernameExists(String username) {
        // 尝试从缓存获取
        String cacheKey = USER_BY_USERNAME_CACHE_PREFIX + username;
        User user = (User) redisTemplate.opsForValue().get(cacheKey);

        if (user != null) {
            return true;
        }

        // 缓存未命中，查询数据库
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return count(wrapper) > 0;
    }

    @Override
    public User getUserInfo(Long userId) {
        // 尝试从缓存获取
        String cacheKey = USER_CACHE_PREFIX + userId;
        User user = (User) redisTemplate.opsForValue().get(cacheKey);

        if (user != null) {
            return user;
        }

        // 缓存未命中，查询数据库
        user = getById(userId);
        if (user == null) {
            throw BusinessException.usernameOrPasswordError();
        }

        // 存入缓存
        redisTemplate.opsForValue().set(cacheKey, user, CACHE_EXPIRE_TIME, TimeUnit.HOURS);

        return user;
    }

    @Override
    @Transactional
    public void updateUserInfo(Long userId, UserInfoDTO userInfoDTO) {
        User user = getById(userId);
        if (user == null) {
            throw BusinessException.usernameOrPasswordError();
        }

        BeanUtils.copyProperties(userInfoDTO, user);
        updateById(user);

        // 更新缓存
        updateUserCache(user);
    }

    @Override
    @Transactional
    public void updatePassword(Long userId, PasswordUpdateDTO passwordUpdateDTO) {
        // 获取用户信息
        User user = getById(userId);
        if (user == null) {
            throw BusinessException.usernameOrPasswordError();
        }

        // 校验旧密码
        if (!MD5Utils.verify(passwordUpdateDTO.getOldPassword(), user.getPassword())) {
            throw new BusinessException("旧密码不正确");
        }

        // 新密码不能与旧密码相同
        if (passwordUpdateDTO.getOldPassword().equals(passwordUpdateDTO.getNewPassword())) {
            throw new BusinessException("新密码不能与旧密码相同");
        }

        // 更新密码
        user.setPassword(MD5Utils.encrypt(passwordUpdateDTO.getNewPassword()));
        updateById(user);

        // 更新缓存
        updateUserCache(user);
    }

    @Override
    public IPage<User> getUserList(int page, int size, String keyword) {
        // 尝试从缓存获取
        String cacheKey = USER_LIST_CACHE_PREFIX + page + ":" + size + ":" + (keyword == null ? "" : keyword);
        IPage<User> userPage = (IPage<User>) redisTemplate.opsForValue().get(cacheKey);

        if (userPage != null) {
            return userPage;
        }

        // 缓存未命中，查询数据库
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();

        // 只查询未删除的用户
        queryWrapper.eq(User::getDeleted, 0);

        // 如果有关键词，添加搜索条件
        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(wrapper ->
                    wrapper.like(User::getUsername, keyword)
                            .or()
                            .like(User::getNickname, keyword)
                            .or()
                            .like(User::getPhone, keyword)
                            .or()
                            .like(User::getEmail, keyword)
            );
        }

        // 按创建时间倒序排序
        queryWrapper.orderByDesc(User::getCreateTime);

        // 创建分页对象
        Page<User> pageParam = new Page<>(page, size);

        // 执行分页查询
        userPage = page(pageParam, queryWrapper);

        // 存入缓存，分页数据变化较快，设置较短的过期时间
        redisTemplate.opsForValue().set(cacheKey, userPage, 1, TimeUnit.HOURS);

        return userPage;
    }

    @Override
    public User getSellerInfo(Long sellerId) {
        // 尝试从缓存获取
        String cacheKey = USER_CACHE_PREFIX + sellerId;
        User seller = (User) redisTemplate.opsForValue().get(cacheKey);

        if (seller != null && seller.getDeleted() == 0) {
            return seller;
        }

        // 缓存未命中或已删除，查询数据库
        seller = getById(sellerId);
        if (seller == null || seller.getDeleted() == 1) {
            throw new BusinessException("卖家不存在或已删除");
        }

        // 存入缓存
        redisTemplate.opsForValue().set(cacheKey, seller, CACHE_EXPIRE_TIME, TimeUnit.HOURS);

        return seller;
    }

    @Override
    @Transactional
    public void updateUserStatus(Long userId, Integer creditScore, Integer role, Long operatorId) {
        // 验证操作者是否为管理员
        if (!isAdmin(operatorId)) {
            throw new BusinessException("无权限执行此操作，需要管理员权限");
        }

        // 获取用户
        User user = getById(userId);
        if (user == null || user.getDeleted() == 1) {
            throw new BusinessException("用户不存在或已删除");
        }

        // 管理员不能修改其他管理员
        if (user.getRole() == 9 && !userId.equals(operatorId)) {
            throw new BusinessException("无法修改其他管理员的状态");
        }

        // 更新用户信息
        if (creditScore != null) {
            user.setCreditScore(creditScore);
        }

        if (role != null) {
            user.setRole(role);
        }

        // 保存更新
        updateById(user);

        // 更新缓存
        updateUserCache(user);

        // 如果角色变为管理员或从管理员变为普通用户，清除管理员列表缓存
        if (role != null && (role == 9 || user.getRole() == 9)) {
            redisTemplate.delete(ADMIN_LIST_CACHE_KEY);
        }
    }

    @Override
    @Transactional
    public void deleteUser(Long userId, Long operatorId) {
        // 验证操作者是否为管理员
        if (!isAdmin(operatorId)) {
            throw new BusinessException("无权限执行此操作，需要管理员权限");
        }

        // 获取用户
        User user = getById(userId);
        if (user == null || user.getDeleted() == 1) {
            throw new BusinessException("用户不存在或已删除");
        }

        // 管理员不能删除其他管理员
        if (user.getRole() == 9 && !userId.equals(operatorId)) {
            throw new BusinessException("无法删除其他管理员账号");
        }

        // 逻辑删除用户
        user.setDeleted(1);
        updateById(user);

        // 更新缓存
        updateUserCache(user);

        // 清除用户列表缓存
        clearUserListCache();

        // 如果是管理员，清除管理员列表缓存
        if (user.getRole() == 9) {
            redisTemplate.delete(ADMIN_LIST_CACHE_KEY);
        }
    }

    @Override
    @Transactional
    public void resetUserPassword(Long userId, String newPassword, Long operatorId) {
        // 验证操作者是否为管理员
        if (!isAdmin(operatorId)) {
            throw new BusinessException("无权限执行此操作，需要管理员权限");
        }

        // 获取用户
        User user = getById(userId);
        if (user == null || user.getDeleted() == 1) {
            throw new BusinessException("用户不存在或已删除");
        }

        // 管理员不能重置其他管理员的密码
        if (user.getRole() == 9 && !userId.equals(operatorId)) {
            throw new BusinessException("无法重置其他管理员的密码");
        }

        // 更新密码
        user.setPassword(MD5Utils.encrypt(newPassword));
        updateById(user);

        // 更新缓存
        updateUserCache(user);
    }

    @Override
    public List<User> getAdminList() {
        // 尝试从缓存获取
        List<User> adminList = (List<User>) redisTemplate.opsForValue().get(ADMIN_LIST_CACHE_KEY);

        if (adminList != null) {
            return adminList;
        }

        // 缓存未命中，查询数据库
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getRole, 9)  // 角色为管理员
                .eq(User::getDeleted, 0);  // 未删除

        adminList = list(queryWrapper);

        // 存入缓存
        redisTemplate.opsForValue().set(ADMIN_LIST_CACHE_KEY, adminList, CACHE_EXPIRE_TIME, TimeUnit.HOURS);

        return adminList;
    }

    @Override
    public boolean isAdmin(Long userId) {
        if (userId == null) {
            return false;
        }

        // 从缓存获取用户信息
        User user = getUserInfo(userId);
        return user != null && user.getRole() == 9 && user.getDeleted() == 0;
    }

    @Override
    public List<User> searchUsers(String keyword) {
        // 此方法返回空列表，未实现实际功能
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.and(wrapper ->
                wrapper.like(User::getUsername, keyword)
                        .or()
                        .like(User::getNickname, keyword)
                        .or()
        );
        return List.of();
    }

    /**
     * 更新用户缓存
     *
     * @param user 用户对象
     */
    private void updateUserCache(User user) {
        if (user != null) {
            // 更新用户ID缓存
            String userIdCacheKey = USER_CACHE_PREFIX + user.getId();
            redisTemplate.opsForValue().set(userIdCacheKey, user, CACHE_EXPIRE_TIME, TimeUnit.HOURS);

            // 更新用户名缓存
            String usernameCacheKey = USER_BY_USERNAME_CACHE_PREFIX + user.getUsername();
            redisTemplate.opsForValue().set(usernameCacheKey, user, CACHE_EXPIRE_TIME, TimeUnit.HOURS);
        }
    }

    /**
     * 清除用户列表缓存
     */
    private void clearUserListCache() {
        Set<String> keys = redisTemplate.keys(USER_LIST_CACHE_PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    /**
     * 刷新所有用户缓存
     */
    public void refreshAllUserCache() {
        // 清除所有用户相关缓存
        Set<String> userKeys = redisTemplate.keys("user:*");
        if (userKeys != null && !userKeys.isEmpty()) {
            redisTemplate.delete(userKeys);
        }
    }
}




