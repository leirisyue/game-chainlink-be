package com.stid.project.fido2server.app.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractExceptionHandler {
    public final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

//    @Autowired
//    protected MessageSource messageSource;

    public RestException unauthorized(String message, Object... params) {
        return unauthorized(message, null, params);
    }

    public RestException unauthorized(String message, Throwable throwable, Object... params) {
        return new UnauthorizedException(message, throwable).withParams(params);
    }

    public RestException badRequest(String message, Object... params) {
        return badRequest(message, null, params);
    }

    public RestException badRequest(String message, Throwable throwable, Object... params) {
        return new BadRequestException(message, throwable).withParams(params);
    }

    public RestException internalServerError(String message, Object... params) {
        return internalServerError(message, null, params);
    }

    public RestException internalServerError(String message, Throwable throwable, Object... params) {
        return new InternalServerException(message, throwable).withParams(params);
    }

    public RestException notFound(String message, Object... params) {
        return notFound(message, null, params);
    }

    public RestException notFound(String message, Throwable throwable, Object... params) {
        return new NotFoundException(message, throwable).withParams(params);
    }

    public RestException forbidden(String message, Object... params) {
        return forbidden(message, null, params);
    }

    public RestException forbidden(String message, Throwable throwable, Object... params) {
        return new ForbiddenException(message, throwable).withParams(params);
    }

}
