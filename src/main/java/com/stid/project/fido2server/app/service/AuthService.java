package com.stid.project.fido2server.app.service;

import com.stid.project.fido2server.app.domain.constant.Status;
import com.stid.project.fido2server.app.domain.entity.RelyingParty;
import com.stid.project.fido2server.app.exception.AbstractExceptionHandler;
import com.stid.project.fido2server.app.repository.RelyingPartyRepository;
import com.stid.project.fido2server.app.security.AccessToken;
import com.stid.project.fido2server.app.security.JwtTokenProvider;
import com.stid.project.fido2server.app.security.JwtTokenScope;
import com.stid.project.fido2server.app.web.form.EndpointLoginForm;
import com.stid.project.fido2server.app.web.form.SystemLoginForm;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.Objects;

@Service
public class AuthService extends AbstractExceptionHandler {
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationProvider authenticationProvider;
    private final RelyingPartyRepository relyingPartyRepository;

    public AuthService(JwtTokenProvider jwtTokenProvider, AuthenticationProvider authenticationProvider, RelyingPartyRepository relyingPartyRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationProvider = authenticationProvider;
        this.relyingPartyRepository = relyingPartyRepository;
    }

    public AccessToken generateSystemAccessToken(SystemLoginForm form) {
        Duration duration = form.isRemember() ? Duration.ofHours(1) : Duration.ofMinutes(10);
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                form.getAccount(), form.getPassword(), Collections.emptySet());
        Authentication authentication = authenticationProvider.authenticate(authenticationToken);
        return jwtTokenProvider.createToken(authentication.getName(), JwtTokenScope.SYSTEM, duration);
    }

    public AccessToken generateEndpointAccessToken(EndpointLoginForm form) {
        RelyingParty relyingParty = relyingPartyRepository.findById(form.getClientId())
                .orElseThrow(() -> unauthorized("Exception.RelyingPartyNotFound"));

        if (!Objects.equals(relyingParty.getSecret(), form.getClientSecret()))
            throw unauthorized("Exception.RelyingPartySecretNotMatch");

        if (relyingParty.getStatus() != Status.ACTIVE)
            throw unauthorized("Exception.RelyingPartyStatusNotActive");

        Duration duration = form.getTimeoutInSeconds() > 0 ? Duration.ofSeconds(form.getTimeoutInSeconds()) : Duration.ofHours(1);
        return jwtTokenProvider.createToken(relyingParty.getId().toString(), JwtTokenScope.ADMIN, duration);
    }

}
