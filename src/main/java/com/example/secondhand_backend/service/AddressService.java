package com.example.secondhand_backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.secondhand_backend.model.entity.Address;

import java.util.List;

/**
 * @author 28619
 * @description 针对表【address(地址表)】的数据库操作Service
 * @createDate 2025-04-29 13:42:12
 */
public interface AddressService extends IService<Address> {

    /**
     * 添加地址
     *
     * @param address 地址信息
     * @return 地址ID
     */
    Long addAddress(Address address);

    /**
     * 获取用户地址列表
     *
     * @param userId 用户ID
     * @return 地址列表
     */
    List<Address> getUserAddresses(Long userId);

    /**
     * 获取地址详情
     *
     * @param addressId 地址ID
     * @return 地址信息
     */
    Address getAddressDetail(Long addressId);

    /**
     * 更新地址
     *
     * @param address 地址信息
     */
    void updateAddress(Address address);

    /**
     * 删除地址
     *
     * @param addressId 地址ID
     * @param userId    用户ID
     */
    void deleteAddress(Long addressId, Long userId);

    /**
     * 设置默认地址
     *
     * @param addressId 地址ID
     * @param userId    用户ID
     */
    void setDefaultAddress(Long addressId, Long userId);
}
