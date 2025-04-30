package com.example.secondhand_backend.model.vo;

import lombok.Data;

import java.util.Date;

/**
 * 系统通知视图对象
 */
@Data
public class SystemNotificationVO {
    /**
     * 通知ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 通知内容
     */
    private String content;
    
    /**
     * 是否已读
     */
    private Integer isRead;
    
    /**
     * 创建时间
     */
    private Date createTime;
} 