package com.example.secondhand_backend.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Utils {
    private static final String SALT = "secondhand";

    /**
     * MD5加密
     */
    public static String encrypt(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            // 加盐
            String str = password + SALT;
            byte[] bytes = md.digest(str.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5加密失败", e);
        }
    }

    /**
     * 验证密码
     */
    public static boolean verify(String password, String encryptedPassword) {
        return encrypt(password).equals(encryptedPassword);
    }
} 