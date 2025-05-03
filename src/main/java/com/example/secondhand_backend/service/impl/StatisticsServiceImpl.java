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
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

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

    @Override
    public Map<String, Object> getDashboardOverview() {
        Map<String, Object> result = new HashMap<>();
        
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
        
        return result;
    }

    @Override
    public Map<String, Object> getTodayStatistics() {
        Map<String, Object> result = new HashMap<>();
        
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
        
        result.put("todayNewUsers", todayNewUsers);
        result.put("todayNewProducts", todayNewProducts);
        result.put("todayNewOrders", todayNewOrders);
        
        return result;
    }

    @Override
    public List<Map<String, Object>> getUserRegisterStatistics(Date startDate, Date endDate, String timeGranularity) {
        String dateFormat = getDateFormat(timeGranularity);
        return userMapper.getUserRegisterStatistics(startDate, endDate, dateFormat);
    }

    @Override
    public List<Map<String, Object>> getOrderStatistics(Date startDate, Date endDate, String timeGranularity) {
        String dateFormat = getDateFormat(timeGranularity);
        return ordersMapper.getOrderStatistics(startDate, endDate, dateFormat);
    }

    @Override
    public List<Map<String, Object>> getTransactionStatistics(Date startDate, Date endDate, String timeGranularity) {
        String dateFormat = getDateFormat(timeGranularity);
        return ordersMapper.getTransactionStatistics(startDate, endDate, dateFormat);
    }

    @Override
    public List<Map<String, Object>> getProductStatusStatistics() {
        return productMapper.getProductStatusStatistics();
    }

    @Override
    public List<Map<String, Object>> getOrderStatusStatistics() {
        return ordersMapper.getOrderStatusStatistics();
    }

    @Override
    public List<Map<String, Object>> getActiveSellersStatistics(int limit) {
        return userMapper.getActiveSellersStatistics(limit);
    }

    @Override
    public List<Map<String, Object>> getActiveBuyersStatistics(int limit) {
        return userMapper.getActiveBuyersStatistics(limit);
    }

    @Override
    public List<Map<String, Object>> getHotProductsStatistics(int limit) {
        return productMapper.getHotProductsStatistics(limit);
    }

    @Override
    public List<Map<String, Object>> getUserActivityStatistics(Date startDate, Date endDate, String timeGranularity) {
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
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : mergedData.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("time_period", entry.getKey());
            item.put("active_users", entry.getValue());
            result.add(item);
        }
        
        // 按时间排序
        result.sort(Comparator.comparing(m -> (String) m.get("time_period")));
        
        return result;
    }
    
    /**
     * 根据时间粒度获取对应的日期格式
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
        return productMapper.getProductRatingStatistics(productId);
    }
} 