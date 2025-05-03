package com.example.secondhand_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.secondhand_backend.mapper.OrdersMapper;
import com.example.secondhand_backend.mapper.ProductMapper;
import com.example.secondhand_backend.mapper.UserMapper;
import com.example.secondhand_backend.model.entity.Orders;
import com.example.secondhand_backend.model.entity.Product;
import com.example.secondhand_backend.model.entity.User;
import com.example.secondhand_backend.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 数据统计服务实现类
 */
@Service
public class StatisticsServiceImpl implements StatisticsService {

    // 缓存相关常量
    private static final String DASHBOARD_OVERVIEW_CACHE_KEY = "statistics:dashboard:overview";
    private static final String TODAY_STATISTICS_CACHE_KEY = "statistics:today";
    private static final String USER_REGISTER_STATISTICS_CACHE_PREFIX = "statistics:user:register:";
    private static final String ORDER_STATISTICS_CACHE_PREFIX = "statistics:order:";
    private static final String TRANSACTION_STATISTICS_CACHE_PREFIX = "statistics:transaction:";
    private static final String PRODUCT_STATUS_STATISTICS_CACHE_KEY = "statistics:product:status";
    private static final String ORDER_STATUS_STATISTICS_CACHE_KEY = "statistics:order:status";
    private static final String ACTIVE_SELLERS_CACHE_PREFIX = "statistics:sellers:active:";
    private static final String ACTIVE_BUYERS_CACHE_PREFIX = "statistics:buyers:active:";
    private static final String HOT_PRODUCTS_CACHE_PREFIX = "statistics:products:hot:";
    private static final String USER_ACTIVITY_CACHE_PREFIX = "statistics:user:activity:";
    private static final String PRODUCT_RATING_CACHE_PREFIX = "statistics:product:rating:";
    // 缓存过期时间（小时）
    private static final long CACHE_EXPIRE_TIME = 1; // 统计数据一般时效性较高，设置1小时过期
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private OrdersMapper ordersMapper;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public Map<String, Object> getDashboardOverview() {
        // 尝试从缓存获取
        Map<String, Object> result = (Map<String, Object>) redisTemplate.opsForValue().get(DASHBOARD_OVERVIEW_CACHE_KEY);

        if (result != null) {
            return result;
        }

        // 缓存未命中，从数据库查询
        result = new HashMap<>();

        // 统计总用户数
        LambdaQueryWrapper<User> userQueryWrapper = new LambdaQueryWrapper<>();
        userQueryWrapper.eq(User::getDeleted, 0);
        long totalUsers = userMapper.selectCount(userQueryWrapper);

        // 统计总商品数
        LambdaQueryWrapper<Product> productQueryWrapper = new LambdaQueryWrapper<>();
        productQueryWrapper.eq(Product::getDeleted, 0);
        long totalProducts = productMapper.selectCount(productQueryWrapper);

        // 统计总订单数
        LambdaQueryWrapper<Orders> ordersQueryWrapper = new LambdaQueryWrapper<>();
        ordersQueryWrapper.eq(Orders::getDeleted, 0);
        long totalOrders = ordersMapper.selectCount(ordersQueryWrapper);

        // 计算已完成订单的交易总额
        BigDecimal totalTransaction = ordersMapper.calculateTotalTransactionAmount();
        if (totalTransaction == null) {
            totalTransaction = BigDecimal.ZERO;
        }

        result.put("totalUsers", totalUsers);
        result.put("totalProducts", totalProducts);
        result.put("totalOrders", totalOrders);
        result.put("totalTransaction", totalTransaction);

        // 存入缓存
        redisTemplate.opsForValue().set(DASHBOARD_OVERVIEW_CACHE_KEY, result, CACHE_EXPIRE_TIME, TimeUnit.HOURS);

        return result;
    }

    @Override
    public Map<String, Object> getTodayStatistics() {
        // 尝试从缓存获取
        Map<String, Object> result = (Map<String, Object>) redisTemplate.opsForValue().get(TODAY_STATISTICS_CACHE_KEY);

        if (result != null) {
            return result;
        }

        // 缓存未命中，从数据库查询
        result = new HashMap<>();

        // 获取今日开始和结束时间
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Date startTime = calendar.getTime();

        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        Date endTime = calendar.getTime();

        // 统计今日新增用户数
        int todayNewUsers = userMapper.countTodayNewUsers(startTime, endTime);

        // 统计今日新增商品数
        int todayNewProducts = productMapper.countTodayNewProducts(startTime, endTime);

        // 统计今日新增订单数
        int todayNewOrders = ordersMapper.countTodayNewOrders(startTime, endTime);

        // 计算今日交易额
        BigDecimal todayTransaction = ordersMapper.calculateTodayTransactionAmount(startTime, endTime);
        if (todayTransaction == null) {
            todayTransaction = BigDecimal.ZERO;
        }

        result.put("todayNewUsers", todayNewUsers);
        result.put("todayNewProducts", todayNewProducts);
        result.put("todayNewOrders", todayNewOrders);
        result.put("todayTransaction", todayTransaction);

        // 由于今日统计数据变化较快，设置较短过期时间
        redisTemplate.opsForValue().set(TODAY_STATISTICS_CACHE_KEY, result, 30, TimeUnit.MINUTES);

        return result;
    }

