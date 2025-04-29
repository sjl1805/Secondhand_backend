package com.example.secondhand_backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.secondhand_backend.model.entity.Orders;
import com.example.secondhand_backend.service.OrdersService;
import com.example.secondhand_backend.mapper.OrdersMapper;
import org.springframework.stereotype.Service;

/**
* @author 28619
* @description 针对表【orders(订单表)】的数据库操作Service实现
* @createDate 2025-04-29 13:42:28
*/
@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders>
    implements OrdersService{

}




