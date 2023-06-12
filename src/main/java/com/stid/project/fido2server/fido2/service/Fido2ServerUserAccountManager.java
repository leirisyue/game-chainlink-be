package com.stid.project.fido2server.fido2.service;

import com.stid.project.fido2server.app.domain.entity.Authenticator;
import com.stid.project.fido2server.app.domain.entity.UserAccount;
import com.stid.project.fido2server.app.service.PackageService;
import com.stid.project.fido2server.app.service.UserAccountService;
import com.stid.project.fido2server.app.web.form.UserAccountCreateForm;
import com.stid.project.fido2server.fido2.model.ServerPublicKeyCredentialDescriptor;
import com.stid.project.fido2server.fido2.model.ServerPublicKeyCredentialUserEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class Fido2ServerUserAccountManager {
    private final UserAccountService userAccountService;
    private final PackageService packageService;

    public Fido2ServerUserAccountManager(UserAccountService userAccountService, PackageService packageService) {
        this.userAccountService = userAccountService;
        this.packageService = packageService;
    }

    @Transactional
    public UserAccount createUserAccount(String username, String displayName, UUID relyingPartyId) {
        //NOTE: check package
        packageService.checkLicenseUser(relyingPartyId);

        //NOTE: create user
        return userAccountService.createUserAccount(UserAccountCreateForm.builder()
                .username(username)
                .displayName(displayName)
                .password(UUID.randomUUID().toString())
                .build(), relyingPartyId);
    }

    @Transactional
    public UserAccount findOrCreateNewUserAccount(String username, String displayName, UUID relyingPartyId) {
        return userAccountService
                .findUserAccount(username, relyingPartyId)
                .orElseGet(() -> createUserAccount(username, displayName, relyingPartyId));
    }

    public Optional<UserAccount> findUserAccount(String username, UUID relyingPartyId) {
        return userAccountService.findUserAccount(username, relyingPartyId);
    }

    public ServerPublicKeyCredentialUserEntity findCredentialUserEntity(String username, UUID relyingPartyId) {
        return userAccountService.findUserAccount(username, relyingPartyId)
                .map(UserAccount::toServerUserEntity).orElse(null);
    }

    public List<ServerPublicKeyCredentialDescriptor> findAllUserCredentialDescriptor(String username, UUID relyingPartyId) {
        List<ServerPublicKeyCredentialDescriptor> list = new ArrayList<>();
        userAccountService
                .findUserAccount(username, relyingPartyId)
                .ifPresent(userAccount -> {
                    list.addAll(userAccountService
                            .findAllUserAuthenticator(userAccount.getId())
                            .stream()
                            .map(Authenticator::toServerCredentialDescriptor)
                            .toList());
                });
        return list;
    }

    public List<ServerPublicKeyCredentialDescriptor> findAllUserCredentialDescriptor(UUID userAccountId) {
        return userAccountService.findAllUserAuthenticator(userAccountId)
                .stream()
                .map(Authenticator::toServerCredentialDescriptor)
                .toList();
    }

}
