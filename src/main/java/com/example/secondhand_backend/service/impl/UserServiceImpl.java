package com.example.secondhand_backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.secondhand_backend.entity.domain.User;
import com.example.secondhand_backend.service.UserService;
import com.example.secondhand_backend.mapper.UserMapper;
import org.springframework.stereotype.Service;

/**
* @author 28619
* @description 针对表【user(用户表)】的数据库操作Service实现
* @createDate 2025-04-29 13:42:42
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

}




