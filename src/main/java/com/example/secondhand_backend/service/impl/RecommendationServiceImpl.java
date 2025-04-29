package com.example.secondhand_backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.secondhand_backend.model.entity.Recommendation;
import com.example.secondhand_backend.service.RecommendationService;
import com.example.secondhand_backend.mapper.RecommendationMapper;
import org.springframework.stereotype.Service;

/**
* @author 28619
* @description 针对表【recommendation(推荐表)】的数据库操作Service实现
* @createDate 2025-04-29 13:42:36
*/
@Service
public class RecommendationServiceImpl extends ServiceImpl<RecommendationMapper, Recommendation>
    implements RecommendationService{

}




