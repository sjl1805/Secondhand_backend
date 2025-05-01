package com.example.secondhand_backend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.secondhand_backend.model.common.Result;
import com.example.secondhand_backend.model.entity.User;
import com.example.secondhand_backend.service.UserService;
import com.example.secondhand_backend.utils.UserUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员控制器
 */
@RestController
@RequestMapping("/admin/user")
@Tag(name = "管理员接口", description = "管理员用户管理等操作")
public class AdminUserController {

    @Autowired
    private UserService userService;

    /**
     * 验证当前用户是否为管理员
     *
     * @return 是否为管理员
     */
    private boolean validateAdminRole() {
        Integer role = UserUtils.getCurrentUserRole();
        return role != null && role == 9;
    }

    @GetMapping("/users")
    @Operation(summary = "获取用户列表", description = "分页获取用户列表，支持搜索")
    public Result<IPage<User>> getUserList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword) {
        // 验证当前用户是否为管理员
        if (!validateAdminRole()) {
            return Result.error("无权访问此接口，需要管理员权限");
        }

        IPage<User> userList = userService.getUserList(page, size, keyword);
        return Result.success(userList);
    }

    @PutMapping("/users/{userId}/status")
    @Operation(summary = "修改用户状态", description = "修改用户信用分和角色")
    public Result<Void> updateUserStatus(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "信用分") @RequestParam(required = false) Integer creditScore,
            @Parameter(description = "角色：0-普通用户 9-管理员") @RequestParam(required = false) Integer role) {
        // 验证当前用户是否为管理员
        if (!validateAdminRole()) {
            return Result.error("无权访问此接口，需要管理员权限");
        }

        Long adminId = UserUtils.getCurrentUserId();
        try {
            userService.updateUserStatus(userId, creditScore, role, adminId);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/users/{userId}")
    @Operation(summary = "删除用户", description = "逻辑删除用户")
    public Result<Void> deleteUser(
            @Parameter(description = "用户ID") @PathVariable Long userId) {
        // 验证当前用户是否为管理员
        if (!validateAdminRole()) {
            return Result.error("无权访问此接口，需要管理员权限");
        }

        Long adminId = UserUtils.getCurrentUserId();
        try {
            userService.deleteUser(userId, adminId);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/users/{userId}/password")
    @Operation(summary = "重置用户密码", description = "管理员重置用户密码")
    public Result<Void> resetUserPassword(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "新密码") @RequestParam String newPassword) {
        // 验证当前用户是否为管理员
        if (!validateAdminRole()) {
            return Result.error("无权访问此接口，需要管理员权限");
        }

        Long adminId = UserUtils.getCurrentUserId();
        try {
            userService.resetUserPassword(userId, newPassword, adminId);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/admins")
    @Operation(summary = "获取管理员列表", description = "获取所有管理员用户列表")
    public Result<List<User>> getAdminList() {
        // 验证当前用户是否为管理员
        if (!validateAdminRole()) {
            return Result.error("无权访问此接口，需要管理员权限");
        }

        List<User> adminList = userService.getAdminList();
        return Result.success(adminList);
    }

    @GetMapping("user/search")
    public Result<List<User>> searchUsers(@RequestParam("keyword") String keyword) {
        List<User> users = userService.searchUsers(keyword);
        return Result.success(users);
    }
}