package com.stid.project.fido2server.app.web.controller;

import com.stid.project.fido2server.app.domain.constant.Status;
import com.stid.project.fido2server.app.domain.entity.Authenticator;
import com.stid.project.fido2server.app.domain.entity.Package;
import com.stid.project.fido2server.app.domain.entity.RelyingParty;
import com.stid.project.fido2server.app.domain.entity.UserAccount;
import com.stid.project.fido2server.app.domain.model.*;
import com.stid.project.fido2server.app.security.JwtTokenScope;
import com.stid.project.fido2server.app.service.PackageService;
import com.stid.project.fido2server.app.service.SystemService;
import com.stid.project.fido2server.app.service.UserAccountService;
import com.stid.project.fido2server.app.web.form.PackageCreateForm;
import com.stid.project.fido2server.app.web.form.PackageUpdateForm;
import com.stid.project.fido2server.app.web.form.RelyingPartyCreateForm;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Tag(name = "SYSTEM")
@RestController
@RequestMapping("/api/system")
@Secured(value = JwtTokenScope.ROLE_SYSTEM)
public class SystemController extends AbstractSecuredController {
    private final SystemService systemService;
    private final PackageService packageService;
    private final UserAccountService userAccountService;

    public SystemController(SystemService systemService, PackageService packageService, UserAccountService userAccountService) {
        this.systemService = systemService;
        this.packageService = packageService;
        this.userAccountService = userAccountService;
    }

    @Operation(summary = "Trạng thái sử dụng hệ thống: số lượng dịch vụ, tài khoản, thiết bị đã đăng ký, gói dịch vụ đã tạo, đã kích hoạt")
    @GetMapping("/usage")
    public ResponseEntity<SystemUsageDto> getSystemUsage() {
        LOGGER.debug("getSystemUsage...");
        SystemUsageDto systemUsage = systemService.getSystemUsage();
        return ResponseEntity.ok(systemUsage);
    }

    @Operation(summary = "Danh sách dịch vụ đã đăng ký")
    @GetMapping("/relying-party")
    public ResponseEntity<List<RelyingPartyDto>> getRelyingParty() {
        List<RelyingParty> relyingParties = systemService.findAllRelyingParty();
        return ResponseEntity.ok(relyingParties.stream().map(mapperService::mapping).toList());
    }

    @Operation(summary = "Đăng ký dịch vụ")
    @PostMapping("/relying-party")
    public ResponseEntity<RelyingPartyDto> createRelyingParty(@RequestBody @Valid RelyingPartyCreateForm form) {
        LOGGER.debug("createRelyingParty...");
        RelyingParty relyingParty = systemService.createRelyingParty(form);
        return ResponseEntity.ok(mapperService.mapping(relyingParty));
    }

    @Operation(summary = "Kích hoạt sử dụng dịch vụ")
    @PutMapping("/relying-party/{id}/active")
    public ResponseEntity<RelyingPartyDto> updateRelyingPartyStatusActive(@PathVariable UUID id) {
        LOGGER.debug("updateRelyingPartyStatusActive...");
        RelyingParty relyingParty = systemService.updateRelyingPartyStatus(id, Status.ACTIVE);
        return ResponseEntity.ok(mapperService.mapping(relyingParty));
    }

    @Operation(summary = "Hủy kích hoạt dụng dịch vụ")
    @PutMapping("/relying-party/{id}/inactive")
    public ResponseEntity<RelyingPartyDto> updateRelyingPartyStatusInactive(@PathVariable UUID id) {
        LOGGER.debug("updateRelyingPartyStatusInactive...");
        RelyingParty relyingParty = systemService.updateRelyingPartyStatus(id, Status.INACTIVE);
        return ResponseEntity.ok(mapperService.mapping(relyingParty));
    }

    @Operation(summary = "Xóa dịch vụ đã hủy kích hoạt")
    @DeleteMapping("/relying-party/{id}")
    public ResponseEntity<RelyingPartyDto> deleteRelyingParty(@PathVariable UUID id) {
        LOGGER.debug("deleteRelyingParty...");
        RelyingParty relyingParty = systemService.deleteRelyingParty(id);
        return ResponseEntity.ok(mapperService.mapping(relyingParty));
    }

    @Operation(summary = "Danh sách các gói dịch vụ (đã kích hoạt) của 1 dịch vụ")
    @GetMapping("/relying-party/{id}/license")
    public ResponseEntity<ServiceLicenseDto> getRelyingPartyServiceLicense(@PathVariable UUID id) {
        LOGGER.debug("getRelyingPartyServiceLicense...");
        ServiceLicenseDto serviceLicense = packageService.calculateServiceLicense(id);
        return ResponseEntity.ok(serviceLicense);
    }

