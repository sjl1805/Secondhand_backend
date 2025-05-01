package com.example.secondhand_backend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.secondhand_backend.model.dto.CommentDTO;
import com.example.secondhand_backend.model.entity.Comment;
import com.example.secondhand_backend.model.vo.CommentVO;

/**
 * @author 28619
 * @description 针对表【comment(评价表)】的数据库操作Service
 * @createDate 2025-04-29 13:42:19
 */
public interface CommentService extends IService<Comment> {

    /**
     * 添加评价
     *
     * @param commentDTO 评价DTO
     * @param userId     用户ID
     * @return 评价ID
     */
    Long addComment(CommentDTO commentDTO, Long userId);

    /**
     * 获取评价详情
     *
     * @param commentId 评价ID
     * @return 评价VO
     */
    CommentVO getCommentDetail(Long commentId);

    /**
     * 获取商品评价列表
     *
     * @param productId 商品ID
     * @param page      页码
     * @param size      每页数量
     * @return 评价列表
     */
    IPage<CommentVO> getProductComments(Long productId, int page, int size);

    /**
     * 获取用户评价列表
     *
     * @param userId 用户ID
     * @param page   页码
     * @param size   每页数量
     * @return 评价列表
     */
    IPage<CommentVO> getUserComments(Long userId, int page, int size);

    /**
     * 删除评价
     *
     * @param commentId 评价ID
     * @param userId    用户ID
     */
    void deleteComment(Long commentId, Long userId);

    /**
     * 获取商品评分
     *
     * @param productId 商品ID
     * @return 平均评分
     */
    double getProductRating(Long productId);

    /**
     * 检查订单是否已评价
     *
     * @param orderId 订单ID
     * @return 是否已评价
     */
    boolean isOrderCommented(Long orderId);
}
