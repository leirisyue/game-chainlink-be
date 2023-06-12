package com.stid.project.fido2server.app.service;

import com.stid.project.fido2server.app.domain.entity.Authenticator;
import com.stid.project.fido2server.app.domain.entity.UserAccount;
import com.stid.project.fido2server.app.exception.AbstractExceptionHandler;
import com.stid.project.fido2server.app.repository.AuthenticatorRepository;
import com.stid.project.fido2server.app.repository.UserAccountRepository;
import com.stid.project.fido2server.app.util.HelperUtil;
import com.stid.project.fido2server.app.web.form.UserAccountCreateForm;
import com.stid.project.fido2server.app.web.form.UserAuthenticatorUpdateForm;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserAccountService extends AbstractExceptionHandler {
    private final PasswordEncoder passwordEncoder;
    private final UserAccountRepository userAccountRepository;
    private final AuthenticatorRepository authenticatorRepository;

    public UserAccountService(PasswordEncoder passwordEncoder, UserAccountRepository userAccountRepository, AuthenticatorRepository authenticatorRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userAccountRepository = userAccountRepository;
        this.authenticatorRepository = authenticatorRepository;
    }

    @Transactional
    public UserAccount createUserAccount(UserAccountCreateForm form, UUID relyingPartyId) {
        if (userAccountRepository.existsByUserLoginAndRelyingPartyId(form.getUsername(), relyingPartyId))
            throw badRequest("Exception.UserAccountExists");
        UserAccount userAccount = new UserAccount();
        userAccount.setUserHandle(HelperUtil.randomUserHandle());
        userAccount.setUserLogin(form.getUsername());
        userAccount.setDisplayName(form.getDisplayName());
        userAccount.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
        userAccount.setRelyingPartyId(relyingPartyId);
        userAccount = userAccountRepository.save(userAccount);
        return userAccount;
    }

    @Transactional
    public UserAccount deleteUserAccount(UUID id, UUID relyingPartyId) {
        UserAccount userAccount = userAccountRepository.findByIdAndRelyingPartyId(id, relyingPartyId)
                .orElseThrow(() -> badRequest("Exception.UserAccountNotFound"));
        userAccountRepository.delete(userAccount);
        return userAccount;
    }

    @Transactional
    public List<UserAccount> deleteUserAccount(UUID relyingPartyId) {
        List<UserAccount> userAccounts = userAccountRepository.findByRelyingPartyId(relyingPartyId);
        userAccountRepository.deleteAll(userAccounts);
        return userAccounts;
    }

    @Transactional
    public Authenticator updateUserAuthenticator(UUID userAuthenticatorId, UUID userAccountId, UserAuthenticatorUpdateForm form, UUID relyingPartyId) {
        UserAccount userAccount = userAccountRepository.findByIdAndRelyingPartyId(userAccountId, relyingPartyId)
                .orElseThrow(() -> badRequest("Exception.UserAccountNotFound"));
        Authenticator userAuthenticator = authenticatorRepository.findByIdAndUserId(userAuthenticatorId, userAccount.getId())
                .orElseThrow(() -> badRequest("Exception.UserAuthenticatorNotFound"));
        userAuthenticator.setName(form.getName());
        authenticatorRepository.save(userAuthenticator);
        return userAuthenticator;
    }

    @Transactional
    public Authenticator deleteUserAuthenticator(UUID userAuthenticatorId, UUID userAccountId, UUID relyingPartyId) {
        UserAccount userAccount = userAccountRepository.findByIdAndRelyingPartyId(userAccountId, relyingPartyId)
                .orElseThrow(() -> badRequest("Exception.UserAccountNotFound"));
        Authenticator userAuthenticator = authenticatorRepository.findByIdAndUserId(userAuthenticatorId, userAccount.getId())
                .orElseThrow(() -> badRequest("Exception.UserAuthenticatorNotFound"));
        authenticatorRepository.delete(userAuthenticator);
        return userAuthenticator;
    }

    public Optional<UserAccount> findUserAccount(UUID userAccountId, UUID relyingPartyId) {
        return userAccountRepository.findByIdAndRelyingPartyId(userAccountId, relyingPartyId);
    }

    public Optional<UserAccount> findUserAccount(String username, UUID relyingPartyId) {
        return userAccountRepository.findByUserLoginAndRelyingPartyId(username, relyingPartyId);
    }

    public List<UserAccount> findAllUserAccount(UUID relyingPartyId) {
        return userAccountRepository.findByRelyingPartyId(relyingPartyId);
    }

    public List<Authenticator> findAllUserAuthenticator(UUID userAccountId) {
        return authenticatorRepository.findByUserId(userAccountId);
    }

    public List<Authenticator> findAllUserAuthenticator(UUID userAccountId, UUID relyingPartyId) {
        UserAccount userAccount = userAccountRepository.findByIdAndRelyingPartyId(userAccountId, relyingPartyId)
                .orElseThrow(() -> badRequest("Exception.UserAccountNotFound"));
        return authenticatorRepository.findByUserId(userAccount.getId());
    }
}
