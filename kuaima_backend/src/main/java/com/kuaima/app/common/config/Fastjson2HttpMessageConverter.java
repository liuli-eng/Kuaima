package com.kuaima.app.common.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.util.StreamUtils;

import com.alibaba.fastjson2.JSON;

/**
 * 基于 fastjson2 的 JSON 消息转换器。
 * fastjson2 默认不输出值为 null 的字段，因此 Result 中 page/total
 * 仅在列表分页响应时有值并输出，普通响应自动省略。
 */
public class Fastjson2HttpMessageConverter extends AbstractHttpMessageConverter<Object> {

    public Fastjson2HttpMessageConverter() {
        super(MediaType.APPLICATION_JSON, new MediaType("application", "*+json"));
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        // 只处理业务接口的响应对象，其他对象（SpringDoc OpenAPI、byte[]、String 等）交给 Jackson
        // fastjson2 无法正确序列化 OpenAPI 嵌套结构，会导致 Swagger UI 报错

          // 排除 byte[] 和 String（可能是上游预序列化的结果）
        if (clazz == byte[].class || clazz == String.class) {
            return false;
        }

        String className = clazz.getName();
        // 排除 SpringDoc / swagger-core
        if (className.startsWith("io.swagger.v3.")
                || className.startsWith("org.springdoc.")) {
            return false;
        }
      
        return true;
    }

    @Override
    protected Object readInternal(Class<?> clazz, HttpInputMessage inputMessage)
            throws IOException, HttpMessageNotReadableException {
        String text = StreamUtils.copyToString(inputMessage.getBody(), StandardCharsets.UTF_8);
        if (text.isEmpty()) {
            return null;
        }
        return JSON.parseObject(text, clazz);
    }

    @Override
    protected void writeInternal(Object value, HttpOutputMessage outputMessage)
            throws IOException, HttpMessageNotWritableException {
        // 如果 value 已经是 byte[]（可能是上游 converter 预序列化的结果），直接写入原始字节
        // 避免 fastjson2 把 byte[] 当作数组对象再次序列化成 [123,34,111,...] 形式
        if (value instanceof byte[]) {
            outputMessage.getBody().write((byte[]) value);
            return;
        }
        // 如果 value 是 String，直接写入字符串字节
        if (value instanceof String) {
            outputMessage.getBody().write(((String) value).getBytes(StandardCharsets.UTF_8));
            return;
        }
        byte[] bytes = JSON.toJSONBytes(value);
        outputMessage.getBody().write(bytes);
    }
}
