package com.suma.app.common.config;

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
        byte[] bytes = JSON.toJSONBytes(value);
        outputMessage.getBody().write(bytes);
    }
}
