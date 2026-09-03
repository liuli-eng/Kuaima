package com.suma.app.common;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Result<T> {

    private int code;
    private String message;
    private T data;
    /** 当前页码（仅列表分页响应时返回） */
    private Integer page;
    /** 总条数（仅列表分页响应时返回） */
    private Long total;

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.code = 200;
        result.message = "success";
        result.data = data;
        return result;
    }

    /** 列表分页成功响应：data 为当前页列表，page 为页码，total 为总条数 */
    public static <T> Result<T> success(T data, int page, long total) {
        Result<T> result = success(data);
        result.page = page;
        result.total = total;
        return result;
    }

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> error(int code, String message) {
        Result<T> result = new Result<>();
        result.code = code;
        result.message = message;
        return result;
    }

    public static <T> Result<T> error(String message) {
        return error(500, message);
    }
}
