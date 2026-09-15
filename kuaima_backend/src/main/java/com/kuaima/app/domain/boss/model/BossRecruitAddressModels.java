package com.kuaima.app.domain.boss.model;

public final class BossRecruitAddressModels {
    private BossRecruitAddressModels() {}

    public record AddressRequest(String name, String contactName, String contactPhone, String city,
            String district, String detail, Double latitude, Double longitude, Boolean isDefault) {}

    public record AddressView(Long id, String name, String contactName, String contactPhone, String city,
            String district, String detail, Double latitude, Double longitude, Boolean isDefault) {}
}
