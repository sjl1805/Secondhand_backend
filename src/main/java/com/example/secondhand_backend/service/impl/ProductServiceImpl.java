package com.example.secondhand_backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.secondhand_backend.model.entity.Product;
import com.example.secondhand_backend.service.ProductService;
import com.example.secondhand_backend.mapper.ProductMapper;
import org.springframework.stereotype.Service;

/**
* @author 28619
* @description 针对表【product(商品表)】的数据库操作Service实现
* @createDate 2025-04-29 13:42:31
*/
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product>
    implements ProductService{

}




