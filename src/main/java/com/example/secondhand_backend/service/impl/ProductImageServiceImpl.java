package com.example.secondhand_backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.secondhand_backend.model.entity.ProductImage;
import com.example.secondhand_backend.service.ProductImageService;
import com.example.secondhand_backend.mapper.ProductImageMapper;
import org.springframework.stereotype.Service;

/**
* @author 28619
* @description 针对表【product_image(商品图片表)】的数据库操作Service实现
* @createDate 2025-04-29 13:42:34
*/
@Service
public class ProductImageServiceImpl extends ServiceImpl<ProductImageMapper, ProductImage>
    implements ProductImageService{

}




