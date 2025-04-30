package com.example.secondhand_backend.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Component
public class FileUtil {

    @Value("${file.upload.path}")
    private String uploadPath;

    @Value("${file.upload.allowed-types}")
    private String allowedTypes;

    @Value("${file.upload.max-size}")
    private Integer maxSize;

    @Value("${file.upload.avatar-path}")
    private String avatarPath;

    @Value("${file.upload.product-path}")
    private String productPath;

    @Value("${file.upload.domain}")
    private String domain;

    /**
     * 上传商品图片
     * @param file 文件
     * @return 文件访问路径
     */
    public String uploadProductImage(MultipartFile file) throws IOException {
        return uploadFile(file, productPath);
    }

    /**
     * 上传用户头像
     * @param file 文件
     * @return 文件访问路径
     */
    public String uploadAvatar(MultipartFile file) throws IOException {
        return uploadFile(file, avatarPath);
    }

    /**
     * 通用上传文件方法
     * @param file 文件
     * @param subDir 子目录
     * @return 文件访问路径
     */
    public String uploadFile(MultipartFile file, String subDir) throws IOException {
        // 验证文件大小
        if (file.getSize() > maxSize * 1024 * 1024) {
            throw new IOException("文件大小超过限制：" + maxSize + "MB");
        }

        // 验证文件类型
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        List<String> types = Arrays.asList(allowedTypes.split(","));
        
        if (!types.contains(extension.toLowerCase())) {
            throw new IOException("不支持的文件类型：" + extension);
        }

        // 生成唯一文件名
        String fileName = UUID.randomUUID().toString().replaceAll("-", "") + "." + extension;
        String relativePath = "images/" + subDir + "/" + fileName;
        
        // 创建目录
        Path staticDir = Paths.get("static");
        if (!Files.exists(staticDir)) {
            Files.createDirectories(staticDir);
        }
        
        Path imagesDir = Paths.get("static/images");
        if (!Files.exists(imagesDir)) {
            Files.createDirectories(imagesDir);
        }
        
        String dir = "static/images/" + subDir + "/";
        Path targetDir = Paths.get(dir);
        if (!Files.exists(targetDir)) {
            Files.createDirectories(targetDir);
        }
        
        // 保存文件
        Path targetPath = Paths.get(dir + fileName);
        Files.createDirectories(targetPath.getParent());
        file.transferTo(targetPath);
        
        return relativePath;
    }

    /**
     * 删除文件
     * @param relativePath 相对路径
     * @return 是否删除成功
     */
    public boolean deleteFile(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) {
            return false;
        }
        
        try {
            Path filePath = Paths.get("static/" + relativePath);
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 获取文件访问URL
     * @param relativePath 相对路径
     * @return 完整的URL
     */
    public String getFileUrl(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) {
            return "";
        }
        
        // 去除前导斜杠
        if (relativePath.startsWith("/")) {
            relativePath = relativePath.substring(1);
        }
        
        return domain + "/static/" + relativePath;
    }

    /**
     * 获取文件扩展名
     * @param fileName 文件名
     * @return 扩展名
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex == -1) {
            return "";
        }
        return fileName.substring(dotIndex + 1);
    }
} 