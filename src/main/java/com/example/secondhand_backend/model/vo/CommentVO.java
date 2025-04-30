package com.example.secondhand_backend.model.vo;

import lombok.Data;

import java.util.Date;

/**
 * 评价视图对象
 */
@Data
public class CommentVO {
    /**
     * 评价ID
     */
    private Long id;
    
    /**
     * 订单ID
     */
    private Long orderId;
    
    /**
     * 评价用户ID
     */
    private Long userId;
    
    /**
     * 评价用户昵称
     */
    private String nickname;
    
    /**
     * 评价用户头像
     */
    private String avatar;
    
    /**
     * 商品ID
     */
    private Long productId;
    
    /**
     * 商品标题
     */
    private String productTitle;
    
    /**
     * 商品图片
     */
    private String productImage;
    
    /**
     * 评价内容
     */
    private String content;
    
    /**
     * 评分：1-5星
     */
    private Integer rating;
    
    /**
     * 创建时间
     */
    private Date createTime;
} 