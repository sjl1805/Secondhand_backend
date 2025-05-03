package com.example.secondhand_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.secondhand_backend.exception.BusinessException;
import com.example.secondhand_backend.mapper.CategoryMapper;
import com.example.secondhand_backend.mapper.ProductMapper;
import com.example.secondhand_backend.mapper.UserMapper;
import com.example.secondhand_backend.model.dto.ProductDTO;
import com.example.secondhand_backend.model.entity.Category;
import com.example.secondhand_backend.model.entity.Product;
import com.example.secondhand_backend.model.entity.User;
import com.example.secondhand_backend.model.vo.ProductVO;
import com.example.secondhand_backend.service.FavoriteService;
import com.example.secondhand_backend.service.ProductImageService;
import com.example.secondhand_backend.service.ProductService;
import com.example.secondhand_backend.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author 28619
 * @description 针对表【product(商品表)】的数据库操作Service实现
 * @createDate 2025-04-29 13:42:31
 */
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product>
        implements ProductService {

    @Autowired
    private ProductImageService productImageService;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserService userService;

    @Autowired
    @Lazy
    private FavoriteService favoriteService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String PRODUCT_CACHE_PREFIX = "product:";
    private static final String PRODUCT_LIST_CACHE_PREFIX = "product:list:";
    private static final String USER_PRODUCTS_CACHE_PREFIX = "product:user:";
    private static final String PRODUCT_PRICE_RANGE_CACHE_PREFIX = "product:price:";
    private static final String LATEST_PRODUCTS_CACHE_KEY = "product:latest:";
    private static final String HOT_PRODUCTS_CACHE_KEY = "product:hot:";
    private static final String RECOMMEND_PRODUCTS_CACHE_PREFIX = "product:recommend:";
    private static final String ADVANCED_SEARCH_CACHE_PREFIX = "product:search:";
    private static final String SELLER_PRODUCTS_CACHE_PREFIX = "product:seller:";
    private static final long CACHE_EXPIRE_TIME = 12; // 缓存过期时间（小时）

    @Override
    @Transactional
    public Long publishProduct(ProductDTO productDTO, Long userId) {
        // 创建商品实体
        Product product = new Product();
        BeanUtils.copyProperties(productDTO, product);

        // 设置用户ID和初始状态
        product.setUserId(userId);
        product.setStatus(1); // 在售状态
        product.setViewCount(0); // 初始浏览次数为0
        product.setDeleted(0); // 未删除

        // 保存商品
        save(product);

        // 保存商品图片
        if (productDTO.getImageUrls() != null && !productDTO.getImageUrls().isEmpty()) {
            productImageService.saveProductImages(product.getId(), productDTO.getImageUrls());
        }

        return product.getId();
    }

    @Override
    public ProductVO getProductDetail(Long productId) {
        // 从缓存获取
        String cacheKey = PRODUCT_CACHE_PREFIX + productId;
        ProductVO productVO = (ProductVO) redisTemplate.opsForValue().get(cacheKey);
        
        if (productVO != null) {
            return productVO;
        }
        
        // 缓存未命中，查询数据库
        // 获取商品
        Product product = getById(productId);
        if (product == null || product.getDeleted() == 1) {
            throw new BusinessException("商品不存在或已删除");
        }

        productVO = convertToProductVO(product, null);
        
        // 将结果存入缓存
        redisTemplate.opsForValue().set(cacheKey, productVO, CACHE_EXPIRE_TIME, TimeUnit.HOURS);
        
        return productVO;
    }

    @Override
    public ProductVO getProductDetailWithFavorite(Long productId, Long userId) {
        // 从缓存获取
        String cacheKey = PRODUCT_CACHE_PREFIX + productId + ":" + userId;
        ProductVO productVO = (ProductVO) redisTemplate.opsForValue().get(cacheKey);
        
        if (productVO != null) {
            return productVO;
        }
        
        // 缓存未命中，查询数据库
        // 获取商品
        Product product = getById(productId);
        if (product == null || product.getDeleted() == 1) {
            throw new BusinessException("商品不存在或已删除");
        }

        productVO = convertToProductVO(product, userId);
        
        // 将结果存入缓存
        redisTemplate.opsForValue().set(cacheKey, productVO, CACHE_EXPIRE_TIME, TimeUnit.HOURS);
        
        return productVO;
    }

    @Override
    public void incrementViewCount(Long productId) {
        // 更新浏览次数
        Product product = getById(productId);
        if (product != null && product.getDeleted() == 0) {
            LambdaUpdateWrapper<Product> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Product::getId, productId)
                    .set(Product::getViewCount, product.getViewCount() + 1);
            update(updateWrapper);
            
            // 清除相关缓存
            clearProductCache(productId);
        }
    }

    @Override
    public IPage<ProductVO> getProductList(int page, int size, Integer categoryId, String keyword) {
        // 构建缓存键
        String cacheKey = PRODUCT_LIST_CACHE_PREFIX + page + ":" + size + ":" + 
                (categoryId == null ? "all" : categoryId) + ":" + 
                (keyword == null ? "all" : keyword);
        
        // 从缓存获取
        IPage<ProductVO> productVOPage = (IPage<ProductVO>) redisTemplate.opsForValue().get(cacheKey);
        
        if (productVOPage != null) {
            return productVOPage;
        }
        
        // 缓存未命中，查询数据库
        // 创建查询条件
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Product::getDeleted, 0)
                .eq(Product::getStatus, 1); // 只查询在售商品

        // 如果有分类ID，添加分类条件
        if (categoryId != null && categoryId > 0) {
            queryWrapper.eq(Product::getCategoryId, categoryId);
        }

        // 如果有关键词，添加关键词条件
        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(wrapper ->
                    wrapper.like(Product::getTitle, keyword)
                            .or()
                            .like(Product::getDescription, keyword)
            );
        }

        // 按创建时间倒序排序
        queryWrapper.orderByDesc(Product::getCreateTime);

        // 分页查询
        Page<Product> productPage = new Page<>(page, size);
        Page<Product> resultPage = page(productPage, queryWrapper);

        // 转换为ProductVO
        productVOPage = convertToProductVOPage(resultPage);
        
        // 将结果存入缓存
        redisTemplate.opsForValue().set(cacheKey, productVOPage, CACHE_EXPIRE_TIME, TimeUnit.HOURS);
        
        return productVOPage;
    }

    @Override
    public IPage<ProductVO> getUserProducts(Long userId, int page, int size) {
        // 构建缓存键
        String cacheKey = USER_PRODUCTS_CACHE_PREFIX + userId + ":" + page + ":" + size;
        
        // 从缓存获取
        IPage<ProductVO> productVOPage = (IPage<ProductVO>) redisTemplate.opsForValue().get(cacheKey);
        
        if (productVOPage != null) {
            return productVOPage;
        }
        
        // 缓存未命中，查询数据库
        // 创建查询条件
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Product::getUserId, userId)
                .eq(Product::getDeleted, 0)
                .orderByDesc(Product::getCreateTime);

        // 分页查询
        Page<Product> productPage = new Page<>(page, size);
        Page<Product> resultPage = page(productPage, queryWrapper);

        // 转换为ProductVO
        productVOPage = convertToProductVOPage(resultPage);
        
        // 将结果存入缓存
        redisTemplate.opsForValue().set(cacheKey, productVOPage, CACHE_EXPIRE_TIME, TimeUnit.HOURS);
        
        return productVOPage;
    }

    @Override
    @Transactional
    public void updateProductStatus(Long productId, Integer status, Long userId) {
        // 获取商品
        Product product = getById(productId);
        if (product == null || product.getDeleted() == 1) {
            throw new BusinessException("商品不存在或已删除");
        }

        // 验证权限
        if (!product.getUserId().equals(userId)) {
            throw new BusinessException("无权操作该商品");
        }

        // 更新状态
        product.setStatus(status);
        updateById(product);
        
        // 清除相关缓存
        clearProductCache(productId);
        
        // 清除用户商品列表缓存
        String userProductsPattern = USER_PRODUCTS_CACHE_PREFIX + userId + "*";
        redisTemplate.delete(redisTemplate.keys(userProductsPattern));
    }

    @Override
    @Transactional
    public void deleteProduct(Long productId, Long userId) {
        // 获取商品
        Product product = getById(productId);
        if (product == null || product.getDeleted() == 1) {
            throw new BusinessException("商品不存在或已删除");
        }

        // 验证权限
        if (!product.getUserId().equals(userId)) {
            throw new BusinessException("无权操作该商品");
        }

        // 逻辑删除
        product.setDeleted(1);
        updateById(product);
        
        // 清除相关缓存
        clearProductCache(productId);
        
        // 清除用户商品列表缓存
        String userProductsPattern = USER_PRODUCTS_CACHE_PREFIX + userId + "*";
        redisTemplate.delete(redisTemplate.keys(userProductsPattern));
        
        // 清除商品列表缓存
        redisTemplate.delete(redisTemplate.keys(PRODUCT_LIST_CACHE_PREFIX + "*"));
    }

    @Override
    @Transactional
    public void updateProduct(Long productId, ProductDTO productDTO, Long userId) {
        // 获取商品
        Product product = getById(productId);
        if (product == null || product.getDeleted() == 1) {
            throw new BusinessException("商品不存在或已删除");
        }

        // 验证权限
        if (!product.getUserId().equals(userId)) {
            throw new BusinessException("无权操作该商品");
        }

        // 只有在售状态或已下架状态的商品可以编辑
        if (product.getStatus() != 1 && product.getStatus() != 3) {
            throw new BusinessException("该商品当前状态不允许编辑");
        }

        // 更新商品信息
        BeanUtils.copyProperties(productDTO, product);
        
        // 保留原有的用户ID、状态、浏览次数和删除标记
        product.setId(productId);
        product.setUserId(userId);
        product.setStatus(product.getStatus());
        product.setViewCount(product.getViewCount());
        product.setDeleted(0);
        
        // 更新商品
        updateById(product);
        
        // 更新商品图片
        if (productDTO.getImageUrls() != null && !productDTO.getImageUrls().isEmpty()) {
            // 先删除旧图片关联
            productImageService.deleteProductImages(productId);
            // 保存新图片关联
            productImageService.saveProductImages(productId, productDTO.getImageUrls());
        }
        
        // 清除相关缓存
        clearProductCache(productId);
        
        // 清除用户商品列表缓存
        String userProductsPattern = USER_PRODUCTS_CACHE_PREFIX + userId + "*";
        redisTemplate.delete(redisTemplate.keys(userProductsPattern));
        
        // 清除商品列表缓存
        redisTemplate.delete(redisTemplate.keys(PRODUCT_LIST_CACHE_PREFIX + "*"));
    }

    @Override
    public IPage<ProductVO> adminGetProductList(int page, int size, Integer categoryId,
                                                Integer status, String keyword, Long userId) {
        // 创建查询条件
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Product::getDeleted, 0);

        // 如果有分类ID，添加分类条件
        if (categoryId != null && categoryId > 0) {
            queryWrapper.eq(Product::getCategoryId, categoryId);
        }

        // 如果有状态，添加状态条件
        if (status != null) {
            queryWrapper.eq(Product::getStatus, status);
        }

        // 如果有用户ID，添加用户条件
        if (userId != null) {
            queryWrapper.eq(Product::getUserId, userId);
        }

        // 如果有关键词，添加关键词条件
        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(wrapper ->
                    wrapper.like(Product::getTitle, keyword)
                            .or()
                            .like(Product::getDescription, keyword)
            );
        }

        // 按创建时间倒序排序
        queryWrapper.orderByDesc(Product::getCreateTime);

        // 分页查询
        Page<Product> productPage = new Page<>(page, size);
        Page<Product> resultPage = page(productPage, queryWrapper);

        // 转换为ProductVO
        return convertToProductVOPage(resultPage);
    }

    @Override
    @Transactional
    public void adminUpdateProductStatus(Long productId, Integer status, Long operatorId) {
        // 检查是否为管理员
        if (!isAdmin(operatorId)) {
            throw new BusinessException("无权限执行此操作，需要管理员权限");
        }

        // 获取商品
        Product product = getById(productId);
        if (product == null || product.getDeleted() == 1) {
            throw new BusinessException("商品不存在或已删除");
        }

        // 更新状态
        product.setStatus(status);
        updateById(product);
    }

    @Override
    @Transactional
    public void adminDeleteProduct(Long productId, Long operatorId) {
        // 检查是否为管理员
        if (!isAdmin(operatorId)) {
            throw new BusinessException("无权限执行此操作，需要管理员权限");
        }

        // 获取商品
        Product product = getById(productId);
        if (product == null || product.getDeleted() == 1) {
            throw new BusinessException("商品不存在或已删除");
        }

        // 逻辑删除
        product.setDeleted(1);
        updateById(product);

        // 删除商品图片（不需要物理删除图片文件，只删除数据库记录）
        productImageService.deleteProductImages(productId);
    }

    @Override
    @Transactional
    public int adminBatchUpdateProductStatus(List<Long> productIds, Integer status, Long operatorId) {
        // 检查是否为管理员
        if (!isAdmin(operatorId)) {
            throw new BusinessException("无权限执行此操作，需要管理员权限");
        }

        if (productIds == null || productIds.isEmpty()) {
            return 0;
        }

        int successCount = 0;

        for (Long productId : productIds) {
            try {
                // 获取商品
                Product product = getById(productId);
                if (product != null && product.getDeleted() == 0) {
                    // 更新状态
                    product.setStatus(status);
                    updateById(product);
                    successCount++;
                }
            } catch (Exception e) {
                // 忽略单个商品的更新错误，继续处理其他商品
                continue;
            }
        }

        return successCount;
    }

    @Override
    @Transactional
    public int adminBatchDeleteProduct(List<Long> productIds, Long operatorId) {
        // 检查是否为管理员
        if (!isAdmin(operatorId)) {
            throw new BusinessException("无权限执行此操作，需要管理员权限");
        }

        if (productIds == null || productIds.isEmpty()) {
            return 0;
        }

        int successCount = 0;

        for (Long productId : productIds) {
            try {
                // 获取商品
                Product product = getById(productId);
                if (product != null && product.getDeleted() == 0) {
                    // 逻辑删除
                    product.setDeleted(1);
                    updateById(product);

                    // 删除商品图片
                    productImageService.deleteProductImages(productId);

                    successCount++;
                }
            } catch (Exception e) {
                // 忽略单个商品的删除错误，继续处理其他商品
                continue;
            }
        }

        return successCount;
    }

    /**
     * 检查用户是否为管理员
     *
     * @param userId 用户ID
     * @return 是否为管理员
     */
    private boolean isAdmin(Long userId) {
        return userService.isAdmin(userId);
    }

    /**
     * 将Product转换为ProductVO
     *
     * @param product 商品实体
     * @param userId  当前用户ID，用于判断是否收藏
     * @return 商品视图对象
     */
    @Override
    public ProductVO convertToProductVO(Product product, Long userId) {
        ProductVO productVO = new ProductVO();
        BeanUtils.copyProperties(product, productVO);

        // 获取商品图片
        List<String> imageUrls = productImageService.getProductImages(product.getId());
        productVO.setImageUrls(imageUrls);

        // 获取分类名称
        Category category = categoryMapper.selectById(product.getCategoryId());
        if (category != null) {
            productVO.setCategoryName(category.getName());
        }

        // 获取发布者信息
        User user = userMapper.selectById(product.getUserId());
        if (user != null) {
            productVO.setNickname(user.getNickname());
            productVO.setAvatar(user.getAvatar());
        }

        // 获取收藏数量
        int favoriteCount = favoriteService.getProductFavoriteCount(product.getId());
        productVO.setFavoriteCount(favoriteCount);

        // 判断当前用户是否收藏
        if (userId != null) {
            boolean isFavorite = favoriteService.isFavorite(userId, product.getId());
            productVO.setIsFavorite(isFavorite);
        } else {
            productVO.setIsFavorite(false);
        }

        return productVO;
    }

    /**
     * 将Product分页结果转换为ProductVO分页结果
     *
     * @param productPage 商品分页
     * @return 商品VO分页
     */
    private IPage<ProductVO> convertToProductVOPage(Page<Product> productPage) {
        List<ProductVO> productVOList = productPage.getRecords().stream()
                .map(product -> convertToProductVO(product, null))
                .collect(Collectors.toList());

        Page<ProductVO> voPage = new Page<>();
        voPage.setRecords(productVOList);
        voPage.setCurrent(productPage.getCurrent());
        voPage.setSize(productPage.getSize());
        voPage.setTotal(productPage.getTotal());
        voPage.setPages(productPage.getPages());

        return voPage;
    }

    @Override
    public IPage<ProductVO> getProductsByPriceRange(int page, int size, BigDecimal minPrice,
                                                    BigDecimal maxPrice, Integer categoryId, String keyword) {
        // 创建查询条件
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Product::getDeleted, 0)
                .eq(Product::getStatus, 1); // 只查询在售商品

        // 添加价格区间条件
        if (minPrice != null) {
            queryWrapper.ge(Product::getPrice, minPrice);
        }

        if (maxPrice != null) {
            queryWrapper.le(Product::getPrice, maxPrice);
        }

        // 如果有分类ID，添加分类条件
        if (categoryId != null && categoryId > 0) {
            queryWrapper.eq(Product::getCategoryId, categoryId);
        }

        // 如果有关键词，添加关键词条件
        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(wrapper ->
                    wrapper.like(Product::getTitle, keyword)
                            .or()
                            .like(Product::getDescription, keyword)
            );
        }

        // 按创建时间倒序排序
        queryWrapper.orderByDesc(Product::getCreateTime);

        // 分页查询
        Page<Product> productPage = new Page<>(page, size);
        Page<Product> resultPage = page(productPage, queryWrapper);

        // 转换为ProductVO
        return convertToProductVOPage(resultPage);
    }

    @Override
    public List<ProductVO> getLatestProducts(int limit) {
        // 构建缓存键
        String cacheKey = LATEST_PRODUCTS_CACHE_KEY + limit;
        
        // 从缓存获取
        List<ProductVO> productVOList = (List<ProductVO>) redisTemplate.opsForValue().get(cacheKey);
        
        if (productVOList != null) {
            return productVOList;
        }
        
        // 缓存未命中，查询数据库
        // 创建查询条件
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Product::getDeleted, 0)
                .eq(Product::getStatus, 1) // 只查询在售商品
                .orderByDesc(Product::getCreateTime)
                .last("LIMIT " + limit); // 限制返回数量

        // 查询最新商品
        List<Product> products = list(queryWrapper);

        // 转换为ProductVO
        productVOList = products.stream()
                .map(product -> convertToProductVO(product, null))
                .collect(Collectors.toList());
        
        // 将结果存入缓存
        redisTemplate.opsForValue().set(cacheKey, productVOList, CACHE_EXPIRE_TIME, TimeUnit.HOURS);
        
        return productVOList;
    }

    @Override
    public List<ProductVO> getHotProducts(int limit) {
        // 构建缓存键
        String cacheKey = HOT_PRODUCTS_CACHE_KEY + limit;
        
        // 从缓存获取
        List<ProductVO> productVOList = (List<ProductVO>) redisTemplate.opsForValue().get(cacheKey);
        
        if (productVOList != null) {
            return productVOList;
        }
        
        // 缓存未命中，查询数据库
        // 创建查询条件
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Product::getDeleted, 0)
                .eq(Product::getStatus, 1) // 只查询在售商品
                .orderByDesc(Product::getViewCount)
                .last("LIMIT " + limit); // 限制返回数量

        // 查询热门商品
        List<Product> products = list(queryWrapper);

        // 转换为ProductVO
        productVOList = products.stream()
                .map(product -> convertToProductVO(product, null))
                .collect(Collectors.toList());
        
        // 将结果存入缓存
        redisTemplate.opsForValue().set(cacheKey, productVOList, CACHE_EXPIRE_TIME, TimeUnit.HOURS);
        
        return productVOList;
    }

    @Override
    public List<ProductVO> getRecommendProductsByCategory(Integer categoryId, Long productId, int limit) {
        // 构建缓存键
        String cacheKey = RECOMMEND_PRODUCTS_CACHE_PREFIX + categoryId + ":" + 
                (productId == null ? "all" : productId) + ":" + limit;
        
        // 从缓存获取
        List<ProductVO> productVOList = (List<ProductVO>) redisTemplate.opsForValue().get(cacheKey);
        
        if (productVOList != null) {
            return productVOList;
        }
        
        // 缓存未命中，查询数据库
        // 创建查询条件
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Product::getDeleted, 0)
                .eq(Product::getStatus, 1) // 只查询在售商品
                .eq(Product::getCategoryId, categoryId)
                .ne(productId != null, Product::getId, productId) // 排除当前商品
                .orderByDesc(Product::getCreateTime)
                .last("LIMIT " + limit); // 限制返回数量

        // 查询同类商品
        List<Product> products = list(queryWrapper);

        // 转换为ProductVO
        productVOList = products.stream()
                .map(product -> convertToProductVO(product, null))
                .collect(Collectors.toList());
        
        // 将结果存入缓存
        redisTemplate.opsForValue().set(cacheKey, productVOList, CACHE_EXPIRE_TIME, TimeUnit.HOURS);
        
        return productVOList;
    }

    @Override
    public IPage<ProductVO> advancedSearchProducts(int page, int size, String keyword, Integer categoryId,
                                                   BigDecimal minPrice, BigDecimal maxPrice,
                                                   String sortField, String sortOrder) {
        // 创建查询条件
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Product::getDeleted, 0);

        // 关键词搜索
        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(wrapper ->
                    wrapper.like(Product::getTitle, keyword)
                            .or()
                            .like(Product::getDescription, keyword)
            );
        }

        // 分类筛选
        if (categoryId != null && categoryId > 0) {
            queryWrapper.eq(Product::getCategoryId, categoryId);
        }

        // 价格区间
        if (minPrice != null) {
            queryWrapper.ge(Product::getPrice, minPrice);
        }
        if (maxPrice != null) {
            queryWrapper.le(Product::getPrice, maxPrice);
        }

        // 排序
        if ("price".equals(sortField)) {
            if ("asc".equals(sortOrder)) {
                queryWrapper.orderByAsc(Product::getPrice);
            } else {
                queryWrapper.orderByDesc(Product::getPrice);
            }
        } else if ("viewCount".equals(sortField)) {
            queryWrapper.orderByDesc(Product::getViewCount);
        } else {
            // 默认按创建时间排序
            queryWrapper.orderByDesc(Product::getCreateTime);
        }

        // 分页查询
        Page<Product> productPage = new Page<>(page, size);
        Page<Product> resultPage = page(productPage, queryWrapper);

        // 转换为ProductVO
        return convertToProductVOPage(resultPage);
    }

    @Override
    public IPage<ProductVO> getSellerProducts(Long userId, int page, int size, Integer status) {
        // 创建查询条件
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Product::getUserId, userId)
                .eq(Product::getDeleted, 0);

        // 如果指定了商品状态，添加状态条件
        if (status != null) {
            queryWrapper.eq(Product::getStatus, status);
        }

        // 按创建时间倒序排序
        queryWrapper.orderByDesc(Product::getCreateTime);

        // 分页查询
        Page<Product> productPage = new Page<>(page, size);
        Page<Product> resultPage = page(productPage, queryWrapper);

        // 转换为ProductVO
        return convertToProductVOPage(resultPage);
    }

    /**
     * 清除商品相关缓存
     *
     * @param productId 商品ID
     */
    private void clearProductCache(Long productId) {
        // 清除商品详情缓存（模糊删除所有与该商品相关的缓存）
        String productCachePattern = PRODUCT_CACHE_PREFIX + productId + "*";
        redisTemplate.delete(redisTemplate.keys(productCachePattern));
        
        // 清除最新商品和热门商品缓存
        redisTemplate.delete(LATEST_PRODUCTS_CACHE_KEY + "*");
        redisTemplate.delete(HOT_PRODUCTS_CACHE_KEY + "*");
        
        // 清除推荐商品缓存（与该商品相关的）
        String recommendCachePattern = RECOMMEND_PRODUCTS_CACHE_PREFIX + "*" + productId + "*";
        redisTemplate.delete(redisTemplate.keys(recommendCachePattern));
    }
}




