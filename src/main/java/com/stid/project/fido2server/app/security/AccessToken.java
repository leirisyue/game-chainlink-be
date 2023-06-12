package com.stid.project.fido2server.app.security;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;

@Getter
@Setter
@ToString
public class AccessToken {
    private String id;
    private String subject;
    private String token;
    private Instant expiresAt;
}
