package com.example.secondhand_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.secondhand_backend.mapper.ProductImageMapper;
import com.example.secondhand_backend.model.entity.ProductImage;
import com.example.secondhand_backend.service.ProductImageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 28619
 * @description 针对表【product_image(商品图片表)】的数据库操作Service实现
 * @createDate 2025-04-29 13:42:34
 */
@Service
public class ProductImageServiceImpl extends ServiceImpl<ProductImageMapper, ProductImage>
        implements ProductImageService {

    @Override
    @Transactional
    public void saveProductImages(Long productId, List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return;
        }

        // 先删除已有的图片
        deleteProductImages(productId);

        // 添加新图片
        List<ProductImage> imageList = new ArrayList<>();
        for (int i = 0; i < imageUrls.size(); i++) {
            ProductImage image = new ProductImage();
            image.setProductId(productId);
            image.setImageUrl(imageUrls.get(i));
            image.setSort(i);
            imageList.add(image);
        }

        saveBatch(imageList);
    }

    @Override
    public List<String> getProductImages(Long productId) {
        LambdaQueryWrapper<ProductImage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductImage::getProductId, productId)
                .orderByAsc(ProductImage::getSort);

        List<ProductImage> imageList = list(queryWrapper);
        return imageList.stream()
                .map(ProductImage::getImageUrl)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteProductImages(Long productId) {
        LambdaQueryWrapper<ProductImage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductImage::getProductId, productId);
        remove(queryWrapper);
    }
}




