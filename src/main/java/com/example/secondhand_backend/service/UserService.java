package com.example.secondhand_backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.secondhand_backend.model.entity.User;
import com.example.secondhand_backend.model.dto.LoginDTO;
import com.example.secondhand_backend.model.dto.RegisterDTO;
import com.example.secondhand_backend.model.dto.UserInfoDTO;

/**
* @author 28619
* @description 针对表【user(用户表)】的数据库操作Service
* @createDate 2025-04-29 13:42:42
*/
public interface UserService extends IService<User> {
    /**
     * 根据用户名查询用户
     */
    User getByUsername(String username);

    /**
     * 用户登录
     */
    User login(LoginDTO loginDTO);

    /**
     * 用户注册
     */
    User register(RegisterDTO registerDTO);

    /**
     * 检查用户名是否已存在
     */
    boolean checkUsernameExists(String username);

    /**
     * 获取用户信息
     */
    User getUserInfo(Long userId);

    /**
     * 更新用户信息
     */
    void updateUserInfo(Long userId, UserInfoDTO userInfoDTO);
}
