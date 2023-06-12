package com.stid.project.fido2server.app.web.controller;

import com.stid.project.fido2server.app.domain.constant.EventName;
import com.stid.project.fido2server.app.domain.constant.EventType;
import com.stid.project.fido2server.app.domain.entity.Authenticator;
import com.stid.project.fido2server.app.domain.entity.UserAccount;
import com.stid.project.fido2server.app.domain.model.*;
import com.stid.project.fido2server.app.security.CurrentSpringUser;
import com.stid.project.fido2server.app.security.JwtTokenScope;
import com.stid.project.fido2server.app.security.SpringUser;
import com.stid.project.fido2server.app.service.EndpointService;
import com.stid.project.fido2server.app.service.EventService;
import com.stid.project.fido2server.app.service.Fido2Service;
import com.stid.project.fido2server.app.util.TimeUtils;
import com.stid.project.fido2server.app.web.form.RelyingPartyUpdateForm;
import com.stid.project.fido2server.app.web.form.UserAccountCreateForm;
import com.stid.project.fido2server.app.web.form.UserAuthenticatorUpdateForm;
import com.stid.project.fido2server.fido2.model.*;
import com.webauthn4j.converter.util.JsonConverter;
import com.webauthn4j.converter.util.ObjectConverter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Tag(name = "ENDPOINT")
@RestController
@RequestMapping("/api/endpoint")
@Secured(value = JwtTokenScope.ROLE_ADMIN)
public class EndpointController extends AbstractSecuredController {
    private final EndpointService endpointService;
    private final Fido2Service fido2Service;
    private final EventService eventService;
    private final JsonConverter jsonConverter;

    public EndpointController(EndpointService endpointService, Fido2Service fido2Service, EventService eventService, ObjectConverter objectConverter) {
        this.endpointService = endpointService;
        this.fido2Service = fido2Service;
        this.eventService = eventService;
        this.jsonConverter = objectConverter.getJsonConverter();
    }

    @Operation(summary = "Check the FIDO2-Server connection status and expiration")
    @GetMapping("/status")
    public ResponseEntity<EndpointStatus> getStatus(@CurrentSpringUser SpringUser springUser) {
        endpointService.checkLicenseTime(springUser.getRelyingParty().getId());
        return ResponseEntity.ok(new EndpointStatus());
    }

    @Operation(summary = "Get service information")
    @GetMapping("/info")
    public ResponseEntity<RelyingPartyDto> getInfo(@CurrentSpringUser SpringUser springUser) {
        return ResponseEntity.ok(mapperService.mapping(springUser.getRelyingParty()));
    }

    @Operation(summary = "Update service information")
    @PutMapping("/info")
    public ResponseEntity<RelyingPartyDto> updateInfo(
            @RequestBody @Valid RelyingPartyUpdateForm form, @CurrentSpringUser SpringUser springUser) {
        endpointService.updateInfo(form, springUser.getRelyingParty());
        return ResponseEntity.ok(mapperService.mapping(springUser.getRelyingParty()));
    }

    @Operation(summary = "Get service license: user, subdomain, port, time")
    @GetMapping("/license")
    public ResponseEntity<ServiceLicenseDto> getServiceLicense(@CurrentSpringUser SpringUser springUser) {
        ServiceLicenseDto serviceLicense = endpointService.calculateServiceLicense(springUser.getRelyingParty().getId());
        return ResponseEntity.ok(serviceLicense);
    }

    @Operation(summary = "Get all users")
    @GetMapping("/users")
    public ResponseEntity<List<UserAccountDto>> getAllUserAccount(@CurrentSpringUser SpringUser springUser) {
        List<UserAccount> userAccounts = endpointService.findAllUserAccount(springUser.getRelyingParty().getId());
        return ResponseEntity.ok(userAccounts.stream()
                .map(mapperService::mapping)
                .collect(Collectors.toList()));
    }

