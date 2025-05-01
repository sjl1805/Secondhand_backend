package com.example.secondhand_backend.controller;

import com.example.secondhand_backend.model.common.Result;
import com.example.secondhand_backend.util.FileUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/file")
@Tag(name = "文件管理", description = "文件上传、下载、删除等操作")
public class FileController {

    @Autowired
    private FileUtil fileUtil;

    /**
     * 上传商品图片
     *
     * @param file 图片文件
     * @return 图片访问路径
     */
    @PostMapping("/upload/product")
    @Operation(summary = "上传商品图片", description = "上传商品图片并返回访问路径")
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
     *
     * @param file 头像文件
     * @return 头像访问路径
     */
    @PostMapping("/upload/avatar")
    @Operation(summary = "上传用户头像", description = "上传用户头像并返回访问路径")
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
     *
     * @param path 文件路径
     * @return 是否删除成功
     */
    @DeleteMapping("/delete")
    @Operation(summary = "删除文件", description = "删除指定路径的文件")
    public Result<?> deleteFile(@RequestParam("path") String path) {
        boolean success = fileUtil.deleteFile(path);
        if (success) {
            return Result.success("删除成功", null);
        } else {
            return Result.error("删除失败");
        }
    }

    /**
     * 预览文件
     *
     * @param filePath 文件路径 格式为：{type}/{filename} 如：products/1234.jpg
     * @return 文件资源
     */
    @GetMapping("/preview/{type}/{filename:.+}")
    @Operation(summary = "预览文件", description = "预览指定路径的文件")
    public ResponseEntity<Resource> previewFile(
            @PathVariable String type,
            @PathVariable String filename) {
        try {
            String relativePath = "images/" + type + "/" + filename;
            Path filePath = Paths.get("static/" + relativePath);

            if (!Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new UrlResource(filePath.toUri());
            String contentType = Files.probeContentType(filePath);

            if (contentType == null) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(resource);
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 下载文件
     *
     * @param filePath 文件路径 格式为：{type}/{filename} 如：products/1234.jpg
     * @return 文件资源
     */
    @GetMapping("/download/{type}/{filename:.+}")
    @Operation(summary = "下载文件", description = "下载指定路径的文件")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable String type,
            @PathVariable String filename) {
        try {
            String relativePath = "images/" + type + "/" + filename;
            Path filePath = Paths.get("static/" + relativePath);

            if (!Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new UrlResource(filePath.toUri());
            String contentType = Files.probeContentType(filePath);

            if (contentType == null) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .body(resource);
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 获取图片信息
     *
     * @param path 文件路径
     * @return 图片信息
     */
    @GetMapping("/info")
    @Operation(summary = "获取图片信息", description = "获取指定路径的图片信息")
    public Result<?> getFileInfo(@RequestParam("path") String path) {
        try {
            // 去除前导斜杠，确保路径格式正确
            if (path.startsWith("/")) {
                path = path.substring(1);
            }

            // 尝试多种路径
            // 1. 相对于static目录的路径
            Path filePath = Paths.get("static/" + path);

            // 如果文件不存在，尝试不同的路径
            if (!Files.exists(filePath)) {
                // 2. 相对于resources/static目录的路径
                filePath = Paths.get("src/main/resources/static/" + path);

                // 3. 作为绝对路径尝试
                if (!Files.exists(filePath)) {
                    filePath = Paths.get(path);

                    // 如果还不存在，返回错误
                    if (!Files.exists(filePath)) {
                        // 添加调试信息
                        Map<String, String> debug = new HashMap<>();
                        debug.put("requestedPath", path);
                        debug.put("staticPath", "static/" + path);
                        debug.put("resourcesPath", "src/main/resources/static/" + path);
                        debug.put("absolutePath", filePath.toAbsolutePath().toString());
                        debug.put("workingDirectory", new File("").getAbsolutePath());

                        return Result.error("文件不存在，调试信息：" + debug);
                    }
                }
            }

            Map<String, Object> info = new HashMap<>();
            info.put("exists", true);
            info.put("size", Files.size(filePath));
            info.put("lastModified", Files.getLastModifiedTime(filePath).toMillis());
            info.put("path", filePath.toAbsolutePath().toString());
            info.put("url", fileUtil.getFileUrl(path));
            info.put("contentType", Files.probeContentType(filePath));

            return Result.success("获取成功", info);
        } catch (IOException e) {
            return Result.error("获取文件信息失败: " + e.getMessage());
        }
    }
} 