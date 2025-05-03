package com.example.secondhand_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.secondhand_backend.mapper.*;
import com.example.secondhand_backend.model.entity.Orders;
import com.example.secondhand_backend.model.entity.Product;
import com.example.secondhand_backend.model.entity.User;
import com.example.secondhand_backend.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
    private CommentMapper commentMapper;


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
        // 根据时间单位确定日期格式
        String dateFormat = determineSqlDateFormat(timeUnit);
        
        // 使用UserMapper接口方法获取用户注册统计
        List<Map<String, Object>> results = userMapper.getUserRegisterStatistics(startDate, endDate, dateFormat);
        
        // 转换结果格式
        return results.stream()
                .map(row -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("date", row.get("time_period")); 
                    map.put("value", row.get("count"));
                    return map;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getOrderStatistics(Date startDate, Date endDate, String timeUnit) {
        // 根据时间单位确定日期格式
        String dateFormat = determineSqlDateFormat(timeUnit);
        
        // 使用OrdersMapper接口方法获取订单统计
        List<Map<String, Object>> results = ordersMapper.getOrderStatistics(startDate, endDate, dateFormat);
        
        // 转换结果格式
        return results.stream()
                .map(row -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("date", row.get("time_period"));
                    map.put("value", row.get("count"));
                    return map;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getTransactionStatistics(Date startDate, Date endDate, String timeUnit) {
        // 根据时间单位确定日期格式
        String dateFormat = determineSqlDateFormat(timeUnit);
        
        // 使用OrdersMapper接口方法获取交易额统计
        List<Map<String, Object>> results = ordersMapper.getTransactionStatistics(startDate, endDate, dateFormat);
        
        // 转换结果格式
        return results.stream()
                .map(row -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("date", row.get("time_period"));
                    map.put("value", row.get("amount"));
                    return map;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getCategoryProductStatistics() {
        // 使用CategoryMapper接口方法获取分类商品统计
        List<Map<String, Object>> results = categoryMapper.getCategoryProductStatistics();
        
        // 转换结果格式
        return results.stream()
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
        String dateFormat = determineSqlDateFormat(timeUnit);

        // 获取不同来源的用户活跃度数据
        List<Map<String, Object>> buyerActivities = ordersMapper.getUserActivityByBuyerStatistics(startDate, endDate, dateFormat);
        List<Map<String, Object>> sellerActivities = ordersMapper.getUserActivityBySellerStatistics(startDate, endDate, dateFormat);
        List<Map<String, Object>> commentActivities = commentMapper.getUserActivityByCommentStatistics(startDate, endDate, dateFormat);

        // 合并结果并按时间段汇总
        Map<String, Integer> consolidatedMap = new HashMap<>();
        
        // 处理买家活跃数据
        for (Map<String, Object> row : buyerActivities) {
            String timePeriod = (String) row.get("time_period");
            Object activeUsersObj = row.get("active_users");
            int activeUsers = 0;
            if (activeUsersObj instanceof Number) {
                activeUsers = ((Number) activeUsersObj).intValue();
            } else if (activeUsersObj != null) {
                activeUsers = Integer.parseInt(activeUsersObj.toString());
            }
            consolidatedMap.put(timePeriod, consolidatedMap.getOrDefault(timePeriod, 0) + activeUsers);
        }
        
        // 处理卖家活跃数据
        for (Map<String, Object> row : sellerActivities) {
            String timePeriod = (String) row.get("time_period");
            Object activeUsersObj = row.get("active_users");
            int activeUsers = 0;
            if (activeUsersObj instanceof Number) {
                activeUsers = ((Number) activeUsersObj).intValue();
            } else if (activeUsersObj != null) {
                activeUsers = Integer.parseInt(activeUsersObj.toString());
            }
            consolidatedMap.put(timePeriod, consolidatedMap.getOrDefault(timePeriod, 0) + activeUsers);
        }
        
        // 处理评论活跃数据
        for (Map<String, Object> row : commentActivities) {
            String timePeriod = (String) row.get("time_period");
            Object activeUsersObj = row.get("active_users");
            int activeUsers = 0;
            if (activeUsersObj instanceof Number) {
                activeUsers = ((Number) activeUsersObj).intValue();
            } else if (activeUsersObj != null) {
                activeUsers = Integer.parseInt(activeUsersObj.toString());
            }
            consolidatedMap.put(timePeriod, consolidatedMap.getOrDefault(timePeriod, 0) + activeUsers);
        }
        
        // 转换为结果列表
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : consolidatedMap.entrySet()) {
            Map<String, Object> map = new HashMap<>();
            map.put("timePeriod", entry.getKey());
            map.put("activeUsers", entry.getValue());
            result.add(map);
        }
        
        // 按时间段排序
        result.sort(Comparator.comparing(map -> (String) map.get("timePeriod")));
        
        return result;
    }

    @Override
    public Map<String, Integer> getProductStatusStatistics() {
        Map<String, Integer> result = new HashMap<>();

        // 使用ProductMapper接口方法获取商品状态统计
        List<Map<String, Object>> rows = productMapper.getProductStatusStatistics();
        for (Map<String, Object> row : rows) {
            // 安全地转换status
            Object statusObj = row.get("status");
            int status = 0;
            if (statusObj instanceof Number) {
                status = ((Number) statusObj).intValue();
            } else if (statusObj instanceof Boolean) {
                status = ((Boolean) statusObj) ? 1 : 0;
            } else if (statusObj != null) {
                status = Integer.parseInt(statusObj.toString());
            }
            
            // 安全地转换count
            Object countObj = row.get("count");
            int count = 0;
            if (countObj instanceof Number) {
                count = ((Number) countObj).intValue();
            } else if (countObj instanceof Boolean) {
                count = ((Boolean) countObj) ? 1 : 0;
            } else if (countObj != null) {
                count = Integer.parseInt(countObj.toString());
            }

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

        // 使用OrdersMapper接口方法获取订单状态统计
        List<Map<String, Object>> rows = ordersMapper.getOrderStatusStatistics();
        for (Map<String, Object> row : rows) {
            // 安全地转换status
            Object statusObj = row.get("status");
            int status = 0;
            if (statusObj instanceof Number) {
                status = ((Number) statusObj).intValue();
            } else if (statusObj instanceof Boolean) {
                status = ((Boolean) statusObj) ? 1 : 0;
            } else if (statusObj != null) {
                status = Integer.parseInt(statusObj.toString());
            }
            
            // 安全地转换count
            Object countObj = row.get("count");
            int count = 0;
            if (countObj instanceof Number) {
                count = ((Number) countObj).intValue();
            } else if (countObj instanceof Boolean) {
                count = ((Boolean) countObj) ? 1 : 0;
            } else if (countObj != null) {
                count = Integer.parseInt(countObj.toString());
            }

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
        // 使用OrdersMapper接口方法计算平台收入
        BigDecimal income = ordersMapper.getPlatformIncome(startDate, endDate);
        return income != null ? income : BigDecimal.ZERO;
    }

    @Override
    public List<Map<String, Object>> getHotProductsStatistics(int limit) {
        // 使用ProductMapper接口方法获取热门商品统计
        List<Map<String, Object>> results = productMapper.getHotProductsStatistics(limit);
        
        // 转换结果格式
        return results.stream()
                .map(row -> {
                    Map<String, Object> map = new HashMap<>();
                    // 添加排名属性
                    map.put("rank", results.indexOf(row) + 1);
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
        // 使用UserMapper接口方法获取活跃卖家统计
        List<Map<String, Object>> results = userMapper.getActiveSellersStatistics(limit);
        
        // 转换结果格式
        return results.stream()
                .map(row -> {
                    Map<String, Object> map = new HashMap<>();
                    // 添加排名属性
                    map.put("rank", results.indexOf(row) + 1);
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
        // 使用UserMapper接口方法获取活跃买家统计
        List<Map<String, Object>> results = userMapper.getActiveBuyersStatistics(limit);
        
        // 转换结果格式
        return results.stream()
                .map(row -> {
                    Map<String, Object> map = new HashMap<>();
                    // 添加排名属性
                    map.put("rank", results.indexOf(row) + 1);
                    map.put("buyerId", row.get("id"));
                    map.put("nickname", row.get("nickname"));
                    map.put("avatar", row.get("avatar"));
                    map.put("orderCount", row.get("order_count"));
                    map.put("totalSpent", row.get("total_spent"));
                    return map;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Integer> getProductRatingStatistics(Long productId) {
        // 使用CommentMapper接口方法获取商品评分统计
        List<Map<String, Object>> results = commentMapper.getProductRatingStatistics(productId);
        
        // 转换结果格式
        Map<String, Integer> ratingMap = new HashMap<>();
        
        // 初始化所有评分为0
        for (int i = 1; i <= 5; i++) {
            ratingMap.put(String.valueOf(i), 0);
        }
        
        // 填充实际评分数据
        for (Map<String, Object> row : results) {
            // 安全地转换rating
            Object ratingObj = row.get("rating");
            int rating = 0;
            if (ratingObj instanceof Number) {
                rating = ((Number) ratingObj).intValue();
            } else if (ratingObj != null) {
                rating = Integer.parseInt(ratingObj.toString());
            }
            
            // 安全地转换count
            Object countObj = row.get("count");
            int count = 0;
            if (countObj instanceof Number) {
                count = ((Number) countObj).intValue();
            } else if (countObj != null) {
                count = Integer.parseInt(countObj.toString());
            }
            
            ratingMap.put(String.valueOf(rating), count);
        }
        
        return ratingMap;
    }

    /**
     * 根据时间单位确定SQL日期格式
     *
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
     *
     * @return 交易总额
     */
    private BigDecimal calculateTotalTransactionAmount() {
        // 使用OrdersMapper接口方法计算总交易额
        BigDecimal total = ordersMapper.calculateTotalTransactionAmount();
        return total != null ? total : BigDecimal.ZERO;
    }

    /**
     * 统计今日新增用户数
     *
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

        // 使用UserMapper接口方法统计今日新增用户数
        return userMapper.countTodayNewUsers(startTime, endTime);
    }

    /**
     * 统计今日新增订单数
     *
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

        // 使用OrdersMapper接口方法统计今日新增订单数
        return ordersMapper.countTodayNewOrders(startTime, endTime);
    }

    /**
     * 统计今日新增商品数
     *
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

        // 使用ProductMapper接口方法统计今日新增商品数
        return productMapper.countTodayNewProducts(startTime, endTime);
    }
} 