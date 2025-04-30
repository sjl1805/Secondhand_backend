package com.example.secondhand_backend.controller;

import com.example.secondhand_backend.model.common.Result;
import com.example.secondhand_backend.utils.FileUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/file")
@Tag(name = "文件上传接口", description = "处理文件上传和删除的接口")
public class FileController {

    @Value("${file.upload.path}")
    private String uploadPath;
    
    @Value("${file.upload.product-path}")
    private String productPath;
    
    @Value("${file.upload.avatar-path}")
    private String avatarPath;
    
    @PostMapping("/upload")
    @Operation(summary = "上传通用文件", description = "上传文件到默认目录")
    public Result<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String fileName = FileUtils.uploadFile(file, uploadPath);
            Map<String, String> data = new HashMap<>();
            data.put("fileName", fileName);
            data.put("url", "/uploads/" + fileName);
            return Result.success("文件上传成功", data);
        } catch (IOException e) {
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/upload/product")
    @Operation(summary = "上传商品图片", description = "上传商品相关图片")
    public Result<Map<String, String>> uploadProductImage(@RequestParam("file") MultipartFile file) {
        try {
            String fileName = FileUtils.uploadFile(file, productPath);
            Map<String, String> data = new HashMap<>();
            data.put("fileName", fileName);
            data.put("url", "/uploads/product/" + fileName);
            return Result.success("商品图片上传成功", data);
        } catch (IOException e) {
            return Result.error("商品图片上传失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/upload/avatar")
    @Operation(summary = "上传用户头像", description = "上传用户头像图片")
    public Result<Map<String, String>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        try {
            String fileName = FileUtils.uploadFile(file, avatarPath);
            Map<String, String> data = new HashMap<>();
            data.put("fileName", fileName);
            data.put("url", "/uploads/avatar/" + fileName);
            return Result.success("头像上传成功", data);
        } catch (IOException e) {
            return Result.error("头像上传失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/preview/{type}/{fileName}")
    @Operation(summary = "预览文件", description = "根据文件类型和文件名预览文件，无需认证")
    public ResponseEntity<Resource> previewFile(
            @Parameter(description = "文件类型：common-通用文件 product-商品图片 avatar-用户头像") 
            @PathVariable("type") String type,
            @Parameter(description = "文件名") 
            @PathVariable("fileName") String fileName) {
        try {
            // 根据类型确定文件路径
            String filePath;
            switch (type) {
                case "product":
                    filePath = productPath.replace("classpath:", "") + "/" + fileName;
                    break;
                case "avatar":
                    filePath = avatarPath.replace("classpath:", "") + "/" + fileName;
                    break;
                default:
                    filePath = uploadPath.replace("classpath:", "") + "/" + fileName;
            }
            
            // 获取文件资源
            ClassPathResource resource = new ClassPathResource(filePath);
            
            // 确定文件的媒体类型
            String contentType = Files.probeContentType(Paths.get(resource.getFilename()));
            if (contentType == null) {
                // 默认为二进制流
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }
            
            // 返回文件资源
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                    .body(resource);
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/delete")
    @Operation(summary = "删除文件", description = "删除指定路径的文件")
    public Result<Void> deleteFile(@RequestParam("filePath") String filePath) {
        try {
            FileUtils.deleteFile(filePath);
            return Result.success("文件删除成功", null);
        } catch (IOException e) {
            return Result.error("文件删除失败: " + e.getMessage());
        }
    }
} 