    @Override
    public List<Map<String, Object>> getUserRegisterStatistics(Date startDate, Date endDate, String timeGranularity) {
        // 构建缓存key
        String cacheKey = USER_REGISTER_STATISTICS_CACHE_PREFIX + timeGranularity + ":"
                + startDate.getTime() + ":" + endDate.getTime();

        // 尝试从缓存获取
        List<Map<String, Object>> result = (List<Map<String, Object>>) redisTemplate.opsForValue().get(cacheKey);

        if (result != null) {
            return result;
        }

        // 缓存未命中，查询数据库
        String dateFormat = getDateFormat(timeGranularity);
        result = userMapper.getUserRegisterStatistics(startDate, endDate, dateFormat);

        // 存入缓存
        redisTemplate.opsForValue().set(cacheKey, result, CACHE_EXPIRE_TIME, TimeUnit.HOURS);

        return result;
    }

    @Override
    public List<Map<String, Object>> getOrderStatistics(Date startDate, Date endDate, String timeGranularity) {
        // 构建缓存key
        String cacheKey = ORDER_STATISTICS_CACHE_PREFIX + timeGranularity + ":"
                + startDate.getTime() + ":" + endDate.getTime();

        // 尝试从缓存获取
        List<Map<String, Object>> result = (List<Map<String, Object>>) redisTemplate.opsForValue().get(cacheKey);

        if (result != null) {
            return result;
        }

        // 缓存未命中，查询数据库
        String dateFormat = getDateFormat(timeGranularity);
        result = ordersMapper.getOrderStatistics(startDate, endDate, dateFormat);

        // 存入缓存
        redisTemplate.opsForValue().set(cacheKey, result, CACHE_EXPIRE_TIME, TimeUnit.HOURS);

        return result;
    }

    @Override
    public List<Map<String, Object>> getTransactionStatistics(Date startDate, Date endDate, String timeGranularity) {
        // 构建缓存key
        String cacheKey = TRANSACTION_STATISTICS_CACHE_PREFIX + timeGranularity + ":"
                + startDate.getTime() + ":" + endDate.getTime();

        // 尝试从缓存获取
        List<Map<String, Object>> result = (List<Map<String, Object>>) redisTemplate.opsForValue().get(cacheKey);

        if (result != null) {
            return result;
        }

        // 缓存未命中，查询数据库
        String dateFormat = getDateFormat(timeGranularity);
        result = ordersMapper.getTransactionStatistics(startDate, endDate, dateFormat);

        // 存入缓存
        redisTemplate.opsForValue().set(cacheKey, result, CACHE_EXPIRE_TIME, TimeUnit.HOURS);

        return result;
    }

    @Override
    public List<Map<String, Object>> getProductStatusStatistics() {
        // 尝试从缓存获取
        List<Map<String, Object>> result = (List<Map<String, Object>>) redisTemplate.opsForValue()
                .get(PRODUCT_STATUS_STATISTICS_CACHE_KEY);

        if (result != null) {
            return result;
        }

        // 缓存未命中，查询数据库
        result = productMapper.getProductStatusStatistics();

        // 存入缓存
        redisTemplate.opsForValue().set(PRODUCT_STATUS_STATISTICS_CACHE_KEY, result, CACHE_EXPIRE_TIME, TimeUnit.HOURS);

        return result;
    }

    @Override
    public List<Map<String, Object>> getOrderStatusStatistics() {
        // 尝试从缓存获取
        List<Map<String, Object>> result = (List<Map<String, Object>>) redisTemplate.opsForValue()
                .get(ORDER_STATUS_STATISTICS_CACHE_KEY);

        if (result != null) {
            return result;
        }

        // 缓存未命中，查询数据库
        result = ordersMapper.getOrderStatusStatistics();

        // 存入缓存
        redisTemplate.opsForValue().set(ORDER_STATUS_STATISTICS_CACHE_KEY, result, CACHE_EXPIRE_TIME, TimeUnit.HOURS);

        return result;
    }

    @Override
    public List<Map<String, Object>> getActiveSellersStatistics(int limit) {
        // 构建缓存key
        String cacheKey = ACTIVE_SELLERS_CACHE_PREFIX + limit;

        // 尝试从缓存获取
        List<Map<String, Object>> result = (List<Map<String, Object>>) redisTemplate.opsForValue().get(cacheKey);

        if (result != null) {
            return result;
        }

        // 缓存未命中，查询数据库
        result = userMapper.getActiveSellersStatistics(limit);

        // 存入缓存
        redisTemplate.opsForValue().set(cacheKey, result, CACHE_EXPIRE_TIME, TimeUnit.HOURS);

        return result;
    }

