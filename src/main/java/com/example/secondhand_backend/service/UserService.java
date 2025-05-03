package com.example.secondhand_backend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.secondhand_backend.model.dto.LoginDTO;
import com.example.secondhand_backend.model.dto.RegisterDTO;
import com.example.secondhand_backend.model.dto.UserInfoDTO;
import com.example.secondhand_backend.model.dto.PasswordUpdateDTO;
import com.example.secondhand_backend.model.entity.User;

import java.util.List;

/**
 * @author 28619
 * @description 针对表【user(用户表)】的数据库操作Service
 * @createDate 2025-04-29 13:42:42
 */
public interface UserService extends IService<User> {
    /**
     * 根据用户名查询用户
     */
    User getByUsername(String username);

    /**
     * 用户登录
     */
    User login(LoginDTO loginDTO);

    /**
     * 用户注册
     */
    User register(RegisterDTO registerDTO);

    /**
     * 检查用户名是否已存在
     */
    boolean checkUsernameExists(String username);

    /**
     * 获取用户信息
     */
    User getUserInfo(Long userId);

    /**
     * 获取卖家信息
     */
    User getSellerInfo(Long sellerId);

    /**
     * 更新用户信息
     */
    void updateUserInfo(Long userId, UserInfoDTO userInfoDTO);

    /**
     * 修改密码
     * @param userId 用户ID
     * @param passwordUpdateDTO 修改密码数据
     */
    void updatePassword(Long userId, PasswordUpdateDTO passwordUpdateDTO);

    /**
     * 管理员分页查询用户列表
     *
     * @param page    页码
     * @param size    每页数量
     * @param keyword 搜索关键词（用户名、昵称、手机号、邮箱）
     * @return 用户分页列表
     */
    IPage<User> getUserList(int page, int size, String keyword);

    /**
     * 管理员修改用户状态
     *
     * @param userId      用户ID
     * @param creditScore 信用分
     * @param role        角色（0-普通用户，9-管理员）
     * @param operatorId  操作者ID（必须是管理员）
     */
    void updateUserStatus(Long userId, Integer creditScore, Integer role, Long operatorId);

    /**
     * 管理员删除用户（逻辑删除）
     *
     * @param userId     用户ID
     * @param operatorId 操作者ID（必须是管理员）
     */
    void deleteUser(Long userId, Long operatorId);

    /**
     * 管理员重置用户密码
     *
     * @param userId      用户ID
     * @param newPassword 新密码
     * @param operatorId  操作者ID（必须是管理员）
     */
    void resetUserPassword(Long userId, String newPassword, Long operatorId);

    /**
     * 获取管理员列表
     *
     * @return 管理员用户列表
     */
    List<User> getAdminList();

    /**
     * 检查用户是否为管理员
     *
     * @param userId 用户ID
     * @return 是否为管理员
     */
    boolean isAdmin(Long userId);

    /*
     * 根据关键词搜索用户
     * @param keyword 关键词
     * @return 用户列表
     * */
    List<User> searchUsers(String keyword);
}
