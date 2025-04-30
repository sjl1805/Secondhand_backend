package com.example.secondhand_backend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.secondhand_backend.model.common.Result;
import com.example.secondhand_backend.model.dto.OrderCreateDTO;
import com.example.secondhand_backend.model.vo.OrderVO;
import com.example.secondhand_backend.service.OrdersService;
import com.example.secondhand_backend.utils.UserUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order")
@Tag(name = "订单管理", description = "订单创建、查看等操作")
public class OrderController {

    @Autowired
    private OrdersService ordersService;

    @PostMapping
    @Operation(summary = "创建订单", description = "创建新订单")
    public Result<Long> createOrder(@RequestBody OrderCreateDTO orderCreateDTO) {
        Long userId = UserUtils.getCurrentUserId();
        Long orderId = ordersService.createOrder(orderCreateDTO, userId);
        return Result.success(orderId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取订单详情", description = "获取指定ID的订单详情")
    public Result<OrderVO> getOrderDetail(
            @Parameter(description = "订单ID") @PathVariable("id") Long orderId) {
        Long userId = UserUtils.getCurrentUserId();
        OrderVO orderVO = ordersService.getOrderDetail(orderId, userId);
        return Result.success(orderVO);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新订单状态", description = "更新指定ID的订单状态")
    public Result<Void> updateOrderStatus(
            @Parameter(description = "订单ID") @PathVariable("id") Long orderId,
            @Parameter(description = "订单状态：1-待付款 2-待发货 3-待收货 4-已完成 5-已取消") @RequestParam Integer status) {
        Long userId = UserUtils.getCurrentUserId();
        ordersService.updateOrderStatus(orderId, status, userId);
        return Result.success();
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "取消订单", description = "取消指定ID的订单")
    public Result<Void> cancelOrder(
            @Parameter(description = "订单ID") @PathVariable("id") Long orderId) {
        Long userId = UserUtils.getCurrentUserId();
        ordersService.cancelOrder(orderId, userId);
        return Result.success();
    }

    @GetMapping("/buyer")
    @Operation(summary = "获取买家订单列表", description = "分页获取当前用户作为买家的订单列表")
    public Result<IPage<OrderVO>> getBuyerOrders(
            @Parameter(description = "订单状态：null-全部 1-待付款 2-待发货 3-待收货 4-已完成 5-已取消") @RequestParam(required = false) Integer status,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size) {
        Long userId = UserUtils.getCurrentUserId();
        IPage<OrderVO> orderList = ordersService.getBuyerOrders(userId, status, page, size);
        return Result.success(orderList);
    }

    @GetMapping("/seller")
    @Operation(summary = "获取卖家订单列表", description = "分页获取当前用户作为卖家的订单列表")
    public Result<IPage<OrderVO>> getSellerOrders(
            @Parameter(description = "订单状态：null-全部 1-待付款 2-待发货 3-待收货 4-已完成 5-已取消") @RequestParam(required = false) Integer status,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size) {
        Long userId = UserUtils.getCurrentUserId();
        IPage<OrderVO> orderList = ordersService.getSellerOrders(userId, status, page, size);
        return Result.success(orderList);
    }

    @PutMapping("/{id}/ship")
    @Operation(summary = "卖家发货", description = "卖家确认发货")
    public Result<Void> shipOrder(
            @Parameter(description = "订单ID") @PathVariable("id") Long orderId) {
        Long userId = UserUtils.getCurrentUserId();
        ordersService.shipOrder(orderId, userId);
        return Result.success();
    }

    @PutMapping("/{id}/receive")
    @Operation(summary = "买家收货", description = "买家确认收货")
    public Result<Void> receiveOrder(
            @Parameter(description = "订单ID") @PathVariable("id") Long orderId) {
        Long userId = UserUtils.getCurrentUserId();
        ordersService.receiveOrder(orderId, userId);
        return Result.success();
    }
} 