package com.amalitech.smartecommerce.model;

import java.util.UUID;

public class Country {
    private UUID id;
    private String countryName;
    public Country() {}

    public Country(UUID id, String countryName) {
        this.id = id;
        this.countryName = countryName;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getCountryName() { return countryName; }
    public void setCountryName(String countryName) { this.countryName = countryName; }
}

