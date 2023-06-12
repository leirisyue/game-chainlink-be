package com.stid.project.fido2server.app.web.form;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class RelyingPartyUpdateForm {
    private Set<String> subdomains;
    private Set<Integer> ports;
}
