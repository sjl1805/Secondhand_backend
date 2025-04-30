package com.example.secondhand_backend.model.dto;

import lombok.Data;

/**
 * 评价数据传输对象
 */
@Data
public class CommentDTO {
    /**
     * 订单ID
     */
    private Long orderId;
    
    /**
     * 商品ID
     */
    private Long productId;
    
    /**
     * 评价内容
     */
    private String content;
    
    /**
     * 评分：1-5星
     */
    private Integer rating;
}