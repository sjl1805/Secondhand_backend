package com.example.secondhand_backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.secondhand_backend.model.entity.Message;
import com.example.secondhand_backend.service.MessageService;
import com.example.secondhand_backend.mapper.MessageMapper;
import org.springframework.stereotype.Service;

/**
* @author 28619
* @description 针对表【message(消息表)】的数据库操作Service实现
* @createDate 2025-04-29 13:42:26
*/
@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message>
    implements MessageService{

}




