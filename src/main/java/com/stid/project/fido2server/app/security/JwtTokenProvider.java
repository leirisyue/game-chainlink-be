package com.stid.project.fido2server.app.security;

import com.stid.project.fido2server.app.config.AppProperties;
import com.stid.project.fido2server.app.domain.entity.Authenticator;
import com.stid.project.fido2server.app.domain.entity.RelyingParty;
import com.stid.project.fido2server.app.domain.entity.UserAccount;
import com.stid.project.fido2server.app.repository.AuthenticatorRepository;
import com.stid.project.fido2server.app.repository.RelyingPartyRepository;
import com.stid.project.fido2server.app.repository.UserAccountRepository;
import io.jsonwebtoken.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Configuration
public class JwtTokenProvider implements TokenProvider {
    protected static final Logger LOGGER = LoggerFactory.getLogger(JwtTokenProvider.class);
    private final AppProperties appProperties;
    private final RelyingPartyRepository relyingPartyRepository;
    private final UserAccountRepository userAccountRepository;
    private final AuthenticatorRepository authenticatorRepository;

    public JwtTokenProvider(AppProperties appProperties, RelyingPartyRepository relyingPartyRepository, UserAccountRepository userAccountRepository, AuthenticatorRepository authenticatorRepository) {
        this.appProperties = appProperties;
        this.relyingPartyRepository = relyingPartyRepository;
        this.userAccountRepository = userAccountRepository;
        this.authenticatorRepository = authenticatorRepository;
    }

    @Override
    public AccessToken createToken(String subject, Instant validity, Map<String, Object> claims, Map<String, Object> headers) {
        AccessToken accessToken = new AccessToken();
        accessToken.setId(UUID.randomUUID().toString());
        accessToken.setSubject(subject);
        accessToken.setExpiresAt(validity);

        JwtBuilder jwtBuilder = Jwts.builder()
                .setId(accessToken.getId())
                .setSubject(accessToken.getSubject())
                .setExpiration(Date.from(accessToken.getExpiresAt()))
                .signWith(SignatureAlgorithm.HS512, appProperties.getJwtToken().getSecretKey());

        if (headers != null)
            jwtBuilder.setHeader(headers);
        if (claims != null)
            jwtBuilder.addClaims(claims);

        String token = jwtBuilder.compact();
        accessToken.setToken(token);
        return accessToken;
    }

    @Override
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(TOKEN_TYPE)) {
            return bearerToken.substring(TOKEN_TYPE.length());
        }
        return null;
    }

    @Override
    public Jws<Claims> parseClaims(String authToken) {
        try {
            return Jwts.parser()
                    .setSigningKey(appProperties.getJwtToken().getSecretKey())
                    .parseClaimsJws(authToken);
        } catch (Exception e) {
            LOGGER.debug("Invalid JWT token. {}", e.getMessage());
            return null;
        }
    }

    @Override
    public boolean validateToken(String authToken) {
        return StringUtils.hasText(authToken) && parseClaims(authToken) != null;
    }

    @Override
    public Authentication getAuthentication(String authToken) {
        AppProperties.JwtToken jwtToken = appProperties.getJwtToken();
        Claims claims = Jwts.parser().setSigningKey(jwtToken.getSecretKey()).parseClaimsJws(authToken).getBody();
        if (claims.getSubject() == null)
            return null;

        JwtTokenScope scope = JwtTokenScope.parse(claims.get(JwtTokenScope.SCOPE_ID, String.class), JwtTokenScope.NONE);
        Set<GrantedAuthority> grantedAuthorities = scope.getRoles();

        String username = claims.getSubject();
        String password = claims.getId();
        RelyingParty relyingParty = null;
        UserAccount userAccount = null;
        List<Authenticator> authenticators = null;
        if (scope == JwtTokenScope.ADMIN) {
            UUID relyingPartyId = UUID.fromString(claims.getSubject());
            relyingParty = relyingPartyRepository.findById(relyingPartyId).orElse(null);
            if (relyingParty == null)
                return null;

            username = relyingParty.getName();
            password = relyingParty.getSecret();
        }

        if (scope == JwtTokenScope.USER) {
            UUID relyingPartyId = UUID.fromString(claims.getSubject());
            relyingParty = relyingPartyRepository
                    .findById(relyingPartyId).orElse(null);

            UUID userId = UUID.fromString(claims.get("uid", String.class));
            userAccount = userAccountRepository
                    .findByIdAndRelyingPartyId(userId, relyingPartyId).orElse(null);

            if (relyingParty == null || userAccount == null)
                return null;

            username = userAccount.getUserLogin();
            password = userAccount.getPasswordHash();
            authenticators = authenticatorRepository.findByUserId(userAccount.getId());
        }

        SpringUser principal = new SpringUser(username, password, grantedAuthorities);
        principal.setRelyingParty(relyingParty);
        principal.setUserAccount(userAccount);
        principal.setUserAuthenticators(authenticators);
        principal.setScope(scope);
        principal.setToken(authToken);
        return new UsernamePasswordAuthenticationToken(principal, authToken, principal.getAuthorities());
    }

    public AccessToken createToken(String subject, JwtTokenScope scope, boolean remember) {
        AppProperties.JwtToken jwtToken = appProperties.getJwtToken();
        Duration validityDuration = remember ? jwtToken.getTokenValidityRemember() : jwtToken.getTokenValidity();
        return createToken(subject, scope, validityDuration);
    }

    public AccessToken createToken(String subject, JwtTokenScope scope, Instant expiration) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtTokenScope.SCOPE_ID, scope);
        return createToken(subject, expiration, claims, null);
    }

    public AccessToken createToken(String subject, JwtTokenScope scope, Duration validityDuration) {
        Instant validity = Instant.now().plus(validityDuration);
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtTokenScope.SCOPE_ID, scope);
        return createToken(subject, validity, claims, null);
    }

//    public String resolveToken(HttpServletRequest request) {
//        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
//        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(TOKEN_TYPE)) {
//            return bearerToken.substring(TOKEN_TYPE.length());
//        }
//        return null;
//    }
}
