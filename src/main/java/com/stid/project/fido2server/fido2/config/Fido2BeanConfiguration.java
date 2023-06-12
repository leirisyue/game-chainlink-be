package com.stid.project.fido2server.fido2.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.stid.project.fido2server.app.config.AppProperties;
import com.stid.project.fido2server.fido2.model.ExampleExtensionAuthenticatorOutput;
import com.stid.project.fido2server.fido2.model.ExampleExtensionClientInput;
import com.stid.project.fido2server.fido2.validator.CustomCertPathTrustworthinessValidator;
import com.webauthn4j.WebAuthnManager;
import com.webauthn4j.converter.AttestationObjectConverter;
import com.webauthn4j.converter.CollectedClientDataConverter;
import com.webauthn4j.converter.util.ObjectConverter;
import com.webauthn4j.data.MessageDigestAlgorithm;
import com.webauthn4j.data.SignatureAlgorithm;
import com.webauthn4j.data.attestation.statement.COSEAlgorithmIdentifier;
import com.webauthn4j.data.attestation.statement.COSEKeyType;
import com.webauthn4j.metadata.FidoMDS3MetadataBLOBProvider;
import com.webauthn4j.metadata.MetadataBLOBProvider;
import com.webauthn4j.metadata.anchor.AggregatingTrustAnchorRepository;
import com.webauthn4j.metadata.anchor.MetadataBLOBBasedTrustAnchorRepository;
import com.webauthn4j.metadata.anchor.MetadataStatementsBasedTrustAnchorRepository;
import com.webauthn4j.metadata.converter.jackson.WebAuthnMetadataJSONModule;
import com.webauthn4j.springframework.security.metadata.ResourcesMetadataStatementsProvider;
import com.webauthn4j.util.Base64Util;
import com.webauthn4j.util.CertificateUtil;
import com.webauthn4j.validator.attestation.statement.androidkey.AndroidKeyAttestationStatementValidator;
import com.webauthn4j.validator.attestation.statement.androidsafetynet.AndroidSafetyNetAttestationStatementValidator;
import com.webauthn4j.validator.attestation.statement.apple.AppleAnonymousAttestationStatementValidator;
import com.webauthn4j.validator.attestation.statement.none.NoneAttestationStatementValidator;
import com.webauthn4j.validator.attestation.statement.packed.PackedAttestationStatementValidator;
import com.webauthn4j.validator.attestation.statement.tpm.TPMAttestationStatementValidator;
import com.webauthn4j.validator.attestation.statement.u2f.FIDOU2FAttestationStatementValidator;
import com.webauthn4j.validator.attestation.trustworthiness.certpath.CertPathTrustworthinessValidator;
import com.webauthn4j.validator.attestation.trustworthiness.certpath.DefaultCertPathTrustworthinessValidator;
import com.webauthn4j.validator.attestation.trustworthiness.self.DefaultSelfAttestationTrustworthinessValidator;
import jakarta.annotation.PostConstruct;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Field;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Configuration
public class Fido2BeanConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(Fido2BeanConfiguration.class);
    private final AppProperties appProperties;

    public Fido2BeanConfiguration(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @Bean
    public WebAuthnManager webAuthnManager(
            CertPathTrustworthinessValidator certPathTrustworthinessValidator,
            ObjectConverter objectConverter
    ) {
        Security.addProvider(new BouncyCastleProvider());
        return new WebAuthnManager(
                Arrays.asList(
                        new PackedAttestationStatementValidator(),
                        new FIDOU2FAttestationStatementValidator(),
                        new AndroidKeyAttestationStatementValidator(),
                        new AndroidSafetyNetAttestationStatementValidator(),
                        new TPMAttestationStatementValidator(),
                        new AppleAnonymousAttestationStatementValidator(),
                        new NoneAttestationStatementValidator()
                ),
                certPathTrustworthinessValidator,
                new DefaultSelfAttestationTrustworthinessValidator(),
                objectConverter
        );
    }

    @Bean
    public MetadataBLOBBasedTrustAnchorRepository metadataBLOBBasedTrustAnchorRepository(ObjectConverter objectConverter, ResourcePatternResolver resourcePatternResolver) {
        LOGGER.info("Loading FidoMDS3MetadataBLOB...");
        AppProperties.Metadata metadata = appProperties.getMetadata();
        if (metadata != null && metadata.getMds3Endpoints() != null) {
            Resource resource = resourcePatternResolver
                    .getResource("classpath:metadata/MDS3ROOT.crt");
            X509Certificate mds3RootCertificate = mds3TestRootCertificate(resource);
            MetadataBLOBProvider[] fidoMDS3MetadataBLOBProviders = metadata.getMds3Endpoints().stream().map(url -> {
                        try {
                            FidoMDS3MetadataBLOBProvider fidoMDS3MetadataBLOBProvider = new FidoMDS3MetadataBLOBProvider(objectConverter, url, mds3RootCertificate);
                            fidoMDS3MetadataBLOBProvider.setRevocationCheckEnabled(true); // FIDO Conformance test env workaround
                            fidoMDS3MetadataBLOBProvider.refresh();
                            return fidoMDS3MetadataBLOBProvider;
                        } catch (RuntimeException e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .toArray(MetadataBLOBProvider[]::new);

            LOGGER.info("Loaded {} FidoMDS3MetadataBLOB", fidoMDS3MetadataBLOBProviders.length);
            return new MetadataBLOBBasedTrustAnchorRepository(fidoMDS3MetadataBLOBProviders);
        }
        return new MetadataBLOBBasedTrustAnchorRepository();
    }

    @Bean
    public MetadataStatementsBasedTrustAnchorRepository metadataStatementsBasedTrustAnchorRepository(ObjectConverter objectConverter, ResourcePatternResolver resourcePatternResolver) {
        LOGGER.info("Loading ResourcesMetadataStatements...");
        AppProperties.Metadata metadata = appProperties.getMetadata();
        if (metadata != null) {
            try {
                Resource[] resources = resourcePatternResolver
                        .getResources("classpath:metadata/metadataStatements/*.json");

                ResourcesMetadataStatementsProvider metadataStatementsProvider = new ResourcesMetadataStatementsProvider(objectConverter);
                metadataStatementsProvider.setResources(Arrays.stream(resources).collect(Collectors.toList()));
                LOGGER.info("Loaded {} ResourcesMetadataStatements", metadataStatementsProvider.getResources().size());
                return new MetadataStatementsBasedTrustAnchorRepository(metadataStatementsProvider);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return null;
    }

    @Bean
    public DefaultCertPathTrustworthinessValidator defaultCertPathTrustworthinessValidator(
            MetadataStatementsBasedTrustAnchorRepository metadataStatementsBasedTrustAnchorRepository,
            MetadataBLOBBasedTrustAnchorRepository metadataBLOBBasedTrustAnchorRepository) {
        CustomCertPathTrustworthinessValidator defaultCertPathTrustworthinessValidator = new CustomCertPathTrustworthinessValidator(
                new AggregatingTrustAnchorRepository(metadataStatementsBasedTrustAnchorRepository, metadataBLOBBasedTrustAnchorRepository));
        defaultCertPathTrustworthinessValidator.setFullChainProhibited(true);
        return defaultCertPathTrustworthinessValidator;
    }

    @Bean
    public ObjectConverter objectConverter(ObjectMapper jsonMapper) {
        jsonMapper.registerSubtypes(new NamedType(ExampleExtensionClientInput.class, ExampleExtensionClientInput.ID));
        jsonMapper.registerModule(new JavaTimeModule());
        jsonMapper.registerModule(new WebAuthnMetadataJSONModule());
        ObjectMapper cborMapper = new ObjectMapper(new CBORFactory());
        cborMapper.registerSubtypes(new NamedType(ExampleExtensionAuthenticatorOutput.class, ExampleExtensionAuthenticatorOutput.ID));
        return new ObjectConverter(jsonMapper, cborMapper);
    }

    @Bean
    public CollectedClientDataConverter collectedClientDataConverter(ObjectConverter objectConverter) {
        return new CollectedClientDataConverter(objectConverter);
    }

    @Bean
    public AttestationObjectConverter attestationObjectConverter(ObjectConverter objectConverter) {
        return new AttestationObjectConverter(objectConverter);
    }

    public X509Certificate mds3TestRootCertificate(Resource resource) {
        try {
            return CertificateUtil.generateX509Certificate(resource.getInputStream());
        } catch (IOException e) {
            System.err.println(e.getMessage());
            byte[] bytes = Base64Util.decode(
                    "MIICaDCCAe6gAwIBAgIPBCqih0DiJLW7+UHXx/o1MAoGCCqGSM49BAMDMGcxCzAJ" +
                            "BgNVBAYTAlVTMRYwFAYDVQQKDA1GSURPIEFsbGlhbmNlMScwJQYDVQQLDB5GQUtF" +
                            "IE1ldGFkYXRhIDMgQkxPQiBST09UIEZBS0UxFzAVBgNVBAMMDkZBS0UgUm9vdCBG" +
                            "QUtFMB4XDTE3MDIwMTAwMDAwMFoXDTQ1MDEzMTIzNTk1OVowZzELMAkGA1UEBhMC" +
                            "VVMxFjAUBgNVBAoMDUZJRE8gQWxsaWFuY2UxJzAlBgNVBAsMHkZBS0UgTWV0YWRh" +
                            "dGEgMyBCTE9CIFJPT1QgRkFLRTEXMBUGA1UEAwwORkFLRSBSb290IEZBS0UwdjAQ" +
                            "BgcqhkjOPQIBBgUrgQQAIgNiAASKYiz3YltC6+lmxhPKwA1WFZlIqnX8yL5RybSL" +
                            "TKFAPEQeTD9O6mOz+tg8wcSdnVxHzwnXiQKJwhrav70rKc2ierQi/4QUrdsPes8T" +
                            "EirZOkCVJurpDFbXZOgs++pa4XmjYDBeMAsGA1UdDwQEAwIBBjAPBgNVHRMBAf8E" +
                            "BTADAQH/MB0GA1UdDgQWBBQGcfeCs0Y8D+lh6U5B2xSrR74eHTAfBgNVHSMEGDAW" +
                            "gBQGcfeCs0Y8D+lh6U5B2xSrR74eHTAKBggqhkjOPQQDAwNoADBlAjEA/xFsgri0" +
                            "xubSa3y3v5ormpPqCwfqn9s0MLBAtzCIgxQ/zkzPKctkiwoPtDzI51KnAjAmeMyg" +
                            "X2S5Ht8+e+EQnezLJBJXtnkRWY+Zt491wgt/AwSs5PHHMv5QgjELOuMxQBc=");
            return CertificateUtil.generateX509Certificate(bytes);
        }
    }

    @PostConstruct
    public void initRSA_SSA_PSS() {
        try {
            COSEAlgorithmIdentifier COSE_PSS256 = COSEAlgorithmIdentifier.create(-37L);
            COSEAlgorithmIdentifier COSE_PSS384 = COSEAlgorithmIdentifier.create(-38L);
            COSEAlgorithmIdentifier COSE_PSS512 = COSEAlgorithmIdentifier.create(-39L);

            SignatureAlgorithm SIG_PSS256 = SignatureAlgorithm.create("SHA256withRSA/PSS", MessageDigestAlgorithm.SHA256.getJcaName());
            SignatureAlgorithm SIG_PSS384 = SignatureAlgorithm.create("SHA384withRSA/PSS", MessageDigestAlgorithm.SHA384.getJcaName());
            SignatureAlgorithm SIG_PSS512 = SignatureAlgorithm.create("SHA512withRSA/PSS", MessageDigestAlgorithm.SHA512.getJcaName());

            Field keyTypeMapField = COSEAlgorithmIdentifier.class.getDeclaredField("keyTypeMap");
            Field algorithmMapField = COSEAlgorithmIdentifier.class.getDeclaredField("algorithmMap");
            Field reverseAlgorithmMapField = COSEAlgorithmIdentifier.class.getDeclaredField("reverseAlgorithmMap");
            keyTypeMapField.setAccessible(true);
            algorithmMapField.setAccessible(true);
            reverseAlgorithmMapField.setAccessible(true);

            @SuppressWarnings("unchecked")
            Map<COSEAlgorithmIdentifier, COSEKeyType> keyTypeMap = (Map<COSEAlgorithmIdentifier, COSEKeyType>) keyTypeMapField.get(null);
            keyTypeMap.put(COSE_PSS256, COSEKeyType.RSA);
            keyTypeMap.put(COSE_PSS384, COSEKeyType.RSA);
            keyTypeMap.put(COSE_PSS512, COSEKeyType.RSA);

            @SuppressWarnings("unchecked")
            Map<COSEAlgorithmIdentifier, SignatureAlgorithm> algorithmMap = (Map<COSEAlgorithmIdentifier, SignatureAlgorithm>) algorithmMapField.get(null);
            algorithmMap.put(COSE_PSS256, SIG_PSS256);
            algorithmMap.put(COSE_PSS384, SIG_PSS384);
            algorithmMap.put(COSE_PSS512, SIG_PSS512);

            @SuppressWarnings("unchecked")
            Map<SignatureAlgorithm, COSEAlgorithmIdentifier> reverseAlgorithmMap = (Map<SignatureAlgorithm, COSEAlgorithmIdentifier>) reverseAlgorithmMapField.get(null);
            reverseAlgorithmMap.put(SIG_PSS256, COSE_PSS256);
            reverseAlgorithmMap.put(SIG_PSS384, COSE_PSS384);
            reverseAlgorithmMap.put(SIG_PSS512, COSE_PSS512);
            LOGGER.info("initialized RSA_SSA_PSS");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
