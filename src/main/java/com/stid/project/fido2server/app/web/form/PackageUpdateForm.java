package com.stid.project.fido2server.app.web.form;

import com.stid.project.fido2server.app.domain.constant.PackageType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PackageUpdateForm {
    @NotNull
    private PackageType type;

    @Min(0)
    private long amount;

    private String description;
}
