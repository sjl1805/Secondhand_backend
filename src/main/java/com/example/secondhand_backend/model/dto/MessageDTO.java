package com.example.secondhand_backend.model.dto;

import lombok.Data;

/**
 * 消息数据传输对象
 */
@Data
public class MessageDTO {
    /**
     * 接收者ID
     */
    private Long receiverId;

    /**
     * 消息内容
     */
    private String content;
} 