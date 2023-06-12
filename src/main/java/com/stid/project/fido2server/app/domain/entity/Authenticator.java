package com.stid.project.fido2server.app.domain.entity;

import com.stid.project.fido2server.app.util.*;
import com.stid.project.fido2server.fido2.model.ServerPublicKeyCredentialDescriptor;
import com.webauthn4j.authenticator.AuthenticatorImpl;
import com.webauthn4j.data.AuthenticatorTransport;
import com.webauthn4j.data.PublicKeyCredentialType;
import com.webauthn4j.data.attestation.authenticator.AttestedCredentialData;
import com.webauthn4j.data.attestation.statement.AttestationStatement;
import com.webauthn4j.data.extension.authenticator.AuthenticationExtensionsAuthenticatorOutputs;
import com.webauthn4j.data.extension.authenticator.RegistrationExtensionAuthenticatorOutput;
import com.webauthn4j.data.extension.client.AuthenticationExtensionsClientOutputs;
import com.webauthn4j.data.extension.client.RegistrationExtensionClientOutput;
import com.webauthn4j.util.Base64UrlUtil;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Formula;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "tbl_authenticator")
public class Authenticator {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "aaguid", column = @Column(name = "aaguid", columnDefinition = "binary")),
            @AttributeOverride(name = "credentialId", column = @Column(name = "credential_id", columnDefinition = "binary")),
            @AttributeOverride(name = "coseKey", column = @Column(name = "cose_key", columnDefinition = "binary"))
    })
    @Converts({
            @Convert(converter = AAGUIDConverter.class, attributeName = "aaguid"),
            @Convert(converter = COSEKeyConverter.class, attributeName = "coseKey")
    })
    private AttestedCredentialData attestedCredentialData;

    @Convert(converter = AttestationStatementConverter.class)
    @Column(name = "attestation_statement", nullable = false)
    private AttestationStatement attestationStatement;

    @Column(name = "credential_id", insertable = false, updatable = false)
    private byte[] credentialId;

    @Convert(converter = ClientExtensionsConverter.class)
    @Column(name = "client_extensions")
    private AuthenticationExtensionsClientOutputs<RegistrationExtensionClientOutput> clientExtensions;

    @Convert(converter = AuthenticatorExtensionsConverter.class)
    @Column(name = "authenticator_extensions")
    private AuthenticationExtensionsAuthenticatorOutputs<RegistrationExtensionAuthenticatorOutput> authenticatorExtensions;

    @Convert(converter = SetAuthenticatorTransportConverter.class)
    @Column(name = "authenticator_transports")
    private Set<AuthenticatorTransport> authenticatorTransports;

    @Column(name = "created_date", nullable = false, updatable = false)
    private Instant createdDate = Instant.now();

    @Column(name = "last_access")
    private Instant lastAccess;

    @Column(name = "counter", nullable = false)
    private long counter;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Formula("(select tbl_user.user_login from tbl_user where tbl_user.id = user_id)")
    private String username;

    public ServerPublicKeyCredentialDescriptor toServerCredentialDescriptor() {
        return new ServerPublicKeyCredentialDescriptor(
                PublicKeyCredentialType.PUBLIC_KEY,
                Base64UrlUtil.encodeToString(attestedCredentialData.getCredentialId()),
                authenticatorTransports);
    }

    public AuthenticatorImpl toImpl() {
        return new AuthenticatorImpl(
                getAttestedCredentialData(),
                getAttestationStatement(),
                getCounter(),
                getAuthenticatorTransports(),
                getClientExtensions(),
                getAuthenticatorExtensions()
        );
    }
}
