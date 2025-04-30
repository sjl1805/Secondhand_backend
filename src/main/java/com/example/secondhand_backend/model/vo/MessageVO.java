package com.example.secondhand_backend.model.vo;

import lombok.Data;

import java.util.Date;

/**
 * 消息视图对象
 */
@Data
public class MessageVO {
    /**
     * 消息ID
     */
    private Long id;
    
    /**
     * 发送者ID
     */
    private Long senderId;
    
    /**
     * 发送者昵称
     */
    private String senderNickname;
    
    /**
     * 发送者头像
     */
    private String senderAvatar;
    
    /**
     * 接收者ID
     */
    private Long receiverId;
    
    /**
     * 接收者昵称
     */
    private String receiverNickname;
    
    /**
     * 接收者头像
     */
    private String receiverAvatar;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 是否已读
     */
    private Boolean isRead;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 是否是当前用户发送的消息
     */
    private Boolean isSelf;
} 