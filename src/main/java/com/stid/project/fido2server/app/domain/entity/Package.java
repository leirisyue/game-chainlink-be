package com.stid.project.fido2server.app.domain.entity;

import com.stid.project.fido2server.app.domain.constant.PackageType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "tbl_package")
public class Package {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private PackageType type;

    @Column(name = "amount")
    private long amount;

    @Column(name = "created_date", nullable = false, updatable = false)
    private Instant createdDate = Instant.now();

    @Column(name = "activated_date")
    private Instant activatedDate;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "relying_party_id", nullable = false, updatable = false)
    private UUID relyingPartyId;
}
