package com.example.secondhand_backend.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    public static final int USERNAME_OR_PASSWORD_ERROR = 1001;
    public static final int USERNAME_EXISTS = 1002;
    public static final int CAPTCHA_ERROR = 1003;
    public static final int CAPTCHA_EXPIRED = 1004;
    private final int code;
    private final String message;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public BusinessException(String message) {
        this(400, message);
    }

    public static BusinessException usernameOrPasswordError() {
        return new BusinessException(USERNAME_OR_PASSWORD_ERROR, "用户名或密码错误");
    }

    public static BusinessException usernameExists() {
        return new BusinessException(USERNAME_EXISTS, "用户名已存在");
    }

    public static BusinessException captchaError() {
        return new BusinessException(CAPTCHA_ERROR, "验证码错误");
    }

    public static BusinessException captchaExpired() {
        return new BusinessException(CAPTCHA_EXPIRED, "验证码已过期");
    }
} 