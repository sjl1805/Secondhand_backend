package com.example.secondhand_backend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.secondhand_backend.model.common.Result;
import com.example.secondhand_backend.model.dto.CommentDTO;
import com.example.secondhand_backend.model.vo.CommentVO;
import com.example.secondhand_backend.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 评论控制器
 */
@RestController
@RequestMapping("/comment")
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
    public Result<Long> addComment(@RequestBody CommentDTO commentDTO, HttpServletRequest request) {
        Long userId = (Long) request.getSession().getAttribute("userId");
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
    public Result<CommentVO> getCommentDetail(@PathVariable Long commentId) {
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
    public Result<IPage<CommentVO>> getProductComments(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "1") int page,
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
    public Result<IPage<CommentVO>> getUserComments(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
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
    public Result<Boolean> deleteComment(@PathVariable Long commentId, HttpServletRequest request) {
        Long userId = (Long) request.getSession().getAttribute("userId");
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
    public Result<Double> getProductRating(@PathVariable Long productId) {
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
    public Result<Boolean> isOrderCommented(@PathVariable Long orderId) {
        boolean isCommented = commentService.isOrderCommented(orderId);
        return Result.success(isCommented);
    }
} 