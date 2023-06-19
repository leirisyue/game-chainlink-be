package com.stid.project.fido2server.app.web.form;

import com.webauthn4j.data.client.Origin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RelyingPartyCreateForm {
    @NotBlank
    private String name;

    @NotNull
    private Origin origin;

    private String email;

    private Integer phone;

    private String description;
}
