package com.example.secondhand_backend.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 支付订单DTO
 */
@Data
@Schema(description = "支付订单数据传输对象")
public class PaymentDTO {

    @Schema(description = "支付金额")
    private BigDecimal amount;

    @Schema(description = "支付方式: 1-支付宝 2-微信支付 3-银行卡")
    private Integer paymentMethod;

    @Schema(description = "支付账号信息（加密后的）")
    private String paymentAccount;

    @Schema(description = "订单留言")
    private String message;
} 