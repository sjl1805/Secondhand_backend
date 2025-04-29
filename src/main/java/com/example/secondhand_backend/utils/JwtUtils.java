package com.example.secondhand_backend.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expire}")
    private long expire;

    @Value("${jwt.token-prefix}")
    private String tokenPrefix;

    private SecretKey key;

    public JwtUtils() {
        this.key = Keys.secretKeyFor(SignatureAlgorithm.HS512);
    }

    /**
     * 生成token
     * @param userId 用户ID
     * @return token
     */
    public String generateToken(Long userId) {
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + expire * 1000);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expireDate)
                .signWith(key)
                .compact();
    }

    /**
     * 解析token
     * @param token token
     * @return Claims
     */
    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 获取token中的用户ID
     * @param token token
     * @return 用户ID
     */
    public Long getUserId(String token) {
        Claims claims = parseToken(token);
        return claims.get("userId", Long.class);
    }

    /**
     * 验证token是否过期
     * @param token token
     * @return 是否过期
     */
    public boolean isExpired(String token) {
        Claims claims = parseToken(token);
        return claims.getExpiration().before(new Date());
    }

    /**
     * 获取带前缀的token
     * @param token token
     * @return 带前缀的token
     */
    public String getTokenWithPrefix(String token) {
        return tokenPrefix + " " + token;
    }

    /**
     * 移除token前缀
     * @param token 带前缀的token
     * @return token
     */
    public String removeTokenPrefix(String token) {
        if (token != null && token.startsWith(tokenPrefix)) {
            return token.substring(tokenPrefix.length() + 1);
        }
        return token;
    }
} 