package com.stid.project.fido2server.app.web.controller;

import com.stid.project.fido2server.app.service.MapperService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractBaseController {
    protected final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    @Autowired
    protected MapperService mapperService;
}
