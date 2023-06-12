package com.stid.project.fido2server.app.repository;

import com.stid.project.fido2server.app.domain.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserAccountRepository extends JpaRepository<UserAccount, UUID> {
    boolean existsByUserHandle(byte[] userHandle);

    boolean existsByUserLoginAndRelyingPartyId(String userLogin, UUID relyingPartyId);

    long countByRelyingPartyId(UUID relyingPartyId);

    Optional<UserAccount> findByIdAndRelyingPartyId(UUID id, UUID relyingPartyId);

    Optional<UserAccount> findByUserLogin(String userLogin);

    Optional<UserAccount> findByUserLoginAndRelyingPartyId(String userLogin, UUID relyingPartyId);

    Optional<UserAccount> findByUserHandle(byte[] userHandle);

    List<UserAccount> findByRelyingPartyId(UUID relyingPartyId);

}