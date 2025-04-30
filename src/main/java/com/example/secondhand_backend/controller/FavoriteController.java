package com.example.secondhand_backend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.secondhand_backend.model.common.Result;
import com.example.secondhand_backend.model.vo.ProductVO;
import com.example.secondhand_backend.service.FavoriteService;
import com.example.secondhand_backend.utils.UserUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/favorite")
@Tag(name = "收藏管理", description = "商品收藏相关操作")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    @PostMapping("/{productId}")
    @Operation(summary = "收藏商品", description = "收藏指定ID的商品")
    public Result<Long> addFavorite(
            @Parameter(description = "商品ID") @PathVariable("productId") Long productId) {
        Long userId = UserUtils.getCurrentUserId();
        Long favoriteId = favoriteService.addFavorite(userId, productId);
        return Result.success(favoriteId);
    }

    @DeleteMapping("/{productId}")
    @Operation(summary = "取消收藏", description = "取消收藏指定ID的商品")
    public Result<Void> cancelFavorite(
            @Parameter(description = "商品ID") @PathVariable("productId") Long productId) {
        Long userId = UserUtils.getCurrentUserId();
        favoriteService.cancelFavorite(userId, productId);
        return Result.success();
    }

    @GetMapping("/check/{productId}")
    @Operation(summary = "检查是否已收藏", description = "检查当前用户是否已收藏指定ID的商品")
    public Result<Boolean> checkFavorite(
            @Parameter(description = "商品ID") @PathVariable("productId") Long productId) {
        Long userId = UserUtils.getCurrentUserId();
        boolean isFavorite = favoriteService.isFavorite(userId, productId);
        return Result.success(isFavorite);
    }

    @GetMapping("/list")
    @Operation(summary = "获取收藏列表", description = "分页获取当前用户的收藏列表")
    public Result<IPage<ProductVO>> getFavoriteList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size) {
        Long userId = UserUtils.getCurrentUserId();
        IPage<ProductVO> favoriteList = favoriteService.getUserFavorites(userId, page, size);
        return Result.success(favoriteList);
    }

    @GetMapping("/count/{productId}")
    @Operation(summary = "获取收藏数量", description = "获取指定ID商品的收藏数量")
    public Result<Integer> getFavoriteCount(
            @Parameter(description = "商品ID") @PathVariable("productId") Long productId) {
        int count = favoriteService.getProductFavoriteCount(productId);
        return Result.success(count);
    }
} 