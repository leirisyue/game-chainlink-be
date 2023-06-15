package com.stid.project.fido2server.app.web.form;

import com.stid.project.fido2server.app.domain.entity.RelyingParty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class EndpointInfoUpdateForm {
    @NotNull
    private Set<RelyingParty.Subdomain> subdomains;
    @NotNull
    private Set<Integer> ports;
}
