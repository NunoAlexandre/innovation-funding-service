package com.worth.ifs.project.grantofferletter.form;

import com.worth.ifs.controller.BaseBindingResultTarget;
import org.springframework.web.multipart.MultipartFile;

/**
 * Form backing the Grant offer letter page
 **/
public class ProjectInternalGOLForm extends BaseBindingResultTarget {

    private MultipartFile additionalContract;

    public MultipartFile getAdditionalContract() {
        return additionalContract;
    }

    public void setAdditionalContract(MultipartFile additionalContract) {
        this.additionalContract = additionalContract;
    }

}
