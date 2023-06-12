package com.stid.project.fido2server.app.repository;

import com.stid.project.fido2server.app.domain.entity.Package;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PackageRepository extends JpaRepository<Package, UUID> {
    long countByActivatedDateNotNull();

    List<Package> findByRelyingPartyId(UUID relyingPartyId);

    List<Package> findByRelyingPartyIdAndActivatedDateNotNull(UUID relyingPartyId);
}