package com.example.secondhand_backend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.secondhand_backend.model.dto.MessageDTO;
import com.example.secondhand_backend.model.entity.Message;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.secondhand_backend.model.vo.MessageVO;

import java.util.List;

/**
* @author 28619
* @description 针对表【message(消息表)】的数据库操作Service
* @createDate 2025-04-29 13:42:26
*/
public interface MessageService extends IService<Message> {

    /**
     * 发送消息
     * @param messageDTO 消息DTO
     * @param senderId 发送者ID
     * @return 消息ID
     */
    Long sendMessage(MessageDTO messageDTO, Long senderId);
    
    /**
     * 获取消息详情
     * @param messageId 消息ID
     * @param userId 当前用户ID
     * @return 消息VO
     */
    MessageVO getMessageDetail(Long messageId, Long userId);
    
    /**
     * 获取与指定用户的聊天记录
     * @param userId 当前用户ID
     * @param targetUserId 目标用户ID
     * @param page 页码
     * @param size 每页数量
     * @return 消息列表
     */
    IPage<MessageVO> getChatHistory(Long userId, Long targetUserId, int page, int size);
    
    /**
     * 获取消息列表（按联系人分组）
     * @param userId 用户ID
     * @return 最新消息列表（每个联系人一条）
     */
    List<MessageVO> getMessageList(Long userId);
    
    /**
     * 标记消息为已读
     * @param messageId 消息ID
     * @param userId 用户ID
     */
    void markAsRead(Long messageId, Long userId);
    
    /**
     * 标记与指定用户的所有消息为已读
     * @param userId 当前用户ID
     * @param targetUserId 目标用户ID
     * @return 标记数量
     */
    int markAllAsRead(Long userId, Long targetUserId);
    
    /**
     * 获取未读消息数量
     * @param userId 用户ID
     * @return 未读消息数量
     */
    int getUnreadCount(Long userId);
    
    /**
     * 删除消息
     * @param messageId 消息ID
     * @param userId 用户ID
     */
    void deleteMessage(Long messageId, Long userId);
}
