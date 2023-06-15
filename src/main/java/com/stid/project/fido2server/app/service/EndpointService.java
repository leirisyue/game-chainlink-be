package com.stid.project.fido2server.app.service;

import com.stid.project.fido2server.app.domain.constant.EventName;
import com.stid.project.fido2server.app.domain.constant.EventType;
import com.stid.project.fido2server.app.domain.entity.Authenticator;
import com.stid.project.fido2server.app.domain.entity.Package;
import com.stid.project.fido2server.app.domain.entity.RelyingParty;
import com.stid.project.fido2server.app.domain.entity.UserAccount;
import com.stid.project.fido2server.app.domain.model.ServiceLicenseDto;
import com.stid.project.fido2server.app.exception.AbstractExceptionHandler;
import com.stid.project.fido2server.app.repository.RelyingPartyRepository;
import com.stid.project.fido2server.app.web.form.EndpointInfoUpdateForm;
import com.stid.project.fido2server.app.web.form.UserAccountCreateForm;
import com.stid.project.fido2server.app.web.form.UserAuthenticatorUpdateForm;
import com.webauthn4j.converter.util.JsonConverter;
import com.webauthn4j.converter.util.ObjectConverter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class EndpointService extends AbstractExceptionHandler {
    private final RelyingPartyRepository relyingPartyRepository;
    private final UserAccountService userAccountService;
    private final PackageService packageService;
    private final EventService eventService;
    private final JsonConverter jsonConverter;

    public EndpointService(ObjectConverter objectConverter, RelyingPartyRepository relyingPartyRepository, UserAccountService userAccountService, PackageService packageService, EventService eventService) {
        this.jsonConverter = objectConverter.getJsonConverter();
        this.relyingPartyRepository = relyingPartyRepository;
        this.userAccountService = userAccountService;
        this.packageService = packageService;
        this.eventService = eventService;
    }

    @Transactional
    public void updateInfo(EndpointInfoUpdateForm form, RelyingParty relyingParty) {
        //NOTE: check subdomain
        Set<RelyingParty.Subdomain> subdomains = form.getSubdomains().stream()
                .filter(p -> p != null && StringUtils.hasText(p.getSubdomain()))
                .collect(Collectors.toSet());

        Set<Integer> ports = form.getPorts().stream()
                .filter(p -> p != null && p > -1)
                .collect(Collectors.toSet());

        subdomains.forEach(p -> {
            if (!Pattern.matches("^[a-zA-Z0-9][a-zA-Z0-9\\-\\.]*[a-zA-Z0-9]$", p.getSubdomain()))
                throw badRequest("Exception.InvalidSubdomainPattern", p.getSubdomain());

            if (p.toOrigin(relyingParty.getOrigin()).getHost() == null)
                throw badRequest("Exception.InvalidSubdomainForOrigin", relyingParty.getRpId());
        });

        //NOTE: check package
        ServiceLicenseDto serviceLicense = packageService.calculateServiceLicense(relyingParty.getId());
        long updateSubdomains = subdomains.size();
        long availableSubdomains = serviceLicense.getSubdomain().getFreeQuantity() + relyingParty.getSubdomains().size();
        if (updateSubdomains > availableSubdomains)
            throw forbidden("Exception.SubdomainQuantityReachedLimit");


        long updatePorts = ports.size();
        long availablePorts = serviceLicense.getPort().getFreeQuantity() + relyingParty.getPorts().size();
        if (updatePorts > availablePorts)
            throw forbidden("Exception.PortQuantityReachedLimit");

        eventService.logEvent(relyingParty.getId(), EventName.RELYING_PARTY_UPDATE_INFO, EventType.UPDATE, jsonConverter.writeValueAsString(form));
        relyingParty.setSubdomains(subdomains);
        relyingParty.setPorts(ports);
        relyingPartyRepository.save(relyingParty);
        eventService.saveEvent();
    }

    public UserAccount createUserAccount(UserAccountCreateForm form, UUID relyingPartyId) {
        //NOTE: check package
        packageService.checkLicenseUser(relyingPartyId);

        //NOTE: create user
        eventService.logEvent(relyingPartyId, EventName.RELYING_PARTY_CREATE_USER, EventType.CREATE, jsonConverter.writeValueAsString(form));
        UserAccount userAccount = userAccountService.createUserAccount(form, relyingPartyId);
        eventService.saveEvent();
        return userAccount;
    }

    public UserAccount deleteUserAccount(UUID id, UUID relyingPartyId) {
        eventService.logEvent(relyingPartyId, EventName.RELYING_PARTY_DELETE_USER, EventType.DELETE, id.toString());
        UserAccount userAccount = userAccountService.deleteUserAccount(id, relyingPartyId);
        eventService.saveEvent();
        return userAccount;
    }

    public Authenticator updateUserAuthenticator(UUID userAuthenticatorId, UUID userAccountId, UserAuthenticatorUpdateForm form, UUID relyingPartyId) {
        eventService.logEvent(relyingPartyId, userAccountId, userAuthenticatorId, EventName.RELYING_PARTY_UPDATE_AUTHENTICATOR, EventType.UPDATE, jsonConverter.writeValueAsString(form));
        Authenticator authenticator = userAccountService.updateUserAuthenticator(userAuthenticatorId, userAccountId, form, relyingPartyId);
        eventService.saveEvent();
        return authenticator;
    }

    public Authenticator deleteUserAuthenticator(UUID userAuthenticatorId, UUID userAccountId, UUID relyingPartyId) {
        eventService.logEvent(relyingPartyId, userAccountId, EventName.RELYING_PARTY_DELETE_AUTHENTICATOR, EventType.DELETE, userAuthenticatorId.toString());
        Authenticator authenticator = userAccountService.deleteUserAuthenticator(userAuthenticatorId, userAccountId, relyingPartyId);
        eventService.saveEvent();
        return authenticator;
    }

    public Optional<UserAccount> findUserAccountById(UUID id, UUID relyingPartyId) {
        return userAccountService.findUserAccount(id, relyingPartyId);
    }

    public List<UserAccount> findAllUserAccount(UUID relyingPartyId) {
        return userAccountService.findAllUserAccount(relyingPartyId);
    }

    public List<Authenticator> findAllUserAuthenticator(UUID userAccountId, UUID relyingPartyId) {
        return userAccountService.findAllUserAuthenticator(userAccountId, relyingPartyId);
    }

    public void checkLicenseTime(UUID relyingPartyId) {
        packageService.checkLicenseTime(relyingPartyId);
    }

    public List<Package> findAllPackageByRelyingPartyId(UUID id) {
        return packageService.findAllByRelyingPartyId(id);
    }

    public ServiceLicenseDto calculateServiceLicense(UUID id) {
        return packageService.calculateServiceLicense(id);
    }
}
