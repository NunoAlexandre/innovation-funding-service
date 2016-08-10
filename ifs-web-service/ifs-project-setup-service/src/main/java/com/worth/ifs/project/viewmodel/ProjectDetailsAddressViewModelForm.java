package com.worth.ifs.project.viewmodel;

import com.worth.ifs.address.resource.OrganisationAddressType;
import com.worth.ifs.form.AddressForm;
import com.worth.ifs.controller.BaseBindingResultTarget;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class ProjectDetailsAddressViewModelForm  extends BaseBindingResultTarget {
    @NotNull(message = "You need to select a project address before you can continue")
    private OrganisationAddressType addressType;

    @Valid
    private AddressForm addressForm = new AddressForm();

    // for spring form binding
    public ProjectDetailsAddressViewModelForm() {
    }

    public AddressForm getAddressForm() {
        return addressForm;
    }

    public OrganisationAddressType getAddressType() {
        return addressType;
    }

    public void setAddressType(OrganisationAddressType addressType) {
        this.addressType = addressType;
    }
}
