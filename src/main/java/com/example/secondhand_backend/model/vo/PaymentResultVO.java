package com.example.secondhand_backend.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 支付结果VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "支付结果视图对象")
public class PaymentResultVO {

    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "订单编号")
    private String orderNo;

    @Schema(description = "支付状态: 1-待支付 2-支付成功 3-支付失败")
    private Integer paymentStatus;

    @Schema(description = "支付状态描述")
    private String paymentStatusDesc;

    @Schema(description = "支付金额")
    private BigDecimal amount;

    @Schema(description = "支付方式: 1-支付宝 2-微信支付 3-银行卡")
    private Integer paymentMethod;

    @Schema(description = "支付方式描述")
    private String paymentMethodDesc;

    @Schema(description = "支付时间")
    private Date paymentTime;

    @Schema(description = "支付交易号")
    private String transactionNo;
} 