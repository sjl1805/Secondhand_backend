package com.example.secondhand_backend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.secondhand_backend.model.entity.Favorite;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.secondhand_backend.model.vo.ProductVO;


/**
* @author 28619
* @description 针对表【favorite(收藏表)】的数据库操作Service
* @createDate 2025-04-29 13:42:22
*/
public interface FavoriteService extends IService<Favorite> {
    
    /**
     * 添加收藏
     * @param userId 用户ID
     * @param productId 商品ID
     * @return 收藏ID
     */
    Long addFavorite(Long userId, Long productId);
    
    /**
     * 取消收藏
     * @param userId 用户ID
     * @param productId 商品ID
     */
    void cancelFavorite(Long userId, Long productId);
    
    /**
     * 检查是否已收藏
     * @param userId 用户ID
     * @param productId 商品ID
     * @return 是否已收藏
     */
    boolean isFavorite(Long userId, Long productId);
    
    /**
     * 获取用户收藏列表
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页数量
     * @return 收藏的商品列表
     */
    IPage<ProductVO> getUserFavorites(Long userId, int page, int size);
    
    /**
     * 获取商品的收藏数量
     * @param productId 商品ID
     * @return 收藏数量
     */
    int getProductFavoriteCount(Long productId);
}
