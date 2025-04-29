package com.example.secondhand_backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "captcha")
public class CaptchaConfig {

    private int width = 120;
    private int height = 40;
    private int codeCount = 4;
    private int lineCount = 150;
    private long expire = 300;

    @Bean
    public cn.hutool.captcha.LineCaptcha lineCaptcha() {
        return cn.hutool.captcha.CaptchaUtil.createLineCaptcha(width, height, codeCount, lineCount);
    }
} 