package com.stid.project.fido2server.app.service;

import com.stid.project.fido2server.app.domain.constant.Status;
import com.stid.project.fido2server.app.domain.entity.Authenticator;
import com.stid.project.fido2server.app.domain.entity.RelyingParty;
import com.stid.project.fido2server.app.domain.entity.UserAccount;
import com.stid.project.fido2server.app.domain.model.SystemUsageDto;
import com.stid.project.fido2server.app.exception.AbstractExceptionHandler;
import com.stid.project.fido2server.app.repository.AuthenticatorRepository;
import com.stid.project.fido2server.app.repository.PackageRepository;
import com.stid.project.fido2server.app.repository.RelyingPartyRepository;
import com.stid.project.fido2server.app.repository.UserAccountRepository;
import com.stid.project.fido2server.app.security.AccessToken;
import com.stid.project.fido2server.app.security.JwtTokenProvider;
import com.stid.project.fido2server.app.security.JwtTokenScope;
import com.stid.project.fido2server.app.util.HelperUtil;
import com.stid.project.fido2server.app.web.form.RelyingPartyCreateForm;
import com.webauthn4j.data.client.Origin;
import com.webauthn4j.validator.exception.BadOriginException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
public class SystemService extends AbstractExceptionHandler {
    private final RelyingPartyRepository relyingPartyRepository;
    private final UserAccountRepository userAccountRepository;
    private final AuthenticatorRepository authenticatorRepository;
    private final PackageRepository packageRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public SystemService(RelyingPartyRepository relyingPartyRepository, UserAccountRepository userAccountRepository, AuthenticatorRepository authenticatorRepository, PackageRepository packageRepository, JwtTokenProvider jwtTokenProvider) {
        this.relyingPartyRepository = relyingPartyRepository;
        this.userAccountRepository = userAccountRepository;
        this.authenticatorRepository = authenticatorRepository;
        this.packageRepository = packageRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public SystemUsageDto getSystemUsage() {
        long relyingParties = relyingPartyRepository.count();
        long userAccounts = userAccountRepository.count();
        long userAuthenticators = authenticatorRepository.count();
        long createdPackages = packageRepository.count();
        long activatedPackages = packageRepository.countByActivatedDateNotNull();
        return new SystemUsageDto(relyingParties, userAccounts, userAuthenticators, createdPackages, activatedPackages);
    }

    @Transactional
    public RelyingParty createRelyingParty(RelyingPartyCreateForm form) {
        //URI uri = URI.create(form.getUrl());
        String name = form.getName();

        Origin origin = form.getOrigin();
        if (origin.getHost() == null || !"https".equals(origin.getScheme()))
            throw new BadOriginException("'origin' must be a valid URL and start with 'https://'");

        String secret = HelperUtil.randomSecret();

        RelyingParty relyingParty = new RelyingParty();
        relyingParty.setSecret(secret);
        relyingParty.setName(name);
        relyingParty.setOrigin(origin);
        relyingParty.setDescription(form.getDescription());

        relyingParty = relyingPartyRepository.save(relyingParty);
        return relyingParty;
    }

    @Transactional
    public RelyingParty updateRelyingPartyStatus(UUID id, Status status) {
        RelyingParty relyingParty = relyingPartyRepository.findById(id)
                .orElseThrow(() -> notFound("Exception.RelyingPartyNotFound"));
        relyingParty.setStatus(status);
        relyingPartyRepository.save(relyingParty);
        return relyingParty;
    }

    @Transactional
    public RelyingParty deleteRelyingParty(UUID id) {
        RelyingParty relyingParty = relyingPartyRepository.findById(id)
                .orElseThrow(() -> notFound("Exception.RelyingPartyNotFound"));

        if (relyingParty.getStatus() == Status.ACTIVE)
            throw badRequest("Exception.RelyingPartyStatusActive");

        relyingPartyRepository.delete(relyingParty);
        return relyingParty;
    }

    public AccessToken generateRelyingPartyAccessToken(UUID id, int days) {
        RelyingParty relyingParty = relyingPartyRepository.findById(id)
                .orElseThrow(() -> notFound("Exception.RelyingPartyNotFound"));

        if (relyingParty.getStatus() != Status.ACTIVE)
            throw badRequest("Exception.RelyingPartyStatusNotActive");

        return jwtTokenProvider.createToken(relyingParty.getId().toString(), JwtTokenScope.ADMIN, Duration.ofDays(days));
    }

    public List<RelyingParty> findAllRelyingParty() {
        return relyingPartyRepository.findAll();
    }

    public List<UserAccount> findAllUserAccount(UUID relyingPartyId) {
        return userAccountRepository.findByRelyingPartyId(relyingPartyId);
    }

    public List<Authenticator> findAllUserAuthenticator(UUID userId) {
        return authenticatorRepository.findByUserId(userId);
    }
}
