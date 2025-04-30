package com.example.secondhand_backend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.secondhand_backend.model.dto.ProductDTO;
import com.example.secondhand_backend.model.entity.Product;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.secondhand_backend.model.vo.ProductVO;

import java.util.List;

/**
* @author 28619
* @description 针对表【product(商品表)】的数据库操作Service
* @createDate 2025-04-29 13:42:31
*/
public interface ProductService extends IService<Product> {
    
    /**
     * 发布商品
     * @param productDTO 商品信息
     * @param userId 用户ID
     * @return 商品ID
     */
    Long publishProduct(ProductDTO productDTO, Long userId);
    
    /**
     * 获取商品详情
     * @param productId 商品ID
     * @return 商品详情
     */
    ProductVO getProductDetail(Long productId);
    
    /**
     * 获取带收藏状态的商品详情
     * @param productId 商品ID
     * @param userId 用户ID
     * @return 商品详情
     */
    ProductVO getProductDetailWithFavorite(Long productId, Long userId);
    
    /**
     * 增加商品浏览次数
     * @param productId 商品ID
     */
    void incrementViewCount(Long productId);
    
    /**
     * 分页获取商品列表
     * @param page 页码
     * @param size 每页数量
     * @param categoryId 分类ID，可为空
     * @param keyword 关键词，可为空
     * @return 商品列表
     */
    IPage<ProductVO> getProductList(int page, int size, Integer categoryId, String keyword);
    
    /**
     * 获取用户发布的商品
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页数量
     * @return 商品列表
     */
    IPage<ProductVO> getUserProducts(Long userId, int page, int size);
    
    /**
     * 更新商品状态
     * @param productId 商品ID
     * @param status 状态：1-在售 2-已售 3-下架
     * @param userId 用户ID
     */
    void updateProductStatus(Long productId, Integer status, Long userId);
    
    /**
     * 删除商品
     * @param productId 商品ID
     * @param userId 用户ID
     */
    void deleteProduct(Long productId, Long userId);
    
    /**
     * 管理员获取商品列表（包括所有状态）
     * @param page 页码
     * @param size 每页数量
     * @param categoryId 分类ID，可为空
     * @param status 状态，可为空
     * @param keyword 关键词，可为空
     * @param userId 用户ID，可为空
     * @return 商品列表
     */
    IPage<ProductVO> adminGetProductList(int page, int size, Integer categoryId, 
                                        Integer status, String keyword, Long userId);
    
    /**
     * 管理员更新商品状态
     * @param productId 商品ID
     * @param status 状态：1-在售 2-已售 3-下架
     * @param operatorId 操作者ID（管理员）
     */
    void adminUpdateProductStatus(Long productId, Integer status, Long operatorId);
    
    /**
     * 管理员删除商品
     * @param productId 商品ID
     * @param operatorId 操作者ID（管理员）
     */
    void adminDeleteProduct(Long productId, Long operatorId);
    
    /**
     * 管理员批量更新商品状态
     * @param productIds 商品ID列表
     * @param status 状态：1-在售 2-已售 3-下架
     * @param operatorId 操作者ID（管理员）
     * @return 更新成功的数量
     */
    int adminBatchUpdateProductStatus(List<Long> productIds, Integer status, Long operatorId);
    
    /**
     * 管理员批量删除商品
     * @param productIds 商品ID列表
     * @param operatorId 操作者ID（管理员）
     * @return 删除成功的数量
     */
    int adminBatchDeleteProduct(List<Long> productIds, Long operatorId);
}
