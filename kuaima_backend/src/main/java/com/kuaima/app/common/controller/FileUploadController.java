package com.kuaima.app.common.controller;

import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.kuaima.app.common.Result;
import com.kuaima.app.common.service.OssStorageService;

/** 文件上传接口 */
@RestController
@RequestMapping("/admin/upload")
@Tag(name = "后台-文件上传", description = "图片文件上传")
public class FileUploadController {

    @Autowired
    private OssStorageService ossStorageService;

    /** 上传图片：POST /admin/upload，form-data: file */
    @Operation(summary = "上传图片", description = "form-data 字段 file；仅支持 image/* 类型，单文件不超过 2MB；按日期分目录存储，文件名随机生成；返回可访问 url 与 fileName")
    @PostMapping
    public Result<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("上传文件不能为空");
        }

        // 校验文件类型（仅图片）
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.error("仅支持图片格式上传");
        }

        // 校验文件大小（最大 2MB）
        if (file.getSize() > 2 * 1024 * 1024) {
            return Result.error("图片大小不能超过 2MB");
        }

        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf(".")).toLowerCase();
        }
        return Result.success(ossStorageService.upload(file, "admin", ext));
    }
}
