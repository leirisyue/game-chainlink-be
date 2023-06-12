package com.stid.project.fido2server.app.web.form;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserAccountCreateForm {
    @NotBlank
    private String username;

    @NotBlank
    private String password;

    @NotBlank
    private String displayName;
}
