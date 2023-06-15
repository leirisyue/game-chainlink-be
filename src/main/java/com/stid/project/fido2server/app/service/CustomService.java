package com.stid.project.fido2server.app.service;

import com.stid.project.fido2server.app.domain.constant.EventName;
import com.stid.project.fido2server.app.domain.constant.EventType;
import com.stid.project.fido2server.app.domain.entity.RelyingParty;
import com.stid.project.fido2server.app.domain.entity.UserAccount;
import com.stid.project.fido2server.app.exception.AbstractExceptionHandler;
import com.stid.project.fido2server.app.repository.RelyingPartyRepository;

import com.stid.project.fido2server.app.web.form.CustomCreateForm;
import com.webauthn4j.converter.util.JsonConverter;
import com.webauthn4j.converter.util.ObjectConverter;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CustomService extends AbstractExceptionHandler {

    private final PackageService packageService;
    private final EventService eventService;

    private final JsonConverter jsonConverter;
    private final UserAccountService userAccountService;
    private final RelyingPartyRepository relyingPartyRepository;
    public CustomService(ObjectConverter objectConverter, PackageService packageService, EventService eventService, UserAccountService userAccountService, RelyingPartyRepository relyingPartyRepository){
        this.packageService = packageService;
        this.eventService = eventService;
        this.jsonConverter = objectConverter.getJsonConverter();
        this.userAccountService = userAccountService;
        this.relyingPartyRepository = relyingPartyRepository;
    }

    public UserAccount createCustomAccount(CustomCreateForm form, UUID relyingPartyId) {
        //NOTE: check package
        packageService.checkLicenseUser(relyingPartyId);
        //NOTE: create user

        eventService.logEvent(relyingPartyId, EventName.RELYING_PARTY_CREATE_USER, EventType.CREATE, jsonConverter.writeValueAsString(form));
        UserAccount userAccount = userAccountService.createCustomerAccount(form, relyingPartyId);
        eventService.saveEvent();
        return userAccount;
    }

    public List<RelyingParty> findAllRelyingParty() {
        return relyingPartyRepository.findAll();
    }

}
