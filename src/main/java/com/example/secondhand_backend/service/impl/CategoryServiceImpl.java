package com.example.secondhand_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.secondhand_backend.exception.BusinessException;
import com.example.secondhand_backend.mapper.CategoryMapper;
import com.example.secondhand_backend.model.entity.Category;
import com.example.secondhand_backend.model.entity.Product;
import com.example.secondhand_backend.model.vo.CategoryVO;
import com.example.secondhand_backend.service.CategoryService;
import com.example.secondhand_backend.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author 28619
 * @description 针对表【category(商品分类表)】的数据库操作Service实现
 * @createDate 2025-04-29 13:42:16
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category>
        implements CategoryService {

    @Autowired
    private ProductService productService;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    private static final String CATEGORY_CACHE_PREFIX = "category:";
    private static final String ALL_CATEGORIES_CACHE_KEY = "category:all";
    private static final String CATEGORY_TREE_CACHE_KEY = "category:tree";
    private static final String SUB_CATEGORIES_CACHE_PREFIX = "category:sub:";
    private static final long CACHE_EXPIRE_TIME = 24; // 缓存过期时间（小时）

    @Override
    public List<Category> getAllCategories() {
        // 先从缓存获取
        List<Category> categories = (List<Category>) redisTemplate.opsForValue().get(ALL_CATEGORIES_CACHE_KEY);
        
        if (categories != null) {
            return categories;
        }
        
        // 缓存未命中，查询数据库
        // 按照排序字段排序
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(Category::getParentId)
                .orderByAsc(Category::getSort);
        categories = list(queryWrapper);
        
        // 将结果存入缓存
        redisTemplate.opsForValue().set(ALL_CATEGORIES_CACHE_KEY, categories, CACHE_EXPIRE_TIME, TimeUnit.HOURS);
        
        return categories;
    }

    @Override
    public Category getCategoryDetail(Integer categoryId) {
        // 先从缓存获取
        String cacheKey = CATEGORY_CACHE_PREFIX + categoryId;
        Category category = (Category) redisTemplate.opsForValue().get(cacheKey);
        
        if (category != null) {
            return category;
        }
        
        // 缓存未命中，查询数据库
        category = getById(categoryId);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        
        // 将结果存入缓存
        redisTemplate.opsForValue().set(cacheKey, category, CACHE_EXPIRE_TIME, TimeUnit.HOURS);
        
        return category;
    }

    @Override
    public List<Category> getSubCategories(Integer parentId) {
        // 先从缓存获取
        String cacheKey = SUB_CATEGORIES_CACHE_PREFIX + parentId;
        List<Category> subCategories = (List<Category>) redisTemplate.opsForValue().get(cacheKey);
        
        if (subCategories != null) {
            return subCategories;
        }
        
        // 缓存未命中，查询数据库
        // 查询指定父分类下的子分类
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Category::getParentId, parentId)
                .orderByAsc(Category::getSort);
        subCategories = list(queryWrapper);
        
        // 将结果存入缓存
        redisTemplate.opsForValue().set(cacheKey, subCategories, CACHE_EXPIRE_TIME, TimeUnit.HOURS);
        
        return subCategories;
    }

    @Override
    public List<Map<String, Object>> getCategoryTree() {
        // 先从缓存获取
        List<Map<String, Object>> categoryTree = (List<Map<String, Object>>) redisTemplate.opsForValue().get(CATEGORY_TREE_CACHE_KEY);
        
        if (categoryTree != null) {
            return categoryTree;
        }
        
        // 缓存未命中，构建分类树
        // 获取所有分类
        List<Category> allCategories = getAllCategories();

        // 转换为分类VO
        List<CategoryVO> categoryVOList = allCategories.stream()
                .map(CategoryVO::fromCategory)
                .collect(Collectors.toList());

        // 构建分类树
        List<CategoryVO> rootCategories = buildCategoryTree(categoryVOList);

        // 转换为Map格式
        categoryTree = convertToMap(rootCategories);
        
        // 将结果存入缓存
        redisTemplate.opsForValue().set(CATEGORY_TREE_CACHE_KEY, categoryTree, CACHE_EXPIRE_TIME, TimeUnit.HOURS);
        
        return categoryTree;
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
        
        // 清除相关缓存
        clearCategoryCache(category.getParentId());
        
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
        
        // 更新缓存
        String cacheKey = CATEGORY_CACHE_PREFIX + category.getId();
        redisTemplate.opsForValue().set(cacheKey, category, CACHE_EXPIRE_TIME, TimeUnit.HOURS);
        
        // 清除相关缓存
        clearCategoryCache(existingCategory.getParentId());
        if (existingCategory.getParentId() != category.getParentId()) {
            // 如果更改了父分类，则清除原父分类和新父分类的子分类缓存
            clearCategoryCache(category.getParentId());
        }
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
        
        // 删除缓存
        String cacheKey = CATEGORY_CACHE_PREFIX + categoryId;
        redisTemplate.delete(cacheKey);
        
        // 清除相关缓存
        clearCategoryCache(category.getParentId());
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
     *
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
     *
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
    
    /**
     * 清除分类相关缓存
     * 
     * @param parentId 父分类ID
     */
    private void clearCategoryCache(Integer parentId) {
        // 清除全部分类缓存
        redisTemplate.delete(ALL_CATEGORIES_CACHE_KEY);
        // 清除分类树缓存
        redisTemplate.delete(CATEGORY_TREE_CACHE_KEY);
        // 清除父分类的子分类缓存
        String subCategoriesCacheKey = SUB_CATEGORIES_CACHE_PREFIX + parentId;
        redisTemplate.delete(subCategoriesCacheKey);
    }
}




