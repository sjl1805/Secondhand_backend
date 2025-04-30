package com.example.secondhand_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.secondhand_backend.mapper.*;
import com.example.secondhand_backend.model.entity.*;
import com.example.secondhand_backend.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据统计服务实现类
 */
@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private ProductMapper productMapper;
    
    @Autowired
    private OrdersMapper ordersMapper;
    
    @Autowired
    private CategoryMapper categoryMapper;
    
    @Autowired
    private FavoriteMapper favoriteMapper;
    
    @Autowired
    private CommentMapper commentMapper;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public Map<String, Object> getBasicStatistics() {
        Map<String, Object> result = new HashMap<>();
        
        // 用户总数（未删除的用户）
        LambdaQueryWrapper<User> userQueryWrapper = new LambdaQueryWrapper<>();
        userQueryWrapper.eq(User::getDeleted, 0);
        Long totalUsers = userMapper.selectCount(userQueryWrapper);
        result.put("totalUsers", totalUsers);
        
        // 商品总数（未删除的商品）
        LambdaQueryWrapper<Product> productQueryWrapper = new LambdaQueryWrapper<>();
        productQueryWrapper.eq(Product::getDeleted, 0);
        Long totalProducts = productMapper.selectCount(productQueryWrapper);
        result.put("totalProducts", totalProducts);
        
        // 在售商品数
        LambdaQueryWrapper<Product> onSaleQueryWrapper = new LambdaQueryWrapper<>();
        onSaleQueryWrapper.eq(Product::getDeleted, 0).eq(Product::getStatus, 1);
        Long onSaleProducts = productMapper.selectCount(onSaleQueryWrapper);
        result.put("onSaleProducts", onSaleProducts);
        
        // 订单总数（未删除的订单）
        LambdaQueryWrapper<Orders> orderQueryWrapper = new LambdaQueryWrapper<>();
        orderQueryWrapper.eq(Orders::getDeleted, 0);
        Long totalOrders = ordersMapper.selectCount(orderQueryWrapper);
        result.put("totalOrders", totalOrders);
        
        // 完成订单数
        LambdaQueryWrapper<Orders> completedOrderQueryWrapper = new LambdaQueryWrapper<>();
        completedOrderQueryWrapper.eq(Orders::getDeleted, 0).eq(Orders::getStatus, 4);
        Long completedOrders = ordersMapper.selectCount(completedOrderQueryWrapper);
        result.put("completedOrders", completedOrders);
        
        // 交易总额（已完成订单）
        BigDecimal totalTransactionAmount = calculateTotalTransactionAmount();
        result.put("totalTransactionAmount", totalTransactionAmount);
        
        // 今日新增用户数
        int todayNewUsers = countTodayNewUsers();
        result.put("todayNewUsers", todayNewUsers);
        
        // 今日新增订单数
        int todayNewOrders = countTodayNewOrders();
        result.put("todayNewOrders", todayNewOrders);
        
        // 今日新增商品数
        int todayNewProducts = countTodayNewProducts();
        result.put("todayNewProducts", todayNewProducts);
        
        return result;
    }

    @Override
    public List<Map<String, Object>> getUserRegisterStatistics(Date startDate, Date endDate, String timeUnit) {
        // 转换为sql查询
        String sqlFormat = determineSqlDateFormat(timeUnit);
        String sql = "SELECT " +
                "DATE_FORMAT(create_time, '" + sqlFormat + "') as time_period, " +
                "COUNT(id) as count " +
                "FROM user " +
                "WHERE deleted = 0 " +
                "AND create_time BETWEEN ? AND ? " +
                "GROUP BY time_period " +
                "ORDER BY time_period";
        
        return jdbcTemplate.queryForList(sql, startDate, endDate)
                .stream()
                .map(row -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("timePeriod", row.get("time_period"));
                    map.put("count", row.get("count"));
                    return map;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getOrderStatistics(Date startDate, Date endDate, String timeUnit) {
        // 转换为sql查询
        String sqlFormat = determineSqlDateFormat(timeUnit);
        String sql = "SELECT " +
                "DATE_FORMAT(create_time, '" + sqlFormat + "') as time_period, " +
                "COUNT(id) as count " +
                "FROM orders " +
                "WHERE deleted = 0 " +
                "AND create_time BETWEEN ? AND ? " +
                "GROUP BY time_period " +
                "ORDER BY time_period";
        
        return jdbcTemplate.queryForList(sql, startDate, endDate)
                .stream()
                .map(row -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("timePeriod", row.get("time_period"));
                    map.put("count", row.get("count"));
                    return map;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getTransactionStatistics(Date startDate, Date endDate, String timeUnit) {
        // 转换为sql查询，只计算已完成订单（状态为4）
        String sqlFormat = determineSqlDateFormat(timeUnit);
        String sql = "SELECT " +
                "DATE_FORMAT(create_time, '" + sqlFormat + "') as time_period, " +
                "SUM(price) as amount " +
                "FROM orders " +
                "WHERE deleted = 0 " +
                "AND status = 4 " + // 已完成订单
                "AND create_time BETWEEN ? AND ? " +
                "GROUP BY time_period " +
                "ORDER BY time_period";
        
        return jdbcTemplate.queryForList(sql, startDate, endDate)
                .stream()
                .map(row -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("timePeriod", row.get("time_period"));
                    map.put("amount", row.get("amount"));
                    return map;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getCategoryProductStatistics() {
        String sql = "SELECT " +
                "c.id, c.name, " +
                "COUNT(p.id) as product_count " +
                "FROM category c " +
                "LEFT JOIN product p ON c.id = p.category_id AND p.deleted = 0 " +
                "GROUP BY c.id, c.name " +
                "ORDER BY product_count DESC";
        
        return jdbcTemplate.queryForList(sql)
                .stream()
                .map(row -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("categoryId", row.get("id"));
                    map.put("categoryName", row.get("name"));
                    map.put("productCount", row.get("product_count"));
                    return map;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getUserActivityStatistics(Date startDate, Date endDate, String timeUnit) {
        // 用户活跃度统计（通过订单和评论数量）
        String sqlFormat = determineSqlDateFormat(timeUnit);
        
        String sql = "SELECT " +
                "DATE_FORMAT(time_date, '" + sqlFormat + "') as time_period, " +
                "COUNT(DISTINCT user_id) as active_users " +
                "FROM ( " +
                "  SELECT buyer_id as user_id, create_time as time_date FROM orders WHERE deleted = 0 AND create_time BETWEEN ? AND ? " +
                "  UNION ALL " +
                "  SELECT seller_id as user_id, create_time as time_date FROM orders WHERE deleted = 0 AND create_time BETWEEN ? AND ? " +
                "  UNION ALL " +
                "  SELECT user_id, create_time as time_date FROM comment WHERE deleted = 0 AND create_time BETWEEN ? AND ? " +
                ") as user_activities " +
                "GROUP BY time_period " +
                "ORDER BY time_period";
        
        return jdbcTemplate.queryForList(sql, startDate, endDate, startDate, endDate, startDate, endDate)
                .stream()
                .map(row -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("timePeriod", row.get("time_period"));
                    map.put("activeUsers", row.get("active_users"));
                    return map;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Integer> getProductStatusStatistics() {
        Map<String, Integer> result = new HashMap<>();
        
        // 统计各状态商品数量
        String sql = "SELECT " +
                "status, COUNT(id) as count " +
                "FROM product " +
                "WHERE deleted = 0 " +
                "GROUP BY status";
        
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
        for (Map<String, Object> row : rows) {
            int status = ((Number) row.get("status")).intValue();
            int count = ((Number) row.get("count")).intValue();
            
            switch (status) {
                case 1:
                    result.put("onSale", count); // 在售
                    break;
                case 2:
                    result.put("sold", count); // 已售
                    break;
                case 3:
                    result.put("offShelf", count); // 下架
                    break;
                default:
                    result.put("unknown", count);
                    break;
            }
        }
        
        return result;
    }

    @Override
    public Map<String, Integer> getOrderStatusStatistics() {
        Map<String, Integer> result = new HashMap<>();
        
        // 统计各状态订单数量
        String sql = "SELECT " +
                "status, COUNT(id) as count " +
                "FROM orders " +
                "WHERE deleted = 0 " +
                "GROUP BY status";
        
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
        for (Map<String, Object> row : rows) {
            int status = ((Number) row.get("status")).intValue();
            int count = ((Number) row.get("count")).intValue();
            
            switch (status) {
                case 1:
                    result.put("pendingPayment", count); // 待付款
                    break;
                case 2:
                    result.put("pendingShipment", count); // 待发货
                    break;
                case 3:
                    result.put("pendingReceipt", count); // 待收货
                    break;
                case 4:
                    result.put("completed", count); // 已完成
                    break;
                case 5:
                    result.put("cancelled", count); // 已取消
                    break;
                default:
                    result.put("unknown", count);
                    break;
            }
        }
        
        return result;
    }

    @Override
    public BigDecimal getPlatformIncome(Date startDate, Date endDate) {
        // 假设平台收入为交易额的3%
        String sql = "SELECT " +
                "SUM(price) * 0.03 as platform_income " +
                "FROM orders " +
                "WHERE deleted = 0 " +
                "AND status = 4 " + // 已完成订单
                "AND create_time BETWEEN ? AND ?";
        
        BigDecimal income = jdbcTemplate.queryForObject(sql, BigDecimal.class, startDate, endDate);
        return income != null ? income : BigDecimal.ZERO;
    }

    @Override
    public List<Map<String, Object>> getHotProductsStatistics(int limit) {
        // 热门商品统计（基于浏览次数和收藏数）
        String sql = "SELECT " +
                "p.id, p.title, p.view_count, " +
                "(SELECT COUNT(*) FROM favorite f WHERE f.product_id = p.id) as favorite_count, " +
                "p.price, p.status, u.nickname as seller_name " +
                "FROM product p " +
                "LEFT JOIN user u ON p.user_id = u.id " +
                "WHERE p.deleted = 0 " +
                "ORDER BY (p.view_count + (SELECT COUNT(*) FROM favorite f WHERE f.product_id = p.id) * 2) DESC " + // 浏览量 + 收藏数*2 作为热度指标
                "LIMIT ?";
        
        return jdbcTemplate.queryForList(sql, limit)
                .stream()
                .map(row -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("productId", row.get("id"));
                    map.put("title", row.get("title"));
                    map.put("viewCount", row.get("view_count"));
                    map.put("favoriteCount", row.get("favorite_count"));
                    map.put("price", row.get("price"));
                    map.put("status", row.get("status"));
                    map.put("sellerName", row.get("seller_name"));
                    return map;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getActiveSellersStatistics(int limit) {
        // 活跃卖家统计（基于发布商品数和成交订单数）
        String sql = "SELECT " +
                "u.id, u.nickname, u.avatar, " +
                "(SELECT COUNT(*) FROM product p WHERE p.user_id = u.id AND p.deleted = 0) as product_count, " +
                "(SELECT COUNT(*) FROM orders o WHERE o.seller_id = u.id AND o.status = 4 AND o.deleted = 0) as completed_order_count, " +
                "(SELECT SUM(o.price) FROM orders o WHERE o.seller_id = u.id AND o.status = 4 AND o.deleted = 0) as total_sales " +
                "FROM user u " +
                "WHERE u.deleted = 0 " +
                "HAVING product_count > 0 OR completed_order_count > 0 " +
                "ORDER BY (product_count + completed_order_count * 2) DESC " + // 商品数 + 成交订单数*2 作为活跃度指标
                "LIMIT ?";
        
        return jdbcTemplate.queryForList(sql, limit)
                .stream()
                .map(row -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("sellerId", row.get("id"));
                    map.put("nickname", row.get("nickname"));
                    map.put("avatar", row.get("avatar"));
                    map.put("productCount", row.get("product_count"));
                    map.put("completedOrderCount", row.get("completed_order_count"));
                    map.put("totalSales", row.get("total_sales"));
                    return map;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getActiveBuyersStatistics(int limit) {
        // 活跃买家统计（基于下单次数和下单金额）
        String sql = "SELECT " +
                "u.id, u.nickname, u.avatar, " +
                "COUNT(o.id) as order_count, " +
                "SUM(o.price) as total_spent " +
                "FROM user u " +
                "JOIN orders o ON u.id = o.buyer_id " +
                "WHERE u.deleted = 0 AND o.deleted = 0 " +
                "GROUP BY u.id, u.nickname, u.avatar " +
                "ORDER BY order_count DESC, total_spent DESC " +
                "LIMIT ?";
        
        return jdbcTemplate.queryForList(sql, limit)
                .stream()
                .map(row -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("buyerId", row.get("id"));
                    map.put("nickname", row.get("nickname"));
                    map.put("avatar", row.get("avatar"));
                    map.put("orderCount", row.get("order_count"));
                    map.put("totalSpent", row.get("total_spent"));
                    return map;
                })
                .collect(Collectors.toList());
    }

    /**
     * 根据时间单位确定SQL日期格式
     * @param timeUnit 时间单位：day-天、week-周、month-月
     * @return SQL日期格式
     */
    private String determineSqlDateFormat(String timeUnit) {
        switch (timeUnit) {
            case "day":
                return "%Y-%m-%d";
            case "week":
                return "%Y-%u"; // ISO周，%u表示1-53周
            case "month":
                return "%Y-%m";
            default:
                return "%Y-%m-%d";
        }
    }

    /**
     * 计算已完成订单的交易总额
     * @return 交易总额
     */
    private BigDecimal calculateTotalTransactionAmount() {
        String sql = "SELECT SUM(price) FROM orders WHERE deleted = 0 AND status = 4";
        BigDecimal total = jdbcTemplate.queryForObject(sql, BigDecimal.class);
        return total != null ? total : BigDecimal.ZERO;
    }

    /**
     * 统计今日新增用户数
     * @return 今日新增用户数
     */
    private int countTodayNewUsers() {
        // 获取今天的开始和结束时间
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Date startTime = calendar.getTime();
        
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        Date endTime = calendar.getTime();
        
        String sql = "SELECT COUNT(*) FROM user WHERE deleted = 0 AND create_time BETWEEN ? AND ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, startTime, endTime);
    }

    /**
     * 统计今日新增订单数
     * @return 今日新增订单数
     */
    private int countTodayNewOrders() {
        // 获取今天的开始和结束时间
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Date startTime = calendar.getTime();
        
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        Date endTime = calendar.getTime();
        
        String sql = "SELECT COUNT(*) FROM orders WHERE deleted = 0 AND create_time BETWEEN ? AND ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, startTime, endTime);
    }

    /**
     * 统计今日新增商品数
     * @return 今日新增商品数
     */
    private int countTodayNewProducts() {
        // 获取今天的开始和结束时间
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Date startTime = calendar.getTime();
        
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        Date endTime = calendar.getTime();
        
        String sql = "SELECT COUNT(*) FROM product WHERE deleted = 0 AND create_time BETWEEN ? AND ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, startTime, endTime);
    }
} 