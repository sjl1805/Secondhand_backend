package com.example.secondhand_backend.utils;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.core.util.StrUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class CaptchaUtils {

    private static final String CAPTCHA_PREFIX = "captcha:";
    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 生成验证码
     *
     * @param width     宽度
     * @param height    高度
     * @param codeCount 验证码字符数
     * @param lineCount 干扰线条数
     * @return 验证码对象
     */
    public LineCaptcha generateCaptcha(int width, int height, int codeCount, int lineCount) {
        LineCaptcha captcha = CaptchaUtil.createLineCaptcha(width, height, codeCount, lineCount);
        return captcha;
    }

    /**
     * 保存验证码到Redis
     *
     * @param key    键
     * @param code   验证码
     * @param expire 过期时间（秒）
     */
    public void saveCaptcha(String key, String code, long expire) {
        redisTemplate.opsForValue().set(CAPTCHA_PREFIX + key, code, expire, TimeUnit.SECONDS);
    }

    /**
     * 验证验证码
     *
     * @param key  键
     * @param code 验证码
     * @return 是否验证通过
     */
    public boolean verifyCaptcha(String key, String code) {
        if (StrUtil.isBlank(key) || StrUtil.isBlank(code)) {
            return false;
        }
        String storedCode = redisTemplate.opsForValue().get(CAPTCHA_PREFIX + key);
        if (StrUtil.isBlank(storedCode)) {
            return false;
        }
        boolean result = code.equalsIgnoreCase(storedCode);
        if (result) {
            // 验证成功后删除验证码
            redisTemplate.delete(CAPTCHA_PREFIX + key);
        }
        return result;
    }
} 