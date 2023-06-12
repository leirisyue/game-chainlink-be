package com.stid.project.fido2server.app.repository;

import com.stid.project.fido2server.app.domain.entity.Authenticator;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AuthenticatorRepository extends JpaRepository<Authenticator, UUID> {

    boolean existsByCredentialId(byte[] credentialId);

    Optional<Authenticator> findByIdAndUserId(UUID id, UUID userId);

    Optional<Authenticator> findByCredentialId(byte[] credentialId);

    List<Authenticator> findByUserId(UUID userId);
}