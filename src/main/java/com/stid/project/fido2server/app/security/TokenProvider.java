package com.stid.project.fido2server.app.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;

import java.time.Instant;
import java.util.Map;

public interface TokenProvider {
    String AUTHORIZATION_HEADER = "Authorization";
    String TOKEN_TYPE = "Bearer ";

    AccessToken createToken(String subject, Instant validity, Map<String, Object> claims, Map<String, Object> headers);

    String resolveToken(HttpServletRequest request);

    Jws<Claims> parseClaims(String authToken);

    boolean validateToken(String authToken);

    Authentication getAuthentication(String authToken);
}
