package com.example.secondhand_backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.secondhand_backend.model.common.Result;
import com.example.secondhand_backend.model.entity.SystemNotification;
import com.example.secondhand_backend.model.entity.User;
import com.example.secondhand_backend.service.SystemNotificationService;
import com.example.secondhand_backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 管理员系统通知控制器
 */
@RestController
@RequestMapping("/admin/notification")
@Tag(name = "管理员系统通知", description = "管理员发送、查看系统通知接口")
public class AdminNotificationController {

    @Autowired
    private SystemNotificationService systemNotificationService;
    
    @Autowired
    private UserService userService;

    @GetMapping("/list")
    @Operation(summary = "获取系统通知列表", description = "管理员获取所有系统通知列表")
    public Result<IPage<SystemNotification>> getNotificationList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId) {
        
        LambdaQueryWrapper<SystemNotification> queryWrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            queryWrapper.eq(SystemNotification::getUserId, userId);
        }
        queryWrapper.orderByDesc(SystemNotification::getCreateTime);
        
        IPage<SystemNotification> notificationPage = systemNotificationService.page(
                new Page<>(page, size),
                queryWrapper
        );
        
        return Result.success(notificationPage);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取通知详情", description = "管理员获取指定ID的系统通知详情")
    public Result<SystemNotification> getNotificationDetail(
            @Parameter(description = "通知ID") @PathVariable("id") Long notificationId) {
        
        SystemNotification notification = systemNotificationService.getById(notificationId);
        if (notification == null) {
            return Result.error("通知不存在");
        }
        
        return Result.success(notification);
    }

    @PostMapping("/send")
    @Operation(summary = "发送系统通知", description = "管理员发送系统通知给指定用户")
    public Result<Void> sendNotification(
            @Parameter(description = "通知内容") @RequestParam String content,
            @Parameter(description = "用户ID") @RequestParam Long userId) {
        
        // 检查用户是否存在
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        SystemNotification notification = new SystemNotification();
        notification.setUserId(userId);
        notification.setContent(content);
        notification.setIsRead(0); // 未读
        
        systemNotificationService.save(notification);
        
        return Result.success();
    }

    @PostMapping("/broadcast")
    @Operation(summary = "广播系统通知", description = "管理员向所有用户发送系统通知")
    public Result<Integer> broadcastNotification(
            @Parameter(description = "通知内容") @RequestParam String content) {
        
        // 获取所有用户ID
        List<User> users = userService.list();
        
        List<SystemNotification> notifications = new ArrayList<>(users.size());
        for (User user : users) {
            SystemNotification notification = new SystemNotification();
            notification.setUserId(user.getId());
            notification.setContent(content);
            notification.setIsRead(0); // 未读
            notifications.add(notification);
        }
        
        // 批量保存
        systemNotificationService.saveBatch(notifications);
        
        return Result.success(notifications.size());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除系统通知", description = "管理员删除指定的系统通知")
    public Result<Void> deleteNotification(
            @Parameter(description = "通知ID") @PathVariable("id") Long notificationId) {
        
        SystemNotification notification = systemNotificationService.getById(notificationId);
        if (notification == null) {
            return Result.error("通知不存在");
        }
        
        systemNotificationService.removeById(notificationId);
        
        return Result.success();
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除系统通知", description = "管理员批量删除系统通知")
    public Result<Integer> batchDeleteNotifications(
            @Parameter(description = "通知ID列表") @RequestBody List<Long> notificationIds) {
        
        if (notificationIds == null || notificationIds.isEmpty()) {
            return Result.error("请选择要删除的通知");
        }
        
        systemNotificationService.removeByIds(notificationIds);
        
        return Result.success(notificationIds.size());
    }
} 