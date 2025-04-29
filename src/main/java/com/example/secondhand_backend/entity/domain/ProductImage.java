package com.example.secondhand_backend.entity.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 商品图片表
 * @TableName product_image
 */
@TableName(value ="product_image")
@Data
public class ProductImage implements Serializable {
    /**
     * 图片ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 图片URL
     */
    private String imageUrl;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 创建时间
     */
    private Date createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}