package com.example.secondhand_backend.interceptor;

import com.example.secondhand_backend.service.UserService;
import com.example.secondhand_backend.utils.JwtUtils;
import com.example.secondhand_backend.utils.UserUtils;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

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
            sendError(response, "未提供有效的认证信息");
            return false;
        }

        // 去掉token前缀和空格
        token = token.substring(jwtUtils.getTokenPrefix().length()).trim();

        try {
            // 验证token
            if (jwtUtils.isTokenExpired(token)) {
                sendError(response, "认证信息已过期");
                return false;
            }

            // 从token中获取用户信息
            Long userId = jwtUtils.getUserIdFromToken(token);
            Integer role = jwtUtils.getRoleFromToken(token);

            // 查询用户是否存在
            if (userService.getById(userId) == null) {
                sendError(response, "用户不存在");
                return false;
            }

            // 将用户信息存入ThreadLocal
            UserUtils.setCurrentUser(userService.getById(userId));
            return true;
        } catch (ExpiredJwtException e) {
            sendError(response, "认证信息已过期");
            return false;
        } catch (UnsupportedJwtException e) {
            sendError(response, "不支持的认证信息格式");
            return false;
        } catch (MalformedJwtException e) {
            sendError(response, "认证信息格式错误");
            return false;
        } catch (SignatureException e) {
            sendError(response, "认证信息签名错误");
            return false;
        } catch (Exception e) {
            sendError(response, "认证信息验证失败");
            return false;
        }
    }

    private void sendError(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":401,\"message\":\"" + message + "\"}");
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 清除ThreadLocal中的用户信息
        UserUtils.clear();
    }
} 