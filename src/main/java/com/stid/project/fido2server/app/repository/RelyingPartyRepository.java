package com.stid.project.fido2server.app.repository;

import com.stid.project.fido2server.app.domain.constant.Status;
import com.stid.project.fido2server.app.domain.entity.RelyingParty;
import com.webauthn4j.data.client.Origin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RelyingPartyRepository extends JpaRepository<RelyingParty, UUID> {
    boolean existsByNameAndOrigin(String name, Origin origin);

    Optional<RelyingParty> findByIdAndStatus(UUID id, Status status);

}