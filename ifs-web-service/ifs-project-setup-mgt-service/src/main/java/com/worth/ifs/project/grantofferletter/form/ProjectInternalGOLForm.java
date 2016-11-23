package com.worth.ifs.project.grantofferletter.form;

import com.worth.ifs.controller.BaseBindingResultTarget;
import org.springframework.web.multipart.MultipartFile;

/**
 * Form backing the Grant offer letter page
 **/
public class ProjectInternalGOLForm extends BaseBindingResultTarget {

    private MultipartFile grantOfferLetter;
    private MultipartFile additionalContract;


    public MultipartFile getGrantOfferLetter() {
        return grantOfferLetter;
    }

    public void setGrantOfferLetter(MultipartFile grantOfferLetter) {
        this.grantOfferLetter = grantOfferLetter;
    }

    public MultipartFile getAdditionalContract() {
        return additionalContract;
    }

    public void setAdditionalContract(MultipartFile additionalContract) {
        this.additionalContract = additionalContract;
    }

}
