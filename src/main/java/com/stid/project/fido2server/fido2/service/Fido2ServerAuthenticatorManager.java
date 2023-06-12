package com.stid.project.fido2server.fido2.service;

import com.stid.project.fido2server.app.domain.entity.Authenticator;
import com.stid.project.fido2server.app.repository.AuthenticatorRepository;
import com.stid.project.fido2server.app.repository.UserAccountRepository;
import com.stid.project.fido2server.fido2.exception.BadCredentialIdException;
import com.webauthn4j.authenticator.AuthenticatorImpl;
import com.webauthn4j.util.Base64UrlUtil;
import com.webauthn4j.util.exception.WebAuthnException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class Fido2ServerAuthenticatorManager {
    protected final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    private final UserAccountRepository userAccountRepository;
    private final AuthenticatorRepository authenticatorRepository;

    public Fido2ServerAuthenticatorManager(UserAccountRepository userAccountRepository, AuthenticatorRepository authenticatorRepository) {
        this.userAccountRepository = userAccountRepository;
        this.authenticatorRepository = authenticatorRepository;
    }

    @Transactional
    public Authenticator createAuthenticator(AuthenticatorImpl authenticatorImpl, UUID userAccountId) {
        if (authenticatorExists(authenticatorImpl.getAttestedCredentialData().getCredentialId()))
            throw new BadCredentialIdException("credentialId already registered");

        Authenticator authenticator = new Authenticator();
        authenticator.setName("Authenticator");
        authenticator.setAttestedCredentialData(authenticatorImpl.getAttestedCredentialData());
        authenticator.setAttestationStatement(authenticatorImpl.getAttestationStatement());
        authenticator.setClientExtensions(authenticatorImpl.getClientExtensions());
        authenticator.setAuthenticatorExtensions(authenticatorImpl.getAuthenticatorExtensions());
        authenticator.setAuthenticatorTransports(authenticatorImpl.getTransports());
        authenticator.setCounter(authenticatorImpl.getCounter());
        authenticator.setUserId(userAccountId);
        authenticator = authenticatorRepository.save(authenticator);
        LOGGER.info("Created Authenticator: id={}, credentialId={}", authenticator.getId(), Base64UrlUtil.encodeToString(authenticator.getAttestedCredentialData().getCredentialId()));
        return authenticator;
    }

    @Transactional
    public void updateCounter(Authenticator authenticator, long counter) {
        authenticator.setCounter(counter);
        authenticator.setLastAccess(Instant.now());
        authenticatorRepository.save(authenticator);
        LOGGER.info("Updated Authenticator: id={}, counter={}", authenticator.getId(), authenticator.getCounter());
    }

    public boolean authenticatorExists(byte[] credentialId) {
        return authenticatorRepository.existsByCredentialId(credentialId);
    }

    public Authenticator loadAuthenticatorByCredentialId(byte[] credentialId) {
        return authenticatorRepository.findByCredentialId(credentialId)
                .orElseThrow(() -> new WebAuthnException("Exception.UserAuthenticatorNotFound"));
    }

    public List<Authenticator> loadAuthenticatorsByUserHandle(byte[] userHandle) {
        List<Authenticator> authenticators = new ArrayList<>();
        userAccountRepository.findByUserHandle(userHandle)
                .ifPresent(userAccount -> {
                    authenticators.addAll(authenticatorRepository.findByUserId(userAccount.getId()));
                });
        return authenticators;
    }
}
