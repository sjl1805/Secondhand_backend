package com.example.secondhand_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.secondhand_backend.exception.BusinessException;
import com.example.secondhand_backend.mapper.ProductMapper;
import com.example.secondhand_backend.model.entity.Favorite;
import com.example.secondhand_backend.model.entity.Product;
import com.example.secondhand_backend.model.vo.ProductVO;
import com.example.secondhand_backend.service.FavoriteService;
import com.example.secondhand_backend.mapper.FavoriteMapper;
import com.example.secondhand_backend.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author 28619
* @description 针对表【favorite(收藏表)】的数据库操作Service实现
* @createDate 2025-04-29 13:42:22
*/
@Service
public class FavoriteServiceImpl extends ServiceImpl<FavoriteMapper, Favorite>
    implements FavoriteService{

    @Autowired
    @Lazy
    private ProductService productService;
    
    @Autowired
    private ProductMapper productMapper;

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
    }

    @Override
    public boolean isFavorite(Long userId, Long productId) {
        LambdaQueryWrapper<Favorite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Favorite::getUserId, userId)
                    .eq(Favorite::getProductId, productId);
        
        return count(queryWrapper) > 0;
    }

    @Override
    public IPage<ProductVO> getUserFavorites(Long userId, int page, int size) {
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
        Page<ProductVO> productVOPage = new Page<>();
        productVOPage.setCurrent(favoriteResult.getCurrent());
        productVOPage.setSize(favoriteResult.getSize());
        productVOPage.setTotal(favoriteResult.getTotal());
        productVOPage.setPages(favoriteResult.getPages());
        
        // 如果收藏列表为空，返回空列表
        if (productIds.isEmpty()) {
            productVOPage.setRecords(new ArrayList<>());
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
        return productVOPage;
    }

    @Override
    public int getProductFavoriteCount(Long productId) {
        LambdaQueryWrapper<Favorite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Favorite::getProductId, productId);
        
        return Math.toIntExact(count(queryWrapper));
    }
}




