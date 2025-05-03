package com.example.secondhand_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.secondhand_backend.exception.BusinessException;
import com.example.secondhand_backend.mapper.FavoriteMapper;
import com.example.secondhand_backend.mapper.ProductMapper;
import com.example.secondhand_backend.model.entity.Favorite;
import com.example.secondhand_backend.model.entity.Product;
import com.example.secondhand_backend.model.vo.ProductVO;
import com.example.secondhand_backend.service.FavoriteService;
import com.example.secondhand_backend.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author 28619
 * @description 针对表【favorite(收藏表)】的数据库操作Service实现
 * @createDate 2025-04-29 13:42:22
 */
@Service
public class FavoriteServiceImpl extends ServiceImpl<FavoriteMapper, Favorite>
        implements FavoriteService {

    private static final String USER_FAVORITES_CACHE_PREFIX = "favorite:user:";
    private static final String IS_FAVORITE_CACHE_PREFIX = "favorite:is:";
    private static final String PRODUCT_FAVORITE_COUNT_CACHE_PREFIX = "favorite:count:";
    private static final long CACHE_EXPIRE_TIME = 24; // 缓存过期时间（小时）
    @Autowired
    @Lazy
    private ProductService productService;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public Long addFavorite(Long userId, Long productId) {
        // 检查商品是否存在
        Product product = productMapper.selectById(productId);
        if (product == null || product.getDeleted() == 1) {
            throw new BusinessException("商品不存在或已删除");
        }

        // 检查是否已收藏
        if (isFavorite(userId, productId)) {
            throw new BusinessException("已收藏该商品");
        }

        // 添加收藏记录
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setProductId(productId);
        save(favorite);

        // 更新缓存
        String isFavoriteCacheKey = getIsFavoriteCacheKey(userId, productId);
        redisTemplate.opsForValue().set(isFavoriteCacheKey, true, CACHE_EXPIRE_TIME, TimeUnit.HOURS);

        // 清除用户收藏列表缓存和商品收藏数缓存
        clearFavoriteCache(userId, productId);

        return favorite.getId();
    }

    @Override
    public void cancelFavorite(Long userId, Long productId) {
        // 查询收藏记录
        LambdaQueryWrapper<Favorite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Favorite::getUserId, userId)
                .eq(Favorite::getProductId, productId);

        // 删除收藏记录
        remove(queryWrapper);

        // 更新缓存
        String isFavoriteCacheKey = getIsFavoriteCacheKey(userId, productId);
        redisTemplate.opsForValue().set(isFavoriteCacheKey, false, CACHE_EXPIRE_TIME, TimeUnit.HOURS);

        // 清除用户收藏列表缓存和商品收藏数缓存
        clearFavoriteCache(userId, productId);
    }

    @Override
    public boolean isFavorite(Long userId, Long productId) {
        // 从缓存获取
        String cacheKey = getIsFavoriteCacheKey(userId, productId);
        Boolean isFavorite = (Boolean) redisTemplate.opsForValue().get(cacheKey);

        if (isFavorite != null) {
            return isFavorite;
        }

        // 缓存未命中，查询数据库
        LambdaQueryWrapper<Favorite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Favorite::getUserId, userId)
                .eq(Favorite::getProductId, productId);

        isFavorite = count(queryWrapper) > 0;

        // 将结果存入缓存
        redisTemplate.opsForValue().set(cacheKey, isFavorite, CACHE_EXPIRE_TIME, TimeUnit.HOURS);

        return isFavorite;
    }

    @Override
    public IPage<ProductVO> getUserFavorites(Long userId, int page, int size) {
        // 从缓存获取
        String cacheKey = USER_FAVORITES_CACHE_PREFIX + userId + ":" + page + ":" + size;
        IPage<ProductVO> productVOPage = (IPage<ProductVO>) redisTemplate.opsForValue().get(cacheKey);

        if (productVOPage != null) {
            return productVOPage;
        }

        // 缓存未命中，查询数据库
        // 分页查询用户收藏记录
        LambdaQueryWrapper<Favorite> favoriteQueryWrapper = new LambdaQueryWrapper<>();
        favoriteQueryWrapper.eq(Favorite::getUserId, userId)
                .orderByDesc(Favorite::getCreateTime);

        Page<Favorite> favoritePage = new Page<>(page, size);
        Page<Favorite> favoriteResult = page(favoritePage, favoriteQueryWrapper);

        // 获取收藏的商品ID列表
        List<Long> productIds = favoriteResult.getRecords().stream()
                .map(Favorite::getProductId)
                .collect(Collectors.toList());

        // 创建商品VO分页对象
        productVOPage = new Page<>();
        productVOPage.setCurrent(favoriteResult.getCurrent());
        productVOPage.setSize(favoriteResult.getSize());
        productVOPage.setTotal(favoriteResult.getTotal());
        productVOPage.setPages(favoriteResult.getPages());

        // 如果收藏列表为空，返回空列表
        if (productIds.isEmpty()) {
            productVOPage.setRecords(new ArrayList<>());
            // 将结果存入缓存
            redisTemplate.opsForValue().set(cacheKey, productVOPage, CACHE_EXPIRE_TIME, TimeUnit.HOURS);
            return productVOPage;
        }

        // 批量查询商品详情
        List<ProductVO> productVOList = new ArrayList<>();
        for (Long productId : productIds) {
            try {
                ProductVO productVO = productService.getProductDetail(productId);
                productVOList.add(productVO);
            } catch (Exception e) {
                // 如果商品已被删除或不存在，跳过
                continue;
            }
        }

        productVOPage.setRecords(productVOList);

        // 将结果存入缓存
        redisTemplate.opsForValue().set(cacheKey, productVOPage, CACHE_EXPIRE_TIME, TimeUnit.HOURS);

        return productVOPage;
    }

    @Override
    public int getProductFavoriteCount(Long productId) {
        // 从缓存获取
        String cacheKey = PRODUCT_FAVORITE_COUNT_CACHE_PREFIX + productId;
        Integer count = (Integer) redisTemplate.opsForValue().get(cacheKey);

        if (count != null) {
            return count;
        }

        // 缓存未命中，查询数据库
        LambdaQueryWrapper<Favorite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Favorite::getProductId, productId);

        count = Math.toIntExact(count(queryWrapper));

        // 将结果存入缓存
        redisTemplate.opsForValue().set(cacheKey, count, CACHE_EXPIRE_TIME, TimeUnit.HOURS);

        return count;
    }

    /**
     * 获取收藏状态缓存键
     *
     * @param userId    用户ID
     * @param productId 商品ID
     * @return 缓存键
     */
    private String getIsFavoriteCacheKey(Long userId, Long productId) {
        return IS_FAVORITE_CACHE_PREFIX + userId + ":" + productId;
    }

    /**
     * 清除收藏相关缓存
     *
     * @param userId    用户ID
     * @param productId 商品ID
     */
    private void clearFavoriteCache(Long userId, Long productId) {
        // 清除用户收藏列表缓存（模糊删除）
        String userFavoritesPattern = USER_FAVORITES_CACHE_PREFIX + userId + "*";
        redisTemplate.delete(redisTemplate.keys(userFavoritesPattern));

        // 清除商品收藏数缓存
        String productFavoriteCountCacheKey = PRODUCT_FAVORITE_COUNT_CACHE_PREFIX + productId;
        redisTemplate.delete(productFavoriteCountCacheKey);
    }
}




