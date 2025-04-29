package com.example.secondhand_backend.interceptor;

import com.example.secondhand_backend.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtils jwtUtils;

    @Value("${jwt.header}")
    private String tokenHeader;

    @Value("${jwt.white-list}")
    private String whiteListStr;

    private List<String> whiteList;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (whiteList == null) {
            whiteList = Arrays.asList(whiteListStr.split(","));
        }
        
        String uri = request.getRequestURI();
        
        // 白名单直接放行
        if (whiteList.stream().anyMatch(uri::startsWith)) {
            return true;
        }

        // 获取token
        String token = request.getHeader(tokenHeader);
        if (token == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        // 验证token
        try {
            token = jwtUtils.removeTokenPrefix(token);
            if (jwtUtils.isExpired(token)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return false;
            }
            // 将用户ID存入request
            Long userId = jwtUtils.getUserId(token);
            request.setAttribute("userId", userId);
            return true;
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
    }
} 