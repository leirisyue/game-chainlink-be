package com.stid.project.fido2server.app.web.form;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SystemLoginForm {
    @NotBlank
    private String account;

    @NotBlank
    private String password;

    private boolean remember;
}
