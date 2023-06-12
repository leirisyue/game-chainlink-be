package com.stid.project.fido2server.app.web.controller;

import com.stid.project.fido2server.app.domain.constant.EventStatus;
import com.stid.project.fido2server.app.domain.entity.RelyingParty;
import com.stid.project.fido2server.app.service.EventService;
import com.stid.project.fido2server.app.service.Fido2Service;
import com.stid.project.fido2server.fido2.model.*;
import com.stid.project.fido2server.fido2.util.ServletUtil;
import com.webauthn4j.converter.util.ObjectConverter;
import com.webauthn4j.data.client.Origin;
import com.webauthn4j.util.exception.WebAuthnException;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

@Tag(name = "WebAuthn")
@RestController
@ControllerAdvice
@RequestMapping("/webauthn")
public class WebAuthnController extends AbstractUnsecuredController {
    private final Fido2Service fido2Service;
    private final ObjectConverter objectConverter;
    private final MessageSource messageSource;
    private final EventService eventService;

    public WebAuthnController(Fido2Service fido2Service, ObjectConverter objectConverter, MessageSource messageSource, EventService eventService) {
        this.fido2Service = fido2Service;
        this.objectConverter = objectConverter;
        this.messageSource = messageSource;
        this.eventService = eventService;
    }

    @PostMapping("/attestation/options")
    public ResponseEntity<AttestationOptionsResponse> webAuthnAttestationOptions(
            HttpServletRequest httpServletRequest,
            @RequestBody @Valid AttestationOptionsRequest request) {
        LOGGER.debug(">>> AttestationOptionsRequest: {}", objectConverter.getJsonConverter().writeValueAsString(request));
        AttestationOptionsResponse attestationOptions = fido2Service
                .attestationOptions(request, getMds3TestRelyingParty(httpServletRequest));
        LOGGER.debug("<<< AttestationOptionsResponse: {}", objectConverter.getJsonConverter().writeValueAsString(attestationOptions));
        return ResponseEntity.ok(attestationOptions);
    }

    @PostMapping("/attestation/result")
    public ResponseEntity<AttestationResultResponse> webAuthnAttestationResult(
            HttpServletRequest httpServletRequest,
            @RequestBody @Valid AttestationResultRequest request) {
        LOGGER.debug(">>> AttestationResultRequest: {}", objectConverter.getJsonConverter().writeValueAsString(request));
        AttestationResultResponse attestationResult = fido2Service
                .attestationResult(request.toCredential(), getMds3TestRelyingParty(httpServletRequest));
        LOGGER.debug("<<< AttestationResultResponse: {}", objectConverter.getJsonConverter().writeValueAsString(attestationResult));
        return ResponseEntity.ok(attestationResult);
    }

    @PostMapping("/assertion/options")
    public ResponseEntity<AssertionOptionsResponse> webAuthnAssertionOptions(
            HttpServletRequest httpServletRequest,
            @RequestBody @Valid AssertionOptionsRequest request) {
        LOGGER.debug(">>> AssertionOptionsRequest: {}", objectConverter.getJsonConverter().writeValueAsString(request));
        AssertionOptionsResponse assertionOptions = fido2Service
                .assertionOptions(request, getMds3TestRelyingParty(httpServletRequest));
        LOGGER.debug("<<< AssertionOptionsResponse: {}", objectConverter.getJsonConverter().writeValueAsString(assertionOptions));
        return ResponseEntity.ok(assertionOptions);
    }

    @PostMapping("/assertion/result")
    public ResponseEntity<AssertionResultResponse> webAuthnAssertionResult(
            HttpServletRequest httpServletRequest,
            @RequestBody @Valid AssertionResultRequest request) {
        LOGGER.debug(">>> AssertionResultRequest: {}", objectConverter.getJsonConverter().writeValueAsString(request));
        AssertionResultResponse assertionResult = fido2Service
                .assertionResult(request.toCredential(), getMds3TestRelyingParty(httpServletRequest));
        LOGGER.debug("<<< AssertionResultResponse: {}", objectConverter.getJsonConverter().writeValueAsString(assertionResult));
        return ResponseEntity.ok(assertionResult);
    }

    @ExceptionHandler(WebAuthnException.class)
    public ResponseEntity<Object> handleWebAuthnException(WebAuthnException ex, Locale locale) {
        String errorMessage = messageSource.getMessage(ex.getMessage(), null, locale);
        eventService.saveEvent(EventStatus.FAILURE, errorMessage);
        ErrorResponse errorResponse = new ErrorResponse(errorMessage);
        LOGGER.debug("<<< WebAuthnException: {}", objectConverter.getJsonConverter().writeValueAsString(errorResponse));
        return ResponseEntity.badRequest().body(errorResponse);
    }

    private RelyingParty getMds3TestRelyingParty(HttpServletRequest request) {
        Origin origin = ServletUtil.getOrigin(request);
        RelyingParty relyingParty = new RelyingParty();
        relyingParty.setName("Mds3TestRelyingParty");
        relyingParty.setOrigin(origin);
        return relyingParty;
    }
}
