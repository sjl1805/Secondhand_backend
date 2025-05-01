package com.example.secondhand_backend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.secondhand_backend.model.dto.ProductDTO;
import com.example.secondhand_backend.model.entity.Product;
import com.example.secondhand_backend.model.vo.ProductVO;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author 28619
 * @description 针对表【product(商品表)】的数据库操作Service
 * @createDate 2025-04-29 13:42:31
 */
public interface ProductService extends IService<Product> {

    /**
     * 发布商品
     *
     * @param productDTO 商品信息
     * @param userId     用户ID
     * @return 商品ID
     */
    Long publishProduct(ProductDTO productDTO, Long userId);

    /**
     * 获取商品详情
     *
     * @param productId 商品ID
     * @return 商品详情
     */
    ProductVO getProductDetail(Long productId);

    /**
     * 获取带收藏状态的商品详情
     *
     * @param productId 商品ID
     * @param userId    用户ID
     * @return 商品详情
     */
    ProductVO getProductDetailWithFavorite(Long productId, Long userId);

    /**
     * 增加商品浏览次数
     *
     * @param productId 商品ID
     */
    void incrementViewCount(Long productId);

    /**
     * 分页获取商品列表
     *
     * @param page       页码
     * @param size       每页数量
     * @param categoryId 分类ID，可为空
     * @param keyword    关键词，可为空
     * @return 商品列表
     */
    IPage<ProductVO> getProductList(int page, int size, Integer categoryId, String keyword);

    /**
     * 获取用户发布的商品
     *
     * @param userId 用户ID
     * @param page   页码
     * @param size   每页数量
     * @return 商品列表
     */
    IPage<ProductVO> getUserProducts(Long userId, int page, int size);

    /**
     * 更新商品状态
     *
     * @param productId 商品ID
     * @param status    状态：1-在售 2-已售 3-下架
     * @param userId    用户ID
     */
    void updateProductStatus(Long productId, Integer status, Long userId);

    /**
     * 删除商品
     *
     * @param productId 商品ID
     * @param userId    用户ID
     */
    void deleteProduct(Long productId, Long userId);

    /**
     * 管理员获取商品列表（包括所有状态）
     *
     * @param page       页码
     * @param size       每页数量
     * @param categoryId 分类ID，可为空
     * @param status     状态，可为空
     * @param keyword    关键词，可为空
     * @param userId     用户ID，可为空
     * @return 商品列表
     */
    IPage<ProductVO> adminGetProductList(int page, int size, Integer categoryId,
                                         Integer status, String keyword, Long userId);

    /**
     * 管理员更新商品状态
     *
     * @param productId  商品ID
     * @param status     状态：1-在售 2-已售 3-下架
     * @param operatorId 操作者ID（管理员）
     */
    void adminUpdateProductStatus(Long productId, Integer status, Long operatorId);

    /**
     * 管理员删除商品
     *
     * @param productId  商品ID
     * @param operatorId 操作者ID（管理员）
     */
    void adminDeleteProduct(Long productId, Long operatorId);

    /**
     * 管理员批量更新商品状态
     *
     * @param productIds 商品ID列表
     * @param status     状态：1-在售 2-已售 3-下架
     * @param operatorId 操作者ID（管理员）
     * @return 更新成功的数量
     */
    int adminBatchUpdateProductStatus(List<Long> productIds, Integer status, Long operatorId);

    /**
     * 管理员批量删除商品
     *
     * @param productIds 商品ID列表
     * @param operatorId 操作者ID（管理员）
     * @return 删除成功的数量
     */
    int adminBatchDeleteProduct(List<Long> productIds, Long operatorId);

    /**
     * 按价格区间查询商品
     *
     * @param page       页码
     * @param size       每页数量
     * @param minPrice   最低价格，可为空
     * @param maxPrice   最高价格，可为空
     * @param categoryId 分类ID，可为空
     * @param keyword    关键词，可为空
     * @return 商品列表
     */
    IPage<ProductVO> getProductsByPriceRange(int page, int size, BigDecimal minPrice,
                                             BigDecimal maxPrice, Integer categoryId, String keyword);

    /**
     * 获取最新上架商品
     *
     * @param limit 返回数量限制
     * @return 商品列表
     */
    List<ProductVO> getLatestProducts(int limit);

    /**
     * 获取热门商品（基于浏览量）
     *
     * @param limit 返回数量限制
     * @return 商品列表
     */
    List<ProductVO> getHotProducts(int limit);

    /**
     * 获取推荐商品（基于分类）
     *
     * @param categoryId 分类ID
     * @param productId  当前商品ID（排除此ID）
     * @param limit      返回数量限制
     * @return 商品列表
     */
    List<ProductVO> getRecommendProductsByCategory(Integer categoryId, Long productId, int limit);

    /**
     * 高级搜索商品
     *
     * @param page       页码
     * @param size       每页数量
     * @param keyword    关键词，可为空
     * @param categoryId 分类ID，可为空
     * @param minPrice   最低价格，可为空
     * @param maxPrice   最高价格，可为空
     * @param sortField  排序字段：createTime-创建时间 price-价格 viewCount-浏览次数
     * @param sortOrder  排序方式：asc-升序 desc-降序
     * @return 商品列表
     */
    IPage<ProductVO> advancedSearchProducts(int page, int size, String keyword, Integer categoryId,
                                            BigDecimal minPrice, BigDecimal maxPrice,
                                            String sortField, String sortOrder);

    /**
     * 获取特定卖家的商品列表
     *
     * @param userId 卖家用户ID
     * @param page   页码
     * @param size   每页数量
     * @param status 商品状态：1-在售 2-已售 3-下架，可为空
     * @return 商品列表
     */
    IPage<ProductVO> getSellerProducts(Long userId, int page, int size, Integer status);

    /**
     * 将Product转换为ProductVO
     *
     * @param product 商品实体
     * @param userId 当前用户ID，用于判断是否收藏
     * @return 商品视图对象
     */
    ProductVO convertToProductVO(Product product, Long userId);
}
