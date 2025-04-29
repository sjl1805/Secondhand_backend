package com.example.secondhand_backend.utils;

import com.example.secondhand_backend.entity.domain.User;
import org.springframework.stereotype.Component;

@Component
public class UserUtils {

    private static final ThreadLocal<User> userThreadLocal = new ThreadLocal<>();

    /**
     * 设置当前用户
     */
    public static void setCurrentUser(User user) {
        userThreadLocal.set(user);
    }

    /**
     * 获取当前用户
     */
    public static User getCurrentUser() {
        return userThreadLocal.get();
    }

    /**
     * 获取当前用户ID
     */
    public static Long getCurrentUserId() {
        User user = getCurrentUser();
        return user != null ? user.getId() : null;
    }

    /**
     * 获取当前用户角色
     */
    public static Integer getCurrentUserRole() {
        User user = getCurrentUser();
        return user != null ? user.getRole() : null;
    }

    /**
     * 获取当前用户名
     */
    public static String getCurrentUsername() {
        User user = getCurrentUser();
        return user != null ? user.getUsername() : null;
    }

    /**
     * 清除当前用户
     */
    public static void clear() {
        userThreadLocal.remove();
    }
} 