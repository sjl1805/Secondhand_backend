package com.example.secondhand_backend.controller;

import com.example.secondhand_backend.model.dto.AuthResponseDTO;
import com.example.secondhand_backend.model.dto.LoginDTO;
import com.example.secondhand_backend.model.dto.RegisterDTO;
import com.example.secondhand_backend.service.UserService;
import com.example.secondhand_backend.utils.CaptchaUtils;
import com.example.secondhand_backend.utils.JwtUtils;
import cn.hutool.captcha.LineCaptcha;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
@Tag(name = "认证模块", description = "认证模块")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private CaptchaUtils captchaUtils;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户登录")
    public AuthResponseDTO login(@RequestBody @Validated LoginDTO loginDTO) {
        // 验证用户
        var user = userService.login(loginDTO);

        // 生成token
        String token = jwtUtils.generateToken(user.getId(), user.getRole());

        // 返回认证信息
        return new AuthResponseDTO(
                token,
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getAvatar(),
                user.getRole()
        );
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "用户注册")
    public AuthResponseDTO register(@RequestBody @Validated RegisterDTO registerDTO) {
        // 注册用户
        var user = userService.register(registerDTO);

        // 生成token
        String token = jwtUtils.generateToken(user.getId(), user.getRole());

        // 返回认证信息
        return new AuthResponseDTO(
                token,
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getAvatar(),
                user.getRole()
        );
    }

    /**
     * 获取验证码
     * @return 验证码图片和key
     */
    @GetMapping("/captcha")
    @Operation(summary = "获取验证码", description = "获取验证码")
    public Map<String, String> getCaptcha() {
        // 生成验证码
        LineCaptcha captcha = captchaUtils.generateCaptcha(130, 48, 4, 2);
        
        // 生成唯一key
        String key = UUID.randomUUID().toString();
        
        // 保存验证码到Redis，有效期5分钟
        captchaUtils.saveCaptcha(key, captcha.getCode(), 300);
        System.out.println(captcha.getCode());
        System.out.println(key);
        // 返回验证码图片和key
        Map<String, String> result = new HashMap<>();
        result.put("key", key);
        result.put("image", captcha.getImageBase64());
        
        return result;
    }
} 