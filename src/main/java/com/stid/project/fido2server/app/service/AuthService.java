package com.stid.project.fido2server.app.service;

import com.stid.project.fido2server.app.domain.constant.Status;
import com.stid.project.fido2server.app.domain.entity.RelyingParty;
import com.stid.project.fido2server.app.domain.entity.UserAccount;
import com.stid.project.fido2server.app.exception.AbstractExceptionHandler;
import com.stid.project.fido2server.app.repository.RelyingPartyRepository;
import com.stid.project.fido2server.app.repository.UserAccountRepository;
import com.stid.project.fido2server.app.security.AccessToken;
import com.stid.project.fido2server.app.security.JwtTokenProvider;
import com.stid.project.fido2server.app.security.JwtTokenScope;
import com.stid.project.fido2server.app.web.form.CustomLoginForm;
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
    private final UserAccountRepository userAccountRepository;

    public AuthService(JwtTokenProvider jwtTokenProvider, AuthenticationProvider authenticationProvider, RelyingPartyRepository relyingPartyRepository, UserAccountRepository userAccountRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationProvider = authenticationProvider;
        this.relyingPartyRepository = relyingPartyRepository;
        this.userAccountRepository = userAccountRepository;
    }

    public AccessToken generateSystemAccessToken(SystemLoginForm form) {
        Duration duration = form.isRemember() ? Duration.ofDays(1) : Duration.ofHours(1);
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

    public AccessToken generateCustomerAccessToken(CustomLoginForm form) {
        UserAccount userAccount = userAccountRepository.findByUserLogin(form.getUsername())
                .orElseThrow(() -> unauthorized("Exception.RelyingPartyNotFound"));

        if (!Objects.equals(userAccount.getUserLogin(), form.getUsername()))
            throw unauthorized("Exception.CustomUsernameNotMatch");

        if (!Objects.equals(userAccount.getPasswordHash(), form.getPassword()))
            throw unauthorized("Exception.CustomPasswordNotMatch");

        if (!Objects.equals(userAccount.getRelyingPartyId(), form.getRelyingPartyId()))
            throw unauthorized("Exception.CustomRelyingPartyNotMatch");

        Duration duration = form.getTimeoutInSeconds() > 0 ? Duration.ofSeconds(form.getTimeoutInSeconds()) : Duration.ofHours(1);
        return jwtTokenProvider.createToken(userAccount.getId().toString(), JwtTokenScope.USER, duration);
    }


}
