package com.example.secondhand_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.secondhand_backend.model.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author 28619
 * @description 针对表【comment(评价表)】的数据库操作Mapper
 * @createDate 2025-04-29 13:42:19
 * @Entity generator.domain.Comment
 */
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    /**
     * 获取用户活跃度统计数据(评论部分)
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param dateFormat 日期格式
     * @return 包含时间点和活跃用户数的列表
     */
    @Select("SELECT DATE_FORMAT(create_time, #{dateFormat}) as time_period, " +
            "COUNT(DISTINCT user_id) as active_users " +
            "FROM comment " +
            "WHERE deleted = 0 " +
            "AND create_time BETWEEN #{startDate} AND #{endDate} " +
            "GROUP BY time_period " +
            "ORDER BY time_period")
    List<Map<String, Object>> getUserActivityByCommentStatistics(
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate,
            @Param("dateFormat") String dateFormat);
    
    /**
     * 获取商品评分统计数据
     * @param productId 商品ID
     * @return 包含评分和数量的列表
     */
    @Select("SELECT rating, COUNT(id) as count " +
            "FROM comment " +
            "WHERE deleted = 0 AND product_id = #{productId} " +
            "GROUP BY rating " +
            "ORDER BY rating DESC")
    List<Map<String, Object>> getProductRatingStatistics(@Param("productId") Long productId);
}




