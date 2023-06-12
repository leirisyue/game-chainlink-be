package com.stid.project.fido2server.fido2.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.stid.project.fido2server.app.domain.entity.Authenticator;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AttestationResultResponse extends ServerResponseBase {
    @JsonIgnore
    private Authenticator authenticator;
}
