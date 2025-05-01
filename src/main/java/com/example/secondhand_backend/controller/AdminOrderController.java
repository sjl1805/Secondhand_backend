package com.example.secondhand_backend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.secondhand_backend.model.common.Result;
import com.example.secondhand_backend.model.vo.OrderVO;
import com.example.secondhand_backend.service.OrdersService;
import com.example.secondhand_backend.utils.UserUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员订单管理控制器
 */
@RestController
@RequestMapping("/admin/order")
@Tag(name = "管理员订单管理", description = "管理员订单管理接口")
public class AdminOrderController {

    @Autowired
    private OrdersService ordersService;

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
    @Operation(summary = "获取订单列表", description = "分页获取订单列表，支持多条件搜索")
    public Result<IPage<OrderVO>> getOrderList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "买家ID") @RequestParam(required = false) Long buyerId,
            @Parameter(description = "卖家ID") @RequestParam(required = false) Long sellerId,
            @Parameter(description = "订单状态：1-待付款 2-待发货 3-待收货 4-已完成 5-已取消") @RequestParam(required = false) Integer status,
            @Parameter(description = "订单号") @RequestParam(required = false) String orderNo) {
        // 验证管理员权限
        if (!validateAdminRole()) {
            return Result.error("无权限执行此操作，需要管理员权限");
        }

        Long adminId = UserUtils.getCurrentUserId();
        IPage<OrderVO> orderList = ordersService.adminGetOrderList(
                page, size, buyerId, sellerId, status, orderNo, adminId);
        return Result.success(orderList);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取订单详情", description = "获取指定ID的订单详情")
    public Result<OrderVO> getOrderDetail(
            @Parameter(description = "订单ID") @PathVariable("id") Long orderId) {
        // 验证管理员权限
        if (!validateAdminRole()) {
            return Result.error("无权限执行此操作，需要管理员权限");
        }

        Long adminId = UserUtils.getCurrentUserId();
        try {
            OrderVO orderVO = ordersService.adminGetOrderDetail(orderId, adminId);
            return Result.success(orderVO);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新订单状态", description = "更新指定ID的订单状态")
    public Result<Void> updateOrderStatus(
            @Parameter(description = "订单ID") @PathVariable("id") Long orderId,
            @Parameter(description = "订单状态：1-待付款 2-待发货 3-待收货 4-已完成 5-已取消") @RequestParam Integer status) {
        // 验证管理员权限
        if (!validateAdminRole()) {
            return Result.error("无权限执行此操作，需要管理员权限");
        }

        Long adminId = UserUtils.getCurrentUserId();
        try {
            ordersService.adminUpdateOrderStatus(orderId, status, adminId);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除订单", description = "删除指定ID的订单")
    public Result<Void> deleteOrder(
            @Parameter(description = "订单ID") @PathVariable("id") Long orderId) {
        // 验证管理员权限
        if (!validateAdminRole()) {
            return Result.error("无权限执行此操作，需要管理员权限");
        }

        Long adminId = UserUtils.getCurrentUserId();
        try {
            ordersService.adminDeleteOrder(orderId, adminId);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/batch/status")
    @Operation(summary = "批量更新订单状态", description = "批量更新订单状态")
    public Result<Integer> batchUpdateOrderStatus(
            @Parameter(description = "订单ID列表") @RequestBody List<Long> orderIds,
            @Parameter(description = "订单状态：1-待付款 2-待发货 3-待收货 4-已完成 5-已取消") @RequestParam Integer status) {
        // 验证管理员权限
        if (!validateAdminRole()) {
            return Result.error("无权限执行此操作，需要管理员权限");
        }

        Long adminId = UserUtils.getCurrentUserId();
        try {
            int count = ordersService.adminBatchUpdateOrderStatus(orderIds, status, adminId);
            return Result.success(count);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除订单", description = "批量删除订单")
    public Result<Integer> batchDeleteOrder(
            @Parameter(description = "订单ID列表") @RequestBody List<Long> orderIds) {
        // 验证管理员权限
        if (!validateAdminRole()) {
            return Result.error("无权限执行此操作，需要管理员权限");
        }

        Long adminId = UserUtils.getCurrentUserId();
        try {
            int count = ordersService.adminBatchDeleteOrder(orderIds, adminId);
            return Result.success(count);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
} 