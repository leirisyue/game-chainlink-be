package com.stid.project.fido2server.app.web.controller;

import com.stid.project.fido2server.app.domain.constant.PackageType;
import com.stid.project.fido2server.app.domain.entity.Authenticator;
import com.stid.project.fido2server.app.domain.entity.Package;
import com.stid.project.fido2server.app.domain.entity.RelyingParty;
import com.stid.project.fido2server.app.domain.entity.UserAccount;
import com.stid.project.fido2server.app.repository.AuthenticatorRepository;
import com.stid.project.fido2server.app.repository.RelyingPartyRepository;
import com.stid.project.fido2server.app.repository.UserAccountRepository;
import com.stid.project.fido2server.app.service.PackageService;
import com.stid.project.fido2server.app.service.SystemService;
import com.stid.project.fido2server.app.web.form.PackageCreateForm;
import com.stid.project.fido2server.app.web.form.RelyingPartyCreateForm;
import com.stid.project.fido2server.fido2.service.Fido2ServerChallengeManager;
import com.stid.project.fido2server.fido2.util.ServletUtil;
import com.webauthn4j.data.client.Origin;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;

@Tag(name = "DEV")
@RestController
@RequestMapping("/dev")
public class DevController extends AbstractUnsecuredController {
    private final RelyingPartyRepository relyingPartyRepository;
    private final UserAccountRepository userAccountRepository;
    private final AuthenticatorRepository authenticatorRepository;
    private final SystemService systemService;
    private final PackageService packageService;
    private final Fido2ServerChallengeManager challengeManager;

    public DevController(RelyingPartyRepository relyingPartyRepository, UserAccountRepository userAccountRepository, AuthenticatorRepository authenticatorRepository, SystemService systemService, PackageService packageService, Fido2ServerChallengeManager challengeManager) {
        this.relyingPartyRepository = relyingPartyRepository;
        this.userAccountRepository = userAccountRepository;
        this.authenticatorRepository = authenticatorRepository;
        this.systemService = systemService;
        this.packageService = packageService;
        this.challengeManager = challengeManager;
    }

    @GetMapping("/findAllRelyingParty")
    public ResponseEntity<List<RelyingParty>> findAllRelyingParty() {
        return ResponseEntity.ok(relyingPartyRepository.findAll());
    }

    @GetMapping("/findAllUserAccount")
    public ResponseEntity<List<UserAccount>> findAllUserAccount() {
        return ResponseEntity.ok(userAccountRepository.findAll());
    }

    @GetMapping("/findAllAuthenticator")
    public ResponseEntity<List<Authenticator>> findAllAuthenticator() {
        return ResponseEntity.ok(authenticatorRepository.findAll());
    }

    @GetMapping("/createLocalTestRelyingParty")
    public ResponseEntity<RelyingParty> createLocalTestRelyingParty(HttpServletRequest request) {
        LOGGER.debug("createRelyingParty...");
        RelyingPartyCreateForm form = new RelyingPartyCreateForm();
        form.setName("Local RP Test");
        form.setOrigin(ServletUtil.getOrigin(request));
        form.setDescription("Local RP Test Description");

        RelyingParty relyingParty = systemService.createRelyingParty(form);

        PackageCreateForm packageTimeForm = new PackageCreateForm();
        packageTimeForm.setRelyingPartyId(relyingParty.getId());
        packageTimeForm.setType(PackageType.TIME);
        packageTimeForm.setAmount(Duration.ofDays(30).toMillis());
        Package packageTime = packageService.createPackage(packageTimeForm);

        PackageCreateForm packageUserForm = new PackageCreateForm();
        packageUserForm.setRelyingPartyId(relyingParty.getId());
        packageUserForm.setType(PackageType.USER);
        packageUserForm.setAmount(1000);
        Package packageUser = packageService.createPackage(packageUserForm);

        PackageCreateForm packageSubdomainForm = new PackageCreateForm();
        packageSubdomainForm.setRelyingPartyId(relyingParty.getId());
        packageSubdomainForm.setType(PackageType.SUBDOMAIN);
        packageSubdomainForm.setAmount(10);
        Package packageSubdomain = packageService.createPackage(packageSubdomainForm);

        PackageCreateForm packagePortForm = new PackageCreateForm();
        packagePortForm.setRelyingPartyId(relyingParty.getId());
        packagePortForm.setType(PackageType.PORT);
        packagePortForm.setAmount(10);
        Package packagePort = packageService.createPackage(packagePortForm);

        packageService.activatePackage(packageTime.getId());
        packageService.activatePackage(packageUser.getId());
        packageService.activatePackage(packageSubdomain.getId());
        packageService.activatePackage(packagePort.getId());

        return ResponseEntity.ok(relyingParty);
    }

    @GetMapping("/getChallengeCacheServiceCounter")
    public ResponseEntity<Long> getChallengeCacheServiceCounter() {
        return ResponseEntity.ok(challengeManager.getChallengeCacheService().getCacheCounter());
    }

    @GetMapping("/checkOrigin")
    public ResponseEntity<HashMap<String, Object>> checkOrigin(@RequestParam String url) {
        Origin origin = new Origin(url);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("Scheme", origin.getScheme());
        hashMap.put("Host", origin.getHost());
        hashMap.put("Port", origin.getPort());
        hashMap.put("URL", origin.toString());
        return ResponseEntity.ok(hashMap);
    }

}
