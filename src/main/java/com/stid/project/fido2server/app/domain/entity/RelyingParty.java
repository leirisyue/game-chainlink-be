package com.stid.project.fido2server.app.domain.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.stid.project.fido2server.app.domain.constant.Status;
import com.stid.project.fido2server.app.util.OriginConverter;
import com.stid.project.fido2server.app.util.SetIntegerConverter;
import com.stid.project.fido2server.app.util.SetSubdomainConverter;
import com.webauthn4j.data.PublicKeyCredentialRpEntity;
import com.webauthn4j.data.client.Origin;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@Setter
@Entity
@Table(name = "tbl_relying_party")
public class RelyingParty {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "secret", nullable = false, length = 500)
    private String secret;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Convert(converter = OriginConverter.class)
    @Column(name = "origin", nullable = false, length = 256)
    private Origin origin;

    @Convert(converter = SetSubdomainConverter.class)
    @Column(name = "subdomains")
    private Set<Subdomain> subdomains = new HashSet<>();

    @Convert(converter = SetIntegerConverter.class)
    @Column(name = "ports")
    private Set<Integer> ports = new HashSet<>();

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "created_date", nullable = false, updatable = false)
    private Instant createdDate = Instant.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.ACTIVE;

    public String getRpId() {
        return origin.getHost();
    }

    public Set<Origin> getOrigins() {
        Set<Origin> origins = new HashSet<>();
        origins.add(origin);
        if (ports != null) {
            ports.stream().map(port -> Origin.create(origin.getScheme() + "://" + origin.getHost() + ":" + port)).forEach(origins::add);
        }
        if (subdomains != null) {
            final Set<Origin> subOrigins = subdomains.stream()
                    .map(subdomain -> subdomain.toOrigin(origin))
                    .collect(Collectors.toSet());
            origins.addAll(subOrigins);
        }
        return origins;
    }

    public PublicKeyCredentialRpEntity toRpEntity() {
        return new PublicKeyCredentialRpEntity(getRpId(), name);
    }

    @Data
    @EqualsAndHashCode
    public static class Subdomain implements Serializable {
        private String subdomain;
        private int port = -1;

        @JsonCreator
        public static Subdomain fromString(String value) {
            Subdomain subdomain = new Subdomain();
            if (StringUtils.hasText(value)) {
                final String[] split = value.split(":", 2);
                subdomain.setSubdomain(split[0]);
                subdomain.setPort(-1);
                if (split.length == 2) {
                    try {
                        int port = Integer.parseInt(split[1]);
                        if (port > -1) {
                            subdomain.setPort(port);
                        }
                    } catch (Exception ignored) {
                    }
                }
                return subdomain;
            }
            return null;
        }

        public Origin toOrigin(Origin origin) {
            String subOrigin = origin.getScheme() + "://" + subdomain + "." + origin.getHost();
            if (port > -1)
                subOrigin += ":" + port;
            return Origin.create(subOrigin);
        }

        @JsonValue
        public String toString() {
            //return port != null && port > 0 ? subdomain + ":" + port : subdomain;
            return port != -1 ? subdomain + ":" + port : subdomain;
        }
    }
}
