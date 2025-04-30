package com.example.secondhand_backend.controller;

import com.example.secondhand_backend.model.common.Result;
import com.example.secondhand_backend.model.entity.Address;
import com.example.secondhand_backend.service.AddressService;
import com.example.secondhand_backend.utils.UserUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/address")
@Tag(name = "地址管理", description = "用户收货地址管理")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @PostMapping
    @Operation(summary = "添加地址", description = "添加新的收货地址")
    public Result<Long> addAddress(@RequestBody Address address) {
        // 设置用户ID
        Long userId = UserUtils.getCurrentUserId();
        address.setUserId(userId);
        
        // 添加地址
        Long addressId = addressService.addAddress(address);
        return Result.success(addressId);
    }

    @GetMapping("/list")
    @Operation(summary = "获取地址列表", description = "获取当前用户的所有收货地址")
    public Result<List<Address>> getAddressList() {
        Long userId = UserUtils.getCurrentUserId();
        List<Address> addressList = addressService.getUserAddresses(userId);
        return Result.success(addressList);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取地址详情", description = "获取指定ID的地址详情")
    public Result<Address> getAddressDetail(
            @Parameter(description = "地址ID") @PathVariable("id") Long addressId) {
        Address address = addressService.getAddressDetail(addressId);
        
        // 验证地址归属
        Long userId = UserUtils.getCurrentUserId();
        if (!address.getUserId().equals(userId)) {
            return Result.error("无权访问该地址");
        }
        
        return Result.success(address);
    }

    @PutMapping
    @Operation(summary = "更新地址", description = "更新指定ID的地址信息")
    public Result<Void> updateAddress(@RequestBody Address address) {
        // 验证地址归属
        Long userId = UserUtils.getCurrentUserId();
        if (!address.getUserId().equals(userId)) {
            return Result.error("无权修改该地址");
        }
        
        // 更新地址
        addressService.updateAddress(address);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除地址", description = "删除指定ID的地址")
    public Result<Void> deleteAddress(
            @Parameter(description = "地址ID") @PathVariable("id") Long addressId) {
        Long userId = UserUtils.getCurrentUserId();
        addressService.deleteAddress(addressId, userId);
        return Result.success();
    }

    @PutMapping("/{id}/default")
    @Operation(summary = "设置默认地址", description = "将指定ID的地址设为默认地址")
    public Result<Void> setDefaultAddress(
            @Parameter(description = "地址ID") @PathVariable("id") Long addressId) {
        Long userId = UserUtils.getCurrentUserId();
        addressService.setDefaultAddress(addressId, userId);
        return Result.success();
    }
} 