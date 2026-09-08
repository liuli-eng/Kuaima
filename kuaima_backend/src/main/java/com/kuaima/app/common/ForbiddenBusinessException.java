package com.kuaima.app.common;

/** 业务资格不足，统一映射为 HTTP 403。 */
public class ForbiddenBusinessException extends RuntimeException {

    public ForbiddenBusinessException(String message) {
        super(message);
    }
}
