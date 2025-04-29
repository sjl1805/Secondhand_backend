package com.example.secondhand_backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.secondhand_backend.model.entity.Address;
import com.example.secondhand_backend.service.AddressService;
import com.example.secondhand_backend.mapper.AddressMapper;
import org.springframework.stereotype.Service;

/**
* @author 28619
* @description 针对表【address(地址表)】的数据库操作Service实现
* @createDate 2025-04-29 13:42:12
*/
@Service
public class AddressServiceImpl extends ServiceImpl<AddressMapper, Address>
    implements AddressService{

}




