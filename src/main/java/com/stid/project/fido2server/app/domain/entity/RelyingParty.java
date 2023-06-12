package com.stid.project.fido2server.app.domain.entity;

import com.stid.project.fido2server.app.domain.constant.Status;
import com.stid.project.fido2server.app.util.OriginConverter;
import com.stid.project.fido2server.app.util.SetIntegerConverter;
import com.stid.project.fido2server.app.util.SetStringConverter;
import com.webauthn4j.data.PublicKeyCredentialRpEntity;
import com.webauthn4j.data.client.Origin;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

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

    @Convert(converter = SetStringConverter.class)
    @Column(name = "subdomains")
    private Set<String> subdomains = new HashSet<>();

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
        if (subdomains != null) {
            subdomains.forEach(subdomain -> {
                origins.add(Origin.create(origin.getScheme() + "://" + subdomain + "." + origin.getHost() + ":" + origin.getPort()));
                if (ports != null) {
                    ports.forEach(port -> {
                        origins.add(Origin.create(origin.getScheme() + "://" + subdomain + "." + origin.getHost() + ":" + port));
                    });
                }
            });
        }
        if (ports != null) {
            ports.forEach(port -> {
                origins.add(Origin.create(origin.getScheme() + "://" + origin.getHost() + ":" + port));
                if (subdomains != null) {
                    subdomains.forEach(subdomain -> {
                        origins.add(Origin.create(origin.getScheme() + "://" + subdomain + "." + origin.getHost() + ":" + port));
                    });
                }
            });
        }
        return origins;
    }

    public PublicKeyCredentialRpEntity toRpEntity() {
        return new PublicKeyCredentialRpEntity(getRpId(), name);
    }
}
