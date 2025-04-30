package com.example.secondhand_backend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.secondhand_backend.model.dto.OrderCreateDTO;
import com.example.secondhand_backend.model.entity.Orders;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.secondhand_backend.model.vo.OrderVO;

import java.util.List;

/**
* @author 28619
* @description 针对表【orders(订单表)】的数据库操作Service
* @createDate 2025-04-29 13:42:28
*/
public interface OrdersService extends IService<Orders> {
    
    /**
     * 创建订单
     * @param orderCreateDTO 创建订单DTO
     * @param userId 用户ID（买家）
     * @return 订单ID
     */
    Long createOrder(OrderCreateDTO orderCreateDTO, Long userId);
    
    /**
     * 获取订单详情
     * @param orderId 订单ID
     * @param userId 用户ID（买家或卖家）
     * @return 订单详情
     */
    OrderVO getOrderDetail(Long orderId, Long userId);
    
    /**
     * 更新订单状态
     * @param orderId 订单ID
     * @param status 状态：1-待付款 2-待发货 3-待收货 4-已完成 5-已取消
     * @param userId 用户ID（买家或卖家）
     */
    void updateOrderStatus(Long orderId, Integer status, Long userId);
    
    /**
     * 取消订单
     * @param orderId 订单ID
     * @param userId 用户ID（买家）
     */
    void cancelOrder(Long orderId, Long userId);
    
    /**
     * 获取买家订单列表
     * @param buyerId 买家ID
     * @param status 状态：null-全部 1-待付款 2-待发货 3-待收货 4-已完成 5-已取消
     * @param page 页码
     * @param size 每页数量
     * @return 订单列表
     */
    IPage<OrderVO> getBuyerOrders(Long buyerId, Integer status, int page, int size);
    
    /**
     * 获取卖家订单列表
     * @param sellerId 卖家ID
     * @param status 状态：null-全部 1-待付款 2-待发货 3-待收货 4-已完成 5-已取消
     * @param page 页码
     * @param size 每页数量
     * @return 订单列表
     */
    IPage<OrderVO> getSellerOrders(Long sellerId, Integer status, int page, int size);
    
    /**
     * 确认发货
     * @param orderId 订单ID
     * @param userId 用户ID（卖家）
     */
    void shipOrder(Long orderId, Long userId);
    
    /**
     * 确认收货
     * @param orderId 订单ID
     * @param userId 用户ID（买家）
     */
    void receiveOrder(Long orderId, Long userId);
    
    /**
     * 根据订单号获取订单
     * @param orderNo 订单号
     * @return 订单
     */
    Orders getByOrderNo(String orderNo);
    
    /**
     * 管理员获取订单列表
     * @param page 页码
     * @param size 每页数量
     * @param buyerId 买家ID，可为空
     * @param sellerId 卖家ID，可为空
     * @param status 状态，可为空
     * @param orderNo 订单号，可为空
     * @param operatorId 操作者ID（管理员）
     * @return 订单列表
     */
    IPage<OrderVO> adminGetOrderList(int page, int size, Long buyerId, Long sellerId, 
                                     Integer status, String orderNo, Long operatorId);
    
    /**
     * 管理员获取订单详情
     * @param orderId 订单ID
     * @param operatorId 操作者ID（管理员）
     * @return 订单详情
     */
    OrderVO adminGetOrderDetail(Long orderId, Long operatorId);
    
    /**
     * 管理员更新订单状态
     * @param orderId 订单ID
     * @param status 状态：1-待付款 2-待发货 3-待收货 4-已完成 5-已取消
     * @param operatorId 操作者ID（管理员）
     */
    void adminUpdateOrderStatus(Long orderId, Integer status, Long operatorId);
    
    /**
     * 管理员删除订单
     * @param orderId 订单ID
     * @param operatorId 操作者ID（管理员）
     */
    void adminDeleteOrder(Long orderId, Long operatorId);
    
    /**
     * 管理员批量更新订单状态
     * @param orderIds 订单ID列表
     * @param status 状态：1-待付款 2-待发货 3-待收货 4-已完成 5-已取消
     * @param operatorId 操作者ID（管理员）
     * @return 成功更新的数量
     */
    int adminBatchUpdateOrderStatus(List<Long> orderIds, Integer status, Long operatorId);
    
    /**
     * 管理员批量删除订单
     * @param orderIds 订单ID列表
     * @param operatorId 操作者ID（管理员）
     * @return 成功删除的数量
     */
    int adminBatchDeleteOrder(List<Long> orderIds, Long operatorId);
}
