package com.example.secondhand_backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.secondhand_backend.entity.domain.Comment;
import com.example.secondhand_backend.service.CommentService;
import com.example.secondhand_backend.mapper.CommentMapper;
import org.springframework.stereotype.Service;

/**
* @author 28619
* @description 针对表【comment(评价表)】的数据库操作Service实现
* @createDate 2025-04-29 13:42:19
*/
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment>
    implements CommentService{

}




