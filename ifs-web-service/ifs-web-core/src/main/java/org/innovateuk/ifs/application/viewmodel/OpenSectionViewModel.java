package org.innovateuk.ifs.application.viewmodel;

import org.innovateuk.ifs.applicant.resource.ApplicantSectionResource;
import org.innovateuk.ifs.application.viewmodel.forminput.AbstractFormInputViewModel;
import org.innovateuk.ifs.application.viewmodel.section.AbstractSectionViewModel;

import java.util.List;

/**
 * View model extending the {@link AbstractSectionViewModel} for open sections (not finance, but used by finances overview)
 */
public class OpenSectionViewModel extends AbstractSectionViewModel {

    public OpenSectionViewModel(ApplicantSectionResource applicantResource, List<AbstractFormInputViewModel> formInputViewModels, NavigationViewModel navigationViewModel, boolean allReadOnly) {
        super(applicantResource, formInputViewModels, navigationViewModel, allReadOnly);
    }
}
