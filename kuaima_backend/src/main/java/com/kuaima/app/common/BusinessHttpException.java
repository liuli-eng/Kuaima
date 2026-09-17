package com.kuaima.app.common;

import org.springframework.http.HttpStatus;

public class BusinessHttpException extends RuntimeException {
    private final HttpStatus status;

    public BusinessHttpException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
