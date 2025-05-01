package com.example.secondhand_backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.secondhand_backend.model.entity.Category;

import java.util.List;
import java.util.Map;

/**
 * @author 28619
 * @description 针对表【category(商品分类表)】的数据库操作Service
 * @createDate 2025-04-29 13:42:16
 */
public interface CategoryService extends IService<Category> {

    /**
     * 获取所有分类
     *
     * @return 分类列表
     */
    List<Category> getAllCategories();

    /**
     * 获取分类详情
     *
     * @param categoryId 分类ID
     * @return 分类信息
     */
    Category getCategoryDetail(Integer categoryId);

    /**
     * 获取指定父分类下的子分类列表
     *
     * @param parentId 父分类ID
     * @return 子分类列表
     */
    List<Category> getSubCategories(Integer parentId);

    /**
     * 获取分类树结构
     *
     * @return 分类树
     */
    List<Map<String, Object>> getCategoryTree();

    /**
     * 添加分类
     *
     * @param category 分类信息
     * @return 新增分类的ID
     */
    Integer addCategory(Category category);

    /**
     * 更新分类
     *
     * @param category 分类信息
     */
    void updateCategory(Category category);

    /**
     * 删除分类
     *
     * @param categoryId 分类ID
     */
    void deleteCategory(Integer categoryId);

    /**
     * 检查分类名称是否已存在
     *
     * @param name      分类名称
     * @param parentId  父分类ID
     * @param excludeId 排除的分类ID（用于更新时检查）
     * @return 是否存在
     */
    boolean checkNameExists(String name, Integer parentId, Integer excludeId);

    /**
     * 获取分类的所有子分类ID列表（包括子分类的子分类）
     *
     * @param categoryId 分类ID
     * @return 子分类ID列表
     */
    List<Integer> getAllSubCategoryIds(Integer categoryId);
}
