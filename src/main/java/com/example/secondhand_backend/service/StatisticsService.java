package com.example.secondhand_backend.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 数据统计服务接口
 */
public interface StatisticsService {

    /**
     * 获取概览数据
     * @return 包含总用户数、总商品数、总订单数、完成交易额的Map
     */
    Map<String, Object> getDashboardOverview();

    /**
     * 获取今日数据统计
     * @return 包含今日新增用户数、今日新增商品数、今日新增订单数的Map
     */
    Map<String, Object> getTodayStatistics();

    /**
     * 获取用户注册统计数据
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param timeGranularity 时间粒度：day, week, month
     * @return 用户注册统计数据
     */
    List<Map<String, Object>> getUserRegisterStatistics(Date startDate, Date endDate, String timeGranularity);

    /**
     * 获取订单统计数据
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param timeGranularity 时间粒度：day, week, month
     * @return 订单统计数据
     */
    List<Map<String, Object>> getOrderStatistics(Date startDate, Date endDate, String timeGranularity);

    /**
     * 获取交易额统计数据
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param timeGranularity 时间粒度：day, week, month
     * @return 交易额统计数据
     */
    List<Map<String, Object>> getTransactionStatistics(Date startDate, Date endDate, String timeGranularity);

    /**
     * 获取商品状态统计
     * @return 商品状态统计数据
     */
    List<Map<String, Object>> getProductStatusStatistics();

    /**
     * 获取订单状态统计
     * @return 订单状态统计数据
     */
    List<Map<String, Object>> getOrderStatusStatistics();

    /**
     * 获取活跃卖家统计数据
     * @param limit 返回数量限制
     * @return 活跃卖家统计数据
     */
    List<Map<String, Object>> getActiveSellersStatistics(int limit);

    /**
     * 获取活跃买家统计数据
     * @param limit 返回数量限制
     * @return 活跃买家统计数据
     */
    List<Map<String, Object>> getActiveBuyersStatistics(int limit);

    /**
     * 获取热门商品统计数据
     * @param limit 返回数量限制
     * @return 热门商品统计数据
     */
    List<Map<String, Object>> getHotProductsStatistics(int limit);

    /**
     * 获取用户活跃度统计数据
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param timeGranularity 时间粒度：day, week, month
     * @return 用户活跃度统计数据
     */
    List<Map<String, Object>> getUserActivityStatistics(Date startDate, Date endDate, String timeGranularity);

    /**
     * 获取商品评分统计
     * @param productId 商品ID
     * @return 商品评分统计数据
     */
    Map<String, Integer> getProductRatingStatistics(Long productId);
} 
