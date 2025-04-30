package com.example.secondhand_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.secondhand_backend.model.entity.User;
import com.example.secondhand_backend.model.dto.LoginDTO;
import com.example.secondhand_backend.model.dto.RegisterDTO;
import com.example.secondhand_backend.model.dto.UserInfoDTO;
import com.example.secondhand_backend.exception.BusinessException;
import com.example.secondhand_backend.mapper.UserMapper;
import com.example.secondhand_backend.service.UserService;
import com.example.secondhand_backend.utils.CaptchaUtils;
import com.example.secondhand_backend.utils.MD5Utils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
* @author 28619
* @description 针对表【user(用户表)】的数据库操作Service实现
* @createDate 2025-04-29 13:42:42
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private CaptchaUtils captchaUtils;

    @Override
    public User getByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return getOne(wrapper);
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
        return user;
    }

    @Override
    public boolean checkUsernameExists(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return count(wrapper) > 0;
    }

    @Override
    public User getUserInfo(Long userId) {
        User user = getById(userId);
        if (user == null) {
            throw BusinessException.usernameOrPasswordError();
        }
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
    }
    
    @Override
    public IPage<User> getUserList(int page, int size, String keyword) {
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
        Page<User> userPage = new Page<>(page, size);
        
        // 执行分页查询
        return page(userPage, queryWrapper);
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
    }
    
    @Override
    public List<User> getAdminList() {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getRole, 9)  // 角色为管理员
                   .eq(User::getDeleted, 0);  // 未删除
        
        return list(queryWrapper);
    }
    
    @Override
    public boolean isAdmin(Long userId) {
        if (userId == null) {
            return false;
        }
        
        User user = getById(userId);
        return user != null && user.getRole() == 9 && user.getDeleted() == 0;
    }
}




