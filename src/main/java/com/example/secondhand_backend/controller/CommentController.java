package com.example.secondhand_backend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.secondhand_backend.model.common.Result;
import com.example.secondhand_backend.model.dto.CommentDTO;
import com.example.secondhand_backend.model.vo.CommentVO;
import com.example.secondhand_backend.service.CommentService;
import com.example.secondhand_backend.utils.UserUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


/**
 * 评论控制器
 */
@RestController
@RequestMapping("/comment")
@Tag(name = "评论管理", description = "评论的添加、查询、删除等操作")
public class CommentController {

    @Autowired
    private CommentService commentService;

    /**
     * 添加评论
     *
     * @param commentDTO 评论DTO
     * @param request    HTTP请求
     * @return 评论ID
     */
    @PostMapping("/add")
    @Operation(summary = "添加评论", description = "用户对商品或订单进行评论")
    public Result<Long> addComment(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "评论信息")
            @RequestBody CommentDTO commentDTO) {
        Long userId = UserUtils.getCurrentUserId();
        Long commentId = commentService.addComment(commentDTO, userId);
        return Result.success(commentId);
    }

    /**
     * 获取评论详情
     *
     * @param commentId 评论ID
     * @return 评论详情
     */
    @GetMapping("/detail/{commentId}")
    @Operation(summary = "获取评论详情", description = "根据评论ID获取评论的详细信息")
    public Result<CommentVO> getCommentDetail(
            @Parameter(description = "评论ID")
            @PathVariable Long commentId) {
        CommentVO commentVO = commentService.getCommentDetail(commentId);
        return Result.success(commentVO);
    }

    /**
     * 获取商品评论列表
     *
     * @param productId 商品ID
     * @param page      页码
     * @param size      每页大小
     * @return 评论列表
     */
    @GetMapping("/product/{productId}")
    @Operation(summary = "获取商品评论列表", description = "分页获取指定商品的所有评论")
    public Result<IPage<CommentVO>> getProductComments(
            @Parameter(description = "商品ID")
            @PathVariable Long productId,
            @Parameter(description = "页码")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量")
            @RequestParam(defaultValue = "10") int size) {
        IPage<CommentVO> comments = commentService.getProductComments(productId, page, size);
        return Result.success(comments);
    }

    /**
     * 获取用户评论列表
     *
     * @param userId 用户ID
     * @param page   页码
     * @param size   每页大小
     * @return 评论列表
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "获取用户评论列表", description = "分页获取指定用户发表的所有评论")
    public Result<IPage<CommentVO>> getUserComments(
            @Parameter(description = "用户ID")
            @PathVariable Long userId,
            @Parameter(description = "页码")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量")
            @RequestParam(defaultValue = "10") int size) {
        IPage<CommentVO> comments = commentService.getUserComments(userId, page, size);
        return Result.success(comments);
    }

    /**
     * 删除评论
     *
     * @param commentId 评论ID
     * @param request   HTTP请求
     * @return 成功响应
     */
    @PostMapping("/delete/{commentId}")
    @Operation(summary = "删除评论", description = "用户删除自己发表的评论")
    public Result<Boolean> deleteComment(
            @Parameter(description = "评论ID")
            @PathVariable Long commentId) {
        Long userId = UserUtils.getCurrentUserId();
        commentService.deleteComment(commentId, userId);
        return Result.success(true);
    }

    /**
     * 获取商品评分
     *
     * @param productId 商品ID
     * @return 评分
     */
    @GetMapping("/rating/{productId}")
    @Operation(summary = "获取商品评分", description = "获取商品的平均评分")
    public Result<Double> getProductRating(
            @Parameter(description = "商品ID")
            @PathVariable Long productId) {
        double rating = commentService.getProductRating(productId);
        return Result.success(rating);
    }

    /**
     * 检查订单是否已评论
     *
     * @param orderId 订单ID
     * @return 是否已评论
     */
    @GetMapping("/check/{orderId}")
    @Operation(summary = "检查订单是否已评论", description = "检查指定订单是否已经评论")
    public Result<Boolean> isOrderCommented(
            @Parameter(description = "订单ID")
            @PathVariable Long orderId) {
        boolean isCommented = commentService.isOrderCommented(orderId);
        return Result.success(isCommented);
    }
} 