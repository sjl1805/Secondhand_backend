package com.example.secondhand_backend.controller;

import com.example.secondhand_backend.model.common.Result;
import com.example.secondhand_backend.model.dto.UserInfoDTO;
import com.example.secondhand_backend.model.entity.User;
import com.example.secondhand_backend.service.UserService;
import com.example.secondhand_backend.utils.UserUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@Tag(name = "用户管理", description = "用户信息管理")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/info")
    @Operation(summary = "获取用户信息", description = "获取当前登录用户的信息")
    public Result<User> getUserInfo() {
        Long userId = UserUtils.getCurrentUserId();
        User user = userService.getUserInfo(userId);
        return Result.success(user);
    }

    @PutMapping("/info")
    @Operation(summary = "更新用户信息", description = "更新当前登录用户的信息")
    public Result<Void> updateUserInfo(@RequestBody UserInfoDTO userInfoDTO) {
        Long userId = UserUtils.getCurrentUserId();
        userService.updateUserInfo(userId, userInfoDTO);
        return Result.success();
    }

    @GetMapping("/seller/{id}")
    @Operation(summary = "获取卖家信息", description = "获取指定ID的卖家信息")
    public Result<User> getSellerInfo(@PathVariable Long id) {
        User seller = userService.getSellerInfo(id);
        return Result.success(seller);
    }
} 