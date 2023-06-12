package com.stid.project.fido2server.app.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public abstract class RestException extends RuntimeException {
    @Getter
    private Object[] params;

    public RestException() {
        super();
    }

    public RestException(String message) {
        super(message);
    }

    public RestException(String message, Throwable cause) {
        super(message, cause);
    }

    public RestException(Throwable cause) {
        super(cause);
    }

    protected RestException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public RestException withParams(Object... params) {
        this.params = params;
        return this;
    }

    public HttpStatus getHttpStatus() {
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
