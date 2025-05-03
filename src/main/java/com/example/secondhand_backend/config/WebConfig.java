package com.example.secondhand_backend.config;

import com.example.secondhand_backend.interceptor.JwtInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

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
                        "/product/{id}/view",

                        // 文件公开访问路径
                        "/file/preview/**",
                        "/file/download/**",
                        "/file/info",
                        "/uploads/**",
                        "/uploads/avatar/**",
                        "/uploads/product/**",
                        "/static/**",
                        "/static/images/**",

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
        System.out.println("配置资源处理器...");
        System.out.println("工作目录: " + new File("").getAbsolutePath());

        // Swagger UI资源
        registry.addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/springdoc-openapi-ui/")
                .resourceChain(false);

        // 静态资源和上传文件
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/", "file:static/", "file:./static/")
                .resourceChain(false);

        // 确保能够访问上传的文件
        registry.addResourceHandler("/static/images/**")
                .addResourceLocations("classpath:/static/images/", "file:static/images/", "file:./static/images/")
                .resourceChain(false);

        // 添加针对具体上传目录的映射
        registry.addResourceHandler("/static/images/products/**")
                .addResourceLocations("classpath:/static/images/products/", "file:static/images/products/", "file:./static/images/products/")
                .resourceChain(false);

        registry.addResourceHandler("/static/images/avatar/**")
                .addResourceLocations("classpath:/static/images/avatar/", "file:static/images/avatar/", "file:./static/images/avatar/")
                .resourceChain(false);

        System.out.println("资源处理器配置完成");
    }
} 