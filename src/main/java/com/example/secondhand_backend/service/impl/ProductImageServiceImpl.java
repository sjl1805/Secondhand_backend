package com.example.secondhand_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.secondhand_backend.mapper.ProductImageMapper;
import com.example.secondhand_backend.model.entity.ProductImage;
import com.example.secondhand_backend.service.ProductImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author 28619
 * @description 针对表【product_image(商品图片表)】的数据库操作Service实现
 * @createDate 2025-04-29 13:42:34
 */
@Service
public class ProductImageServiceImpl extends ServiceImpl<ProductImageMapper, ProductImage>
        implements ProductImageService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    private static final String PRODUCT_IMAGES_CACHE_PREFIX = "product:images:";
    private static final long CACHE_EXPIRE_TIME = 24; // 缓存过期时间（小时）

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
        
        // 更新缓存
        String cacheKey = PRODUCT_IMAGES_CACHE_PREFIX + productId;
        redisTemplate.opsForValue().set(cacheKey, imageUrls, CACHE_EXPIRE_TIME, TimeUnit.HOURS);
    }

    @Override
    public List<String> getProductImages(Long productId) {
        // 从缓存获取
        String cacheKey = PRODUCT_IMAGES_CACHE_PREFIX + productId;
        List<String> imageUrls = (List<String>) redisTemplate.opsForValue().get(cacheKey);
        
        if (imageUrls != null) {
            return imageUrls;
        }
        
        // 缓存未命中，查询数据库
        LambdaQueryWrapper<ProductImage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductImage::getProductId, productId)
                .orderByAsc(ProductImage::getSort);

        List<ProductImage> imageList = list(queryWrapper);
        imageUrls = imageList.stream()
                .map(ProductImage::getImageUrl)
                .collect(Collectors.toList());
        
        // 将结果存入缓存
        if (!imageUrls.isEmpty()) {
            redisTemplate.opsForValue().set(cacheKey, imageUrls, CACHE_EXPIRE_TIME, TimeUnit.HOURS);
        }
        
        return imageUrls;
    }

    @Override
    public void deleteProductImages(Long productId) {
        LambdaQueryWrapper<ProductImage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductImage::getProductId, productId);
        remove(queryWrapper);
        
        // 删除缓存
        String cacheKey = PRODUCT_IMAGES_CACHE_PREFIX + productId;
        redisTemplate.delete(cacheKey);
    }
}




