package com.example.secondhand_backend.model.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 订单视图对象
 */
@Data
public class OrderVO {
    /**
     * 订单ID
     */
    private Long id;
    
    /**
     * 订单编号
     */
    private String orderNo;
    
    /**
     * 买家ID
     */
    private Long buyerId;
    
    /**
     * 买家昵称
     */
    private String buyerNickname;
    
    /**
     * 买家头像
     */
    private String buyerAvatar;
    
    /**
     * 卖家ID
     */
    private Long sellerId;
    
    /**
     * 卖家昵称
     */
    private String sellerNickname;
    
    /**
     * 卖家头像
     */
    private String sellerAvatar;
    
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
     * 成交价格
     */
    private BigDecimal price;
    
    /**
     * 状态：1-待付款 2-待发货 3-待收货 4-已完成 5-已取消
     */
    private Integer status;
    
    /**
     * 状态文本
     */
    private String statusText;
    
    /**
     * 收货地址
     */
    private String address;
    
    /**
     * 收货人姓名
     */
    private String receiverName;
    
    /**
     * 收货人电话
     */
    private String receiverPhone;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 更新时间
     */
    private Date updateTime;
} 