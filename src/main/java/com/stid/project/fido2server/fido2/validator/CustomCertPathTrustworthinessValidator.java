package com.stid.project.fido2server.fido2.validator;

import com.webauthn4j.anchor.TrustAnchorRepository;
import com.webauthn4j.data.attestation.authenticator.AAGUID;
import com.webauthn4j.data.attestation.statement.CertificateBaseAttestationStatement;
import com.webauthn4j.data.attestation.statement.FIDOU2FAttestationStatement;
import com.webauthn4j.util.AssertUtil;
import com.webauthn4j.util.CertificateUtil;
import com.webauthn4j.validator.attestation.trustworthiness.certpath.DefaultCertPathTrustworthinessValidator;
import com.webauthn4j.validator.exception.CertificateException;
import com.webauthn4j.validator.exception.TrustAnchorNotFoundException;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.InvalidAlgorithmParameterException;
import java.security.cert.*;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import java.util.Set;

public class CustomCertPathTrustworthinessValidator extends DefaultCertPathTrustworthinessValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomCertPathTrustworthinessValidator.class);
    private final TrustAnchorRepository trustAnchorRepository;
    private boolean fullChainProhibited = false;
    private boolean revocationCheckEnabled = false;
    private boolean policyQualifiersRejected = false;

    public CustomCertPathTrustworthinessValidator(TrustAnchorRepository trustAnchorRepository) {
        super(trustAnchorRepository);
        this.trustAnchorRepository = trustAnchorRepository;
    }

    @Override
    public void validate(@NonNull AAGUID aaguid, @NonNull CertificateBaseAttestationStatement attestationStatement, @NonNull Instant timestamp) {
        AssertUtil.notNull(aaguid, "aaguid must not be null");
        AssertUtil.notNull(aaguid, "attestationStatement must not be null");
        AssertUtil.notNull(aaguid, "timestamp must not be null");
        AssertUtil.notNull(attestationStatement.getX5c(), "x5c must not be null");
        CertPath certPath = attestationStatement.getX5c().createCertPath();
        Set<TrustAnchor> trustAnchors;
        if (attestationStatement instanceof FIDOU2FAttestationStatement fidou2fAttestationStatement) {
            byte[] subjectKeyIdentifier = CertificateUtil.extractSubjectKeyIdentifier(fidou2fAttestationStatement.getX5c().getEndEntityAttestationCertificate().getCertificate());
            trustAnchors = this.trustAnchorRepository.find(subjectKeyIdentifier);
        } else {
            trustAnchors = this.trustAnchorRepository.find(aaguid);
        }

        this.validateCertPath(certPath, trustAnchors, timestamp);
    }

    private void validateCertPath(CertPath certPath, Set<TrustAnchor> trustAnchors, Instant timestamp) {
        if (trustAnchors.isEmpty()) {
            throw new TrustAnchorNotFoundException("TrustAnchors are not found");
        } else {
            CertPathValidator certPathValidator = CertificateUtil.createCertPathValidator();
            PKIXParameters certPathParameters = CertificateUtil.createPKIXParameters(trustAnchors);
            certPathParameters.setPolicyQualifiersRejected(this.policyQualifiersRejected);
            certPathParameters.setRevocationEnabled(this.revocationCheckEnabled);
            certPathParameters.setDate(Date.from(timestamp));

            PKIXCertPathValidatorResult result;
            try {
                result = (PKIXCertPathValidatorResult) certPathValidator.validate(certPath, certPathParameters);
            } catch (InvalidAlgorithmParameterException var8) {
                throw new CertificateException("invalid algorithm parameter", var8);
            } catch (CertPathValidatorException var9) {
                if (var9.getReason() == PKIXReason.NO_TRUST_ANCHOR) {
                    if (certPath.getCertificates().size() == 1 && trustAnchors.size() == 1) {
                        final Certificate pathCertificate = certPath.getCertificates().get(0);
                        final Certificate trustCertificate = trustAnchors.stream().map(TrustAnchor::getTrustedCert).findAny().orElse(null);
                        if (pathCertificate.equals(trustCertificate)) {
                            return;
                        }
                    }
                }
                LOGGER.debug("CertPathValidatorException: Reason={}, Message={}", var9.getReason(), var9.getMessage());
                throw new CertificateException("CertPathValidatorException: " + var9.getMessage());
            }

            if (this.fullChainProhibited && certPath.getCertificates().contains(result.getTrustAnchor().getTrustedCert())) {
                throw new CertificateException("`certpath` must not contain full chain.");
            } else {
                trustAnchors.stream()
                        .filter((item) -> Objects.equals(item, result.getTrustAnchor()))
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("Matching TrustAnchor is not found."));
            }
        }
    }

    public boolean isFullChainProhibited() {
        return this.fullChainProhibited;
    }

    public void setFullChainProhibited(boolean fullChainProhibited) {
        this.fullChainProhibited = fullChainProhibited;
    }

    public boolean isRevocationCheckEnabled() {
        return this.revocationCheckEnabled;
    }

    public void setRevocationCheckEnabled(boolean revocationCheckEnabled) {
        this.revocationCheckEnabled = revocationCheckEnabled;
    }

    public boolean isPolicyQualifiersRejected() {
        return this.policyQualifiersRejected;
    }

    public void setPolicyQualifiersRejected(boolean policyQualifiersRejected) {
        this.policyQualifiersRejected = policyQualifiersRejected;
    }
}
