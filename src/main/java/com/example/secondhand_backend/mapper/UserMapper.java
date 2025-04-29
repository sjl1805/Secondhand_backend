package com.example.secondhand_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.secondhand_backend.model.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
* @author 28619
* @description 针对表【user(用户表)】的数据库操作Mapper
* @createDate 2025-04-29 13:42:42
* @Entity generator.domain.User
*/
@Mapper
public interface UserMapper extends BaseMapper<User> {

}




