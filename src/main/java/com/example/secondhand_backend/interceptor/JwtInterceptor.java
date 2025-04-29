package com.example.secondhand_backend.interceptor;

import com.example.secondhand_backend.service.UserService;
import com.example.secondhand_backend.utils.JwtUtils;
import com.example.secondhand_backend.utils.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 从请求头中获取token
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith(jwtUtils.getTokenPrefix())) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        // 去掉token前缀
        token = token.substring(jwtUtils.getTokenPrefix().length()).trim();

        try {
            // 验证token
            if (jwtUtils.isTokenExpired(token)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return false;
            }

            // 从token中获取用户信息
            Long userId = jwtUtils.getUserIdFromToken(token);
            Integer role = jwtUtils.getRoleFromToken(token);

            // 查询用户是否存在
            if (userService.getById(userId) == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return false;
            }

            // 将用户信息存入ThreadLocal
            UserUtils.setCurrentUser(userService.getById(userId));
            return true;
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 清除ThreadLocal中的用户信息
        UserUtils.clear();
    }
} 