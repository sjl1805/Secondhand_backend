package com.example.secondhand_backend.controller;

import com.example.secondhand_backend.model.common.Result;
import com.example.secondhand_backend.service.StatisticsService;
import com.example.secondhand_backend.utils.UserUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
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

    /**
     * 验证当前用户是否为管理员
     *
     * @return 是否为管理员
     */
    private boolean validateAdminRole() {
        Integer role = UserUtils.getCurrentUserRole();
        return role != null && role == 9;
    }

    @GetMapping("/basic")
    @Operation(summary = "获取基本统计数据", description = "获取平台基本统计数据")
    public Result<Map<String, Object>> getBasicStatistics() {
        // 验证管理员权限
        if (!validateAdminRole()) {
            return Result.error("无权限执行此操作，需要管理员权限");
        }

        Map<String, Object> statistics = statisticsService.getBasicStatistics();
        return Result.success(statistics);
    }

    @GetMapping("/user/register")
    @Operation(summary = "获取用户注册统计数据", description = "获取用户注册统计数据")
    public Result<List<Map<String, Object>>> getUserRegisterStatistics(
            @Parameter(description = "开始日期") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @Parameter(description = "结束日期") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            @Parameter(description = "时间单位：day-天、week-周、month-月") @RequestParam(defaultValue = "day") String timeUnit) {
        // 验证管理员权限
        if (!validateAdminRole()) {
            return Result.error("无权限执行此操作，需要管理员权限");
        }

        List<Map<String, Object>> statistics = statisticsService.getUserRegisterStatistics(startDate, endDate, timeUnit);
        return Result.success(statistics);
    }

    @GetMapping("/order")
    @Operation(summary = "获取订单统计数据", description = "获取订单统计数据")
    public Result<List<Map<String, Object>>> getOrderStatistics(
            @Parameter(description = "开始日期") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @Parameter(description = "结束日期") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            @Parameter(description = "时间单位：day-天、week-周、month-月") @RequestParam(defaultValue = "day") String timeUnit) {
        // 验证管理员权限
        if (!validateAdminRole()) {
            return Result.error("无权限执行此操作，需要管理员权限");
        }

        List<Map<String, Object>> statistics = statisticsService.getOrderStatistics(startDate, endDate, timeUnit);
        return Result.success(statistics);
    }

    @GetMapping("/transaction")
    @Operation(summary = "获取交易额统计数据", description = "获取交易额统计数据")
    public Result<List<Map<String, Object>>> getTransactionStatistics(
            @Parameter(description = "开始日期") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @Parameter(description = "结束日期") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            @Parameter(description = "时间单位：day-天、week-周、month-月") @RequestParam(defaultValue = "day") String timeUnit) {
        // 验证管理员权限
        if (!validateAdminRole()) {
            return Result.error("无权限执行此操作，需要管理员权限");
        }

        List<Map<String, Object>> statistics = statisticsService.getTransactionStatistics(startDate, endDate, timeUnit);
        return Result.success(statistics);
    }

    @GetMapping("/category/product")
    @Operation(summary = "获取分类商品统计数据", description = "获取分类商品统计数据")
    public Result<List<Map<String, Object>>> getCategoryProductStatistics() {
        // 验证管理员权限
        if (!validateAdminRole()) {
            return Result.error("无权限执行此操作，需要管理员权限");
        }

        List<Map<String, Object>> statistics = statisticsService.getCategoryProductStatistics();
        return Result.success(statistics);
    }

    @GetMapping("/user/activity")
    @Operation(summary = "获取用户活跃度统计数据", description = "获取用户活跃度统计数据")
    public Result<List<Map<String, Object>>> getUserActivityStatistics(
            @Parameter(description = "开始日期") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @Parameter(description = "结束日期") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            @Parameter(description = "时间单位：day-天、week-周、month-月") @RequestParam(defaultValue = "day") String timeUnit) {
        // 验证管理员权限
        if (!validateAdminRole()) {
            return Result.error("无权限执行此操作，需要管理员权限");
        }

        List<Map<String, Object>> statistics = statisticsService.getUserActivityStatistics(startDate, endDate, timeUnit);
        return Result.success(statistics);
    }

    @GetMapping("/product/status")
    @Operation(summary = "获取商品状态统计数据", description = "获取商品状态统计数据")
    public Result<Map<String, Integer>> getProductStatusStatistics() {
        // 验证管理员权限
        if (!validateAdminRole()) {
            return Result.error("无权限执行此操作，需要管理员权限");
        }

        Map<String, Integer> statistics = statisticsService.getProductStatusStatistics();
        return Result.success(statistics);
    }

    @GetMapping("/order/status")
    @Operation(summary = "获取订单状态统计数据", description = "获取订单状态统计数据")
    public Result<Map<String, Integer>> getOrderStatusStatistics() {
        // 验证管理员权限
        if (!validateAdminRole()) {
            return Result.error("无权限执行此操作，需要管理员权限");
        }

        Map<String, Integer> statistics = statisticsService.getOrderStatusStatistics();
        return Result.success(statistics);
    }

    @GetMapping("/platform/income")
    @Operation(summary = "获取平台收入统计数据", description = "获取平台收入统计数据")
    public Result<BigDecimal> getPlatformIncome(
            @Parameter(description = "开始日期") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @Parameter(description = "结束日期") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        // 验证管理员权限
        if (!validateAdminRole()) {
            return Result.error("无权限执行此操作，需要管理员权限");
        }

        BigDecimal income = statisticsService.getPlatformIncome(startDate, endDate);
        return Result.success(income);
    }

    @GetMapping("/hot/products")
    @Operation(summary = "获取热门商品统计数据", description = "获取热门商品统计数据")
    public Result<List<Map<String, Object>>> getHotProductsStatistics(
            @Parameter(description = "返回数量限制") @RequestParam(defaultValue = "10") int limit) {
        // 验证管理员权限
        if (!validateAdminRole()) {
            return Result.error("无权限执行此操作，需要管理员权限");
        }

        List<Map<String, Object>> statistics = statisticsService.getHotProductsStatistics(limit);
        return Result.success(statistics);
    }

    @GetMapping("/active/sellers")
    @Operation(summary = "获取活跃卖家统计数据", description = "获取活跃卖家统计数据")
    public Result<List<Map<String, Object>>> getActiveSellersStatistics(
            @Parameter(description = "返回数量限制") @RequestParam(defaultValue = "10") int limit) {
        // 验证管理员权限
        if (!validateAdminRole()) {
            return Result.error("无权限执行此操作，需要管理员权限");
        }

        List<Map<String, Object>> statistics = statisticsService.getActiveSellersStatistics(limit);
        return Result.success(statistics);
    }

    @GetMapping("/active/buyers")
    @Operation(summary = "获取活跃买家统计数据", description = "获取活跃买家统计数据")
    public Result<List<Map<String, Object>>> getActiveBuyersStatistics(
            @Parameter(description = "返回数量限制") @RequestParam(defaultValue = "10") int limit) {
        // 验证管理员权限
        if (!validateAdminRole()) {
            return Result.error("无权限执行此操作，需要管理员权限");
        }

        List<Map<String, Object>> statistics = statisticsService.getActiveBuyersStatistics(limit);
        return Result.success(statistics);
    }

    @GetMapping("/product/rating")
    @Operation(summary = "获取商品评分统计数据", description = "获取商品评分统计数据")
    public Result<Map<String, Integer>> getProductRatingStatistics(
            @Parameter(description = "商品ID") @RequestParam Long productId) {
        // 验证管理员权限
        if (!validateAdminRole()) {
            return Result.error("无权限执行此操作，需要管理员权限");
        }

        Map<String, Integer> statistics = statisticsService.getProductRatingStatistics(productId);
        return Result.success(statistics);
    }
} 