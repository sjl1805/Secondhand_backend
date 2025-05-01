package com.example.secondhand_backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.secondhand_backend.model.entity.Product;
import com.example.secondhand_backend.model.entity.Recommendation;
import com.example.secondhand_backend.model.vo.ProductVO;

import java.util.List;
import java.util.Map;

/**
 * @author 28619
 * @description 针对表【recommendation(推荐表)】的数据库操作Service
 * @createDate 2025-04-29 13:42:36
 */
public interface RecommendationService extends IService<Recommendation> {

    /**
     * 基于用户的协同过滤推荐商品
     * 根据用户的收藏、浏览和购买行为，推荐相似用户喜欢的商品
     *
     * @param userId 用户ID
     * @param limit  返回推荐商品数量限制
     * @return 推荐商品列表
     */
    List<ProductVO> recommendProductsForUser(Long userId, int limit);

    /**
     * 基于物品的协同过滤推荐商品
     * 根据物品相似度，推荐与用户已收藏/购买商品相似的其他商品
     *
     * @param userId 用户ID
     * @param limit  返回推荐商品数量限制
     * @return 推荐商品列表
     */
    List<ProductVO> recommendSimilarProducts(Long userId, int limit);

    /**
     * 计算用户相似度矩阵
     *
     * @return 用户相似度矩阵
     */
    Map<Long, Map<Long, Double>> calculateUserSimilarityMatrix();

    /**
     * 计算商品相似度矩阵
     *
     * @return 商品相似度矩阵
     */
    Map<Long, Map<Long, Double>> calculateProductSimilarityMatrix();

    /**
     * 根据推荐结果，保存用户的推荐记录
     *
     * @param userId    用户ID
     * @param productId 推荐的商品ID
     * @return 是否保存成功
     */
    boolean saveRecommendation(Long userId, Long productId);

    /**
     * 更新用户的推荐列表
     *
     * @param userId 用户ID
     * @return 更新的推荐数量
     */
    int updateUserRecommendations(Long userId);
}
