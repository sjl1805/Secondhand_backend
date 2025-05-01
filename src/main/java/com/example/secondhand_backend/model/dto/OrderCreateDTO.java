package com.example.secondhand_backend.model.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 创建订单数据传输对象
 */
@Data
public class OrderCreateDTO {
    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 收货地址ID
     */
    private Long addressId;

    /**
     * 成交价格
     */
    private BigDecimal price;
} 