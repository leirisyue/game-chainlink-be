package com.stid.project.fido2server.app.service;

import com.stid.project.fido2server.app.domain.entity.RelyingParty;
import com.stid.project.fido2server.app.exception.AbstractExceptionHandler;
import com.stid.project.fido2server.fido2.model.*;
import com.stid.project.fido2server.fido2.service.Fido2ServerAssertionOptionsEndpointService;
import com.stid.project.fido2server.fido2.service.Fido2ServerAssertionResultEndpointService;
import com.stid.project.fido2server.fido2.service.Fido2ServerAttestationOptionsEndpointService;
import com.stid.project.fido2server.fido2.service.Fido2ServerAttestationResultEndpointService;
import org.springframework.stereotype.Service;

@Service
public class Fido2Service extends AbstractExceptionHandler {
    private final Fido2ServerAttestationOptionsEndpointService attestationOptionsEndpointService;
    private final Fido2ServerAttestationResultEndpointService attestationResultEndpointService;
    private final Fido2ServerAssertionOptionsEndpointService assertionOptionsEndpointService;
    private final Fido2ServerAssertionResultEndpointService assertionResultEndpointService;
    private final PackageService packageService;

    public Fido2Service(Fido2ServerAttestationOptionsEndpointService attestationOptionsEndpointService, Fido2ServerAttestationResultEndpointService attestationResultEndpointService, Fido2ServerAssertionOptionsEndpointService assertionOptionsEndpointService, Fido2ServerAssertionResultEndpointService assertionResultEndpointService, PackageService packageService) {
        this.attestationOptionsEndpointService = attestationOptionsEndpointService;
        this.attestationResultEndpointService = attestationResultEndpointService;
        this.assertionOptionsEndpointService = assertionOptionsEndpointService;
        this.assertionResultEndpointService = assertionResultEndpointService;
        this.packageService = packageService;
    }

    public AttestationOptionsResponse attestationOptions(
            AttestationOptionsRequest request, RelyingParty relyingParty) {
        //NOTE: check package
        packageService.checkLicenseTime(relyingParty.getId());
        return attestationOptionsEndpointService.processRequest(request, relyingParty);
    }

    public AttestationResultResponse attestationResult(
            ServerPublicKeyCredential<ServerAuthenticatorAttestationResponse> credential, RelyingParty relyingParty) {
        //NOTE: check package
        packageService.checkLicenseTime(relyingParty.getId());
        return attestationResultEndpointService.processRequest(credential, relyingParty);
    }

    public AssertionOptionsResponse assertionOptions(
            AssertionOptionsRequest request, RelyingParty relyingParty) {
        //NOTE: check package
        packageService.checkLicenseTime(relyingParty.getId());
        return assertionOptionsEndpointService.processRequest(request, relyingParty);
    }

    public AssertionResultResponse assertionResult(
            ServerPublicKeyCredential<ServerAuthenticatorAssertionResponse> credential, RelyingParty relyingParty) {
        //NOTE: check package
        packageService.checkLicenseTime(relyingParty.getId());
        return assertionResultEndpointService.attemptAuthentication(credential, relyingParty);
    }
}
