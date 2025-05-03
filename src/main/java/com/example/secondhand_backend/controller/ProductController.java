package com.example.secondhand_backend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.secondhand_backend.model.common.Result;
import com.example.secondhand_backend.model.dto.ProductDTO;
import com.example.secondhand_backend.model.vo.ProductVO;
import com.example.secondhand_backend.service.ProductService;
import com.example.secondhand_backend.service.StatisticsService;
import com.example.secondhand_backend.utils.UserUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/product")
@Tag(name = "商品管理", description = "商品发布、查看等操作")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private StatisticsService statisticsService;

    @PostMapping
    @Operation(summary = "发布商品", description = "发布新商品")
    public Result<Long> publishProduct(@RequestBody ProductDTO productDTO) {
        Long userId = UserUtils.getCurrentUserId();
        Long productId = productService.publishProduct(productDTO, userId);
        return Result.success(productId);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新商品", description = "更新已发布的商品信息")
    public Result<Void> updateProduct(
            @Parameter(description = "商品ID") @PathVariable("id") Long productId,
            @RequestBody ProductDTO productDTO) {
        Long userId = UserUtils.getCurrentUserId();
        productService.updateProduct(productId, productDTO, userId);
        return Result.success();
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取商品详情", description = "获取指定ID的商品详情")
    public Result<ProductVO> getProductDetail(
            @Parameter(description = "商品ID") @PathVariable("id") Long productId) {
        // 获取当前用户ID
        Long userId = UserUtils.getCurrentUserId();

        // 获取带收藏状态的商品详情
        ProductVO productVO = productService.getProductDetailWithFavorite(productId, userId);

        // 增加浏览次数
        productService.incrementViewCount(productId);

        return Result.success(productVO);
    }

    @GetMapping("/list")
    @Operation(summary = "获取商品列表", description = "分页获取商品列表，可按分类和关键词筛选")
    public Result<IPage<ProductVO>> getProductList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "分类ID") @RequestParam(required = false) Integer categoryId,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword) {
        IPage<ProductVO> productList = productService.getProductList(page, size, categoryId, keyword);
        return Result.success(productList);
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "获取分类商品", description = "分页获取指定分类下的商品列表")
    public Result<IPage<ProductVO>> getCategoryProducts(
            @Parameter(description = "分类ID") @PathVariable("categoryId") Integer categoryId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword) {
        IPage<ProductVO> productList = productService.getProductList(page, size, categoryId, keyword);
        return Result.success(productList);
    }

    @GetMapping("/user")
    @Operation(summary = "获取用户发布的商品", description = "分页获取当前用户发布的商品")
    public Result<IPage<ProductVO>> getUserProducts(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size) {
        Long userId = UserUtils.getCurrentUserId();
        IPage<ProductVO> productList = productService.getUserProducts(userId, page, size);
        return Result.success(productList);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新商品状态", description = "更新指定ID的商品状态")
    public Result<Void> updateProductStatus(
            @Parameter(description = "商品ID") @PathVariable("id") Long productId,
            @Parameter(description = "商品状态：1-在售 2-已售 3-下架") @RequestParam Integer status) {
        Long userId = UserUtils.getCurrentUserId();
        productService.updateProductStatus(productId, status, userId);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除商品", description = "删除指定ID的商品")
    public Result<Void> deleteProduct(
            @Parameter(description = "商品ID") @PathVariable("id") Long productId) {
        Long userId = UserUtils.getCurrentUserId();
        productService.deleteProduct(productId, userId);
        return Result.success();
    }

    @GetMapping("/advanced-search")
    @Operation(summary = "高级搜索商品", description = "根据关键词、分类、价格区间、排序条件进行高级搜索")
    public Result<IPage<ProductVO>> advancedSearchProducts(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "分类ID") @RequestParam(required = false) Integer categoryId,
            @Parameter(description = "最低价格") @RequestParam(required = false) BigDecimal minPrice,
            @Parameter(description = "最高价格") @RequestParam(required = false) BigDecimal maxPrice,
            @Parameter(description = "排序字段") @RequestParam(required = false) String sortField,
            @Parameter(description = "排序方式") @RequestParam(required = false) String sortOrder) {
        IPage<ProductVO> productList = productService.advancedSearchProducts(page, size, keyword, categoryId, minPrice, maxPrice, sortField, sortOrder);
        return Result.success(productList);
    }

    @PutMapping("/{id}/view")
    @Operation(summary = "增加商品浏览次数", description = "增加指定ID的商品浏览次数")
    public Result<Void> incrementViewCount(
            @Parameter(description = "商品ID") @PathVariable("id") Long productId) {
        productService.incrementViewCount(productId);
        return Result.success();
    }

    @GetMapping("/seller/{userId}")
    @Operation(summary = "获取指定用户的商品列表", description = "分页获取指定用户ID的商品列表，可按商品状态筛选")
    public Result<IPage<ProductVO>> getSellerProducts(
            @Parameter(description = "卖家用户ID") @PathVariable("userId") Long userId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "商品状态：1-在售 2-已售 3-下架") @RequestParam(required = false) Integer status) {
        IPage<ProductVO> productList = productService.getSellerProducts(userId, page, size, status);
        return Result.success(productList);
    }

    @GetMapping("/{id}/rating")
    @Operation(summary = "获取商品评分统计", description = "获取指定商品ID的评分统计数据")
    public Result<Map<String, Integer>> getProductRatingStatistics(
            @Parameter(description = "商品ID") @PathVariable("id") Long productId) {
        Map<String, Integer> ratingStats = statisticsService.getProductRatingStatistics(productId);
        return Result.success(ratingStats);
    }
} 