package com.worth.ifs.application.form;

import com.worth.ifs.address.resource.AddressResource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

/**
 * Object to store the data that is use form the company house form, while creating a new application.
 */
public class ConfirmCompanyDetailsForm extends CreateApplicationForm{
    private final Log log = LogFactory.getLog(getClass());

    @NotEmpty
    private String postcodeInput;
    private String selectedPostcodeIndex;
    private AddressResource selectedPostcode = null;
    @Valid
    private List<AddressResource> postcodeOptions;
    private boolean useCompanyHouseAddress = false;
    private boolean manualAddress = false;

    public ConfirmCompanyDetailsForm() {
        postcodeOptions = new ArrayList<>();
    }

    public String getPostcodeInput() {
        return postcodeInput;
    }

    public void setPostcodeInput(String postcodeInput) {
        this.postcodeInput = postcodeInput;
    }

    public List<AddressResource> getPostcodeOptions() {
        return postcodeOptions;
    }

    public void setPostcodeOptions(List<AddressResource> postcodeOptions) {
        this.postcodeOptions = postcodeOptions;
    }

    public String getSelectedPostcodeIndex() {
        return selectedPostcodeIndex;
    }

    public void setSelectedPostcodeIndex(String selectedPostcodeIndex) {
        this.selectedPostcodeIndex = selectedPostcodeIndex;
    }

    public AddressResource getSelectedPostcode() {
        if(selectedPostcode == null){
            if(getSelectedPostcodeIndex() == null || getSelectedPostcodeIndex().equals("")){
                log.warn("Returning new postcode a");
                selectedPostcode = new AddressResource();
            }else{
                int indexInt = Integer.parseInt(getSelectedPostcodeIndex());
                if(postcodeOptions == null || postcodeOptions.size() <= indexInt ||postcodeOptions.get(indexInt) == null){
                    log.warn("Returning new postcode b");
                    return new AddressResource();
                }else{
                    selectedPostcode = postcodeOptions.get(indexInt);
                }
            }
        }
        if(selectedPostcode == null){
            log.warn("Returning null postcode");
        }
        return selectedPostcode;
    }

    public void setSelectedPostcode(AddressResource selectedPostcode) {
        this.selectedPostcode = selectedPostcode;
    }

    public boolean isUseCompanyHouseAddress() {
        return useCompanyHouseAddress;
    }

    public void setUseCompanyHouseAddress(boolean useCompanyHouseAddress) {
        this.useCompanyHouseAddress = useCompanyHouseAddress;
    }

    public boolean isManualAddress() {
        return manualAddress;
    }

    public void setManualAddress(boolean manualAddress) {
        this.manualAddress = manualAddress;
    }
}