    @Override
    public List<Map<String, Object>> getActiveBuyersStatistics(int limit) {
        // 构建缓存key
        String cacheKey = ACTIVE_BUYERS_CACHE_PREFIX + limit;

        // 尝试从缓存获取
        List<Map<String, Object>> result = (List<Map<String, Object>>) redisTemplate.opsForValue().get(cacheKey);

        if (result != null) {
            return result;
        }

        // 缓存未命中，查询数据库
        result = userMapper.getActiveBuyersStatistics(limit);

        // 存入缓存
        redisTemplate.opsForValue().set(cacheKey, result, CACHE_EXPIRE_TIME, TimeUnit.HOURS);

        return result;
    }

    @Override
    public List<Map<String, Object>> getHotProductsStatistics(int limit) {
        // 构建缓存key
        String cacheKey = HOT_PRODUCTS_CACHE_PREFIX + limit;

        // 尝试从缓存获取
        List<Map<String, Object>> result = (List<Map<String, Object>>) redisTemplate.opsForValue().get(cacheKey);

        if (result != null) {
            return result;
        }

        // 缓存未命中，查询数据库
        result = productMapper.getHotProductsStatistics(limit);

        // 存入缓存
        redisTemplate.opsForValue().set(cacheKey, result, CACHE_EXPIRE_TIME, TimeUnit.HOURS);

        return result;
    }

    @Override
    public List<Map<String, Object>> getUserActivityStatistics(Date startDate, Date endDate, String timeGranularity) {
        // 构建缓存key
        String cacheKey = USER_ACTIVITY_CACHE_PREFIX + timeGranularity + ":"
                + startDate.getTime() + ":" + endDate.getTime();

        // 尝试从缓存获取
        List<Map<String, Object>> result = (List<Map<String, Object>>) redisTemplate.opsForValue().get(cacheKey);

        if (result != null) {
            return result;
        }

        // 缓存未命中，查询数据库
        String dateFormat = getDateFormat(timeGranularity);

        // 获取买家活跃度数据
        List<Map<String, Object>> buyerActivity = ordersMapper.getUserActivityByBuyerStatistics(startDate, endDate, dateFormat);

        // 获取卖家活跃度数据
        List<Map<String, Object>> sellerActivity = ordersMapper.getUserActivityBySellerStatistics(startDate, endDate, dateFormat);

        // 合并数据
        Map<String, Integer> mergedData = new HashMap<>();

        for (Map<String, Object> item : buyerActivity) {
            String timePeriod = (String) item.get("time_period");
            Integer activeUsers = ((Number) item.get("active_users")).intValue();
            mergedData.put(timePeriod, activeUsers);
        }

        for (Map<String, Object> item : sellerActivity) {
            String timePeriod = (String) item.get("time_period");
            Integer activeUsers = ((Number) item.get("active_users")).intValue();

            if (mergedData.containsKey(timePeriod)) {
                // 去重用户（可能既是买家又是卖家）
                mergedData.put(timePeriod, Math.max(mergedData.get(timePeriod), activeUsers));
            } else {
                mergedData.put(timePeriod, activeUsers);
            }
        }

        // 转换为结果列表
        result = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : mergedData.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("time_period", entry.getKey());
            item.put("active_users", entry.getValue());
            result.add(item);
        }

        // 按时间排序
        result.sort(Comparator.comparing(m -> (String) m.get("time_period")));

        // 存入缓存
        redisTemplate.opsForValue().set(cacheKey, result, CACHE_EXPIRE_TIME, TimeUnit.HOURS);

        return result;
    }

    /**
     * 根据时间粒度获取对应的日期格式
     *
     * @param timeGranularity 时间粒度
     * @return 日期格式
     */
    private String getDateFormat(String timeGranularity) {
        switch (timeGranularity) {
            case "day":
                return "%Y-%m-%d";
            case "week":
                return "%Y-%u"; // %u表示一周中的第几天（1-7，1代表周一）
            case "month":
                return "%Y-%m";
            default:
                return "%Y-%m-%d";
        }
    }

    @Override
    public Map<String, Integer> getProductRatingStatistics(Long productId) {
        // 构建缓存key
        String cacheKey = PRODUCT_RATING_CACHE_PREFIX + productId;

        // 尝试从缓存获取
        Map<String, Integer> result = (Map<String, Integer>) redisTemplate.opsForValue().get(cacheKey);

        if (result != null) {
            return result;
        }

        // 缓存未命中，查询数据库
        result = productMapper.getProductRatingStatistics(productId);

        // 存入缓存
        redisTemplate.opsForValue().set(cacheKey, result, CACHE_EXPIRE_TIME, TimeUnit.HOURS);

        return result;
    }

    /**
     * 刷新统计数据缓存
     */
    public void refreshStatisticsCache() {
        // 删除所有统计相关缓存
        redisTemplate.delete(DASHBOARD_OVERVIEW_CACHE_KEY);
        redisTemplate.delete(TODAY_STATISTICS_CACHE_KEY);

        // 删除带前缀的缓存
        Set<String> keys = redisTemplate.keys("statistics:*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
} 