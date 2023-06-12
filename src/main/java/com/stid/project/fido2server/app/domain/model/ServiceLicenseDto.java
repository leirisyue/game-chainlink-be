package com.stid.project.fido2server.app.domain.model;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
public class ServiceLicenseDto {
    private QuantityDto user;
    private QuantityDto subdomain;
    private QuantityDto port;
    private DurationDto time;
    private List<PackageDto> packages;

    @Getter
    @Setter
    public static class QuantityDto {
        private long totalQuantity;
        private long usedQuantity;
        private long freeQuantity;

        public QuantityDto(long totalQuantity, long usedQuantity) {
            this.totalQuantity = totalQuantity;
            this.usedQuantity = usedQuantity;
            this.freeQuantity = totalQuantity - usedQuantity;
        }
    }

    @Getter
    @Setter
    public static class DurationDto {
        private Instant expiration;
        private long remainingSeconds;

        public DurationDto(Instant expiration, long remainingSeconds) {
            this.expiration = expiration;
            this.remainingSeconds = remainingSeconds;
        }
    }
}
