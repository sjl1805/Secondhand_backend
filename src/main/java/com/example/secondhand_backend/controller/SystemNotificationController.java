package com.example.secondhand_backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.secondhand_backend.model.common.Result;
import com.example.secondhand_backend.model.entity.SystemNotification;
import com.example.secondhand_backend.service.SystemNotificationService;
import com.example.secondhand_backend.utils.UserUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notification")
@Tag(name = "系统通知管理", description = "系统通知查看、标记已读等操作")
public class SystemNotificationController {

    @Autowired
    private SystemNotificationService systemNotificationService;

    @GetMapping("/list")
    @Operation(summary = "获取系统通知列表", description = "获取当前用户的系统通知列表")
    public Result<IPage<SystemNotification>> getNotificationList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size) {
        Long userId = UserUtils.getCurrentUserId();

        LambdaQueryWrapper<SystemNotification> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SystemNotification::getUserId, userId)
                .orderByDesc(SystemNotification::getCreateTime);

        IPage<SystemNotification> notificationPage = systemNotificationService.page(
                new Page<>(page, size),
                queryWrapper
        );

        return Result.success(notificationPage);
    }

    @GetMapping("/unread")
    @Operation(summary = "获取未读系统通知", description = "获取当前用户的未读系统通知")
    public Result<IPage<SystemNotification>> getUnreadNotifications(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size) {
        Long userId = UserUtils.getCurrentUserId();

        LambdaQueryWrapper<SystemNotification> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SystemNotification::getUserId, userId)
                .eq(SystemNotification::getIsRead, 0)
                .orderByDesc(SystemNotification::getCreateTime);

        IPage<SystemNotification> notificationPage = systemNotificationService.page(
                new Page<>(page, size),
                queryWrapper
        );

        return Result.success(notificationPage);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取通知详情", description = "获取指定ID的系统通知详情")
    public Result<SystemNotification> getNotificationDetail(
            @Parameter(description = "通知ID") @PathVariable("id") Long notificationId) {
        Long userId = UserUtils.getCurrentUserId();

        LambdaQueryWrapper<SystemNotification> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SystemNotification::getId, notificationId)
                .eq(SystemNotification::getUserId, userId);

        SystemNotification notification = systemNotificationService.getOne(queryWrapper);
        if (notification == null) {
            return Result.error("通知不存在或无权限查看");
        }

        return Result.success(notification);
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "标记通知为已读", description = "标记指定ID的系统通知为已读")
    public Result<Void> markAsRead(
            @Parameter(description = "通知ID") @PathVariable("id") Long notificationId) {
        Long userId = UserUtils.getCurrentUserId();

        LambdaQueryWrapper<SystemNotification> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SystemNotification::getId, notificationId)
                .eq(SystemNotification::getUserId, userId);

        SystemNotification notification = systemNotificationService.getOne(queryWrapper);
        if (notification == null) {
            return Result.error("通知不存在或无权限操作");
        }

        notification.setIsRead(1);
        systemNotificationService.updateById(notification);

        return Result.success();
    }

    @PutMapping("/read/all")
    @Operation(summary = "标记所有通知为已读", description = "标记当前用户的所有系统通知为已读")
    public Result<Integer> markAllAsRead() {
        Long userId = UserUtils.getCurrentUserId();

        LambdaQueryWrapper<SystemNotification> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SystemNotification::getUserId, userId)
                .eq(SystemNotification::getIsRead, 0);

        SystemNotification update = new SystemNotification();
        update.setIsRead(1);

        long count = systemNotificationService.count(queryWrapper);
        systemNotificationService.update(update, queryWrapper);

        return Result.success((int) count);
    }

    @GetMapping("/unread/count")
    @Operation(summary = "获取未读通知数量", description = "获取当前用户的未读系统通知数量")
    public Result<Integer> getUnreadCount() {
        Long userId = UserUtils.getCurrentUserId();

        LambdaQueryWrapper<SystemNotification> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SystemNotification::getUserId, userId)
                .eq(SystemNotification::getIsRead, 0);

        long count = systemNotificationService.count(queryWrapper);

        return Result.success((int) count);
    }
} 