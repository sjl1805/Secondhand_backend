package com.example.secondhand_backend.config;

import com.example.secondhand_backend.interceptor.JwtInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Value("${file.upload.path}")
    private String uploadPath;
    
    @Value("${file.upload.avatar-path}")
    private String avatarPath;
    
    @Value("${file.upload.product-path}")
    private String productPath;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        // 认证相关
                        "/auth/login",
                        "/auth/register",
                        "/auth/captcha",
                        
                        // 公开的API接口
                        "/category/list",
                        "/category/tree",
                        "/product/list",
                        "/product/detail/**",
                        "/product/search",
                        "/product/hot",
                        "/product/advanced-search",
                        
                        // 文件公开访问路径
                        "/file/preview/**",
                        "/uploads/**",
                        "/uploads/avatar/**",
                        "/uploads/product/**",
                        
                        // Swagger和API文档
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/swagger-resources/**",
                        "/webjars/**"
                );
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Authorization")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 通用上传文件访问路径
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("classpath:/static/uploads/");
        
        // 头像文件访问路径
        registry.addResourceHandler("/uploads/avatar/**")
                .addResourceLocations("classpath:/static/uploads/avatar/");
        
        // 商品图片访问路径
        registry.addResourceHandler("/uploads/product/**")
                .addResourceLocations("classpath:/static/uploads/product/");
        
        // Swagger UI资源
        registry.addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/springdoc-openapi-ui/")
                .resourceChain(false);
    }
} 