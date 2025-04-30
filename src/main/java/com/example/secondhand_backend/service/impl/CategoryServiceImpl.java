package com.example.secondhand_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.secondhand_backend.exception.BusinessException;
import com.example.secondhand_backend.model.entity.Category;
import com.example.secondhand_backend.model.entity.Product;
import com.example.secondhand_backend.model.vo.CategoryVO;
import com.example.secondhand_backend.service.CategoryService;
import com.example.secondhand_backend.mapper.CategoryMapper;
import com.example.secondhand_backend.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
* @author 28619
* @description 针对表【category(商品分类表)】的数据库操作Service实现
* @createDate 2025-04-29 13:42:16
*/
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category>
    implements CategoryService{

    @Autowired
    private ProductService productService;

    @Override
    public List<Category> getAllCategories() {
        // 按照排序字段排序
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(Category::getParentId)
                    .orderByAsc(Category::getSort);
        return list(queryWrapper);
    }

    @Override
    public Category getCategoryDetail(Integer categoryId) {
        Category category = getById(categoryId);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        return category;
    }

    @Override
    public List<Category> getSubCategories(Integer parentId) {
        // 查询指定父分类下的子分类
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Category::getParentId, parentId)
                    .orderByAsc(Category::getSort);
        return list(queryWrapper);
    }

    @Override
    public List<Map<String, Object>> getCategoryTree() {
        // 获取所有分类
        List<Category> allCategories = getAllCategories();
        
        // 转换为分类VO
        List<CategoryVO> categoryVOList = allCategories.stream()
                .map(CategoryVO::fromCategory)
                .collect(Collectors.toList());
        
        // 构建分类树
        List<CategoryVO> rootCategories = buildCategoryTree(categoryVOList);
        
        // 转换为Map格式返回
        return convertToMap(rootCategories);
    }
    
    @Override
    @Transactional
    public Integer addCategory(Category category) {
        // 检查分类名称是否已存在
        if (checkNameExists(category.getName(), category.getParentId(), null)) {
            throw new BusinessException("同级分类下已存在相同名称的分类");
        }
        
        // 如果是子分类，检查父分类是否存在
        if (category.getParentId() != 0) {
            Category parentCategory = getById(category.getParentId());
            if (parentCategory == null) {
                throw new BusinessException("父分类不存在");
            }
        }
        
        // 设置默认排序值
        if (category.getSort() == null) {
            category.setSort(0);
        }
        
        // 保存分类
        save(category);
        return category.getId();
    }
    
    @Override
    @Transactional
    public void updateCategory(Category category) {
        // 检查分类是否存在
        Category existingCategory = getById(category.getId());
        if (existingCategory == null) {
            throw new BusinessException("分类不存在");
        }
        
        // 检查分类名称是否已存在
        if (checkNameExists(category.getName(), category.getParentId(), category.getId())) {
            throw new BusinessException("同级分类下已存在相同名称的分类");
        }
        
        // 如果是子分类，检查父分类是否存在
        if (category.getParentId() != 0) {
            Category parentCategory = getById(category.getParentId());
            if (parentCategory == null) {
                throw new BusinessException("父分类不存在");
            }
            
            // 检查是否将分类设置为自己的子分类（循环引用）
            List<Integer> subCategoryIds = getAllSubCategoryIds(category.getId());
            if (subCategoryIds.contains(category.getParentId())) {
                throw new BusinessException("不能将分类设置为自己的子分类");
            }
        }
        
        // 更新分类
        updateById(category);
    }
    
    @Override
    @Transactional
    public void deleteCategory(Integer categoryId) {
        // 检查分类是否存在
        Category category = getById(categoryId);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        
        // 检查是否有子分类
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Category::getParentId, categoryId);
        long subCategoryCount = count(queryWrapper);
        if (subCategoryCount > 0) {
            throw new BusinessException("该分类下有子分类，无法删除");
        }
        
        // 检查分类下是否有商品
        LambdaQueryWrapper<Product> productQueryWrapper = new LambdaQueryWrapper<>();
        productQueryWrapper.eq(Product::getCategoryId, categoryId)
                           .eq(Product::getDeleted, 0);
        long productCount = productService.count(productQueryWrapper);
        if (productCount > 0) {
            throw new BusinessException("该分类下有商品，无法删除");
        }
        
        // 删除分类
        removeById(categoryId);
    }
    
    @Override
    public boolean checkNameExists(String name, Integer parentId, Integer excludeId) {
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Category::getName, name)
                    .eq(Category::getParentId, parentId);
        
        // 排除指定ID的分类（用于更新时检查）
        if (excludeId != null) {
            queryWrapper.ne(Category::getId, excludeId);
        }
        
        return count(queryWrapper) > 0;
    }
    
    @Override
    public List<Integer> getAllSubCategoryIds(Integer categoryId) {
        List<Integer> subCategoryIds = new ArrayList<>();
        
        // 获取直接子分类
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Category::getParentId, categoryId);
        List<Category> subCategories = list(queryWrapper);
        
        // 递归获取所有子分类
        for (Category subCategory : subCategories) {
            subCategoryIds.add(subCategory.getId());
            subCategoryIds.addAll(getAllSubCategoryIds(subCategory.getId()));
        }
        
        return subCategoryIds;
    }
    
    /**
     * 构建分类树
     * @param categoryList 分类列表
     * @return 分类树
     */
    private List<CategoryVO> buildCategoryTree(List<CategoryVO> categoryList) {
        // 创建结果列表，存放一级分类
        List<CategoryVO> resultList = new ArrayList<>();
        
        // 创建分类ID到分类的映射
        Map<Integer, CategoryVO> categoryMap = new HashMap<>();
        for (CategoryVO category : categoryList) {
            categoryMap.put(category.getId(), category);
        }
        
        // 构建分类树
        for (CategoryVO category : categoryList) {
            // 一级分类
            if (category.getParentId() == 0) {
                resultList.add(category);
            } else {
                // 子分类，添加到父分类的子分类列表中
                CategoryVO parentCategory = categoryMap.get(category.getParentId());
                if (parentCategory != null) {
                    if (parentCategory.getChildren() == null) {
                        parentCategory.setChildren(new ArrayList<>());
                    }
                    parentCategory.getChildren().add(category);
                }
            }
        }
        
        return resultList;
    }
    
    /**
     * 将分类树转换为Map列表
     * @param categoryList 分类树
     * @return Map列表
     */
    private List<Map<String, Object>> convertToMap(List<CategoryVO> categoryList) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (CategoryVO category : categoryList) {
            Map<String, Object> categoryMap = new HashMap<>();
            categoryMap.put("id", category.getId());
            categoryMap.put("name", category.getName());
            categoryMap.put("parentId", category.getParentId());
            categoryMap.put("sort", category.getSort());
            categoryMap.put("createTime", category.getCreateTime());
            
            // 处理子分类
            if (category.getChildren() != null && !category.getChildren().isEmpty()) {
                categoryMap.put("children", convertToMap(category.getChildren()));
            }
            
            result.add(categoryMap);
        }
        
        return result;
    }
}




