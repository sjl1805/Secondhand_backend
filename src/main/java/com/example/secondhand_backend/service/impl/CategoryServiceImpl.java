package com.example.secondhand_backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.secondhand_backend.model.entity.Category;
import com.example.secondhand_backend.service.CategoryService;
import com.example.secondhand_backend.mapper.CategoryMapper;
import org.springframework.stereotype.Service;

/**
* @author 28619
* @description 针对表【category(商品分类表)】的数据库操作Service实现
* @createDate 2025-04-29 13:42:16
*/
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category>
    implements CategoryService{

}




