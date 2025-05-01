package com.example.secondhand_backend.model.dto;

import lombok.Data;

/**
 * 系统通知数据传输对象
 */
@Data
public class SystemNotificationDTO {
    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 通知内容
     */
    private String content;
} 