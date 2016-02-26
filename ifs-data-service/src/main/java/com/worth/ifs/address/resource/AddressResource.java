package com.worth.ifs.address.resource;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;

public class AddressResource {
    private Long id;

    private String addressLine1;
    private String addressLine2;
    private String addressLine3;
    private String town;
    private String county;
    private String postcode;
    private List<Long> organisations = new ArrayList<>();

    public AddressResource() {
    }

    public AddressResource(String addressLine1, String addressLine2, String addressLine3, String town, String county, String postcode) {
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.addressLine3 = addressLine3;
        this.town = town;
        this.county = county;
        this.postcode = postcode;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public String getAddressLine3() {
        return addressLine3;
    }

    public void setAddressLine3(String addressLine3) {
        this.addressLine3 = addressLine3;
    }

    public String getTown() {
        return town;
    }

    public void setTown(String town) {
        this.town = town;
    }

    public String getCounty() {
        return county;
    }

    public void setCounty(String county) {
        this.county = county;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    @JsonIgnore
    public String getCombinedString(){
        String[] location = new String[3];
        location[0] = getPostcode();
        location[1] = getAddressLine1();
        location[2] = getTown();
        return String.join(", ", location);

    }

    public Long getId() {
        return id;
    }

    @JsonIgnore
    public List<Long> getOrganisations() {
        return organisations;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setOrganisations(List<Long> organisations) {
        this.organisations = organisations;
    }
}
