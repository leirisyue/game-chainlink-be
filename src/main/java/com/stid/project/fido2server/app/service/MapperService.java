package com.stid.project.fido2server.app.service;

import com.stid.project.fido2server.app.domain.entity.Package;
import com.stid.project.fido2server.app.domain.entity.*;
import com.stid.project.fido2server.app.domain.model.*;
import com.stid.project.fido2server.app.util.OriginComparator;
import com.webauthn4j.converter.util.CborConverter;
import com.webauthn4j.converter.util.ObjectConverter;
import com.webauthn4j.data.client.Origin;
import com.webauthn4j.util.Base64UrlUtil;
import org.springframework.stereotype.Service;

import java.util.Comparator;

@Service
public class MapperService {
    private final CborConverter cborConverter;

    public MapperService(ObjectConverter objectConverter) {
        this.cborConverter = objectConverter.getCborConverter();
    }

    public PackageDto mapping(Package aPackage) {
        return new PackageDto(
                aPackage.getId(),
                aPackage.getType(),
                aPackage.getAmount(),
                aPackage.getCreatedDate(),
                aPackage.getActivatedDate(),
                aPackage.getDescription());
    }

    public RelyingPartyDto mapping(RelyingParty relyingParty) {
        return new RelyingPartyDto(
                relyingParty.getId(),
                relyingParty.getSecret(),
                relyingParty.getName(),
                relyingParty.getOrigin(),
                relyingParty.getSubdomains().stream()
                        .sorted(Comparator.comparing(RelyingParty.Subdomain::toString))
                        .toList(),
                relyingParty.getPorts().stream().sorted().toList(),
                relyingParty.getOrigins().stream()
                        .sorted(OriginComparator.getComparator())
                        .map(Origin::toString)
                        .toList(),
                relyingParty.getDescription(),
                relyingParty.getCreatedDate(),
                relyingParty.getStatus());
    }

    public UserAccountDto mapping(UserAccount userAccount) {
        return new UserAccountDto(
                userAccount.getId(),
                bytesToBase64Url(userAccount.getUserHandle()),
                userAccount.getUserLogin(),
                userAccount.getUserEmail(),
                userAccount.getDisplayName(),
                userAccount.getCreatedDate(),
                userAccount.getRelyingPartyId());
    }

    public AuthenticatorDto mapping(Authenticator authenticator) {
        return new AuthenticatorDto(
                authenticator.getId(),
                authenticator.getName(),
                bytesToBase64Url(authenticator.getAttestedCredentialData().getCredentialId()),
                authenticator.getAttestedCredentialData().getAaguid().toString(),
                cborToBase64Url(authenticator.getAttestedCredentialData().getCOSEKey()),
                authenticator.getAttestationStatement().getFormat(),
                authenticator.getAuthenticatorTransports(),
                authenticator.getCreatedDate(),
                authenticator.getLastAccess(),
                authenticator.getCounter());
    }

    public EventDto mapping(Event event) {
        return new EventDto(
                event.getId(),
                event.getEventName(),
                event.getEventType(),
                event.getEventStatus(),
                event.getEventDetail(),
                event.getTimestamp());
    }

    String bytesToBase64Url(byte[] bytes) {
        return Base64UrlUtil.encodeToString(bytes);
    }

    String cborToBase64Url(Object o) {
        try {
            return bytesToBase64Url(cborConverter.writeValueAsBytes(o));
        } catch (Exception e) {
            return null;
        }
    }

}
