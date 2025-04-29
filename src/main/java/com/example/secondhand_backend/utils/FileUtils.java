package com.example.secondhand_backend.utils;

import org.springframework.core.io.ClassPathResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class FileUtils {
    
    public static String uploadFile(MultipartFile file, String uploadDir) throws IOException {
        // 获取 classpath 下的实际路径
        ClassPathResource resource = new ClassPathResource("");
        String classpath = resource.getFile().getAbsolutePath();
        String realPath = classpath + File.separator + uploadDir.replace("classpath:", "");

        // 确保上传目录存在
        File dir = new File(realPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String newFilename = UUID.randomUUID().toString() + extension;

        // 保存文件
        Path filePath = Paths.get(realPath, newFilename);
        Files.copy(file.getInputStream(), filePath);

        return newFilename;
    }

    public static void deleteFile(String filePath) throws IOException {
        // 获取 classpath 下的实际路径
        ClassPathResource resource = new ClassPathResource("");
        String classpath = resource.getFile().getAbsolutePath();
        String realPath = classpath + File.separator + filePath.replace("classpath:", "");
        
        Files.deleteIfExists(Paths.get(realPath));
    }
}