package com.stid.project.fido2server.app.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public enum JwtTokenScope {
    SYSTEM(JwtTokenScope.ROLE_SYSTEM),
    ADMIN(JwtTokenScope.ROLE_ADMIN),
    USER(JwtTokenScope.ROLE_USER),
    NONE();
    public static final String SCOPE_ID = "scp";
    public static final String ROLE_SYSTEM = "ROLE_SYSTEM";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_USER = "ROLE_USER";

    private final Set<GrantedAuthority> roles;

    JwtTokenScope(String... roles) {
        this.roles = Arrays.stream(roles)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
    }

    public static JwtTokenScope parse(String scope, JwtTokenScope defaultValue) {
        try {
            return JwtTokenScope.valueOf(scope);
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
