package com.kuaima.app.common.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.kuaima.app.common.Result;

@RestController
@RequestMapping("/files")
public class ExpenseFileUploadController {
    private static final long MAX_SIZE = 10L * 1024 * 1024;
    private static final Set<String> EXTENSIONS = Set.of("jpg", "jpeg", "png");
    private static final Set<String> CONTENT_TYPES = Set.of("image/jpeg", "image/png");

    @Value("${kuaima.upload.dir:./uploads/}")
    private String uploadDir;

    @PostMapping("/upload")
    public Result<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("上传文件不能为空");
        if (file.getSize() > MAX_SIZE) throw new IllegalArgumentException("图片大小不能超过10MB");
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        String original = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String extension = extension(original);
        if (!EXTENSIONS.contains(extension) || !CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("仅支持 jpg、jpeg、png 图片");
        }
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        Path root = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path directory = root.resolve("expenses").resolve(datePath).normalize();
        if (!directory.startsWith(root)) throw new IllegalArgumentException("上传路径无效");
        try {
            Files.createDirectories(directory);
            String fileName = UUID.randomUUID().toString().replace("-", "") + "." + extension;
            Path destination = directory.resolve(fileName).normalize();
            if (!destination.startsWith(directory)) throw new IllegalArgumentException("上传路径无效");
            file.transferTo(destination);
            String url = "/uploads/expenses/" + datePath + "/" + fileName;
            Map<String, String> data = new LinkedHashMap<>(); data.put("url", url); data.put("fileName", fileName);
            return Result.success(data);
        } catch (IOException e) {
            throw new IllegalStateException("文件上传失败", e);
        }
    }

    private String extension(String name) {
        int dot = name.lastIndexOf('.');
        return dot < 0 ? "" : name.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}
