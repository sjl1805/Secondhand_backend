package com.example.secondhand_backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.secondhand_backend.dto.LoginDTO;
import com.example.secondhand_backend.dto.RegisterDTO;
import com.example.secondhand_backend.entity.domain.User;

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
}
