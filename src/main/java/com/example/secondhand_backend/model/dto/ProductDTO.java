package com.example.secondhand_backend.model.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品数据传输对象
 */
@Data
public class ProductDTO {
    /**
     * 商品标题
     */
    private String title;

    /**
     * 商品描述
     */
    private String description;

    /**
     * 价格
     */
    private BigDecimal price;

    /**
     * 分类ID
     */
    private Integer categoryId;

    /**
     * 图片URL列表
     */
    private List<String> imageUrls;

    /**
     * 商品成色：1-全新 2-几乎全新 3-轻微使用痕迹 4-正常使用痕迹 5-明显使用痕迹
     */
    private Integer conditions;

} 