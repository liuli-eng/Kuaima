package com.kuaima.app.common.service;

import java.util.LinkedHashMap;
import java.util.Map;

/** OSS 上传结果，统一返回对象路径、访问地址和文件名。 */
public record OssUploadResult(String objectKey, String url, String fileName) {
    public Map<String, String> toMap() {
        Map<String, String> result = new LinkedHashMap<>();
        result.put("objectKey", objectKey);
        result.put("url", url);
        result.put("fileName", fileName);
        return result;
    }
}
