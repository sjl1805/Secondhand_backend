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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author 28619
 * @description 针对表【message(消息表)】的数据库操作Service实现
 * @createDate 2025-04-29 13:42:26
 */
@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message>
        implements MessageService {

    private static final String MESSAGE_CACHE_PREFIX = "message:";
    private static final String CHAT_HISTORY_CACHE_PREFIX = "message:chat:";
    private static final String MESSAGE_LIST_CACHE_PREFIX = "message:list:";
    private static final String UNREAD_COUNT_CACHE_PREFIX = "message:unread:";
    private static final long CACHE_EXPIRE_TIME = 1; // 缓存过期时间（小时）
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

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

        // 清除相关缓存
        clearMessageCache(senderId, messageDTO.getReceiverId());

        return message.getId();
    }

    @Override
    public MessageVO getMessageDetail(Long messageId, Long userId) {
        // 从缓存获取
        String cacheKey = MESSAGE_CACHE_PREFIX + messageId + ":" + userId;
        MessageVO messageVO = (MessageVO) redisTemplate.opsForValue().get(cacheKey);

        if (messageVO != null) {
            return messageVO;
        }

        // 缓存未命中，查询数据库
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

            // 清除未读计数缓存
            clearUnreadCountCache(userId);
        }

        // 转换为VO
        messageVO = convertToMessageVO(message, userId);

        // 将结果存入缓存
        redisTemplate.opsForValue().set(cacheKey, messageVO, CACHE_EXPIRE_TIME, TimeUnit.HOURS);

        return messageVO;
    }

    @Override
    public IPage<MessageVO> getChatHistory(Long userId, Long targetUserId, int page, int size) {
        // 从缓存获取
        String cacheKey = CHAT_HISTORY_CACHE_PREFIX + userId + ":" + targetUserId + ":" + page + ":" + size;
        IPage<MessageVO> messageVOPage = (IPage<MessageVO>) redisTemplate.opsForValue().get(cacheKey);

        if (messageVOPage != null) {
            return messageVOPage;
        }

        // 缓存未命中，查询数据库
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
        messageVOPage = new Page<>();
        messageVOPage.setCurrent(result.getCurrent());
        messageVOPage.setSize(result.getSize());
        messageVOPage.setTotal(result.getTotal());
        messageVOPage.setPages(result.getPages());
        messageVOPage.setRecords(messageVOList);

        // 将结果存入缓存
        redisTemplate.opsForValue().set(cacheKey, messageVOPage, CACHE_EXPIRE_TIME, TimeUnit.HOURS);

        return messageVOPage;
    }

    @Override
    public List<MessageVO> getMessageList(Long userId) {
        // 从缓存获取
        String cacheKey = MESSAGE_LIST_CACHE_PREFIX + userId;
        List<MessageVO> messageVOList = (List<MessageVO>) redisTemplate.opsForValue().get(cacheKey);

        if (messageVOList != null) {
            return messageVOList;
        }

        // 缓存未命中，查询数据库
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
        messageVOList = latestMessageMap.values().stream()
                .sorted(Comparator.comparing(MessageVO::getCreateTime).reversed())
                .collect(Collectors.toList());

        // 将结果存入缓存
        redisTemplate.opsForValue().set(cacheKey, messageVOList, CACHE_EXPIRE_TIME, TimeUnit.HOURS);

        return messageVOList;
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

        // 清除相关缓存
        clearMessageCache(message.getSenderId(), userId);

        // 清除消息详情缓存
        String messageCacheKey = MESSAGE_CACHE_PREFIX + messageId + ":" + userId;
        redisTemplate.delete(messageCacheKey);
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
        int updatedCount = baseMapper.update(null, updateWrapper);

        // 如果有消息被更新，清除相关缓存
        if (updatedCount > 0) {
            clearMessageCache(targetUserId, userId);
        }

        return updatedCount;
    }

    @Override
    public int getUnreadCount(Long userId) {
        // 从缓存获取
        String cacheKey = UNREAD_COUNT_CACHE_PREFIX + userId;
        Integer unreadCount = (Integer) redisTemplate.opsForValue().get(cacheKey);

        if (unreadCount != null) {
            return unreadCount;
        }

        // 缓存未命中，查询数据库
        // 构建查询条件
        LambdaQueryWrapper<Message> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Message::getReceiverId, userId)
                .eq(Message::getIsRead, 0);

        // 统计数量
        unreadCount = Math.toIntExact(count(queryWrapper));

        // 将结果存入缓存
        redisTemplate.opsForValue().set(cacheKey, unreadCount, CACHE_EXPIRE_TIME, TimeUnit.HOURS);

        return unreadCount;
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

        // 清除相关缓存
        clearMessageCache(message.getSenderId(), message.getReceiverId());

        // 清除消息详情缓存
        String senderCacheKey = MESSAGE_CACHE_PREFIX + messageId + ":" + message.getSenderId();
        String receiverCacheKey = MESSAGE_CACHE_PREFIX + messageId + ":" + message.getReceiverId();
        redisTemplate.delete(senderCacheKey);
        redisTemplate.delete(receiverCacheKey);
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

    /**
     * 清除消息相关缓存
     *
     * @param senderId   发送者ID
     * @param receiverId 接收者ID
     */
    private void clearMessageCache(Long senderId, Long receiverId) {
        // 清除聊天历史缓存（模糊删除）
        String chatHistoryCacheKey1 = CHAT_HISTORY_CACHE_PREFIX + senderId + ":" + receiverId + "*";
        String chatHistoryCacheKey2 = CHAT_HISTORY_CACHE_PREFIX + receiverId + ":" + senderId + "*";
        redisTemplate.delete(redisTemplate.keys(chatHistoryCacheKey1));
        redisTemplate.delete(redisTemplate.keys(chatHistoryCacheKey2));

        // 清除消息列表缓存
        String senderListCacheKey = MESSAGE_LIST_CACHE_PREFIX + senderId;
        String receiverListCacheKey = MESSAGE_LIST_CACHE_PREFIX + receiverId;
        redisTemplate.delete(senderListCacheKey);
        redisTemplate.delete(receiverListCacheKey);

        // 清除未读计数缓存
        clearUnreadCountCache(receiverId);
    }

    /**
     * 清除未读消息计数缓存
     *
     * @param userId 用户ID
     */
    private void clearUnreadCountCache(Long userId) {
        String unreadCountCacheKey = UNREAD_COUNT_CACHE_PREFIX + userId;
        redisTemplate.delete(unreadCountCacheKey);
    }
}




