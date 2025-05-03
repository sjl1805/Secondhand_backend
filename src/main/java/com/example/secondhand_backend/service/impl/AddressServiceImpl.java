package com.example.secondhand_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.secondhand_backend.exception.BusinessException;
import com.example.secondhand_backend.mapper.AddressMapper;
import com.example.secondhand_backend.model.entity.Address;
import com.example.secondhand_backend.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author 28619
 * @description 针对表【address(地址表)】的数据库操作Service实现
 * @createDate 2025-04-29 13:42:12
 */
@Service
public class AddressServiceImpl extends ServiceImpl<AddressMapper, Address>
        implements AddressService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    private static final String ADDRESS_CACHE_PREFIX = "address:";
    private static final String USER_ADDRESSES_CACHE_PREFIX = "user:addresses:";
    private static final long CACHE_EXPIRE_TIME = 24; // 缓存过期时间（小时）

    @Override
    public Long addAddress(Address address) {
        // 如果设置为默认地址，则将其他地址设为非默认
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            resetOtherDefaultAddress(address.getUserId());
        }
        save(address);
        
        // 清除该用户地址列表缓存
        clearUserAddressesCache(address.getUserId());
        
        return address.getId();
    }

    @Override
    public List<Address> getUserAddresses(Long userId) {
        // 先从缓存获取
        String cacheKey = USER_ADDRESSES_CACHE_PREFIX + userId;
        List<Address> addresses = (List<Address>) redisTemplate.opsForValue().get(cacheKey);
        
        if (addresses != null) {
            return addresses;
        }
        
        // 缓存未命中，查询数据库
        LambdaQueryWrapper<Address> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Address::getUserId, userId)
                .eq(Address::getDeleted, 0)
                .orderByDesc(Address::getIsDefault)
                .orderByDesc(Address::getUpdateTime);
        addresses = list(queryWrapper);
        
        // 将结果存入缓存
        redisTemplate.opsForValue().set(cacheKey, addresses, CACHE_EXPIRE_TIME, TimeUnit.HOURS);
        
        return addresses;
    }

    @Override
    public Address getAddressDetail(Long addressId) {
        // 先从缓存获取
        String cacheKey = ADDRESS_CACHE_PREFIX + addressId;
        Address address = (Address) redisTemplate.opsForValue().get(cacheKey);
        
        if (address != null) {
            return address;
        }
        
        // 缓存未命中，查询数据库
        address = getById(addressId);
        if (address == null || address.getDeleted() == 1) {
            throw new BusinessException("地址不存在");
        }
        
        // 将结果存入缓存
        redisTemplate.opsForValue().set(cacheKey, address, CACHE_EXPIRE_TIME, TimeUnit.HOURS);
        
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
        
        // 更新缓存
        String cacheKey = ADDRESS_CACHE_PREFIX + address.getId();
        redisTemplate.opsForValue().set(cacheKey, address, CACHE_EXPIRE_TIME, TimeUnit.HOURS);
        
        // 清除该用户地址列表缓存
        clearUserAddressesCache(address.getUserId());
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
        
        // 删除缓存
        String cacheKey = ADDRESS_CACHE_PREFIX + addressId;
        redisTemplate.delete(cacheKey);
        
        // 清除该用户地址列表缓存
        clearUserAddressesCache(userId);
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
        
        // 更新缓存
        String cacheKey = ADDRESS_CACHE_PREFIX + addressId;
        redisTemplate.opsForValue().set(cacheKey, address, CACHE_EXPIRE_TIME, TimeUnit.HOURS);
        
        // 清除该用户地址列表缓存
        clearUserAddressesCache(userId);
    }

    /**
     * 将用户的其他地址设为非默认
     *
     * @param userId 用户ID
     */
    private void resetOtherDefaultAddress(Long userId) {
        LambdaUpdateWrapper<Address> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Address::getUserId, userId)
                .eq(Address::getIsDefault, 1)
                .set(Address::getIsDefault, 0);
        update(updateWrapper);
    }
    
    /**
     * 清除用户地址列表缓存
     *
     * @param userId 用户ID
     */
    private void clearUserAddressesCache(Long userId) {
        String cacheKey = USER_ADDRESSES_CACHE_PREFIX + userId;
        redisTemplate.delete(cacheKey);
    }
}




