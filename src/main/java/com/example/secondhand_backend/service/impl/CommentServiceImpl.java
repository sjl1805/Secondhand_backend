package com.example.secondhand_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.secondhand_backend.exception.BusinessException;
import com.example.secondhand_backend.mapper.CommentMapper;
import com.example.secondhand_backend.mapper.OrdersMapper;
import com.example.secondhand_backend.mapper.ProductMapper;
import com.example.secondhand_backend.mapper.UserMapper;
import com.example.secondhand_backend.model.dto.CommentDTO;
import com.example.secondhand_backend.model.entity.Comment;
import com.example.secondhand_backend.model.entity.Orders;
import com.example.secondhand_backend.model.entity.Product;
import com.example.secondhand_backend.model.entity.User;
import com.example.secondhand_backend.model.vo.CommentVO;
import com.example.secondhand_backend.service.CommentService;
import com.example.secondhand_backend.service.ProductImageService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 28619
 * @description 针对表【comment(评价表)】的数据库操作Service实现
 * @createDate 2025-04-29 13:42:19
 */
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment>
        implements CommentService {

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ProductImageService productImageService;

    @Override
    @Transactional
    public Long addComment(CommentDTO commentDTO, Long userId) {
        // 1. 验证订单是否存在
        Orders order = ordersMapper.selectById(commentDTO.getOrderId());
        if (order == null || order.getDeleted() == 1) {
            throw new BusinessException("订单不存在或已删除");
        }

        // 2. 验证订单是否属于当前用户
        if (!order.getBuyerId().equals(userId)) {
            throw new BusinessException("只有买家才能评价订单");
        }

        // 3. 验证订单状态是否为已完成
        if (order.getStatus() != 4) {
            throw new BusinessException("只有已完成的订单才能评价");
        }

        // 4. 验证订单是否已评价
        if (isOrderCommented(order.getId())) {
            throw new BusinessException("订单已评价，不能重复评价");
        }

        // 5. 验证评分范围
        if (commentDTO.getRating() < 1 || commentDTO.getRating() > 5) {
            throw new BusinessException("评分必须在1-5之间");
        }

        // 6. 创建评价
        Comment comment = new Comment();
        comment.setOrderId(commentDTO.getOrderId());
        comment.setUserId(userId);
        comment.setProductId(commentDTO.getProductId());
        comment.setContent(commentDTO.getContent());
        comment.setRating(commentDTO.getRating());
        comment.setDeleted(0);

        // 7. 保存评价
        save(comment);
        
        // 8. 更新订单评价状态
        order.setIsCommented(1); // 设置订单已评价
        ordersMapper.updateById(order);

        return comment.getId();
    }

    @Override
    public CommentVO getCommentDetail(Long commentId) {
        // 获取评价
        Comment comment = getById(commentId);
        if (comment == null || comment.getDeleted() == 1) {
            throw new BusinessException("评价不存在或已删除");
        }

        // 转换为VO并返回
        return convertToCommentVO(comment);
    }

    @Override
    public IPage<CommentVO> getProductComments(Long productId, int page, int size) {
        // 验证商品是否存在
        Product product = productMapper.selectById(productId);
        if (product == null || product.getDeleted() == 1) {
            throw new BusinessException("商品不存在或已删除");
        }

        // 构建查询条件
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comment::getProductId, productId)
                .eq(Comment::getDeleted, 0)
                .orderByDesc(Comment::getCreateTime);

        // 分页查询
        Page<Comment> commentPage = new Page<>(page, size);
        Page<Comment> result = page(commentPage, queryWrapper);

        // 转换为VO
        List<CommentVO> commentVOList = result.getRecords().stream()
                .map(this::convertToCommentVO)
                .collect(Collectors.toList());

        // 创建新的分页对象
        Page<CommentVO> commentVOPage = new Page<>();
        commentVOPage.setCurrent(result.getCurrent());
        commentVOPage.setSize(result.getSize());
        commentVOPage.setTotal(result.getTotal());
        commentVOPage.setPages(result.getPages());
        commentVOPage.setRecords(commentVOList);

        return commentVOPage;
    }

    @Override
    public IPage<CommentVO> getUserComments(Long userId, int page, int size) {
        // 构建查询条件
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comment::getUserId, userId)
                .eq(Comment::getDeleted, 0)
                .orderByDesc(Comment::getCreateTime);

        // 分页查询
        Page<Comment> commentPage = new Page<>(page, size);
        Page<Comment> result = page(commentPage, queryWrapper);

        // 转换为VO
        List<CommentVO> commentVOList = result.getRecords().stream()
                .map(this::convertToCommentVO)
                .collect(Collectors.toList());

        // 创建新的分页对象
        Page<CommentVO> commentVOPage = new Page<>();
        commentVOPage.setCurrent(result.getCurrent());
        commentVOPage.setSize(result.getSize());
        commentVOPage.setTotal(result.getTotal());
        commentVOPage.setPages(result.getPages());
        commentVOPage.setRecords(commentVOList);

        return commentVOPage;
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        // 获取评价
        Comment comment = getById(commentId);
        if (comment == null || comment.getDeleted() == 1) {
            throw new BusinessException("评价不存在或已删除");
        }

        // 验证评价是否属于当前用户
        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException("无权删除该评价");
        }

        // 逻辑删除评价
        comment.setDeleted(1);
        updateById(comment);
    }

    @Override
    public double getProductRating(Long productId) {
        // 验证商品是否存在
        Product product = productMapper.selectById(productId);
        if (product == null || product.getDeleted() == 1) {
            throw new BusinessException("商品不存在或已删除");
        }

        // 构建查询条件
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comment::getProductId, productId)
                .eq(Comment::getDeleted, 0);

        // 获取所有评价
        List<Comment> comments = list(queryWrapper);

        // 计算平均评分
        if (comments.isEmpty()) {
            return 0; // 没有评价返回0
        }

        double totalRating = 0;
        for (Comment comment : comments) {
            totalRating += comment.getRating();
        }

        return totalRating / comments.size();
    }

    @Override
    public boolean isOrderCommented(Long orderId) {
        // 首先检查订单表中的is_commented字段
        Orders order = ordersMapper.selectById(orderId);
        if (order != null && order.getIsCommented() != null && order.getIsCommented() == 1) {
            return true;
        }
        
        // 如果订单表中状态不是已评价，再查评价表进行二次确认
        // 构建查询条件
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comment::getOrderId, orderId)
                .eq(Comment::getDeleted, 0);

        // 查询数量
        return count(queryWrapper) > 0;
    }

    /**
     * 将Comment转换为CommentVO
     *
     * @param comment 评价实体
     * @return 评价VO
     */
    private CommentVO convertToCommentVO(Comment comment) {
        CommentVO commentVO = new CommentVO();
        BeanUtils.copyProperties(comment, commentVO);

        // 获取用户信息
        User user = userMapper.selectById(comment.getUserId());
        if (user != null) {
            commentVO.setNickname(user.getNickname());
            commentVO.setAvatar(user.getAvatar());
        }

        // 获取商品信息
        Product product = productMapper.selectById(comment.getProductId());
        if (product != null) {
            commentVO.setProductTitle(product.getTitle());

            // 获取商品第一张图片
            List<String> imageUrls = productImageService.getProductImages(product.getId());
            if (imageUrls != null && !imageUrls.isEmpty()) {
                commentVO.setProductImage(imageUrls.get(0));
            }
        }

        return commentVO;
    }
}




