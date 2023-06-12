package com.stid.project.fido2server.app.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

import java.util.Locale;

public abstract class AbstractExceptionHandler {
    public final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    protected MessageSource messageSource;

    public UnauthorizedException unauthorized(String message, Object... params) {
        return unauthorized(message, null, params);
    }

    public UnauthorizedException unauthorized(String message, Throwable throwable, Object... params) {
        return new UnauthorizedException(messageSource.getMessage(message, params, Locale.ENGLISH), throwable);
    }

    public BadRequestException badRequest(String message, Object... params) {
        return badRequest(message, null, params);
    }

    public BadRequestException badRequest(String message, Throwable throwable, Object... params) {
        return new BadRequestException(messageSource.getMessage(message, params, Locale.ENGLISH), throwable);
    }

    public InternalServerException internalServerError(String message, Object... params) {
        return internalServerError(message, null, params);
    }

    public InternalServerException internalServerError(String message, Throwable throwable, Object... params) {
        return new InternalServerException(messageSource.getMessage(message, params, Locale.ENGLISH), throwable);
    }

    public NotFoundException notFound(String message, Object... params) {
        return notFound(message, null, params);
    }

    public NotFoundException notFound(String message, Throwable throwable, Object... params) {
        return new NotFoundException(messageSource.getMessage(message, params, Locale.ENGLISH), throwable);
    }

    public ForbiddenException forbidden(String message, Object... params) {
        return forbidden(message, null, params);
    }

    public ForbiddenException forbidden(String message, Throwable throwable, Object... params) {
        return new ForbiddenException(messageSource.getMessage(message, params, Locale.ENGLISH), throwable);
    }

}
