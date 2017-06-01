package org.innovateuk.ifs.application.viewmodel.section;

import org.innovateuk.ifs.applicant.resource.ApplicantSectionResource;
import org.innovateuk.ifs.application.resource.SectionResource;
import org.innovateuk.ifs.application.viewmodel.AbstractApplicationFormViewModel;
import org.innovateuk.ifs.application.viewmodel.NavigationViewModel;
import org.innovateuk.ifs.application.viewmodel.forminput.AbstractFormInputViewModel;

import java.util.List;

/**
 * Generic ViewModel for common fields in SectionViewModels
 */
public abstract class AbstractSectionViewModel extends AbstractApplicationFormViewModel<ApplicantSectionResource> {

    public AbstractSectionViewModel(ApplicantSectionResource applicantResource, List<AbstractFormInputViewModel> formInputViewModels, NavigationViewModel navigationViewModel, boolean allReadOnly) {
        super(applicantResource, formInputViewModels, navigationViewModel, allReadOnly);
    }

    @Override
    public String getTitle() {
        return applicantResource.getSection().getName();
    }

    public SectionResource getSection() { return applicantResource.getSection(); }

}
