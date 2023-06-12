package com.stid.project.fido2server.app.domain.entity;

import com.stid.project.fido2server.fido2.model.ServerPublicKeyCredentialUserEntity;
import com.webauthn4j.util.Base64UrlUtil;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "tbl_user", uniqueConstraints = @UniqueConstraint(columnNames = {"user_login", "relying_party_id"}))
public class UserAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "user_handle", nullable = false, unique = true)
    private byte[] userHandle;

    @Column(name = "user_login", length = 200)
    private String userLogin;

    @Column(name = "user_email", length = 200, unique = true)
    private String userEmail;

    @Column(name = "display_name", length = 200)
    private String displayName;

    @Column(name = "password_hash", nullable = false, length = 200)
    private String passwordHash;

    @Column(name = "created_date", nullable = false, updatable = false)
    private Instant createdDate = Instant.now();

    @Column(name = "relying_party_id")
    private UUID relyingPartyId;

    public ServerPublicKeyCredentialUserEntity toServerUserEntity() {
        return new ServerPublicKeyCredentialUserEntity(
                Base64UrlUtil.encodeToString(userHandle), userLogin, displayName);
    }
}
