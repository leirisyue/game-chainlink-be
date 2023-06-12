package com.stid.project.fido2server.app.web.controller;

import com.stid.project.fido2server.app.config.OpenAPIConfiguration;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@SecurityRequirement(name = OpenAPIConfiguration.SECURITY_SCHEMA_NAME)
public abstract class AbstractSecuredController extends AbstractBaseController {
}
