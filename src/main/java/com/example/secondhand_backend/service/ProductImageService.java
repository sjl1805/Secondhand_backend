package com.example.secondhand_backend.service;

import com.example.secondhand_backend.model.entity.ProductImage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author 28619
* @description 针对表【product_image(商品图片表)】的数据库操作Service
* @createDate 2025-04-29 13:42:34
*/
public interface ProductImageService extends IService<ProductImage> {

    /**
     * 批量保存商品图片
     * @param productId 商品ID
     * @param imageUrls 图片URL列表
     */
    void saveProductImages(Long productId, List<String> imageUrls);
    
    /**
     * 获取商品图片
     * @param productId 商品ID
     * @return 图片URL列表
     */
    List<String> getProductImages(Long productId);
    
    /**
     * 删除商品图片
     * @param productId 商品ID
     */
    void deleteProductImages(Long productId);
}
