package com.example.secondhand_backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.secondhand_backend.model.entity.Favorite;
import com.example.secondhand_backend.service.FavoriteService;
import com.example.secondhand_backend.mapper.FavoriteMapper;
import org.springframework.stereotype.Service;

/**
* @author 28619
* @description 针对表【favorite(收藏表)】的数据库操作Service实现
* @createDate 2025-04-29 13:42:22
*/
@Service
public class FavoriteServiceImpl extends ServiceImpl<FavoriteMapper, Favorite>
    implements FavoriteService{

}




