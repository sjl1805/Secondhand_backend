package com.example.secondhand_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.secondhand_backend.model.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * @author 28619
 * @description 针对表【category(商品分类表)】的数据库操作Mapper
 * @createDate 2025-04-29 13:42:16
 * @Entity generator.domain.Category
 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {

    /**
     * 获取分类商品统计数据
     * @return 包含分类名称和商品数量的列表
     */
    @Select("SELECT c.id, c.name, COUNT(p.id) as product_count " +
            "FROM category c " +
            "LEFT JOIN product p ON c.id = p.category_id AND p.deleted = 0 " +
            "GROUP BY c.id, c.name " +
            "ORDER BY product_count DESC")
    List<Map<String, Object>> getCategoryProductStatistics();
}




