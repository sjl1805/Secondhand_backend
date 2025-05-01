package com.example.secondhand_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.secondhand_backend.exception.BusinessException;
import com.example.secondhand_backend.mapper.AddressMapper;
import com.example.secondhand_backend.mapper.OrdersMapper;
import com.example.secondhand_backend.mapper.ProductMapper;
import com.example.secondhand_backend.mapper.UserMapper;
import com.example.secondhand_backend.model.dto.OrderCreateDTO;
import com.example.secondhand_backend.model.dto.PaymentDTO;
import com.example.secondhand_backend.model.entity.Address;
import com.example.secondhand_backend.model.entity.Orders;
import com.example.secondhand_backend.model.entity.Product;
import com.example.secondhand_backend.model.entity.User;
import com.example.secondhand_backend.model.vo.OrderVO;
import com.example.secondhand_backend.model.vo.PaymentResultVO;
import com.example.secondhand_backend.service.OrdersService;
import com.example.secondhand_backend.service.ProductImageService;
import com.example.secondhand_backend.service.ProductService;
import com.example.secondhand_backend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author 28619
 * @description 针对表【orders(订单表)】的数据库操作Service实现
 * @createDate 2025-04-29 13:42:28
 */
@Service
@Slf4j
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders>
        implements OrdersService {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AddressMapper addressMapper;

    @Autowired
    private ProductImageService productImageService;

    @Autowired
    private UserService userService;

    @Override
    @Transactional
    public Long createOrder(OrderCreateDTO orderCreateDTO, Long userId) {
        // 1. 检查商品是否存在和状态是否正确
        Product product = productMapper.selectById(orderCreateDTO.getProductId());
        if (product == null || product.getDeleted() == 1) {
            throw new BusinessException("商品不存在或已删除");
        }
        if (product.getStatus() != 1) {
            throw new BusinessException("商品不可购买");
        }

        // 2. 检查是否是购买自己的商品
        if (product.getUserId().equals(userId)) {
            throw new BusinessException("不能购买自己的商品");
        }

        // 3. 检查地址是否存在
        Address address = addressMapper.selectById(orderCreateDTO.getAddressId());
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException("收货地址不存在或不属于当前用户");
        }

        // 4. 创建订单
        Orders order = new Orders();
        order.setOrderNo(generateOrderNo());
        order.setBuyerId(userId);
        order.setSellerId(product.getUserId());
        order.setProductId(product.getId());
        order.setPrice(orderCreateDTO.getPrice());
        order.setStatus(1); // 待付款
        order.setAddressId(address.getId());
        order.setDeleted(0);

        // 5. 保存订单
        save(order);

        // 6. 更新商品状态为已售
        product.setStatus(2); // 已售
        productMapper.updateById(product);

        return order.getId();
    }

    @Override
    public OrderVO getOrderDetail(Long orderId, Long userId) {
        // 获取订单
        Orders order = getById(orderId);
        if (order == null || order.getDeleted() == 1) {
            throw new BusinessException("订单不存在或已删除");
        }

        // 验证权限，只有买家或卖家可以查看订单
        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
            throw new BusinessException("无权查看该订单");
        }

        // 转换为订单VO并返回
        return convertToOrderVO(order);
    }

    @Override
    @Transactional
    public void updateOrderStatus(Long orderId, Integer status, Long userId) {
        // 获取订单
        Orders order = getById(orderId);
        if (order == null || order.getDeleted() == 1) {
            throw new BusinessException("订单不存在或已删除");
        }

        // 验证状态值是否合法
        if (status < 1 || status > 5) {
            throw new BusinessException("状态值不合法");
        }

        // 验证权限，买家可以更新为取消(5)或确认收货(4)，卖家可以更新为发货(3)
        boolean isAllowed = false;
        if (order.getBuyerId().equals(userId)) {
            if (status == 5 && order.getStatus() == 1) { // 买家取消订单
                isAllowed = true;
            } else if (status == 4 && order.getStatus() == 3) { // 买家确认收货
                isAllowed = true;
            }
        } else if (order.getSellerId().equals(userId)) {
            if (status == 3 && order.getStatus() == 2) { // 卖家发货
                isAllowed = true;
            }
        }

        if (!isAllowed) {
            throw new BusinessException("无权更新订单状态或状态更新不合法");
        }

        // 更新订单状态
        order.setStatus(status);
        updateById(order);

        // 如果订单状态变为已取消，恢复商品状态为在售
        if (status == 5) {
            Product product = productMapper.selectById(order.getProductId());
            if (product != null) {
                product.setStatus(1); // 在售
                productMapper.updateById(product);
            }
        }
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId, Long userId) {
        // 获取订单
        Orders order = getById(orderId);
        if (order == null || order.getDeleted() == 1) {
            throw new BusinessException("订单不存在或已删除");
        }

        // 验证权限，只有买家可以取消订单
        if (!order.getBuyerId().equals(userId)) {
            throw new BusinessException("无权取消该订单");
        }

        // 验证订单状态，只有待付款状态可以取消
        if (order.getStatus() != 1) {
            throw new BusinessException("只有待付款的订单可以取消");
        }

        // 更新订单状态为已取消
        order.setStatus(5);
        updateById(order);

        // 恢复商品状态为在售
        Product product = productMapper.selectById(order.getProductId());
        if (product != null) {
            product.setStatus(1); // 在售
            productMapper.updateById(product);
        }
    }

    @Override
    public IPage<OrderVO> getBuyerOrders(Long buyerId, Integer status, int page, int size) {
        return getOrdersByRole(buyerId, status, page, size, true);
    }

    @Override
    public IPage<OrderVO> getSellerOrders(Long sellerId, Integer status, int page, int size) {
        return getOrdersByRole(sellerId, status, page, size, false);
    }

    @Override
    @Transactional
    public void shipOrder(Long orderId, Long userId) {
        // 获取订单
        Orders order = getById(orderId);
        if (order == null || order.getDeleted() == 1) {
            throw new BusinessException("订单不存在或已删除");
        }

        // 验证权限，只有卖家可以发货
        if (!order.getSellerId().equals(userId)) {
            throw new BusinessException("无权操作该订单");
        }

        // 验证订单状态，只有待发货状态可以发货
        if (order.getStatus() != 2) {
            throw new BusinessException("只有待发货的订单可以发货");
        }

        // 更新订单状态为待收货
        order.setStatus(3);
        updateById(order);
    }

    @Override
    @Transactional
    public void receiveOrder(Long orderId, Long userId) {
        // 获取订单
        Orders order = getById(orderId);
        if (order == null || order.getDeleted() == 1) {
            throw new BusinessException("订单不存在或已删除");
        }

        // 验证权限，只有买家可以确认收货
        if (!order.getBuyerId().equals(userId)) {
            throw new BusinessException("无权操作该订单");
        }

        // 验证订单状态，只有待收货状态可以确认收货
        if (order.getStatus() != 3) {
            throw new BusinessException("只有待收货的订单可以确认收货");
        }

        // 更新订单状态为已完成
        order.setStatus(4);
        updateById(order);
    }

    @Override
    public Orders getByOrderNo(String orderNo) {
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getOrderNo, orderNo)
                .eq(Orders::getDeleted, 0);

        return getOne(queryWrapper);
    }

    @Override
    public IPage<OrderVO> adminGetOrderList(int page, int size, Long buyerId, Long sellerId,
                                            Integer status, String orderNo, Long operatorId) {
        // 验证管理员权限
        if (!isAdmin(operatorId)) {
            throw new BusinessException("无权限执行此操作，需要管理员权限");
        }

        // 构建查询条件
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getDeleted, 0);

        // 添加筛选条件
        if (buyerId != null) {
            queryWrapper.eq(Orders::getBuyerId, buyerId);
        }

        if (sellerId != null) {
            queryWrapper.eq(Orders::getSellerId, sellerId);
        }

        if (status != null) {
            queryWrapper.eq(Orders::getStatus, status);
        }

        if (StringUtils.hasText(orderNo)) {
            queryWrapper.like(Orders::getOrderNo, orderNo);
        }

        // 按创建时间倒序排序
        queryWrapper.orderByDesc(Orders::getCreateTime);

        // 分页查询
        Page<Orders> ordersPage = new Page<>(page, size);
        Page<Orders> result = page(ordersPage, queryWrapper);

        // 转换为OrderVO
        List<OrderVO> orderVOList = result.getRecords().stream()
                .map(this::convertToOrderVO)
                .collect(Collectors.toList());

        // 创建新的分页对象
        Page<OrderVO> orderVOPage = new Page<>();
        orderVOPage.setCurrent(result.getCurrent());
        orderVOPage.setSize(result.getSize());
        orderVOPage.setTotal(result.getTotal());
        orderVOPage.setPages(result.getPages());
        orderVOPage.setRecords(orderVOList);

        return orderVOPage;
    }

    @Override
    public OrderVO adminGetOrderDetail(Long orderId, Long operatorId) {
        // 验证管理员权限
        if (!isAdmin(operatorId)) {
            throw new BusinessException("无权限执行此操作，需要管理员权限");
        }

        // 获取订单
        Orders order = getById(orderId);
        if (order == null || order.getDeleted() == 1) {
            throw new BusinessException("订单不存在或已删除");
        }

        // 转换为订单VO并返回
        return convertToOrderVO(order);
    }

    @Override
    @Transactional
    public void adminUpdateOrderStatus(Long orderId, Integer status, Long operatorId) {
        // 验证管理员权限
        if (!isAdmin(operatorId)) {
            throw new BusinessException("无权限执行此操作，需要管理员权限");
        }

        // 获取订单
        Orders order = getById(orderId);
        if (order == null || order.getDeleted() == 1) {
            throw new BusinessException("订单不存在或已删除");
        }

        // 验证状态值是否合法
        if (status < 1 || status > 5) {
            throw new BusinessException("状态值不合法");
        }

        // 管理员可以将订单设置为任何状态
        order.setStatus(status);
        updateById(order);

        // 如果订单状态变为已取消，恢复商品状态为在售
        if (status == 5) {
            Product product = productMapper.selectById(order.getProductId());
            if (product != null && product.getStatus() == 2) { // 只有商品状态为已售才恢复为在售
                product.setStatus(1); // 在售
                productMapper.updateById(product);
            }
        }
    }

    @Override
    @Transactional
    public void adminDeleteOrder(Long orderId, Long operatorId) {
        // 验证管理员权限
        if (!isAdmin(operatorId)) {
            throw new BusinessException("无权限执行此操作，需要管理员权限");
        }

        // 获取订单
        Orders order = getById(orderId);
        if (order == null || order.getDeleted() == 1) {
            throw new BusinessException("订单不存在或已删除");
        }

        // 逻辑删除订单
        order.setDeleted(1);
        updateById(order);

        // 如果订单状态为待付款或待发货，恢复商品状态为在售
        if (order.getStatus() == 1 || order.getStatus() == 2) {
            Product product = productMapper.selectById(order.getProductId());
            if (product != null && product.getStatus() == 2) { // 只有商品状态为已售才恢复为在售
                product.setStatus(1); // 在售
                productMapper.updateById(product);
            }
        }
    }

    @Override
    @Transactional
    public int adminBatchUpdateOrderStatus(List<Long> orderIds, Integer status, Long operatorId) {
        // 验证管理员权限
        if (!isAdmin(operatorId)) {
            throw new BusinessException("无权限执行此操作，需要管理员权限");
        }

        if (orderIds == null || orderIds.isEmpty()) {
            return 0;
        }

        // 验证状态值是否合法
        if (status < 1 || status > 5) {
            throw new BusinessException("状态值不合法");
        }

        int successCount = 0;

        for (Long orderId : orderIds) {
            try {
                // 获取订单
                Orders order = getById(orderId);
                if (order != null && order.getDeleted() == 0) {
                    // 更新订单状态
                    order.setStatus(status);
                    updateById(order);

                    // 如果订单状态变为已取消，恢复商品状态为在售
                    if (status == 5) {
                        Product product = productMapper.selectById(order.getProductId());
                        if (product != null && product.getStatus() == 2) {
                            product.setStatus(1); // 在售
                            productMapper.updateById(product);
                        }
                    }

                    successCount++;
                }
            } catch (Exception e) {
                // 忽略单个订单的错误，继续处理其他订单
                continue;
            }
        }

        return successCount;
    }

    @Override
    @Transactional
    public int adminBatchDeleteOrder(List<Long> orderIds, Long operatorId) {
        // 验证管理员权限
        if (!isAdmin(operatorId)) {
            throw new BusinessException("无权限执行此操作，需要管理员权限");
        }

        if (orderIds == null || orderIds.isEmpty()) {
            return 0;
        }

        int successCount = 0;

        for (Long orderId : orderIds) {
            try {
                // 获取订单
                Orders order = getById(orderId);
                if (order != null && order.getDeleted() == 0) {
                    // 逻辑删除订单
                    order.setDeleted(1);
                    updateById(order);

                    // 如果订单状态为待付款或待发货，恢复商品状态为在售
                    if (order.getStatus() == 1 || order.getStatus() == 2) {
                        Product product = productMapper.selectById(order.getProductId());
                        if (product != null && product.getStatus() == 2) {
                            product.setStatus(1); // 在售
                            productMapper.updateById(product);
                        }
                    }

                    successCount++;
                }
            } catch (Exception e) {
                // 忽略单个订单的错误，继续处理其他订单
                continue;
            }
        }

        return successCount;
    }

    /**
     * 支付订单
     *
     * @param orderId    订单ID
     * @param paymentDTO 支付信息
     * @param userId     用户ID（买家）
     * @return 支付结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentResultVO payOrder(Long orderId, PaymentDTO paymentDTO, Long userId) {
        // 1. 查询订单
        Orders order = getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        // 2. 校验订单归属
        if (!order.getBuyerId().equals(userId)) {
            throw new BusinessException("无权操作此订单");
        }

        // 3. 校验订单状态
        if (order.getStatus() != 1) {
            throw new BusinessException("订单状态不正确，无法支付");
        }

        // 4. 校验支付金额
        if (paymentDTO.getAmount().compareTo(order.getPrice()) != 0) {
            throw new BusinessException("支付金额不正确");
        }

        // 5. 模拟支付过程 (实际项目中应该调用支付网关API)
        // 假设所有支付都成功
        String transactionNo = UUID.randomUUID().toString().replace("-", "");
        Date now = new Date();

        // 6. 更新订单支付信息
        order.setPaymentMethod(paymentDTO.getPaymentMethod());
        order.setPaymentStatus(2); // 支付成功
        order.setPaymentTime(now);
        order.setTransactionNo(transactionNo);
        order.setStatus(2); // 待发货
        order.setMessage(paymentDTO.getMessage());
        order.setUpdateTime(now);

        // 7. 保存订单
        boolean success = updateById(order);
        if (!success) {
            throw new BusinessException("支付失败，请稍后重试");
        }

        // 8. 构建支付结果
        PaymentResultVO resultVO = PaymentResultVO.builder()
                .orderId(order.getId())
                .orderNo(order.getOrderNo())
                .paymentStatus(order.getPaymentStatus())
                .paymentStatusDesc(getPaymentStatusDesc(order.getPaymentStatus()))
                .amount(order.getPrice())
                .paymentMethod(order.getPaymentMethod())
                .paymentMethodDesc(getPaymentMethodDesc(order.getPaymentMethod()))
                .paymentTime(order.getPaymentTime())
                .transactionNo(order.getTransactionNo())
                .build();

        return resultVO;
    }

    /**
     * 查询支付状态
     *
     * @param orderId 订单ID
     * @param userId  用户ID（买家）
     * @return 支付结果
     */
    @Override
    public PaymentResultVO getPaymentStatus(Long orderId, Long userId) {
        // 1. 查询订单
        Orders order = getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        // 2. 校验订单归属
        if (!order.getBuyerId().equals(userId)) {
            throw new BusinessException("无权查询此订单");
        }

        // 3. 构建支付结果
        PaymentResultVO resultVO = PaymentResultVO.builder()
                .orderId(order.getId())
                .orderNo(order.getOrderNo())
                .paymentStatus(order.getPaymentStatus())
                .paymentStatusDesc(getPaymentStatusDesc(order.getPaymentStatus()))
                .amount(order.getPrice())
                .paymentMethod(order.getPaymentMethod())
                .paymentMethodDesc(getPaymentMethodDesc(order.getPaymentMethod()))
                .paymentTime(order.getPaymentTime())
                .transactionNo(order.getTransactionNo())
                .build();

        return resultVO;
    }

    /**
     * 获取支付状态描述
     *
     * @param status 支付状态：1-待支付 2-支付成功 3-支付失败
     * @return 支付状态描述
     */
    private String getPaymentStatusDesc(Integer status) {
        if (status == null) {
            return "";
        }
        switch (status) {
            case 1:
                return "待支付";
            case 2:
                return "支付成功";
            case 3:
                return "支付失败";
            default:
                return "未知状态";
        }
    }

    /**
     * 获取支付方式描述
     *
     * @param method 支付方式：1-支付宝 2-微信支付 3-银行卡
     * @return 支付方式描述
     */
    private String getPaymentMethodDesc(Integer method) {
        if (method == null) {
            return "";
        }
        switch (method) {
            case 1:
                return "支付宝";
            case 2:
                return "微信支付";
            case 3:
                return "银行卡";
            default:
                return "未知方式";
        }
    }

    /**
     * 检查用户是否为管理员
     *
     * @param userId 用户ID
     * @return 是否为管理员
     */
    private boolean isAdmin(Long userId) {
        return userService.isAdmin(userId);
    }

    /**
     * 获取买家或卖家的订单列表
     *
     * @param userId  用户ID
     * @param status  状态
     * @param page    页码
     * @param size    每页数量
     * @param isBuyer 是否为买家视角
     * @return 订单列表
     */
    private IPage<OrderVO> getOrdersByRole(Long userId, Integer status, int page, int size, boolean isBuyer) {
        // 构建查询条件
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        if (isBuyer) {
            queryWrapper.eq(Orders::getBuyerId, userId);
        } else {
            queryWrapper.eq(Orders::getSellerId, userId);
        }

        // 如果有状态筛选
        if (status != null) {
            queryWrapper.eq(Orders::getStatus, status);
        }

        // 未删除
        queryWrapper.eq(Orders::getDeleted, 0);

        // 按创建时间倒序排序
        queryWrapper.orderByDesc(Orders::getCreateTime);

        // 分页查询
        Page<Orders> ordersPage = new Page<>(page, size);
        Page<Orders> result = page(ordersPage, queryWrapper);

        // 转换为OrderVO
        List<OrderVO> orderVOList = result.getRecords().stream()
                .map(this::convertToOrderVO)
                .collect(Collectors.toList());

        // 创建新的分页对象
        Page<OrderVO> orderVOPage = new Page<>();
        orderVOPage.setCurrent(result.getCurrent());
        orderVOPage.setSize(result.getSize());
        orderVOPage.setTotal(result.getTotal());
        orderVOPage.setPages(result.getPages());
        orderVOPage.setRecords(orderVOList);

        return orderVOPage;
    }

    /**
     * 将Orders转换为OrderVO
     *
     * @param order 订单实体
     * @return 订单VO
     */
    private OrderVO convertToOrderVO(Orders order) {
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(order, orderVO);

        // 获取买家信息
        User buyer = userMapper.selectById(order.getBuyerId());
        if (buyer != null) {
            orderVO.setBuyerNickname(buyer.getNickname());
            orderVO.setBuyerAvatar(buyer.getAvatar());
        }

        // 获取卖家信息
        User seller = userMapper.selectById(order.getSellerId());
        if (seller != null) {
            orderVO.setSellerNickname(seller.getNickname());
            orderVO.setSellerAvatar(seller.getAvatar());
        }

        // 获取商品信息
        Product product = productMapper.selectById(order.getProductId());
        if (product != null) {
            orderVO.setProductTitle(product.getTitle());

            // 获取商品第一张图片
            List<String> imageUrls = productImageService.getProductImages(product.getId());
            if (imageUrls != null && !imageUrls.isEmpty()) {
                orderVO.setProductImage(imageUrls.get(0));
            }
        }

        // 获取收货地址信息
        Address address = addressMapper.selectById(order.getAddressId());
        if (address != null) {
            String fullAddress = address.getProvince() + address.getCity() + address.getDistrict() + address.getDetail();
            orderVO.setAddress(fullAddress);
            orderVO.setReceiverName(address.getReceiverName());
            orderVO.setReceiverPhone(address.getReceiverPhone());
        }

        // 设置状态文本
        orderVO.setStatusText(getStatusText(order.getStatus()));

        return orderVO;
    }

    /**
     * 获取状态文本
     *
     * @param status 状态值
     * @return 状态文本
     */
    private String getStatusText(Integer status) {
        switch (status) {
            case 1:
                return "待付款";
            case 2:
                return "待发货";
            case 3:
                return "待收货";
            case 4:
                return "已完成";
            case 5:
                return "已取消";
            default:
                return "未知状态";
        }
    }

    /**
     * 生成订单号
     *
     * @return 订单号
     */
    private String generateOrderNo() {
        // 使用时间戳和UUID生成唯一订单号
        return System.currentTimeMillis() + UUID.randomUUID().toString().replace("-", "").substring(0, 6);
    }
}




