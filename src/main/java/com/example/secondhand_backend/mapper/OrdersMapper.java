package com.example.secondhand_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.secondhand_backend.model.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author 28619
 * @description 针对表【orders(订单表)】的数据库操作Mapper
 * @createDate 2025-04-29 13:42:28
 * @Entity generator.domain.Orders
 */
@Mapper
public interface OrdersMapper extends BaseMapper<Orders> {

    /**
     * 获取订单状态统计
     * @return 包含订单状态和对应数量的列表
     */
    @Select("SELECT status, COUNT(id) as count FROM orders WHERE deleted = 0 GROUP BY status")
    List<Map<String, Object>> getOrderStatusStatistics();
    
    /**
     * 获取订单统计数据
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param dateFormat 日期格式
     * @return 包含时间点和订单数量的列表
     */
    @Select("SELECT DATE_FORMAT(create_time, #{dateFormat}) as time_period, " +
            "COUNT(id) as count FROM orders WHERE deleted = 0 " +
            "AND create_time BETWEEN #{startDate} AND #{endDate} " +
            "GROUP BY time_period ORDER BY time_period")
    List<Map<String, Object>> getOrderStatistics(
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate,
            @Param("dateFormat") String dateFormat);
    
    /**
     * 获取交易额统计数据
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param dateFormat 日期格式
     * @return 包含时间点和交易金额的列表
     */
    @Select("SELECT DATE_FORMAT(create_time, #{dateFormat}) as time_period, " +
            "SUM(price) as amount FROM orders WHERE deleted = 0 AND status = 4 " +
            "AND create_time BETWEEN #{startDate} AND #{endDate} " +
            "GROUP BY time_period ORDER BY time_period")
    List<Map<String, Object>> getTransactionStatistics(
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate,
            @Param("dateFormat") String dateFormat);
    
    /**
     * 计算平台收入
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 平台收入金额
     */
    @Select("SELECT SUM(price) * 0.03 as platform_income FROM orders " +
            "WHERE deleted = 0 AND status = 4 " +
            "AND create_time BETWEEN #{startDate} AND #{endDate}")
    BigDecimal getPlatformIncome(
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate);
    
    /**
     * 统计今日新增订单数
     * @param startTime 今日开始时间
     * @param endTime 今日结束时间
     * @return 新增订单数
     */
    @Select("SELECT COUNT(*) FROM orders WHERE deleted = 0 " +
            "AND create_time BETWEEN #{startTime} AND #{endTime}")
    int countTodayNewOrders(
            @Param("startTime") Date startTime,
            @Param("endTime") Date endTime);
    
    /**
     * 计算已完成订单的交易总额
     * @return 交易总额
     */
    @Select("SELECT SUM(price) FROM orders WHERE deleted = 0 AND status = 4")
    BigDecimal calculateTotalTransactionAmount();
    
    /**
     * 获取用户活跃度统计数据(买家部分)
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param dateFormat 日期格式
     * @return 包含时间点和活跃用户数的列表
     */
    @Select("SELECT DATE_FORMAT(create_time, #{dateFormat}) as time_period, " +
            "COUNT(DISTINCT buyer_id) as active_users " +
            "FROM orders " +
            "WHERE deleted = 0 " +
            "AND create_time BETWEEN #{startDate} AND #{endDate} " +
            "GROUP BY time_period " +
            "ORDER BY time_period")
    List<Map<String, Object>> getUserActivityByBuyerStatistics(
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate,
            @Param("dateFormat") String dateFormat);
    
    /**
     * 获取用户活跃度统计数据(卖家部分)
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param dateFormat 日期格式
     * @return 包含时间点和活跃用户数的列表
     */
    @Select("SELECT DATE_FORMAT(create_time, #{dateFormat}) as time_period, " +
            "COUNT(DISTINCT seller_id) as active_users " +
            "FROM orders " +
            "WHERE deleted = 0 " +
            "AND create_time BETWEEN #{startDate} AND #{endDate} " +
            "GROUP BY time_period " +
            "ORDER BY time_period")
    List<Map<String, Object>> getUserActivityBySellerStatistics(
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate,
            @Param("dateFormat") String dateFormat);
}




