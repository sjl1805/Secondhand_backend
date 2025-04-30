package com.example.secondhand_backend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.secondhand_backend.model.common.Result;
import com.example.secondhand_backend.model.dto.MessageDTO;
import com.example.secondhand_backend.model.vo.MessageVO;
import com.example.secondhand_backend.service.MessageService;
import com.example.secondhand_backend.utils.UserUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/message")
@Tag(name = "消息管理", description = "消息发送、查看等操作")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @PostMapping
    @Operation(summary = "发送消息", description = "发送新消息")
    public Result<Long> sendMessage(@RequestBody MessageDTO messageDTO) {
        Long userId = UserUtils.getCurrentUserId();
        Long messageId = messageService.sendMessage(messageDTO, userId);
        return Result.success(messageId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取消息详情", description = "获取指定ID的消息详情")
    public Result<MessageVO> getMessageDetail(
            @Parameter(description = "消息ID") @PathVariable("id") Long messageId) {
        Long userId = UserUtils.getCurrentUserId();
        MessageVO messageVO = messageService.getMessageDetail(messageId, userId);
        return Result.success(messageVO);
    }

    @GetMapping("/chat/{targetUserId}")
    @Operation(summary = "获取聊天记录", description = "获取与指定用户的聊天记录")
    public Result<IPage<MessageVO>> getChatHistory(
            @Parameter(description = "目标用户ID") @PathVariable("targetUserId") Long targetUserId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") int size) {
        Long userId = UserUtils.getCurrentUserId();
        IPage<MessageVO> chatHistory = messageService.getChatHistory(userId, targetUserId, page, size);
        return Result.success(chatHistory);
    }

    @GetMapping("/list")
    @Operation(summary = "获取消息列表", description = "获取消息列表（按联系人分组）")
    public Result<List<MessageVO>> getMessageList() {
        Long userId = UserUtils.getCurrentUserId();
        List<MessageVO> messageList = messageService.getMessageList(userId);
        return Result.success(messageList);
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "标记消息为已读", description = "标记指定ID的消息为已读")
    public Result<Void> markAsRead(
            @Parameter(description = "消息ID") @PathVariable("id") Long messageId) {
        Long userId = UserUtils.getCurrentUserId();
        messageService.markAsRead(messageId, userId);
        return Result.success();
    }

    @PutMapping("/read/all/{targetUserId}")
    @Operation(summary = "标记所有消息为已读", description = "标记与指定用户的所有消息为已读")
    public Result<Integer> markAllAsRead(
            @Parameter(description = "目标用户ID") @PathVariable("targetUserId") Long targetUserId) {
        Long userId = UserUtils.getCurrentUserId();
        int count = messageService.markAllAsRead(userId, targetUserId);
        return Result.success(count);
    }

    @GetMapping("/unread/count")
    @Operation(summary = "获取未读消息数量", description = "获取当前用户的未读消息数量")
    public Result<Integer> getUnreadCount() {
        Long userId = UserUtils.getCurrentUserId();
        int count = messageService.getUnreadCount(userId);
        return Result.success(count);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除消息", description = "删除指定ID的消息")
    public Result<Void> deleteMessage(
            @Parameter(description = "消息ID") @PathVariable("id") Long messageId) {
        Long userId = UserUtils.getCurrentUserId();
        messageService.deleteMessage(messageId, userId);
        return Result.success();
    }
} 