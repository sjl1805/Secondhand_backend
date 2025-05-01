package com.example.secondhand_backend.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 订单表
 *
 * @TableName orders
 */
@TableName(value = "orders")
@Data
public class Orders implements Serializable {
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
    /**
     * 订单ID
     */
    @TableId(type = IdType.AUTO)
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
     * 卖家ID
     */
    private Long sellerId;
    /**
     * 商品ID
     */
    private Long productId;
    /**
     * 成交价格
     */
    private BigDecimal price;
    /**
     * 状态：1-待付款 2-待发货 3-待收货 4-已完成 5-已取消
     */
    private Integer status;
    /**
     * 收货地址ID
     */
    private Long addressId;
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
    /**
     * 是否删除
     */
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;
    
    /**
     * 支付方式: 1-支付宝 2-微信支付 3-银行卡
     */
    private Integer paymentMethod;
    
    /**
     * 支付状态: 1-待支付 2-支付成功 3-支付失败
     */
    private Integer paymentStatus;
    
    /**
     * 支付时间
     */
    private Date paymentTime;
    
    /**
     * 支付交易号
     */
    private String transactionNo;
    
    /**
     * 订单留言
     */
    private String message;
}