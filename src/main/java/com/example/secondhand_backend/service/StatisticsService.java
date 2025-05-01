package com.example.secondhand_backend.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 数据统计服务接口
 */
public interface StatisticsService {

    /**
     * 获取基本统计数据
     *
     * @return 包含用户总数、商品总数、订单总数、交易总额等信息的Map
     */
    Map<String, Object> getBasicStatistics();

    /**
     * 获取用户注册统计数据
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param timeUnit  时间单位：day-天、week-周、month-月
     * @return 包含时间点和用户数量的列表
     */
    List<Map<String, Object>> getUserRegisterStatistics(Date startDate, Date endDate, String timeUnit);

    /**
     * 获取订单统计数据
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param timeUnit  时间单位：day-天、week-周、month-月
     * @return 包含时间点和订单数量的列表
     */
    List<Map<String, Object>> getOrderStatistics(Date startDate, Date endDate, String timeUnit);

    /**
     * 获取交易额统计数据
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param timeUnit  时间单位：day-天、week-周、month-月
     * @return 包含时间点和交易金额的列表
     */
    List<Map<String, Object>> getTransactionStatistics(Date startDate, Date endDate, String timeUnit);

    /**
     * 获取分类商品统计数据
     *
     * @return 包含分类名称和商品数量的列表
     */
    List<Map<String, Object>> getCategoryProductStatistics();

    /**
     * 获取用户活跃度统计数据
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param timeUnit  时间单位：day-天、week-周、month-月
     * @return 包含时间点和活跃用户数量的列表
     */
    List<Map<String, Object>> getUserActivityStatistics(Date startDate, Date endDate, String timeUnit);

    /**
     * 获取商品状态统计数据
     *
     * @return 包含商品状态和数量的Map
     */
    Map<String, Integer> getProductStatusStatistics();

    /**
     * 获取订单状态统计数据
     *
     * @return 包含订单状态和数量的Map
     */
    Map<String, Integer> getOrderStatusStatistics();

    /**
     * 获取平台总收入（如果有手续费等收费项目）
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 总收入金额
     */
    BigDecimal getPlatformIncome(Date startDate, Date endDate);

    /**
     * 获取热门商品统计
     *
     * @param limit 返回数量限制
     * @return 包含商品ID、标题、浏览次数、收藏次数等信息的列表
     */
    List<Map<String, Object>> getHotProductsStatistics(int limit);

    /**
     * 获取活跃卖家统计
     *
     * @param limit 返回数量限制
     * @return 包含卖家ID、昵称、发布商品数、成交订单数等信息的列表
     */
    List<Map<String, Object>> getActiveSellersStatistics(int limit);

    /**
     * 获取活跃买家统计
     *
     * @param limit 返回数量限制
     * @return 包含买家ID、昵称、下单次数、下单金额等信息的列表
     */
    List<Map<String, Object>> getActiveBuyersStatistics(int limit);
} 