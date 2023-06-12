package com.stid.project.fido2server.app.web.controller;

import com.stid.project.fido2server.app.domain.constant.EventName;
import com.stid.project.fido2server.app.domain.constant.EventType;
import com.stid.project.fido2server.app.security.AccessToken;
import com.stid.project.fido2server.app.service.AuthService;
import com.stid.project.fido2server.app.service.EventService;
import com.stid.project.fido2server.app.web.form.EndpointLoginForm;
import com.stid.project.fido2server.app.web.form.SystemLoginForm;
import com.webauthn4j.converter.util.JsonConverter;
import com.webauthn4j.converter.util.ObjectConverter;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController extends AbstractUnsecuredController {

    private final AuthService authService;
    private final EventService eventService;
    private final JsonConverter jsonConverter;

    public AuthController(AuthService authService, EventService eventService, ObjectConverter objectConverter) {
        this.authService = authService;
        this.eventService = eventService;
        this.jsonConverter = objectConverter.getJsonConverter();
    }

    @Operation(tags = "SYSTEM", summary = "Đăng nhập quản lý hệ thống bằng tài khoản system")
    @PostMapping("/system/access-token")
    public ResponseEntity<AccessToken> generateSystemAccessToken(
            @RequestBody @Valid SystemLoginForm form) {
        LOGGER.debug("generateSystemAccessToken...");
        AccessToken accessToken = authService.generateSystemAccessToken(form);
        return ResponseEntity.ok(accessToken);
    }

    @Operation(tags = "ENDPOINT", summary = "Login to FIDO2 Server by ClientId and ClientSecret")
    @PostMapping("/endpoint/access-token")
    public ResponseEntity<AccessToken> generateEndpointAccessToken(
            @RequestBody @Valid EndpointLoginForm form) {
        LOGGER.debug("generateEndpointAccessToken...");
        try {
            AccessToken accessToken = authService.generateEndpointAccessToken(form);

            eventService.logEvent(UUID.fromString(accessToken.getSubject()),
                    EventName.RELYING_PARTY_GET_ACCESS_TOKEN, EventType.AUTHENTICATION, jsonConverter.writeValueAsString(accessToken));

            return ResponseEntity.ok(accessToken);
        } finally {
            eventService.saveEvent();
        }
    }

}
