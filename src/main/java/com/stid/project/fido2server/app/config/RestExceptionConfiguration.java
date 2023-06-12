package com.stid.project.fido2server.app.config;

import com.stid.project.fido2server.app.domain.constant.EventStatus;
import com.stid.project.fido2server.app.exception.RestException;
import com.stid.project.fido2server.app.service.EventService;
import com.stid.project.fido2server.fido2.model.ErrorResponse;
import com.webauthn4j.converter.util.ObjectConverter;
import com.webauthn4j.util.exception.WebAuthnException;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.io.IOException;
import java.util.Locale;

@Configuration
@ControllerAdvice
public class RestExceptionConfiguration extends ResponseEntityExceptionHandler implements AuthenticationEntryPoint, AccessDeniedHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestExceptionConfiguration.class);

    private final ObjectConverter objectConverter;
    private final MessageSource messageSource;
    private final EventService eventService;

    public RestExceptionConfiguration(ObjectConverter objectConverter, MessageSource messageSource, EventService eventService) {
        this.objectConverter = objectConverter;
        this.messageSource = messageSource;
        this.eventService = eventService;
    }

    @ExceptionHandler(WebAuthnException.class)
    public ResponseEntity<Object> handleWebAuthnException(WebAuthnException ex, Locale locale) {
        String errorMessage = messageSource.getMessage(ex.getMessage(), null, locale);
        eventService.saveEvent(EventStatus.FAILURE, errorMessage);
        return ResponseEntity.badRequest().body(new ErrorResponse(errorMessage));
    }

    @ExceptionHandler(RestException.class)
    public ResponseEntity<Object> handleRestException(RestException ex, Locale locale) {
        String errorMessage = messageSource.getMessage(ex.getMessage(), null, locale);
        eventService.saveEvent(EventStatus.FAILURE, errorMessage);
        return ResponseEntity.status(ex.getHttpStatus()).body(new ErrorResponse(errorMessage));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleException(Exception ex, Locale locale) {
        String errorMessage = messageSource.getMessage(ex.getMessage(), null, locale);
        eventService.saveEvent(EventStatus.FAILURE, errorMessage);
        HttpStatus httpStatus;
        if (ex instanceof AccessDeniedException)
            httpStatus = HttpStatus.FORBIDDEN;
        else if (ex instanceof AuthenticationException)
            httpStatus = HttpStatus.UNAUTHORIZED;
        else
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(httpStatus).body(new ErrorResponse(errorMessage));
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), headers, status);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), headers, status);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), headers, status);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        StringBuilder stringBuilder = new StringBuilder();
        ex.getBindingResult().getFieldErrors().forEach(fieldError -> {
            stringBuilder.append(String.format("'%s' %s\n", fieldError.getField(), fieldError.getDefaultMessage()));
        });
        String errorMessage = stringBuilder.toString().trim();
        return new ResponseEntity<>(new ErrorResponse(errorMessage), headers, status);
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        String message = authException.getMessage();
        LOGGER.error("AuthenticationException: {}, method: {}, error: {}", request.getRequestURL(), request.getMethod(), message);
        //response.sendError(HttpServletResponse.SC_UNAUTHORIZED, message);

        ErrorResponse errorResponse = new ErrorResponse(message);
        String errorResponseText = objectConverter.getJsonConverter().writeValueAsString(errorResponse);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().print(errorResponseText);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        String message = accessDeniedException.getMessage();
        LOGGER.error("AccessDeniedException: {}, method: {}, error: {}", request.getRequestURL(), request.getMethod(), message);
        //response.sendError(HttpServletResponse.SC_FORBIDDEN, message);

        ErrorResponse errorResponse = new ErrorResponse(message);
        String errorResponseText = objectConverter.getJsonConverter().writeValueAsString(errorResponse);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().print(errorResponseText);
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    }

    @PostConstruct
    protected void onPostConstruct() {
        LOGGER.debug("Initialized AuthorizationEntryPoint");
    }
}
