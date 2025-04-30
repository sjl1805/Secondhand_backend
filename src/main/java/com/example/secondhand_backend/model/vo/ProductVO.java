package com.example.secondhand_backend.model.vo;

import com.example.secondhand_backend.model.entity.Product;
import com.example.secondhand_backend.model.entity.ProductImage;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 商品视图对象
 */
@Data
public class ProductVO {
    /**
     * 商品ID
     */
    private Long id;
    
    /**
     * 发布者ID
     */
    private Long userId;
    
    /**
     * 发布者昵称
     */
    private String nickname;
    
    /**
     * 发布者头像
     */
    private String avatar;
    
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
     * 分类名称
     */
    private String categoryName;
    
    /**
     * 状态：1-在售 2-已售 3-下架
     */
    private Integer status;
    
    /**
     * 浏览次数
     */
    private Integer viewCount;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 更新时间
     */
    private Date updateTime;
    
    /**
     * 商品图片列表
     */
    private List<String> imageUrls;
    
    /**
     * 是否已收藏
     */
    private Boolean isFavorite;
    
    /**
     * 收藏数量
     */
    private Integer favoriteCount;
} 