    @Operation(summary = "Get user by user id")
    @GetMapping("/users/{id}")
    public ResponseEntity<UserAccountDto> getUserAccount(
            @PathVariable UUID id, @CurrentSpringUser SpringUser springUser) {
        Optional<UserAccount> userAccount = endpointService.findUserAccountById(id, springUser.getRelyingParty().getId());
        return userAccount
                .map(mapperService::mapping)
                .map(ResponseEntity::ok).orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @Operation(summary = "Create new user")
    @PostMapping("/users")
    public ResponseEntity<UserAccountDto> createUserAccount(
            @RequestBody @Valid UserAccountCreateForm form, @CurrentSpringUser SpringUser springUser) {
        UserAccount userAccount = endpointService.createUserAccount(form, springUser.getRelyingParty().getId());
        return ResponseEntity.ok(mapperService.mapping(userAccount));
    }

    @Operation(summary = "Delete user by user id")
    @DeleteMapping("/users/{id}")
    public ResponseEntity<UserAccountDto> deleteUserAccount(
            @PathVariable UUID id, @CurrentSpringUser SpringUser springUser) {
        UserAccount userAccount = endpointService.deleteUserAccount(id, springUser.getRelyingParty().getId());
        return ResponseEntity.ok(mapperService.mapping(userAccount));
    }

    @Operation(summary = "Get all user's authenticators")
    @GetMapping("/users/{id}/authenticators")
    public ResponseEntity<List<AuthenticatorDto>> getAllUserAuthenticator(
            @PathVariable UUID id,
            @CurrentSpringUser SpringUser springUser) {
        List<Authenticator> userAuthenticators = endpointService
                .findAllUserAuthenticator(id, springUser.getRelyingParty().getId());
        return ResponseEntity.ok(userAuthenticators.stream()
                .map(mapperService::mapping)
                .collect(Collectors.toList()));
    }

    @Operation(summary = "Update user's authenticator by user id and authenticator id")
    @PutMapping("/users/{id}/authenticators/{authenticatorId}")
    public ResponseEntity<AuthenticatorDto> updateUserAuthenticator(
            @PathVariable UUID id, @PathVariable UUID authenticatorId,
            @RequestBody @Valid UserAuthenticatorUpdateForm form,
            @CurrentSpringUser SpringUser springUser) {
        Authenticator userAuthenticator = endpointService
                .updateUserAuthenticator(authenticatorId, id, form, springUser.getRelyingParty().getId());
        return ResponseEntity.ok(mapperService.mapping(userAuthenticator));
    }

    @Operation(summary = "Delete user's authenticator by user id and authenticator id")
    @DeleteMapping("/users/{id}/authenticators/{authenticatorId}")
    public ResponseEntity<AuthenticatorDto> deleteUserAuthenticator(
            @PathVariable UUID id, @PathVariable UUID authenticatorId,
            @CurrentSpringUser SpringUser springUser) {
        eventService.logEvent(springUser.getRelyingParty().getId(), id,
                EventName.RELYING_PARTY_DELETE_AUTHENTICATOR, EventType.DELETE, authenticatorId.toString());
        Authenticator userAuthenticator = endpointService
                .deleteUserAuthenticator(authenticatorId, id, springUser.getRelyingParty().getId());
        eventService.saveEvent();
        return ResponseEntity.ok(mapperService.mapping(userAuthenticator));
    }

    //TODO: FIDO2
    @Operation(summary = "FIDO Attestation: Start register authenticator by username")
    @PostMapping("/attestation/options")
    public ResponseEntity<AttestationOptionsResponse> attestationOptions(
            @RequestBody @Valid AttestationOptionsRequest request,
            @CurrentSpringUser SpringUser springUser) {
        eventService.logEvent(
                springUser.getRelyingParty().getId(),
                EventName.RELYING_PARTY_ATTESTATION_OPTIONS,
                EventType.AUTHENTICATION,
                jsonConverter.writeValueAsString(request));
        AttestationOptionsResponse attestationOptions = fido2Service.attestationOptions(request, springUser.getRelyingParty());
        eventService.saveEvent();
        return ResponseEntity.ok(attestationOptions);
    }

    @Operation(summary = "FIDO Attestation: Finish register authenticator by username")
    @PostMapping("/attestation/result")
    public ResponseEntity<AttestationResultResponse> attestationResult(
            @RequestBody @Valid AttestationResultRequest request,
            @CurrentSpringUser SpringUser springUser) {
        eventService.logEvent(
                springUser.getRelyingParty().getId(),
                EventName.RELYING_PARTY_ATTESTATION_RESULT,
                EventType.AUTHENTICATION,
                jsonConverter.writeValueAsString(request));
        AttestationResultResponse attestationResult = fido2Service.attestationResult(request.toCredential(), springUser.getRelyingParty());
        eventService.logEvent(
                springUser.getRelyingParty().getId(),
                attestationResult.getAuthenticator().getUserId(),
                EventName.RELYING_PARTY_ATTESTATION_RESULT,
                EventType.AUTHENTICATION,
                jsonConverter.writeValueAsString(mapperService.mapping(attestationResult.getAuthenticator())));
        eventService.saveEvent();
        return ResponseEntity.ok(attestationResult);
    }

    @Operation(summary = "FIDO Assertion: Start authentication by registered username and authenticator")
    @PostMapping("/assertion/options")
    public ResponseEntity<AssertionOptionsResponse> assertionOptions(
            @RequestBody @Valid AssertionOptionsRequest request,
            @CurrentSpringUser SpringUser springUser) {
        eventService.logEvent(
                springUser.getRelyingParty().getId(),
                EventName.RELYING_PARTY_ASSERTION_OPTIONS,
                EventType.AUTHENTICATION,
                jsonConverter.writeValueAsString(request));
        AssertionOptionsResponse assertionOptions = fido2Service.assertionOptions(request, springUser.getRelyingParty());
        eventService.saveEvent();
        return ResponseEntity.ok(assertionOptions);
    }

    @Operation(summary = "FIDO Assertion: Finish authentication by registered username and authenticator")
    @PostMapping("/assertion/result")
    public ResponseEntity<AssertionResultResponse> assertionResult(
            @RequestBody @Valid AssertionResultRequest request,
            @CurrentSpringUser SpringUser springUser) {
        eventService.logEvent(
                springUser.getRelyingParty().getId(),
                EventName.RELYING_PARTY_ASSERTION_RESULT,
                EventType.AUTHENTICATION,
                jsonConverter.writeValueAsString(request));
        AssertionResultResponse assertionResult = fido2Service.assertionResult(request.toCredential(), springUser.getRelyingParty());
        eventService.logEvent(springUser.getRelyingParty().getId(),
                assertionResult.getAuthenticator().getUserId(), assertionResult.getAuthenticator().getId(),
                EventName.RELYING_PARTY_ASSERTION_RESULT,
                EventType.AUTHENTICATION,
                jsonConverter.writeValueAsString(mapperService.mapping(assertionResult.getAuthenticator())));
        eventService.saveEvent();
        return ResponseEntity.ok(assertionResult);
    }

    @Operation(summary = "Get all service log events")
    @GetMapping("/events")
    public ResponseEntity<List<EventDto>> getLogEvent(
            @RequestParam(required = false) Long start,
            @RequestParam(required = false) Long end,
            @CurrentSpringUser SpringUser springUser) {
        Instant queryEnd = (end == null || end <= 0)
                ? TimeUtils.endOfDay(Instant.now())
                : TimeUtils.endOfDay(end);

        Instant queryStart = (start == null || start > queryEnd.toEpochMilli())
                ? TimeUtils.startOfDay(queryEnd)
                : TimeUtils.startOfDay(start);

        return ResponseEntity.ok(eventService
                .findAllByRelyingParty(springUser.getRelyingParty().getId(), queryStart, queryEnd)
                .stream()
                .map(mapperService::mapping)
                .collect(Collectors.toList()));
    }

    @Operation(summary = "Get all service log events by event name")
    @GetMapping("/events/name/{eventName}")
    public ResponseEntity<List<EventDto>> getLogEventByName(
            @PathVariable EventName eventName,
            @RequestParam(required = false) Long start,
            @RequestParam(required = false) Long end,
            @CurrentSpringUser SpringUser springUser) {
        Instant queryEnd = (end == null || end <= 0)
                ? TimeUtils.endOfDay(Instant.now())
                : TimeUtils.endOfDay(end);

        Instant queryStart = (start == null || start > queryEnd.toEpochMilli())
                ? TimeUtils.startOfDay(queryEnd)
                : TimeUtils.startOfDay(start);

        return ResponseEntity.ok(eventService
                .findAllByRelyingParty(springUser.getRelyingParty().getId(), eventName, queryStart, queryEnd)
                .stream()
                .map(mapperService::mapping)
                .collect(Collectors.toList()));
    }

    @Operation(summary = "Get all service log events by event type")
    @GetMapping("/events/type/{eventType}")
    public ResponseEntity<List<EventDto>> getLogEventByType(
            @PathVariable EventType eventType,
            @RequestParam(required = false) Long start,
            @RequestParam(required = false) Long end,
            @CurrentSpringUser SpringUser springUser) {
        Instant queryEnd = (end == null || end <= 0)
                ? TimeUtils.endOfDay(Instant.now())
                : TimeUtils.endOfDay(end);

        Instant queryStart = (start == null || start > queryEnd.toEpochMilli())
                ? TimeUtils.startOfDay(queryEnd)
                : TimeUtils.startOfDay(start);

        return ResponseEntity.ok(eventService
                .findAllByRelyingParty(springUser.getRelyingParty().getId(), eventType, queryStart, queryEnd)
                .stream()
                .map(mapperService::mapping)
                .collect(Collectors.toList()));
    }

    @Operation(summary = "Get all service log events by user id")
    @GetMapping("/events/user/{userId}")
    public ResponseEntity<List<EventDto>> getLogEventByUser(
            @PathVariable UUID userId,
            @RequestParam(required = false) Long start,
            @RequestParam(required = false) Long end,
            @CurrentSpringUser SpringUser springUser) {
        Instant queryEnd = (end == null || end <= 0)
                ? TimeUtils.endOfDay(Instant.now())
                : TimeUtils.endOfDay(end);

        Instant queryStart = (start == null || start > queryEnd.toEpochMilli())
                ? TimeUtils.startOfDay(queryEnd)
                : TimeUtils.startOfDay(start);

        return ResponseEntity.ok(eventService
                .findAllByRelyingParty(springUser.getRelyingParty().getId(), userId, queryStart, queryEnd)
                .stream()
                .map(mapperService::mapping)
                .collect(Collectors.toList()));
    }

    public static class EndpointStatus extends ServerResponseBase {
        public EndpointStatus() {
        }
    }
}
