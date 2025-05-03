package com.example.secondhand_backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.secondhand_backend.model.entity.SystemNotification;

import java.util.List;

/**
 * @author 28619
 * @description 针对表【system_notification(系统通知表)】的数据库操作Service
 * @createDate 2025-04-29 13:42:39
 */
public interface SystemNotificationService extends IService<SystemNotification> {

    /**
     * 根据ID获取通知
     *
     * @param id 通知ID
     * @return 通知对象
     */
    SystemNotification getNotificationById(Long id);

    /**
     * 获取用户的所有通知（包括系统通知）
     *
     * @param userId 用户ID
     * @return 通知列表
     */
    List<SystemNotification> getUserNotifications(Long userId);

    /**
     * 获取所有通知
     *
     * @return 通知列表
     */
    List<SystemNotification> getAllNotifications();

    /**
     * 保存通知
     *
     * @param notification 通知对象
     * @return 是否成功
     */
    boolean saveNotification(SystemNotification notification);

    /**
     * 更新通知
     *
     * @param notification 通知对象
     * @return 是否成功
     */
    boolean updateNotification(SystemNotification notification);

    /**
     * 删除通知
     *
     * @param id 通知ID
     * @return 是否成功
     */
    boolean removeNotification(Long id);
}
