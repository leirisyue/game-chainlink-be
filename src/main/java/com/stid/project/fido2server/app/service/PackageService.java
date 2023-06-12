package com.stid.project.fido2server.app.service;

import com.stid.project.fido2server.app.domain.constant.PackageType;
import com.stid.project.fido2server.app.domain.entity.Package;
import com.stid.project.fido2server.app.domain.entity.RelyingParty;
import com.stid.project.fido2server.app.domain.model.ServiceLicenseDto;
import com.stid.project.fido2server.app.exception.AbstractExceptionHandler;
import com.stid.project.fido2server.app.repository.PackageRepository;
import com.stid.project.fido2server.app.repository.RelyingPartyRepository;
import com.stid.project.fido2server.app.repository.UserAccountRepository;
import com.stid.project.fido2server.app.web.form.PackageCreateForm;
import com.stid.project.fido2server.app.web.form.PackageUpdateForm;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class PackageService extends AbstractExceptionHandler {
    private final RelyingPartyRepository relyingPartyRepository;
    private final UserAccountRepository userAccountRepository;
    private final PackageRepository packageRepository;
    private final MapperService mapperService;

    public PackageService(RelyingPartyRepository relyingPartyRepository, UserAccountRepository userAccountRepository, PackageRepository packageRepository, MapperService mapperService) {
        this.relyingPartyRepository = relyingPartyRepository;
        this.userAccountRepository = userAccountRepository;
        this.packageRepository = packageRepository;
        this.mapperService = mapperService;
    }

    public List<Package> findAll() {
        return packageRepository.findAll();
    }

    public List<Package> findAllByRelyingPartyId(UUID relyingPartyId) {
        return packageRepository.findByRelyingPartyId(relyingPartyId);
    }

    public List<Package> findAllActivatedByRelyingPartyId(UUID relyingPartyId) {
        return packageRepository.findByRelyingPartyIdAndActivatedDateNotNull(relyingPartyId);
    }

    @Transactional
    public Package createPackage(PackageCreateForm form) {
        relyingPartyRepository.findById(form.getRelyingPartyId())
                .orElseThrow(() -> badRequest("Exception.RelyingPartyNotFound"));
        Package aPackage = new Package();
        aPackage.setType(form.getType());
        aPackage.setAmount(form.getAmount());
        aPackage.setDescription(form.getDescription());
        aPackage.setRelyingPartyId(form.getRelyingPartyId());
        aPackage.setCreatedDate(Instant.now());
        aPackage = packageRepository.save(aPackage);
        return aPackage;
    }

    @Transactional
    public Package updatePackage(UUID id, PackageUpdateForm form) {
        Package aPackage = packageRepository.findById(id)
                .orElseThrow(() -> badRequest("Exception.PackageNotFound"));
        if (aPackage.getActivatedDate() != null)
            throw badRequest("Exception.PackageActivated");
        aPackage.setType(form.getType());
        aPackage.setAmount(form.getAmount());
        aPackage.setDescription(form.getDescription());
        aPackage = packageRepository.save(aPackage);
        return aPackage;
    }

    @Transactional
    public Package activatePackage(UUID id) {
        Package aPackage = packageRepository.findById(id)
                .orElseThrow(() -> badRequest("Exception.PackageNotFound"));
        if (aPackage.getActivatedDate() != null)
            throw badRequest("Exception.PackageActivated");
        aPackage.setActivatedDate(Instant.now());
        aPackage = packageRepository.save(aPackage);
        return aPackage;
    }

    @Transactional
    public Package deletePackage(UUID id) {
        Package aPackage = packageRepository.findById(id)
                .orElseThrow(() -> badRequest("Exception.PackageNotFound"));
        if (aPackage.getActivatedDate() != null)
            throw badRequest("Exception.PackageActivated");
        packageRepository.delete(aPackage);
        return aPackage;
    }

    public ServiceLicenseDto calculateServiceLicense(UUID relyingPartyId) {
        Instant now = Instant.now();
        RelyingParty relyingParty = relyingPartyRepository.findById(relyingPartyId).orElse(null);
        List<Package> packages = packageRepository.findByRelyingPartyId(relyingPartyId);
        List<Package> activatedPackages = packages.stream().filter(p -> p.getActivatedDate() != null).toList();

        ServiceLicenseDto serviceLicense = new ServiceLicenseDto();
        serviceLicense.setTime(calculateLicenseTime(activatedPackages, now));
        serviceLicense.setUser(calculateLicenseUser(relyingParty, activatedPackages));
        serviceLicense.setSubdomain(calculateLicenseSubdomain(relyingParty, activatedPackages));
        serviceLicense.setPort(calculateLicensePort(relyingParty, activatedPackages));
        serviceLicense.setPackages(activatedPackages.stream().map(mapperService::mapping).toList());

        return serviceLicense;
    }

    public ServiceLicenseDto.QuantityDto calculateLicenseUser(RelyingParty relyingParty, List<Package> activatedPackages) {
        List<Package> userPackages = activatedPackages.stream()
                .filter(p -> p.getType() == PackageType.USER)
                .toList();
        long totalUserQuantity = userPackages.stream().map(Package::getAmount).reduce(0L, Long::sum);
        long usedUserQuantity = relyingParty != null ? userAccountRepository.countByRelyingPartyId(relyingParty.getId()) : 0;
        return new ServiceLicenseDto.QuantityDto(totalUserQuantity, usedUserQuantity);
    }

    public ServiceLicenseDto.QuantityDto calculateLicenseSubdomain(RelyingParty relyingParty, List<Package> activatedPackages) {
        List<Package> subdomainPackages = activatedPackages.stream()
                .filter(p -> p.getType() == PackageType.SUBDOMAIN)
                .toList();
        long totalSubdomainQuantity = subdomainPackages.stream().map(Package::getAmount).reduce(0L, Long::sum);
        long usedSubdomainQuantity = relyingParty != null ? relyingParty.getSubdomains().size() : 0;
        return new ServiceLicenseDto.QuantityDto(totalSubdomainQuantity, usedSubdomainQuantity);
    }

    public ServiceLicenseDto.QuantityDto calculateLicensePort(RelyingParty relyingParty, List<Package> activatedPackages) {
        List<Package> portPackages = activatedPackages.stream()
                .filter(p -> p.getType() == PackageType.PORT)
                .toList();
        long totalPortQuantity = portPackages.stream().map(Package::getAmount).reduce(0L, Long::sum);
        long usedPortQuantity = relyingParty != null ? relyingParty.getPorts().size() : 0;
        return new ServiceLicenseDto.QuantityDto(totalPortQuantity, usedPortQuantity);
    }

    public ServiceLicenseDto.DurationDto calculateLicenseTime(List<Package> activatedPackages, Instant calculateTime) {
        Instant now = calculateTime != null ? calculateTime : Instant.now();
        List<Package> durationPackages = activatedPackages.stream()
                .filter(p -> p.getType() == PackageType.TIME)
                .toList();
        Instant expiration = null;
        for (Package aPackage : durationPackages) {
            Instant activatedDate = aPackage.getActivatedDate();
            if (expiration == null || expiration.isBefore(activatedDate)) {
                expiration = activatedDate;
            }
            Duration packageDuration = Duration.ofMillis(aPackage.getAmount());
            expiration = expiration.plus(packageDuration);
        }
        Duration remaining = Duration.ZERO;
        if (expiration != null && expiration.isAfter(now)) {
            remaining = Duration.ofMillis(expiration.toEpochMilli() - now.toEpochMilli());
        }
        return new ServiceLicenseDto.DurationDto(expiration, remaining.toSeconds());
    }

    public void checkLicenseTime(UUID relyingPartyId) {
        if (relyingPartyId != null) {
            List<Package> activatedPackages = findAllActivatedByRelyingPartyId(relyingPartyId);
            ServiceLicenseDto.DurationDto licenseTime = calculateLicenseTime(activatedPackages, Instant.now());
            if (licenseTime.getRemainingSeconds() <= 0)
                throw forbidden("Exception.LicenseExpired");
        }
    }

    public void checkLicenseUser(UUID relyingPartyId) {
        if (relyingPartyId != null) {
            RelyingParty relyingParty = relyingPartyRepository.findById(relyingPartyId).orElse(null);
            List<Package> activatedPackages = findAllActivatedByRelyingPartyId(relyingPartyId);
            ServiceLicenseDto.QuantityDto licenseUser = calculateLicenseUser(relyingParty, activatedPackages);
            if (licenseUser.getFreeQuantity() <= 0)
                throw forbidden("Exception.UserQuantityReachedLimit");
        }
    }
}
