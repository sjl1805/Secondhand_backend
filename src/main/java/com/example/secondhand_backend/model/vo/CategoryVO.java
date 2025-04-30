package com.example.secondhand_backend.model.vo;

import com.example.secondhand_backend.model.entity.Category;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 分类VO
 */
@Data
public class CategoryVO {
    /**
     * 分类ID
     */
    private Integer id;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 父分类ID
     */
    private Integer parentId;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 子分类列表
     */
    private List<CategoryVO> children;
    
    /**
     * 从Category实体转换为CategoryVO
     * @param category 分类实体
     * @return CategoryVO
     */
    public static CategoryVO fromCategory(Category category) {
        if (category == null) {
            return null;
        }
        
        CategoryVO vo = new CategoryVO();
        vo.setId(category.getId());
        vo.setName(category.getName());
        vo.setParentId(category.getParentId());
        vo.setSort(category.getSort());
        vo.setCreateTime(category.getCreateTime());
        
        return vo;
    }
} 