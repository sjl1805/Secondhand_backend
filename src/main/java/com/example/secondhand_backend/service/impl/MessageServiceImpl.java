package com.example.secondhand_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.secondhand_backend.exception.BusinessException;
import com.example.secondhand_backend.mapper.MessageMapper;
import com.example.secondhand_backend.mapper.UserMapper;
import com.example.secondhand_backend.model.dto.MessageDTO;
import com.example.secondhand_backend.model.entity.Message;
import com.example.secondhand_backend.model.entity.User;
import com.example.secondhand_backend.model.vo.MessageVO;
import com.example.secondhand_backend.service.MessageService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author 28619
 * @description 针对表【message(消息表)】的数据库操作Service实现
 * @createDate 2025-04-29 13:42:26
 */
@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message>
        implements MessageService {

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional
    public Long sendMessage(MessageDTO messageDTO, Long senderId) {
        // 验证接收者是否存在
        User receiver = userMapper.selectById(messageDTO.getReceiverId());
        if (receiver == null) {
            throw new BusinessException("接收者不存在");
        }

        // 不能给自己发消息
        if (senderId.equals(messageDTO.getReceiverId())) {
            throw new BusinessException("不能给自己发消息");
        }

        // 创建消息
        Message message = new Message();
        message.setSenderId(senderId);
        message.setReceiverId(messageDTO.getReceiverId());
        message.setContent(messageDTO.getContent());
        message.setIsRead(0); // 未读

        // 保存消息
        save(message);

        return message.getId();
    }

    @Override
    public MessageVO getMessageDetail(Long messageId, Long userId) {
        // 获取消息
        Message message = getById(messageId);
        if (message == null) {
            throw new BusinessException("消息不存在");
        }

        // 验证权限，只有发送者或接收者可以查看消息
        if (!message.getSenderId().equals(userId) && !message.getReceiverId().equals(userId)) {
            throw new BusinessException("无权查看该消息");
        }

        // 如果是接收者查看，则标记为已读
        if (message.getReceiverId().equals(userId) && message.getIsRead() == 0) {
            message.setIsRead(1);
            updateById(message);
        }

        // 转换为VO
        return convertToMessageVO(message, userId);
    }

    @Override
    public IPage<MessageVO> getChatHistory(Long userId, Long targetUserId, int page, int size) {
        // 验证目标用户是否存在
        User targetUser = userMapper.selectById(targetUserId);
        if (targetUser == null) {
            throw new BusinessException("用户不存在");
        }

        // 构建查询条件（双向聊天记录）
        LambdaQueryWrapper<Message> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.and(wrapper ->
                wrapper.eq(Message::getSenderId, userId).eq(Message::getReceiverId, targetUserId)
                        .or()
                        .eq(Message::getSenderId, targetUserId).eq(Message::getReceiverId, userId)
        );

        // 按照时间倒序排序
        queryWrapper.orderByDesc(Message::getCreateTime);

        // 分页查询
        Page<Message> messagePage = new Page<>(page, size);
        Page<Message> result = page(messagePage, queryWrapper);

        // 标记所有未读消息为已读
        markAllAsRead(userId, targetUserId);

        // 转换为VO
        List<MessageVO> messageVOList = result.getRecords().stream()
                .map(message -> convertToMessageVO(message, userId))
                .collect(Collectors.toList());

        // 调整为正序
        Collections.reverse(messageVOList);

        // 创建新的分页对象
        Page<MessageVO> messageVOPage = new Page<>();
        messageVOPage.setCurrent(result.getCurrent());
        messageVOPage.setSize(result.getSize());
        messageVOPage.setTotal(result.getTotal());
        messageVOPage.setPages(result.getPages());
        messageVOPage.setRecords(messageVOList);

        return messageVOPage;
    }

    @Override
    public List<MessageVO> getMessageList(Long userId) {
        // 获取所有与当前用户相关的最新消息
        Map<Long, MessageVO> latestMessageMap = new HashMap<>();

        // 查询发送给当前用户的消息
        LambdaQueryWrapper<Message> receivedWrapper = new LambdaQueryWrapper<>();
        receivedWrapper.eq(Message::getReceiverId, userId)
                .orderByDesc(Message::getCreateTime);
        List<Message> receivedMessages = list(receivedWrapper);

        // 查询当前用户发送的消息
        LambdaQueryWrapper<Message> sentWrapper = new LambdaQueryWrapper<>();
        sentWrapper.eq(Message::getSenderId, userId)
                .orderByDesc(Message::getCreateTime);
        List<Message> sentMessages = list(sentWrapper);

        // 处理接收的消息
        for (Message message : receivedMessages) {
            Long contactId = message.getSenderId();
            if (!latestMessageMap.containsKey(contactId)) {
                MessageVO messageVO = convertToMessageVO(message, userId);
                latestMessageMap.put(contactId, messageVO);
            }
        }

        // 处理发送的消息
        for (Message message : sentMessages) {
            Long contactId = message.getReceiverId();
            if (!latestMessageMap.containsKey(contactId)) {
                MessageVO messageVO = convertToMessageVO(message, userId);
                latestMessageMap.put(contactId, messageVO);
            } else {
                // 比较时间，保留最新的
                MessageVO existingVO = latestMessageMap.get(contactId);
                if (message.getCreateTime().after(existingVO.getCreateTime())) {
                    MessageVO messageVO = convertToMessageVO(message, userId);
                    latestMessageMap.put(contactId, messageVO);
                }
            }
        }

        // 转换为列表并按时间倒序排序
        return latestMessageMap.values().stream()
                .sorted(Comparator.comparing(MessageVO::getCreateTime).reversed())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void markAsRead(Long messageId, Long userId) {
        // 获取消息
        Message message = getById(messageId);
        if (message == null) {
            throw new BusinessException("消息不存在");
        }

        // 验证权限，只有接收者可以标记为已读
        if (!message.getReceiverId().equals(userId)) {
            throw new BusinessException("无权操作该消息");
        }

        // 标记为已读
        message.setIsRead(1);
        updateById(message);
    }

    @Override
    @Transactional
    public int markAllAsRead(Long userId, Long targetUserId) {
        // 构建更新条件
        LambdaUpdateWrapper<Message> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Message::getReceiverId, userId)
                .eq(Message::getSenderId, targetUserId)
                .eq(Message::getIsRead, 0)
                .set(Message::getIsRead, 1);

        // 执行更新
        return baseMapper.update(null, updateWrapper);
    }

    @Override
    public int getUnreadCount(Long userId) {
        // 构建查询条件
        LambdaQueryWrapper<Message> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Message::getReceiverId, userId)
                .eq(Message::getIsRead, 0);

        // 统计数量
        return Math.toIntExact(count(queryWrapper));
    }

    @Override
    @Transactional
    public void deleteMessage(Long messageId, Long userId) {
        // 获取消息
        Message message = getById(messageId);
        if (message == null) {
            throw new BusinessException("消息不存在");
        }

        // 验证权限，只有发送者或接收者可以删除消息
        if (!message.getSenderId().equals(userId) && !message.getReceiverId().equals(userId)) {
            throw new BusinessException("无权删除该消息");
        }

        // 删除消息
        removeById(messageId);
    }

    /**
     * 将Message转换为MessageVO
     *
     * @param message       消息实体
     * @param currentUserId 当前用户ID
     * @return 消息VO
     */
    private MessageVO convertToMessageVO(Message message, Long currentUserId) {
        MessageVO messageVO = new MessageVO();
        BeanUtils.copyProperties(message, messageVO);

        // 设置是否为当前用户发送的消息
        messageVO.setIsSelf(message.getSenderId().equals(currentUserId));

        // 设置是否已读（Integer转Boolean）
        messageVO.setIsRead(message.getIsRead() == 1);

        // 获取发送者信息
        User sender = userMapper.selectById(message.getSenderId());
        if (sender != null) {
            messageVO.setSenderNickname(sender.getNickname());
            messageVO.setSenderAvatar(sender.getAvatar());
        }

        // 获取接收者信息
        User receiver = userMapper.selectById(message.getReceiverId());
        if (receiver != null) {
            messageVO.setReceiverNickname(receiver.getNickname());
            messageVO.setReceiverAvatar(receiver.getAvatar());
        }

        return messageVO;
    }
}




