package com.example.secondhand_backend.controller;

import com.example.secondhand_backend.model.common.Result;
import com.example.secondhand_backend.service.StatisticsService;
import com.example.secondhand_backend.utils.UserUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 管理员数据统计控制器
 */
@RestController
@RequestMapping("/admin/statistics")
@Tag(name = "管理员数据统计", description = "管理员数据统计接口")
public class AdminStatisticsController {

    @Autowired
    private StatisticsService statisticsService;
    
    @GetMapping("/dashboard/overview")
    @Operation(summary = "获取仪表盘概览数据", description = "获取总用户数、总商品数、总订单数、完成交易额等数据")
    public Result<Map<String, Object>> getDashboardOverview() {
        // 验证是否为管理员
        Integer userRole = UserUtils.getCurrentUserRole();
        if (userRole == null || userRole != 9) {
            return Result.error(403, "无权限访问");
        }
        
        Map<String, Object> overview = statisticsService.getDashboardOverview();
        return Result.success(overview);
    }
    
    @GetMapping("/today")
    @Operation(summary = "获取今日数据统计", description = "获取今日新增用户数、今日新增商品数、今日新增订单数")
    public Result<Map<String, Object>> getTodayStatistics() {
        // 验证是否为管理员
        Integer userRole = UserUtils.getCurrentUserRole();
        if (userRole == null || userRole != 9) {
            return Result.error(403, "无权限访问");
        }
        
        Map<String, Object> todayStats = statisticsService.getTodayStatistics();
        return Result.success(todayStats);
    }
    
    @GetMapping("/user/register")
    @Operation(summary = "获取用户注册统计数据", description = "按时间粒度统计用户注册数量")
    public Result<List<Map<String, Object>>> getUserRegisterStatistics(
            @Parameter(description = "开始日期，格式：yyyy-MM-dd")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @Parameter(description = "结束日期，格式：yyyy-MM-dd")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            @Parameter(description = "时间粒度：day-日, week-周, month-月")
            @RequestParam(defaultValue = "day") String timeGranularity) {
        // 验证是否为管理员
        Integer userRole = UserUtils.getCurrentUserRole();
        if (userRole == null || userRole != 9) {
            return Result.error(403, "无权限访问");
        }
        
        List<Map<String, Object>> statistics = statisticsService.getUserRegisterStatistics(startDate, endDate, timeGranularity);
        return Result.success(statistics);
    }
    
    @GetMapping("/order/count")
    @Operation(summary = "获取订单统计数据", description = "按时间粒度统计订单数量")
    public Result<List<Map<String, Object>>> getOrderStatistics(
            @Parameter(description = "开始日期，格式：yyyy-MM-dd")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @Parameter(description = "结束日期，格式：yyyy-MM-dd")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            @Parameter(description = "时间粒度：day-日, week-周, month-月")
            @RequestParam(defaultValue = "day") String timeGranularity) {
        // 验证是否为管理员
        Integer userRole = UserUtils.getCurrentUserRole();
        if (userRole == null || userRole != 9) {
            return Result.error(403, "无权限访问");
        }
        
        List<Map<String, Object>> statistics = statisticsService.getOrderStatistics(startDate, endDate, timeGranularity);
        return Result.success(statistics);
    }
    
    @GetMapping("/transaction")
    @Operation(summary = "获取交易额统计数据", description = "按时间粒度统计交易金额")
    public Result<List<Map<String, Object>>> getTransactionStatistics(
            @Parameter(description = "开始日期，格式：yyyy-MM-dd")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @Parameter(description = "结束日期，格式：yyyy-MM-dd")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            @Parameter(description = "时间粒度：day-日, week-周, month-月")
            @RequestParam(defaultValue = "day") String timeGranularity) {
        // 验证是否为管理员
        Integer userRole = UserUtils.getCurrentUserRole();
        if (userRole == null || userRole != 9) {
            return Result.error(403, "无权限访问");
        }
        
        List<Map<String, Object>> statistics = statisticsService.getTransactionStatistics(startDate, endDate, timeGranularity);
        return Result.success(statistics);
    }
    
