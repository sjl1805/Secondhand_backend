package com.example.secondhand_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
}




