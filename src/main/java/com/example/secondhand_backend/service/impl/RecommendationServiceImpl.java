package com.example.secondhand_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.secondhand_backend.mapper.RecommendationMapper;
import com.example.secondhand_backend.model.entity.*;
import com.example.secondhand_backend.model.vo.ProductVO;
import com.example.secondhand_backend.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author 28619
 * @description 针对表【recommendation(推荐表)】的数据库操作Service实现
 * @createDate 2025-04-29 13:42:36
 */
@Service
public class RecommendationServiceImpl extends ServiceImpl<RecommendationMapper, Recommendation>
        implements RecommendationService {

    // 用户行为权重
    private static final double FAVORITE_WEIGHT = 1.0;  // 收藏权重
    private static final double ORDER_WEIGHT = 2.0;     // 购买权重
    private static final double VIEW_WEIGHT = 0.5;      // 浏览权重
    // 缓存相关常量
    private static final String USER_RECOMMENDATIONS_CACHE_PREFIX = "recommendation:user:";
    private static final String SIMILAR_PRODUCTS_CACHE_PREFIX = "recommendation:similar:";
    private static final String USER_SIMILARITY_MATRIX_CACHE_KEY = "recommendation:user_similarity_matrix";
    private static final String PRODUCT_SIMILARITY_MATRIX_CACHE_KEY = "recommendation:product_similarity_matrix";
    private static final String POPULAR_PRODUCTS_CACHE_PREFIX = "recommendation:popular:";
    private static final String USER_INTERACTED_PRODUCTS_CACHE_PREFIX = "recommendation:user_interacted:";
    private static final long CACHE_EXPIRE_TIME = 24; // 缓存过期时间（小时）
    private static final long MATRIX_CACHE_EXPIRE_TIME = 72; // 相似度矩阵缓存过期时间（小时）
    @Autowired
    private FavoriteService favoriteService;
    @Autowired
    private OrdersService ordersService;
    @Autowired
    private ProductService productService;
    @Autowired
    private UserService userService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    // 相似度计算缓存
    private Map<Long, Map<Long, Double>> userSimilarityCache = new HashMap<>();
    private Map<Long, Map<Long, Double>> productSimilarityCache = new HashMap<>();

    @Override
    public List<ProductVO> recommendProductsForUser(Long userId, int limit) {
        // 先从Redis缓存中获取
        String cacheKey = USER_RECOMMENDATIONS_CACHE_PREFIX + userId + ":" + limit;
        List<ProductVO> recommendedProducts = (List<ProductVO>) redisTemplate.opsForValue().get(cacheKey);

        if (recommendedProducts != null) {
            return recommendedProducts;
        }

        // 缓存未命中，计算推荐商品
        // 1. 计算用户相似度矩阵（如果缓存为空）
        if (userSimilarityCache.isEmpty()) {
            userSimilarityCache = calculateUserSimilarityMatrix();
        }

        // 2. 获取与当前用户相似度最高的N个用户
        Map<Long, Double> userSimilarities = userSimilarityCache.getOrDefault(userId, new HashMap<>());
        if (userSimilarities.isEmpty()) {
            // 如果没有相似用户，返回热门商品
            recommendedProducts = getPopularProductsVO(limit);

            // 将结果存入Redis缓存
            redisTemplate.opsForValue().set(cacheKey, recommendedProducts, CACHE_EXPIRE_TIME, TimeUnit.HOURS);

            return recommendedProducts;
        }

        // 按相似度排序，获取最相似的10个用户
        List<Map.Entry<Long, Double>> sortedUsers = userSimilarities.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toList());

        // 3. 获取这些相似用户喜欢的商品（收藏、购买）
        Set<Long> currentUserProductIds = getUserInteractedProductIds(userId);
        Map<Long, Double> productScores = new HashMap<>();

        for (Map.Entry<Long, Double> userEntry : sortedUsers) {
            Long similarUserId = userEntry.getKey();
            Double similarity = userEntry.getValue();

            // 获取相似用户收藏的商品
            LambdaQueryWrapper<Favorite> favoriteQuery = new LambdaQueryWrapper<>();
            favoriteQuery.eq(Favorite::getUserId, similarUserId);
            List<Favorite> favorites = favoriteService.list(favoriteQuery);

            // 获取相似用户购买的商品
            LambdaQueryWrapper<Orders> orderQuery = new LambdaQueryWrapper<>();
            orderQuery.eq(Orders::getBuyerId, similarUserId);
            List<Orders> orders = ordersService.list(orderQuery);

            // 计算商品得分
            for (Favorite favorite : favorites) {
                Long productId = favorite.getProductId();
                if (!currentUserProductIds.contains(productId)) {
                    double score = similarity * FAVORITE_WEIGHT;
                    productScores.put(productId, productScores.getOrDefault(productId, 0.0) + score);
                }
            }

            for (Orders order : orders) {
                Long productId = order.getProductId();
                if (!currentUserProductIds.contains(productId)) {
                    double score = similarity * ORDER_WEIGHT;
                    productScores.put(productId, productScores.getOrDefault(productId, 0.0) + score);
                }
            }
        }

        // 4. 按照得分排序，获取推荐商品
        List<Map.Entry<Long, Double>> sortedProducts = productScores.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toList());

        // 5. 获取最终的推荐商品ID列表
        List<Long> recommendedProductIds = sortedProducts.stream()
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // 6. 保存推荐记录
        for (Long productId : recommendedProductIds) {
            saveRecommendation(userId, productId);
        }

        // 7. 返回商品详情
        if (recommendedProductIds.isEmpty()) {
            recommendedProducts = getPopularProductsVO(limit);
        } else {
            LambdaQueryWrapper<Product> productQuery = new LambdaQueryWrapper<>();
            productQuery.in(Product::getId, recommendedProductIds)
                    .eq(Product::getStatus, 1)  // 仅返回在售商品
                    .orderByDesc(Product::getViewCount);

            List<Product> products = productService.list(productQuery);
            recommendedProducts = products.stream()
                    .map(product -> productService.convertToProductVO(product, userId))
                    .collect(Collectors.toList());
        }

        // 将结果存入Redis缓存
        redisTemplate.opsForValue().set(cacheKey, recommendedProducts, CACHE_EXPIRE_TIME, TimeUnit.HOURS);

        return recommendedProducts;
    }

    @Override
    public List<ProductVO> recommendSimilarProducts(Long userId, int limit) {
        // 先从Redis缓存中获取
        String cacheKey = SIMILAR_PRODUCTS_CACHE_PREFIX + userId + ":" + limit;
        List<ProductVO> recommendedProducts = (List<ProductVO>) redisTemplate.opsForValue().get(cacheKey);

        if (recommendedProducts != null) {
            return recommendedProducts;
        }

        // 缓存未命中，计算推荐商品
        // 1. 计算商品相似度矩阵（如果缓存为空）
        if (productSimilarityCache.isEmpty()) {
            productSimilarityCache = calculateProductSimilarityMatrix();
        }

        // 2. 获取用户已交互过的商品
        Set<Long> interactedProductIds = getUserInteractedProductIds(userId);
        if (interactedProductIds.isEmpty()) {
            // 如果用户没有交互过任何商品，返回热门商品
            recommendedProducts = getPopularProductsVO(limit);

            // 将结果存入Redis缓存
            redisTemplate.opsForValue().set(cacheKey, recommendedProducts, CACHE_EXPIRE_TIME, TimeUnit.HOURS);

            return recommendedProducts;
        }

        // 3. 找出与用户交互过的商品相似的其他商品
        Map<Long, Double> productScores = new HashMap<>();

        for (Long interactedProductId : interactedProductIds) {
            Map<Long, Double> similarProducts = productSimilarityCache.getOrDefault(interactedProductId, new HashMap<>());
            for (Map.Entry<Long, Double> entry : similarProducts.entrySet()) {
                Long similarProductId = entry.getKey();
                Double similarity = entry.getValue();

                // 过滤掉用户已经交互过的商品
                if (!interactedProductIds.contains(similarProductId)) {
                    double currentScore = productScores.getOrDefault(similarProductId, 0.0);
                    productScores.put(similarProductId, currentScore + similarity);
                }
            }
        }

        // 4. 按照相似度得分排序
        List<Map.Entry<Long, Double>> sortedProducts = productScores.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toList());

        // 5. 获取最终的推荐商品ID列表
        List<Long> recommendedProductIds = sortedProducts.stream()
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // 6. 保存推荐记录
        for (Long productId : recommendedProductIds) {
            saveRecommendation(userId, productId);
        }

        // 7. 返回商品详情
        if (recommendedProductIds.isEmpty()) {
            recommendedProducts = getPopularProductsVO(limit);
        } else {
            LambdaQueryWrapper<Product> productQuery = new LambdaQueryWrapper<>();
            productQuery.in(Product::getId, recommendedProductIds)
                    .eq(Product::getStatus, 1)  // 仅返回在售商品
                    .orderByDesc(Product::getViewCount);

            List<Product> products = productService.list(productQuery);
            recommendedProducts = products.stream()
                    .map(product -> productService.convertToProductVO(product, userId))
                    .collect(Collectors.toList());
        }

        // 将结果存入Redis缓存
        redisTemplate.opsForValue().set(cacheKey, recommendedProducts, CACHE_EXPIRE_TIME, TimeUnit.HOURS);

        return recommendedProducts;
    }

    @Override
    public Map<Long, Map<Long, Double>> calculateUserSimilarityMatrix() {
        // 先从Redis缓存中获取
        Map<Long, Map<Long, Double>> userSimilarityMatrix =
                (Map<Long, Map<Long, Double>>) redisTemplate.opsForValue().get(USER_SIMILARITY_MATRIX_CACHE_KEY);

        if (userSimilarityMatrix != null) {
            return userSimilarityMatrix;
        }

        // 缓存未命中，计算相似度矩阵
        // 1. 获取所有用户
        List<User> allUsers = userService.list();
        userSimilarityMatrix = new HashMap<>();

        // 2. 初始化用户-商品交互矩阵
        Map<Long, Map<Long, Double>> userProductMatrix = new HashMap<>();

        for (User user : allUsers) {
            Long userId = user.getId();
            userProductMatrix.put(userId, getUserProductInteractions(userId));
        }

        // 3. 计算用户间相似度（余弦相似度）
        for (int i = 0; i < allUsers.size(); i++) {
            Long userA = allUsers.get(i).getId();
            Map<Long, Double> userAPreferences = userProductMatrix.get(userA);
            Map<Long, Double> similarityMap = new HashMap<>();

            for (int j = 0; j < allUsers.size(); j++) {
                if (i == j) continue;  // 跳过自身

                Long userB = allUsers.get(j).getId();
                Map<Long, Double> userBPreferences = userProductMatrix.get(userB);

                // 计算余弦相似度
                double similarity = calculateCosineSimilarity(userAPreferences, userBPreferences);
                if (similarity > 0) {
                    similarityMap.put(userB, similarity);
                }
            }

            userSimilarityMatrix.put(userA, similarityMap);
        }

        // 将结果存入Redis缓存
        redisTemplate.opsForValue().set(USER_SIMILARITY_MATRIX_CACHE_KEY, userSimilarityMatrix,
                MATRIX_CACHE_EXPIRE_TIME, TimeUnit.HOURS);

        return userSimilarityMatrix;
    }

    @Override
    public Map<Long, Map<Long, Double>> calculateProductSimilarityMatrix() {
        // 先从Redis缓存中获取
        Map<Long, Map<Long, Double>> productSimilarityMatrix =
                (Map<Long, Map<Long, Double>>) redisTemplate.opsForValue().get(PRODUCT_SIMILARITY_MATRIX_CACHE_KEY);

        if (productSimilarityMatrix != null) {
            return productSimilarityMatrix;
        }

        // 缓存未命中，计算相似度矩阵
        // 1. 获取所有商品
        List<Product> allProducts = productService.list();
        productSimilarityMatrix = new HashMap<>();

        // 2. 初始化商品-用户交互矩阵
        Map<Long, Map<Long, Double>> productUserMatrix = new HashMap<>();

        for (Product product : allProducts) {
            Long productId = product.getId();
            productUserMatrix.put(productId, getProductUserInteractions(productId));
        }

        // 3. 计算商品间相似度（余弦相似度）
        for (int i = 0; i < allProducts.size(); i++) {
            Long productA = allProducts.get(i).getId();
            Map<Long, Double> productAUsers = productUserMatrix.get(productA);
            Map<Long, Double> similarityMap = new HashMap<>();

            for (int j = 0; j < allProducts.size(); j++) {
                if (i == j) continue;  // 跳过自身

                Long productB = allProducts.get(j).getId();
                Map<Long, Double> productBUsers = productUserMatrix.get(productB);

                // 计算余弦相似度
                double similarity = calculateCosineSimilarity(productAUsers, productBUsers);
                if (similarity > 0) {
                    similarityMap.put(productB, similarity);
                }
            }

            productSimilarityMatrix.put(productA, similarityMap);
        }

        // 将结果存入Redis缓存
        redisTemplate.opsForValue().set(PRODUCT_SIMILARITY_MATRIX_CACHE_KEY, productSimilarityMatrix,
                MATRIX_CACHE_EXPIRE_TIME, TimeUnit.HOURS);

        return productSimilarityMatrix;
    }

    @Override
    @Transactional
    public boolean saveRecommendation(Long userId, Long productId) {
        // 检查是否已存在该推荐记录
        LambdaQueryWrapper<Recommendation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Recommendation::getUserId, userId)
                .eq(Recommendation::getProductId, productId);

        if (this.count(queryWrapper) > 0) {
            // 已存在推荐记录，无需重复保存
            return true;
        }

        // 创建新的推荐记录
        Recommendation recommendation = new Recommendation();
        recommendation.setUserId(userId);
        recommendation.setProductId(productId);

        return this.save(recommendation);
    }

    @Override
    @Transactional
    public int updateUserRecommendations(Long userId) {
        // 1. 删除用户现有的推荐记录
        LambdaQueryWrapper<Recommendation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Recommendation::getUserId, userId);
        this.remove(queryWrapper);

        // 2. 生成新的推荐
        List<ProductVO> userBasedRecommendations = recommendProductsForUser(userId, 5);
        List<ProductVO> itemBasedRecommendations = recommendSimilarProducts(userId, 5);

        // 3. 合并去重
        Set<Long> recommendedProductIds = new HashSet<>();
        int count = 0;

        for (ProductVO productVO : userBasedRecommendations) {
            if (recommendedProductIds.add(productVO.getId())) {
                saveRecommendation(userId, productVO.getId());
                count++;
            }
        }

        for (ProductVO productVO : itemBasedRecommendations) {
            if (recommendedProductIds.add(productVO.getId())) {
                saveRecommendation(userId, productVO.getId());
                count++;
            }
        }

        return count;
    }

    // 辅助方法：获取热门商品的VO对象
    private List<ProductVO> getPopularProductsVO(int limit) {
        // 从缓存获取
        String cacheKey = POPULAR_PRODUCTS_CACHE_PREFIX + limit;
        List<ProductVO> productVOList = (List<ProductVO>) redisTemplate.opsForValue().get(cacheKey);

        if (productVOList != null) {
            return productVOList;
        }

        // 缓存未命中，查询数据库
        List<Product> popularProducts = getPopularProducts(limit);
        productVOList = popularProducts.stream()
                .map(product -> productService.convertToProductVO(product, null))
                .collect(Collectors.toList());

        // 将结果存入缓存
        redisTemplate.opsForValue().set(cacheKey, productVOList, CACHE_EXPIRE_TIME, TimeUnit.HOURS);

        return productVOList;
    }

    // 辅助方法：获取热门商品
    private List<Product> getPopularProducts(int limit) {
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Product::getStatus, 1)  // 仅返回在售商品
                .orderByDesc(Product::getViewCount)
                .last("LIMIT " + limit);

        return productService.list(queryWrapper);
    }

    // 辅助方法：获取用户已交互的商品ID集合
    private Set<Long> getUserInteractedProductIds(Long userId) {
        // 从缓存获取
        String cacheKey = USER_INTERACTED_PRODUCTS_CACHE_PREFIX + userId;
        Set<Long> cachedProductIds = (Set<Long>) redisTemplate.opsForValue().get(cacheKey);

        if (cachedProductIds != null) {
            return cachedProductIds;
        }

        // 缓存未命中，查询数据库
        final Set<Long> productIds = new HashSet<>();

        // 获取用户收藏的商品
        LambdaQueryWrapper<Favorite> favoriteQuery = new LambdaQueryWrapper<>();
        favoriteQuery.eq(Favorite::getUserId, userId);
        List<Favorite> favorites = favoriteService.list(favoriteQuery);

        // 获取用户购买的商品
        LambdaQueryWrapper<Orders> orderQuery = new LambdaQueryWrapper<>();
        orderQuery.eq(Orders::getBuyerId, userId);
        List<Orders> orders = ordersService.list(orderQuery);

        // 添加所有商品ID
        favorites.forEach(favorite -> productIds.add(favorite.getProductId()));
        orders.forEach(order -> productIds.add(order.getProductId()));

        // 将结果存入缓存
        redisTemplate.opsForValue().set(cacheKey, productIds, CACHE_EXPIRE_TIME, TimeUnit.HOURS);

        return productIds;
    }

    // 辅助方法：获取用户-商品交互矩阵
    private Map<Long, Double> getUserProductInteractions(Long userId) {
        Map<Long, Double> interactions = new HashMap<>();

        // 获取用户收藏的商品
        LambdaQueryWrapper<Favorite> favoriteQuery = new LambdaQueryWrapper<>();
        favoriteQuery.eq(Favorite::getUserId, userId);
        List<Favorite> favorites = favoriteService.list(favoriteQuery);

        // 获取用户购买的商品
        LambdaQueryWrapper<Orders> orderQuery = new LambdaQueryWrapper<>();
        orderQuery.eq(Orders::getBuyerId, userId);
        List<Orders> orders = ordersService.list(orderQuery);

        // 收藏商品的权重
        for (Favorite favorite : favorites) {
            Long productId = favorite.getProductId();
            interactions.put(productId, FAVORITE_WEIGHT);
        }

        // 购买商品的权重（如果已经收藏了，权重累加）
        for (Orders order : orders) {
            Long productId = order.getProductId();
            double currentWeight = interactions.getOrDefault(productId, 0.0);
            interactions.put(productId, currentWeight + ORDER_WEIGHT);
        }

        return interactions;
    }

    // 辅助方法：获取商品-用户交互矩阵
    private Map<Long, Double> getProductUserInteractions(Long productId) {
        Map<Long, Double> interactions = new HashMap<>();

        // 获取收藏该商品的用户
        LambdaQueryWrapper<Favorite> favoriteQuery = new LambdaQueryWrapper<>();
        favoriteQuery.eq(Favorite::getProductId, productId);
        List<Favorite> favorites = favoriteService.list(favoriteQuery);

        // 获取购买该商品的用户
        LambdaQueryWrapper<Orders> orderQuery = new LambdaQueryWrapper<>();
        orderQuery.eq(Orders::getProductId, productId);
        List<Orders> orders = ordersService.list(orderQuery);

        // 收藏用户的权重
        for (Favorite favorite : favorites) {
            Long userId = favorite.getUserId();
            interactions.put(userId, FAVORITE_WEIGHT);
        }

        // 购买用户的权重（如果已经收藏了，权重累加）
        for (Orders order : orders) {
            Long userId = order.getBuyerId();
            double currentWeight = interactions.getOrDefault(userId, 0.0);
            interactions.put(userId, currentWeight + ORDER_WEIGHT);
        }

        return interactions;
    }

    // 辅助方法：计算余弦相似度
    private double calculateCosineSimilarity(Map<Long, Double> vectorA, Map<Long, Double> vectorB) {
        if (vectorA.isEmpty() || vectorB.isEmpty()) {
            return 0.0;
        }

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        // 计算点积
        for (Map.Entry<Long, Double> entryA : vectorA.entrySet()) {
            Long key = entryA.getKey();
            if (vectorB.containsKey(key)) {
                dotProduct += entryA.getValue() * vectorB.get(key);
            }
        }

        // 计算向量A的范数
        for (Double value : vectorA.values()) {
            normA += Math.pow(value, 2);
        }
        normA = Math.sqrt(normA);

        // 计算向量B的范数
        for (Double value : vectorB.values()) {
            normB += Math.pow(value, 2);
        }
        normB = Math.sqrt(normB);

        // 避免除以零
        if (normA == 0 || normB == 0) {
            return 0.0;
        }

        // 计算相似度
        return dotProduct / (normA * normB);
    }

    // 辅助方法：刷新用户交互商品的缓存
    private void refreshUserInteractedProductsCache(Long userId) {
        String cacheKey = USER_INTERACTED_PRODUCTS_CACHE_PREFIX + userId;
        redisTemplate.delete(cacheKey);
        // 重新加载缓存
        getUserInteractedProductIds(userId);
    }

    // 辅助方法：刷新热门商品缓存
    private void refreshPopularProductsCache() {
        // 删除所有热门商品缓存
        Set<String> keys = redisTemplate.keys(POPULAR_PRODUCTS_CACHE_PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    // 辅助方法：刷新所有推荐缓存
    public void refreshAllRecommendationCaches() {
        // 删除用户推荐缓存
        Set<String> userRecommendationKeys = redisTemplate.keys(USER_RECOMMENDATIONS_CACHE_PREFIX + "*");
        if (userRecommendationKeys != null && !userRecommendationKeys.isEmpty()) {
            redisTemplate.delete(userRecommendationKeys);
        }

        // 删除相似商品缓存
        Set<String> similarProductsKeys = redisTemplate.keys(SIMILAR_PRODUCTS_CACHE_PREFIX + "*");
        if (similarProductsKeys != null && !similarProductsKeys.isEmpty()) {
            redisTemplate.delete(similarProductsKeys);
        }

        // 删除相似矩阵缓存
        redisTemplate.delete(USER_SIMILARITY_MATRIX_CACHE_KEY);
        redisTemplate.delete(PRODUCT_SIMILARITY_MATRIX_CACHE_KEY);

        // 刷新热门商品缓存
        refreshPopularProductsCache();
    }
}




