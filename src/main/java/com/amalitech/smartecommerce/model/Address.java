package com.amalitech.smartecommerce.model;

import java.util.UUID;

public class Address {
    private UUID id;
    private String unitNumber;
    private String streetNumber;
    private String addressLine;
    private String city;
    private String region;
    private UUID countryId;
    // Add postalCode and addressType if needed for scalability
    // private String postalCode;
    // private String addressType;

    public Address() {}

    public Address(UUID id, String unitNumber, String streetNumber, String addressLine, String city, String region, UUID countryId) {
        this.id = id;
        this.unitNumber = unitNumber;
        this.streetNumber = streetNumber;
        this.addressLine = addressLine;
        this.city = city;
        this.region = region;
        this.countryId = countryId;
        // this.postalCode = postalCode;
        // this.addressType = addressType;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getUnitNumber() { return unitNumber; }
    public void setUnitNumber(String unitNumber) { this.unitNumber = unitNumber; }
    public String getStreetNumber() { return streetNumber; }
    public void setStreetNumber(String streetNumber) { this.streetNumber = streetNumber; }
    public String getAddressLine() { return addressLine; }
    public void setAddressLine(String addressLine) { this.addressLine = addressLine; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public UUID getCountryId() { return countryId; }
    public void setCountryId(UUID countryId) { this.countryId = countryId; }
    // public String getPostalCode() { return postalCode; }
    // public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
    // public String getAddressType() { return addressType; }
    // public void setAddressType(String addressType) { this.addressType = addressType; }
}
