package com.example.secondhand_backend.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 分类DTO
 */
@Data
@Schema(description = "分类数据传输对象")
public class CategoryDTO {
    /**
     * 分类ID（更新时必填）
     */
    @Schema(description = "分类ID（更新时必填）")
    private Integer id;

    /**
     * 分类名称
     */
    @Schema(description = "分类名称", required = true)
    private String name;

    /**
     * 父分类ID
     */
    @Schema(description = "父分类ID", defaultValue = "0")
    private Integer parentId;

    /**
     * 排序
     */
    @Schema(description = "排序", defaultValue = "0")
    private Integer sort;
} 