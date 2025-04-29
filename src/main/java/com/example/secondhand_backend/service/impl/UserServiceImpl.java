package com.example.secondhand_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.secondhand_backend.entity.domain.User;
import com.example.secondhand_backend.mapper.UserMapper;
import com.example.secondhand_backend.service.UserService;
import org.springframework.stereotype.Service;

/**
* @author 28619
* @description 针对表【user(用户表)】的数据库操作Service实现
* @createDate 2025-04-29 13:42:42
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public User getByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return getOne(wrapper);
    }
}




