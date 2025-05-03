package com.example.secondhand_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.secondhand_backend.mapper.SystemNotificationMapper;
import com.example.secondhand_backend.model.entity.SystemNotification;
import com.example.secondhand_backend.service.SystemNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author 28619
 * @description 针对表【system_notification(系统通知表)】的数据库操作Service实现
 * @createDate 2025-04-29 13:42:39
 */
@Service
public class SystemNotificationServiceImpl extends ServiceImpl<SystemNotificationMapper, SystemNotification>
        implements SystemNotificationService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    // 缓存相关常量
    private static final String NOTIFICATION_CACHE_PREFIX = "notification:";
    private static final String USER_NOTIFICATIONS_CACHE_PREFIX = "notification:user:";
    private static final String ALL_NOTIFICATIONS_CACHE_KEY = "notification:all";
    private static final long CACHE_EXPIRE_TIME = 24; // 缓存过期时间（小时）
    
    @Override
    public SystemNotification getNotificationById(Long id) {
        // 先从缓存获取
        String cacheKey = NOTIFICATION_CACHE_PREFIX + id;
        SystemNotification notification = (SystemNotification) redisTemplate.opsForValue().get(cacheKey);
        
        if (notification != null) {
            return notification;
        }
        
        // 缓存未命中，查询数据库
        notification = getById(id);
        
        // 存入缓存
        if (notification != null) {
            redisTemplate.opsForValue().set(cacheKey, notification, CACHE_EXPIRE_TIME, TimeUnit.HOURS);
        }
        
        return notification;
    }
    
    @Override
    public List<SystemNotification> getUserNotifications(Long userId) {
        // 先从缓存获取
        String cacheKey = USER_NOTIFICATIONS_CACHE_PREFIX + userId;
        List<SystemNotification> notifications = (List<SystemNotification>) redisTemplate.opsForValue().get(cacheKey);
        
        if (notifications != null) {
            return notifications;
        }
        
        // 缓存未命中，查询数据库
        LambdaQueryWrapper<SystemNotification> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SystemNotification::getUserId, userId)
                .or()
                .eq(SystemNotification::getUserId, 0L) // 系统通知(发给所有用户)
                .orderByDesc(SystemNotification::getCreateTime);
        
        notifications = list(queryWrapper);
        
        // 存入缓存
        redisTemplate.opsForValue().set(cacheKey, notifications, CACHE_EXPIRE_TIME, TimeUnit.HOURS);
        
        return notifications;
    }
    
    @Override
    public List<SystemNotification> getAllNotifications() {
        // 先从缓存获取
        List<SystemNotification> notifications = (List<SystemNotification>) redisTemplate.opsForValue().get(ALL_NOTIFICATIONS_CACHE_KEY);
        
        if (notifications != null) {
            return notifications;
        }
        
        // 缓存未命中，查询数据库
        LambdaQueryWrapper<SystemNotification> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(SystemNotification::getCreateTime);
        
        notifications = list(queryWrapper);
        
        // 存入缓存
        redisTemplate.opsForValue().set(ALL_NOTIFICATIONS_CACHE_KEY, notifications, CACHE_EXPIRE_TIME, TimeUnit.HOURS);
        
        return notifications;
    }
    
    @Override
    public boolean saveNotification(SystemNotification notification) {
        boolean result = save(notification);
        
        // 保存成功后，清除相关缓存
        if (result) {
            clearNotificationCache(notification.getUserId());
        }
        
        return result;
    }
    
    @Override
    public boolean updateNotification(SystemNotification notification) {
        boolean result = updateById(notification);
        
        // 更新成功后，清除相关缓存
        if (result) {
            String cacheKey = NOTIFICATION_CACHE_PREFIX + notification.getId();
            redisTemplate.delete(cacheKey);
            clearNotificationCache(notification.getUserId());
        }
        
        return result;
    }
    
    @Override
    public boolean removeNotification(Long id) {
        // 先获取通知信息，用于后续清除缓存
        SystemNotification notification = getById(id);
        
        if (notification != null) {
            boolean result = removeById(id);
            
            // 删除成功后，清除相关缓存
            if (result) {
                String cacheKey = NOTIFICATION_CACHE_PREFIX + id;
                redisTemplate.delete(cacheKey);
                clearNotificationCache(notification.getUserId());
            }
            
            return result;
        }
        
        return false;
    }
    
    /**
     * 清除用户通知相关缓存
     * @param userId 用户ID
     */
    private void clearNotificationCache(Long userId) {
        // 清除指定用户的通知缓存
        String userCacheKey = USER_NOTIFICATIONS_CACHE_PREFIX + userId;
        redisTemplate.delete(userCacheKey);
        
        // 如果是系统通知(userId=0)，则清除所有用户的通知缓存
        if (userId == 0L) {
            Set<String> keys = redisTemplate.keys(USER_NOTIFICATIONS_CACHE_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        }
        
        // 清除全部通知缓存
        redisTemplate.delete(ALL_NOTIFICATIONS_CACHE_KEY);
    }
    
    /**
     * 刷新所有通知缓存
     */
    public void refreshAllNotificationCache() {
        Set<String> keys = redisTemplate.keys(NOTIFICATION_CACHE_PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}




