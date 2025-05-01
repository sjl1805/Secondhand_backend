package com.example.secondhand_backend.controller;

import com.example.secondhand_backend.model.common.Result;
import com.example.secondhand_backend.model.vo.ProductVO;
import com.example.secondhand_backend.service.RecommendationService;
import com.example.secondhand_backend.utils.UserUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品推荐控制器
 */
@RestController
@RequestMapping("/recommendations")
@Tag(name = "商品推荐", description = "基于协同过滤的商品推荐接口")
public class RecommendationController {

    @Autowired
    private RecommendationService recommendationService;

    @GetMapping("/user-based")
    @Operation(summary = "基于用户的推荐", description = "获取基于用户协同过滤的商品推荐")
    public Result<List<ProductVO>> getUserBasedRecommendations(
            @Parameter(description = "推荐数量") @RequestParam(defaultValue = "10") int limit) {
        Long userId = UserUtils.getCurrentUserId();
        List<ProductVO> recommendations = recommendationService.recommendProductsForUser(userId, limit);
        return Result.success(recommendations);
    }

    @GetMapping("/item-based")
    @Operation(summary = "基于商品的推荐", description = "获取基于商品协同过滤的商品推荐")
    public Result<List<ProductVO>> getItemBasedRecommendations(
            @Parameter(description = "推荐数量") @RequestParam(defaultValue = "10") int limit) {
        Long userId = UserUtils.getCurrentUserId();
        List<ProductVO> recommendations = recommendationService.recommendSimilarProducts(userId, limit);
        return Result.success(recommendations);
    }

    @PostMapping("/refresh")
    @Operation(summary = "刷新推荐", description = "刷新当前用户的商品推荐")
    public Result<Integer> refreshRecommendations() {
        Long userId = UserUtils.getCurrentUserId();
        int count = recommendationService.updateUserRecommendations(userId);
        return Result.success(count);
    }
} 