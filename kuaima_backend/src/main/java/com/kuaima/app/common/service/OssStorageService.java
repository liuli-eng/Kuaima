package com.kuaima.app.common.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.ObjectMetadata;
import com.kuaima.app.common.ForbiddenBusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import jakarta.annotation.PreDestroy;

import java.io.IOException;
import java.net.URLEncoder;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class OssStorageService {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    @Value("${aliyun.oss.endpoint:}") private String endpoint;
    @Value("${aliyun.oss.bucket-name:}") private String bucketName;
    @Value("${aliyun.oss.access-key-id:}") private String accessKeyId;
    @Value("${aliyun.oss.access-key-secret:}") private String accessKeySecret;
    @Value("${aliyun.oss.domain:}") private String domain;
    @Value("${aliyun.oss.signed-url-expiration-seconds:3600}") private long signedUrlExpirationSeconds;
    private volatile OSS client;

    public Map<String, String> upload(MultipartFile file, String folder, String extension) {
        return uploadResult(file, folder, extension).toMap();
    }

    public OssUploadResult uploadResult(MultipartFile file, String folder, String extension) {
        if (!StringUtils.hasText(endpoint) || !StringUtils.hasText(bucketName)
                || !StringUtils.hasText(accessKeyId) || !StringUtils.hasText(accessKeySecret)) {
            throw new IllegalStateException("OSS 配置不完整，请检查 aliyun.oss 配置或 ALIYUN_OSS_* 环境变量");
        }
        String suffix = extension == null ? "" : extension.toLowerCase();
        String objectKey = folder + "/" + LocalDate.now().format(DATE_FORMAT) + "/"
                + UUID.randomUUID().toString().replace("-", "") + suffix;
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(file.getSize());
        try {
            getClient().putObject(bucketName, objectKey, file.getInputStream(), metadata);
            return result(objectKey);
        } catch (OSSException e) {
            if ("AccessDenied".equalsIgnoreCase(e.getErrorCode())) {
                throw new ForbiddenBusinessException("OSS 上传权限不足，请为当前 RAM 用户授予目标目录的 oss:PutObject 权限");
            }
            throw new IllegalStateException("上传 OSS 失败，请检查存储配置和服务状态");
        } catch (IOException e) {
            throw new IllegalStateException("上传 OSS 失败", e);
        }
    }

    public Map<String, String> upload(Path file, String contentType, String folder, String extension) {
        return uploadResult(file, contentType, folder, extension).toMap();
    }

    public OssUploadResult uploadResult(Path file, String contentType, String folder, String extension) {
        if (!StringUtils.hasText(endpoint) || !StringUtils.hasText(bucketName)
                || !StringUtils.hasText(accessKeyId) || !StringUtils.hasText(accessKeySecret)) {
            throw new IllegalStateException("OSS 配置不完整，请检查 aliyun.oss 配置或 ALIYUN_OSS_* 环境变量");
        }
        String suffix = extension == null ? "" : extension.toLowerCase();
        String objectKey = folder + "/" + LocalDate.now().format(DATE_FORMAT) + "/"
                + UUID.randomUUID().toString().replace("-", "") + suffix;
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(contentType);
        try { metadata.setContentLength(Files.size(file)); }
        catch (IOException e) { throw new IllegalStateException("读取上传文件失败", e); }
        try (var input = Files.newInputStream(file)) {
            getClient().putObject(bucketName, objectKey, input, metadata);
            return result(objectKey);
        } catch (OSSException e) {
            if ("AccessDenied".equalsIgnoreCase(e.getErrorCode())) {
                throw new ForbiddenBusinessException("OSS 上传权限不足，请为当前 RAM 用户授予目标目录的 oss:PutObject 权限");
            }
            throw new IllegalStateException("上传 OSS 失败，请检查存储配置和服务状态");
        } catch (IOException e) {
            throw new IllegalStateException("上传 OSS 失败", e);
        }
    }

    private String objectUrl(String objectKey) {
        String base = StringUtils.hasText(domain) ? domain.replaceAll("/$", "") : "https://" + bucketName + "." + endpoint;
        return base + "/" + URLEncoder.encode(objectKey, StandardCharsets.UTF_8).replace("+", "%20").replace("%2F", "/");
    }

    /**
     * 私有 Bucket 中对象的临时播放地址。数据库可以保存普通对象 URL，接口返回时再签名，避免
     * 把 academy 目录或整个 Bucket 改成公共读。
     */
    public String playableUrl(String value) {
        return playableUrl(value, signedUrlExpirationSeconds);
    }

    /** 生成自定义有效期的临时地址，单位为秒。 */
    public String playableUrl(String value, long expirationSeconds) {
        if (expirationSeconds <= 0) throw new IllegalArgumentException("签名有效期必须大于 0 秒");
        if (!StringUtils.hasText(value) || !StringUtils.hasText(endpoint)
                || !StringUtils.hasText(bucketName) || !StringUtils.hasText(accessKeyId)
                || !StringUtils.hasText(accessKeySecret)) {
            return value;
        }
        String objectKey = objectKey(value);
        if (!StringUtils.hasText(objectKey)) return value;
        Date expires = new Date(System.currentTimeMillis() + expirationMillis(expirationSeconds));
        return getClient().generatePresignedUrl(bucketName, objectKey, expires).toString()
                .replaceFirst("^http://", "https://");
    }

    private OssUploadResult result(String objectKey) {
        return new OssUploadResult(objectKey, objectUrl(objectKey), objectKey.substring(objectKey.lastIndexOf('/') + 1));
    }

    private long expirationMillis(long expirationSeconds) {
        return TimeUnit.SECONDS.toMillis(expirationSeconds);
    }

    private OSS getClient() {
        OSS current = client;
        if (current == null) {
            synchronized (this) {
                current = client;
                if (current == null) client = current = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
            }
        }
        return current;
    }

    @PreDestroy
    void shutdown() {
        OSS current = client;
        if (current != null) current.shutdown();
    }

    private String objectKey(String value) {
        try {
            URI uri = URI.create(value);
            String host = uri.getHost();
            String expectedHost = bucketName + "." + endpoint;
            if (host != null && !expectedHost.equalsIgnoreCase(host)) return null;
            String path = uri.getPath();
            if (path == null || path.length() <= 1) return null;
            return URLDecoder.decode(path.substring(1), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return value.startsWith("academy/") ? value : null;
        }
    }
}
