package com.example.secondhand_backend.controller;

import com.example.secondhand_backend.model.common.Result;
import com.example.secondhand_backend.model.dto.CategoryDTO;
import com.example.secondhand_backend.model.entity.Category;
import com.example.secondhand_backend.service.CategoryService;
import com.example.secondhand_backend.utils.UserUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员分类管理控制器
 */
@RestController
@RequestMapping("/admin/category")
@Tag(name = "管理员分类管理", description = "管理员分类管理接口")
public class AdminCategoryController {

    @Autowired
    private CategoryService categoryService;
    
    
    /**
     * 验证当前用户是否为管理员
     * @return 是否为管理员
     */
    private boolean validateAdminRole() {
        Integer role = UserUtils.getCurrentUserRole();
        return role != null && role == 9;
    }
    
    @PostMapping("/add")
    @Operation(summary = "添加分类", description = "添加新的商品分类")
    public Result<Integer> addCategory(@RequestBody CategoryDTO categoryDTO) {
        // 验证管理员权限
        if (!validateAdminRole()) {
            return Result.error("无权限执行此操作，需要管理员权限");
        }
        
        try {
            // DTO转Entity
            Category category = new Category();
            BeanUtils.copyProperties(categoryDTO, category);
            
            Integer categoryId = categoryService.addCategory(category);
            return Result.success(categoryId);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PutMapping("/update")
    @Operation(summary = "更新分类", description = "更新商品分类信息")
    public Result<Void> updateCategory(@RequestBody CategoryDTO categoryDTO) {
        // 验证管理员权限
        if (!validateAdminRole()) {
            return Result.error("无权限执行此操作，需要管理员权限");
        }
        
        // 检查分类ID是否为空
        if (categoryDTO.getId() == null) {
            return Result.error("分类ID不能为空");
        }
        
        try {
            // DTO转Entity
            Category category = new Category();
            BeanUtils.copyProperties(categoryDTO, category);
            
            categoryService.updateCategory(category);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @DeleteMapping("/delete/{id}")
    @Operation(summary = "删除分类", description = "删除商品分类")
    public Result<Void> deleteCategory(
            @Parameter(description = "分类ID") @PathVariable("id") Integer categoryId) {
        // 验证管理员权限
        if (!validateAdminRole()) {
            return Result.error("无权限执行此操作，需要管理员权限");
        }
        
        try {
            categoryService.deleteCategory(categoryId);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @GetMapping("/check")
    @Operation(summary = "检查分类名称", description = "检查分类名称是否已存在")
    public Result<Boolean> checkNameExists(
            @Parameter(description = "分类名称") @RequestParam String name,
            @Parameter(description = "父分类ID") @RequestParam(defaultValue = "0") Integer parentId,
            @Parameter(description = "排除的分类ID") @RequestParam(required = false) Integer excludeId) {
        // 验证管理员权限
        if (!validateAdminRole()) {
            return Result.error("无权限执行此操作，需要管理员权限");
        }
        
        boolean exists = categoryService.checkNameExists(name, parentId, excludeId);
        return Result.success(exists);
    }
} 