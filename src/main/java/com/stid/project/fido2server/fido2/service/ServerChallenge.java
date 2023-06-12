package com.stid.project.fido2server.fido2.service;

import com.stid.project.fido2server.app.util.HelperUtil;
import com.webauthn4j.data.UserVerificationRequirement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ServerChallenge {
    private String username;
    private UserVerificationRequirement verification;
    private byte[] value;

    public static ServerChallenge create(String server, String username, UserVerificationRequirement verification) {
        return new ServerChallenge(
                username,
                verification,
                HelperUtil.randomBytes(16));
    }
}