    @Operation(summary = "Danh sách các gói dịch vụ (đã kích hoạt và chưa kích hoạt) của 1 dịch vụ")
    @GetMapping("/relying-party/{id}/packages")
    public ResponseEntity<List<PackageDto>> getRelyingPartyPackage(@PathVariable UUID id) {
        LOGGER.debug("getRelyingPartyPackage...");
        List<Package> packages = packageService.findAllByRelyingPartyId(id);
        return ResponseEntity.ok(packages.stream().map(mapperService::mapping).toList());
    }

    @Operation(summary = "Danh sách các tài khoản người dùng của 1 dịch vụ")
    @GetMapping("/relying-party/{id}/user-accounts")
    public ResponseEntity<List<UserAccountDto>> getRelyingPartyUserAccount(@PathVariable UUID id) {
        LOGGER.debug("getRelyingPartyUserAccount...");
        List<UserAccount> userAccounts = systemService.findAllUserAccount(id);
        return ResponseEntity.ok(userAccounts.stream().map(mapperService::mapping).toList());
    }

    @Operation(summary = "Danh sách các thiết bị người dùng đã thêm của 1 dịch vụ")
    @GetMapping("/relying-party/{id}/user-authenticators")
    public ResponseEntity<List<AuthenticatorDto>> getRelyingPartyUserAuthenticator(@PathVariable UUID id) {
        List<Authenticator> userCredentials = systemService.findAllUserAuthenticator(id);
        return ResponseEntity.ok(userCredentials.stream().map(mapperService::mapping).toList());
    }

    @Operation(summary = "Danh sách các gói dịch vụ đã khởi tạo trên hệ thống")
    @GetMapping("/packages")
    public ResponseEntity<List<PackageDto>> getPackage() {
        LOGGER.debug("getPackage...");
        List<Package> packages = packageService.findAll();
        return ResponseEntity.ok(packages.stream().map(mapperService::mapping).toList());
    }

    @Operation(summary = "Khởi tạo gói dịch vụ cung cấp cho 1 dịch vụ")
    @PostMapping("/packages")
    public ResponseEntity<PackageDto> createPackage(@Valid @RequestBody PackageCreateForm form) {
        LOGGER.debug("createPackage...");
        Package aPackage = packageService.createPackage(form);
        return ResponseEntity.ok(mapperService.mapping(aPackage));
    }

    @Operation(summary = "Cập nhật thông tin gói dịch vụ")
    @PutMapping("/packages/{id}")
    public ResponseEntity<PackageDto> updatePackage(
            @PathVariable UUID id, @Valid @RequestBody PackageUpdateForm form) {
        LOGGER.debug("updatePackage...");
        Package aPackage = packageService.updatePackage(id, form);
        return ResponseEntity.ok(mapperService.mapping(aPackage));
    }

    @Operation(summary = "Kích hoạt gói dịch vụ")
    @PutMapping("/packages/{id}/activate")
    public ResponseEntity<PackageDto> activatePackage(@PathVariable UUID id) {
        LOGGER.debug("activatePackage...");
        Package aPackage = packageService.activatePackage(id);
        return ResponseEntity.ok(mapperService.mapping(aPackage));
    }

    @Operation(summary = "Xóa gói dịch vụ chưa kích hoạt")
    @DeleteMapping("/packages/{id}")
    public ResponseEntity<PackageDto> deletePackage(@PathVariable UUID id) {
        LOGGER.debug("deletePackage...");
        Package aPackage = packageService.deletePackage(id);
        return ResponseEntity.ok(mapperService.mapping(aPackage));
    }

    @Operation(summary = "Danh sách các tài khoản Test FIDO2-Server")
    @GetMapping("/webauthn/accounts")
    public ResponseEntity<List<UserAccountDto>> webAuthnGetAllUserAccount() {
        LOGGER.debug("webAuthnGetAllUserAccount...");
        List<UserAccount> userAccounts = userAccountService.findAllUserAccount(null);
        return ResponseEntity.ok(userAccounts.stream().map(mapperService::mapping).toList());
    }

    @Operation(summary = "Xóa tất cả các tài khoản Test FIDO2-Server")
    @DeleteMapping("/webauthn/accounts")
    public ResponseEntity<List<UserAccountDto>> webAuthnDeleteAllUserAccount() {
        LOGGER.debug("webAuthnDeleteAllUserAccount...");
        List<UserAccount> userAccounts = userAccountService.deleteUserAccount(null);
        return ResponseEntity.ok(userAccounts.stream().map(mapperService::mapping).toList());
    }

    @Operation(summary = "Danh sách các thiết bị Test FIDO2-Server")
    @GetMapping("/webauthn/authenticators")
    public ResponseEntity<List<AuthenticatorDto>> webAuthnGetAllUserAuthenticator() {
        LOGGER.debug("webAuthnGetAllUserAuthenticator...");
        List<Authenticator> authenticators = userAccountService.findAllUserAccount(null)
                .stream()
                .map(UserAccount::getId)
                .map(userAccountService::findAllUserAuthenticator)
                .flatMap(Collection::stream)
                .toList();
        return ResponseEntity.ok(authenticators.stream().map(mapperService::mapping).toList());
    }

}
