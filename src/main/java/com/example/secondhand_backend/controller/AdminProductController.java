package com.example.secondhand_backend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.secondhand_backend.model.common.Result;
import com.example.secondhand_backend.model.vo.ProductVO;
import com.example.secondhand_backend.service.ProductService;
import com.example.secondhand_backend.utils.UserUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员商品管理控制器
 */
@RestController
@RequestMapping("/admin/product")
@Tag(name = "管理员商品管理", description = "管理员商品管理接口")
public class AdminProductController {

    @Autowired
    private ProductService productService;

    /**
     * 验证当前用户是否为管理员
     *
     * @return 是否为管理员
     */
    private boolean validateAdminRole() {
        Integer role = UserUtils.getCurrentUserRole();
        return role != null && role == 9;
    }

    @GetMapping("/list")
    @Operation(summary = "获取商品列表", description = "分页获取商品列表，支持多条件搜索")
    public Result<IPage<ProductVO>> getProductList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "分类ID") @RequestParam(required = false) Integer categoryId,
            @Parameter(description = "状态：1-在售 2-已售 3-下架") @RequestParam(required = false) Integer status,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "发布者ID") @RequestParam(required = false) Long userId) {
        // 验证管理员权限
        if (!validateAdminRole()) {
            return Result.error("无权限执行此操作，需要管理员权限");
        }

        IPage<ProductVO> productList = productService.adminGetProductList(
                page, size, categoryId, status, keyword, userId);
        return Result.success(productList);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取商品详情", description = "获取指定ID的商品详情")
    public Result<ProductVO> getProductDetail(
            @Parameter(description = "商品ID") @PathVariable("id") Long productId) {
        // 验证管理员权限
        if (!validateAdminRole()) {
            return Result.error("无权限执行此操作，需要管理员权限");
        }

        try {
            ProductVO productVO = productService.getProductDetail(productId);
            return Result.success(productVO);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新商品状态", description = "更新指定ID的商品状态")
    public Result<Void> updateProductStatus(
            @Parameter(description = "商品ID") @PathVariable("id") Long productId,
            @Parameter(description = "状态：1-在售 2-已售 3-下架") @RequestParam Integer status) {
        // 验证管理员权限
        if (!validateAdminRole()) {
            return Result.error("无权限执行此操作，需要管理员权限");
        }

        Long adminId = UserUtils.getCurrentUserId();
        try {
            productService.adminUpdateProductStatus(productId, status, adminId);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除商品", description = "删除指定ID的商品")
    public Result<Void> deleteProduct(
            @Parameter(description = "商品ID") @PathVariable("id") Long productId) {
        // 验证管理员权限
        if (!validateAdminRole()) {
            return Result.error("无权限执行此操作，需要管理员权限");
        }

        Long adminId = UserUtils.getCurrentUserId();
        try {
            productService.adminDeleteProduct(productId, adminId);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/batch/status")
    @Operation(summary = "批量更新商品状态", description = "批量更新商品状态")
    public Result<Integer> batchUpdateProductStatus(
            @Parameter(description = "商品ID列表") @RequestBody List<Long> productIds,
            @Parameter(description = "状态：1-在售 2-已售 3-下架") @RequestParam Integer status) {
        // 验证管理员权限
        if (!validateAdminRole()) {
            return Result.error("无权限执行此操作，需要管理员权限");
        }

        Long adminId = UserUtils.getCurrentUserId();
        try {
            int count = productService.adminBatchUpdateProductStatus(productIds, status, adminId);
            return Result.success(count);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除商品", description = "批量删除商品")
    public Result<Integer> batchDeleteProduct(
            @Parameter(description = "商品ID列表") @RequestBody List<Long> productIds) {
        // 验证管理员权限
        if (!validateAdminRole()) {
            return Result.error("无权限执行此操作，需要管理员权限");
        }

        Long adminId = UserUtils.getCurrentUserId();
        try {
            int count = productService.adminBatchDeleteProduct(productIds, adminId);
            return Result.success(count);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
} 