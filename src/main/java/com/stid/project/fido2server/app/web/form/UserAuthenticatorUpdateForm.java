package com.stid.project.fido2server.app.web.form;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
public class UserAuthenticatorUpdateForm {
    @NotBlank
    @Length(min = 1, max = 200)
    private String name;
}
