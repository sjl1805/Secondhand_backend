package com.example.secondhand_backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.secondhand_backend.mapper.SystemNotificationMapper;
import com.example.secondhand_backend.model.entity.SystemNotification;
import com.example.secondhand_backend.service.SystemNotificationService;
import org.springframework.stereotype.Service;

/**
 * @author 28619
 * @description 针对表【system_notification(系统通知表)】的数据库操作Service实现
 * @createDate 2025-04-29 13:42:39
 */
@Service
public class SystemNotificationServiceImpl extends ServiceImpl<SystemNotificationMapper, SystemNotification>
        implements SystemNotificationService {

}




