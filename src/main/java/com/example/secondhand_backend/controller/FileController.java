package com.example.secondhand_backend.controller;

import com.example.secondhand_backend.model.common.Result;
import com.example.secondhand_backend.util.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/file")
public class FileController {

    @Autowired
    private FileUtil fileUtil;

    /**
     * 上传商品图片
     * @param file 图片文件
     * @return 图片访问路径
     */
    @PostMapping("/upload/product")
    public Result<?> uploadProductImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("请选择要上传的文件");
        }
        
        try {
            String filePath = fileUtil.uploadProductImage(file);
            String url = fileUtil.getFileUrl(filePath);
            
            Map<String, String> data = new HashMap<>();
            data.put("path", filePath);
            data.put("url", url);
            
            return Result.success("上传成功", data);
        } catch (IOException e) {
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 上传用户头像
     * @param file 头像文件
     * @return 头像访问路径
     */
    @PostMapping("/upload/avatar")
    public Result<?> uploadAvatar(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("请选择要上传的文件");
        }
        
        try {
            String filePath = fileUtil.uploadAvatar(file);
            String url = fileUtil.getFileUrl(filePath);
            
            Map<String, String> data = new HashMap<>();
            data.put("path", filePath);
            data.put("url", url);
            
            return Result.success("上传成功", data);
        } catch (IOException e) {
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 删除文件
     * @param path 文件路径
     * @return 是否删除成功
     */
    @DeleteMapping("/delete")
    public Result<?> deleteFile(@RequestParam("path") String path) {
        boolean success = fileUtil.deleteFile(path);
        if (success) {
            return Result.success("删除成功", null);
        } else {
            return Result.error("删除失败");
        }
    }
} 