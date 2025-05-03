package com.example.secondhand_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.secondhand_backend.model.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author 28619
 * @description 针对表【product(商品表)】的数据库操作Mapper
 * @createDate 2025-04-29 13:42:31
 * @Entity generator.domain.Product
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    /**
     * 获取商品状态统计
     * @return 包含商品状态和对应数量的列表
     */
    @Select("SELECT status, COUNT(id) as count FROM product WHERE deleted = 0 GROUP BY status")
    List<Map<String, Object>> getProductStatusStatistics();
    
    /**
     * 统计今日新增商品数
     * @param startTime 今日开始时间
     * @param endTime 今日结束时间
     * @return 新增商品数
     */
    @Select("SELECT COUNT(*) FROM product WHERE deleted = 0 AND create_time BETWEEN #{startTime} AND #{endTime}")
    int countTodayNewProducts(@Param("startTime") Date startTime, @Param("endTime") Date endTime);
    
    /**
     * 获取热门商品统计数据
     * @param limit 返回数量限制
     * @return 热门商品列表
     */
    @Select("SELECT p.id, p.title, p.view_count, " +
            "(SELECT COUNT(*) FROM favorite f WHERE f.product_id = p.id) as favorite_count, " +
            "p.price, p.status, u.nickname as seller_name " +
            "FROM product p " +
            "LEFT JOIN user u ON p.user_id = u.id " +
            "WHERE p.deleted = 0 " +
            "ORDER BY (p.view_count + (SELECT COUNT(*) FROM favorite f WHERE f.product_id = p.id) * 2) DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> getHotProductsStatistics(@Param("limit") int limit);
}




