package com.example.secondhand_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.secondhand_backend.exception.BusinessException;
import com.example.secondhand_backend.model.entity.Address;
import com.example.secondhand_backend.service.AddressService;
import com.example.secondhand_backend.mapper.AddressMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
* @author 28619
* @description 针对表【address(地址表)】的数据库操作Service实现
* @createDate 2025-04-29 13:42:12
*/
@Service
public class AddressServiceImpl extends ServiceImpl<AddressMapper, Address>
    implements AddressService{

    @Override
    public Long addAddress(Address address) {
        // 如果设置为默认地址，则将其他地址设为非默认
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            resetOtherDefaultAddress(address.getUserId());
        }
        save(address);
        return address.getId();
    }

    @Override
    public List<Address> getUserAddresses(Long userId) {
        LambdaQueryWrapper<Address> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Address::getUserId, userId)
                .eq(Address::getDeleted, 0)
                .orderByDesc(Address::getIsDefault)
                .orderByDesc(Address::getUpdateTime);
        return list(queryWrapper);
    }

    @Override
    public Address getAddressDetail(Long addressId) {
        Address address = getById(addressId);
        if (address == null || address.getDeleted() == 1) {
            throw new BusinessException("地址不存在");
        }
        return address;
    }

    @Override
    public void updateAddress(Address address) {
        // 检查地址是否存在
        Address existAddress = getById(address.getId());
        if (existAddress == null || existAddress.getDeleted() == 1) {
            throw new BusinessException("地址不存在");
        }
        
        // 如果设置为默认地址，则将其他地址设为非默认
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            resetOtherDefaultAddress(address.getUserId());
        }
        
        updateById(address);
    }

    @Override
    public void deleteAddress(Long addressId, Long userId) {
        LambdaQueryWrapper<Address> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Address::getId, addressId)
                .eq(Address::getUserId, userId)
                .eq(Address::getDeleted, 0);
        
        Address address = getOne(queryWrapper);
        if (address == null) {
            throw new BusinessException("地址不存在或已删除");
        }
        
        // 逻辑删除
        address.setDeleted(1);
        updateById(address);
    }

    @Override
    @Transactional
    public void setDefaultAddress(Long addressId, Long userId) {
        // 检查地址是否存在
        LambdaQueryWrapper<Address> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Address::getId, addressId)
                .eq(Address::getUserId, userId)
                .eq(Address::getDeleted, 0);
        
        Address address = getOne(queryWrapper);
        if (address == null) {
            throw new BusinessException("地址不存在或已删除");
        }
        
        // 将其他地址设为非默认
        resetOtherDefaultAddress(userId);
        
        // 设置当前地址为默认
        address.setIsDefault(1);
        updateById(address);
    }
    
    /**
     * 将用户的其他地址设为非默认
     * @param userId 用户ID
     */
    private void resetOtherDefaultAddress(Long userId) {
        LambdaUpdateWrapper<Address> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Address::getUserId, userId)
                .eq(Address::getIsDefault, 1)
                .set(Address::getIsDefault, 0);
        update(updateWrapper);
    }
}