    @GetMapping("/product/status")
    @Operation(summary = "获取商品状态统计", description = "统计各状态商品数量")
    public Result<List<Map<String, Object>>> getProductStatusStatistics() {
        // 验证是否为管理员
        Integer userRole = UserUtils.getCurrentUserRole();
        if (userRole == null || userRole != 9) {
            return Result.error(403, "无权限访问");
        }
        
        List<Map<String, Object>> statistics = statisticsService.getProductStatusStatistics();
        return Result.success(statistics);
    }
    
    @GetMapping("/order/status")
    @Operation(summary = "获取订单状态统计", description = "统计各状态订单数量")
    public Result<List<Map<String, Object>>> getOrderStatusStatistics() {
        // 验证是否为管理员
        Integer userRole = UserUtils.getCurrentUserRole();
        if (userRole == null || userRole != 9) {
            return Result.error(403, "无权限访问");
        }
        
        List<Map<String, Object>> statistics = statisticsService.getOrderStatusStatistics();
        return Result.success(statistics);
    }
    
    @GetMapping("/seller/active")
    @Operation(summary = "获取活跃卖家统计数据", description = "获取平台活跃卖家数据")
    public Result<List<Map<String, Object>>> getActiveSellersStatistics(
            @Parameter(description = "返回数量限制")
            @RequestParam(defaultValue = "10") int limit) {
        // 验证是否为管理员
        Integer userRole = UserUtils.getCurrentUserRole();
        if (userRole == null || userRole != 9) {
            return Result.error(403, "无权限访问");
        }
        
        List<Map<String, Object>> statistics = statisticsService.getActiveSellersStatistics(limit);
        return Result.success(statistics);
    }
    
    @GetMapping("/buyer/active")
    @Operation(summary = "获取活跃买家统计数据", description = "获取平台活跃买家数据")
    public Result<List<Map<String, Object>>> getActiveBuyersStatistics(
            @Parameter(description = "返回数量限制")
            @RequestParam(defaultValue = "10") int limit) {
        // 验证是否为管理员
        Integer userRole = UserUtils.getCurrentUserRole();
        if (userRole == null || userRole != 9) {
            return Result.error(403, "无权限访问");
        }
        
        List<Map<String, Object>> statistics = statisticsService.getActiveBuyersStatistics(limit);
        return Result.success(statistics);
    }
    
    @GetMapping("/product/hot")
    @Operation(summary = "获取热门商品统计数据", description = "获取平台热门商品数据")
    public Result<List<Map<String, Object>>> getHotProductsStatistics(
            @Parameter(description = "返回数量限制")
            @RequestParam(defaultValue = "10") int limit) {
        // 验证是否为管理员
        Integer userRole = UserUtils.getCurrentUserRole();
        if (userRole == null || userRole != 9) {
            return Result.error(403, "无权限访问");
        }
        
        List<Map<String, Object>> statistics = statisticsService.getHotProductsStatistics(limit);
        return Result.success(statistics);
    }
    
    @GetMapping("/user/activity")
    @Operation(summary = "获取用户活跃度统计数据", description = "按时间粒度统计活跃用户数量")
    public Result<List<Map<String, Object>>> getUserActivityStatistics(
            @Parameter(description = "开始日期，格式：yyyy-MM-dd")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @Parameter(description = "结束日期，格式：yyyy-MM-dd")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            @Parameter(description = "时间粒度：day-日, week-周, month-月")
            @RequestParam(defaultValue = "day") String timeGranularity) {
        // 验证是否为管理员
        Integer userRole = UserUtils.getCurrentUserRole();
        if (userRole == null || userRole != 9) {
            return Result.error(403, "无权限访问");
        }
        
        List<Map<String, Object>> statistics = statisticsService.getUserActivityStatistics(startDate, endDate, timeGranularity);
        return Result.success(statistics);
    }
} 