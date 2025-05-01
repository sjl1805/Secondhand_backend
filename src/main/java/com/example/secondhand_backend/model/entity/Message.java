package com.example.secondhand_backend.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 消息表
 *
 * @TableName message
 */
@TableName(value = "message")
@Data
public class Message implements Serializable {
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
    /**
     * 消息ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 发送者ID
     */
    private Long senderId;
    /**
     * 接收者ID
     */
    private Long receiverId;
    /**
     * 消息内容
     */
    private String content;
    /**
     * 是否已读
     */
    private Integer isRead;
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
}