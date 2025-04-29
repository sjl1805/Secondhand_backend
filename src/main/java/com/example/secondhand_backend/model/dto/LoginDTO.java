package com.example.secondhand_backend.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
public class LoginDTO {
    @NotBlank(message = "用户名不能为空")
    @Schema(description = "用户名", example = "admin")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Schema(description = "密码", example = "123456")
    private String password;

    @NotBlank(message = "验证码不能为空")
    @Schema(description = "验证码", example = "1234")
    private String captcha;

    @NotBlank(message = "验证码key不能为空")
    @Schema(description = "验证码key", example = "1234567890")
    private String captchaKey;
} 