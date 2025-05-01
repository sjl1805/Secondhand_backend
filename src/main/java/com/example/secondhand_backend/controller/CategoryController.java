package com.example.secondhand_backend.controller;

import com.example.secondhand_backend.model.common.Result;
import com.example.secondhand_backend.model.entity.Category;
import com.example.secondhand_backend.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/category")
@Tag(name = "商品分类", description = "商品分类管理")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/list")
    @Operation(summary = "获取分类列表", description = "获取所有商品分类")
    public Result<List<Category>> getAllCategories() {
        List<Category> categories = categoryService.getAllCategories();
        return Result.success(categories);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取分类详情", description = "获取指定ID的分类详情")
    public Result<Category> getCategoryDetail(
            @Parameter(description = "分类ID") @PathVariable("id") Integer categoryId) {
        Category category = categoryService.getCategoryDetail(categoryId);
        return Result.success(category);
    }

    @GetMapping("/sub/{parentId}")
    @Operation(summary = "获取子分类", description = "获取指定父分类下的子分类列表")
    public Result<List<Category>> getSubCategories(
            @Parameter(description = "父分类ID") @PathVariable("parentId") Integer parentId) {
        List<Category> subCategories = categoryService.getSubCategories(parentId);
        return Result.success(subCategories);
    }

    @GetMapping("/tree")
    @Operation(summary = "获取分类树", description = "获取分类的树形结构")
    public Result<List<Map<String, Object>>> getCategoryTree() {
        List<Map<String, Object>> categoryTree = categoryService.getCategoryTree();
        return Result.success(categoryTree);
    }
} 