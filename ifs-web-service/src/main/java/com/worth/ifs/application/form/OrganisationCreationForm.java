package com.worth.ifs.application.form;

import com.worth.ifs.organisation.resource.OrganisationSearchResult;
import com.worth.ifs.user.domain.OrganisationTypeEnum;
import com.worth.ifs.user.resource.OrganisationTypeResource;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Object to store the data that is use form the company house form, while creating a new application.
 */
public class OrganisationCreationForm implements Serializable {
    @Valid
    private AddressForm addressForm = new AddressForm();
    private boolean triedToSave = false;

    @NotNull
    private OrganisationTypeResource organisationType;
    private OrganisationTypeEnum organisationTypeEnum;
    @NotEmpty
    // on empty value don't check pattern since then there already is a validation message.
    @Pattern(regexp = "^$|^[A-Za-z0-9 _\\&-,.:;\\@]+$", message = "Please enter valid characters")
    private String organisationSearchName;
    private String searchOrganisationId;
    private boolean organisationSearching;
    private boolean manualEntry = false;
    private boolean useSearchResultAddress = false;
    private transient List<OrganisationSearchResult> organisationSearchResults;
    @NotEmpty
    private String organisationName;

    public OrganisationCreationForm() {
        this.organisationSearchResults = new ArrayList<>();

    }

    public OrganisationCreationForm(List<OrganisationSearchResult> companyHouseList) {
        this.organisationSearchResults = companyHouseList;
    }

    public boolean isManualEntry() {
        return manualEntry;
    }

    public void setManualEntry(boolean manualEntry) {
        this.manualEntry = manualEntry;
    }

    public OrganisationTypeResource getOrganisationType() {
        return organisationType;
    }

    public void setOrganisationType(OrganisationTypeResource organisationType) {
        this.organisationType = organisationType;
        this.organisationTypeEnum = OrganisationTypeEnum.getFromId(organisationType.getId());
    }

    public String getOrganisationName() {
        return organisationName;
    }

    public void setOrganisationName(String organisationName) {
        this.organisationName = organisationName;
    }

    public String getOrganisationSearchName() {
        return organisationSearchName;
    }

    public void setOrganisationSearchName(String organisationSearchName) {
        this.organisationSearchName = organisationSearchName;
    }

    public boolean isOrganisationSearching() {
        return organisationSearching;
    }

    public void setOrganisationSearching(boolean organisationSearching) {
        this.organisationSearching = organisationSearching;
    }

    public List<OrganisationSearchResult> getOrganisationSearchResults() {
        return organisationSearchResults;
    }

    public void setOrganisationSearchResults(List<OrganisationSearchResult> organisationSearchResults) {
        this.organisationSearchResults = organisationSearchResults;
    }

    public String getSearchOrganisationId() {
        return searchOrganisationId;
    }

    public void setSearchOrganisationId(String searchOrganisationId) {
        this.searchOrganisationId = searchOrganisationId;
    }

    public AddressForm getAddressForm() {
        return addressForm;
    }

    public void setAddressForm(AddressForm addressForm) {
        this.addressForm = addressForm;
    }

    public boolean isTriedToSave() {
        return triedToSave;
    }

    public void setTriedToSave(boolean triedToSave) {
        this.triedToSave = triedToSave;
    }

    public boolean isUseSearchResultAddress() {
        return useSearchResultAddress;
    }

    public void setUseSearchResultAddress(boolean useSearchResultAddress) {
        this.useSearchResultAddress = useSearchResultAddress;
    }

    public OrganisationTypeEnum getOrganisationTypeEnum() {
        return organisationTypeEnum;
    }
}
