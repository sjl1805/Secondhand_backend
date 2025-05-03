package com.example.secondhand_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.secondhand_backend.model.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author 28619
 * @description 针对表【user(用户表)】的数据库操作Mapper
 * @createDate 2025-04-29 13:42:42
 * @Entity generator.domain.User
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 获取用户注册统计数据
     *
     * @param startDate  开始日期
     * @param endDate    结束日期
     * @param dateFormat 日期格式
     * @return 包含时间点和用户数量的列表
     */
    @Select("SELECT DATE_FORMAT(create_time, #{dateFormat}) as time_period, COUNT(id) as count " +
            "FROM user WHERE deleted = 0 AND create_time BETWEEN #{startDate} AND #{endDate} " +
            "GROUP BY time_period ORDER BY time_period")
    List<Map<String, Object>> getUserRegisterStatistics(
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate,
            @Param("dateFormat") String dateFormat);

    /**
     * 统计今日新增用户数
     *
     * @param startTime 今日开始时间
     * @param endTime   今日结束时间
     * @return 新增用户数
     */
    @Select("SELECT COUNT(*) FROM user WHERE deleted = 0 AND create_time BETWEEN #{startTime} AND #{endTime}")
    int countTodayNewUsers(@Param("startTime") Date startTime, @Param("endTime") Date endTime);

    /**
     * 获取活跃卖家统计数据
     *
     * @param limit 返回数量限制
     * @return 活跃卖家列表
     */
    @Select("SELECT u.id, u.nickname, u.avatar, " +
            "(SELECT COUNT(*) FROM product p WHERE p.user_id = u.id AND p.deleted = 0) as product_count, " +
            "(SELECT COUNT(*) FROM orders o WHERE o.seller_id = u.id AND o.status = 4 AND o.deleted = 0) as completed_order_count, " +
            "(SELECT SUM(o.price) FROM orders o WHERE o.seller_id = u.id AND o.status = 4 AND o.deleted = 0) as total_sales " +
            "FROM user u " +
            "WHERE u.deleted = 0 " +
            "HAVING product_count > 0 OR completed_order_count > 0 " +
            "ORDER BY (product_count + completed_order_count * 2) DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> getActiveSellersStatistics(@Param("limit") int limit);

    /**
     * 获取活跃买家统计数据
     *
     * @param limit 返回数量限制
     * @return 活跃买家列表
     */
    @Select("SELECT u.id, u.nickname, u.avatar, " +
            "COUNT(o.id) as order_count, " +
            "SUM(o.price) as total_spent " +
            "FROM user u " +
            "JOIN orders o ON u.id = o.buyer_id " +
            "WHERE u.deleted = 0 AND o.deleted = 0 " +
            "GROUP BY u.id, u.nickname, u.avatar " +
            "ORDER BY order_count DESC, total_spent DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> getActiveBuyersStatistics(@Param("limit") int limit);
}




