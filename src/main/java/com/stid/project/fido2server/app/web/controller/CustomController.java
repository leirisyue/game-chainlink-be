package com.stid.project.fido2server.app.web.controller;

import com.stid.project.fido2server.app.domain.entity.RelyingParty;
import com.stid.project.fido2server.app.domain.entity.UserAccount;
import com.stid.project.fido2server.app.domain.model.RelyingPartyDto;
import com.stid.project.fido2server.app.domain.model.UserAccountDto;
import com.stid.project.fido2server.app.repository.RelyingPartyRepository;
import com.stid.project.fido2server.app.security.CurrentSpringUser;
import com.stid.project.fido2server.app.security.SpringUser;
import com.stid.project.fido2server.app.service.CustomService;
import com.stid.project.fido2server.app.web.form.CustomCreateForm;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "CUSTOMER")
@RestController
@RequestMapping("/api/customer")
public class CustomController extends AbstractUnsecuredController {
    private final CustomService customService;
    public CustomController(CustomService customService, RelyingPartyRepository relyingPartyRepository) {
        this.customService = customService;
    }

    @Operation(summary = "Create new customer")
    @PostMapping("/createUser")
    public ResponseEntity<UserAccountDto> createCustomAccount(
            @RequestBody @Valid CustomCreateForm form, @CurrentSpringUser SpringUser springUser) {
        UserAccount userAccount = customService.createCustomAccount(form, form.getRelyingPartyId());
        return ResponseEntity.ok(mapperService.mapping(userAccount));
    }

    @Operation(summary = "Get list replying party")
    @GetMapping("/relying-party")
    public ResponseEntity<List<RelyingPartyDto>> getRelyingPartyCustom() {
        List<RelyingParty> relyingParties = customService.findAllRelyingParty();
        return ResponseEntity.ok(relyingParties.stream().map(mapperService::mapping).toList());
    }


}
