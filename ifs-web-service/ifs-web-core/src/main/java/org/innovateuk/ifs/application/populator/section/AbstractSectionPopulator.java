package org.innovateuk.ifs.application.populator.section;

import org.innovateuk.ifs.applicant.resource.ApplicantSectionResource;
import org.innovateuk.ifs.application.form.ApplicationForm;
import org.innovateuk.ifs.application.populator.ApplicationNavigationPopulator;
import org.innovateuk.ifs.application.populator.BaseModelPopulator;
import org.innovateuk.ifs.application.resource.SectionType;
import org.innovateuk.ifs.application.viewmodel.NavigationViewModel;
import org.innovateuk.ifs.application.viewmodel.section.AbstractSectionViewModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import java.util.Collections;

/**
 * Abstract populator section view models.
 */
public abstract class AbstractSectionPopulator<M extends AbstractSectionViewModel> extends BaseModelPopulator {

    @Autowired
    private ApplicationNavigationPopulator navigationPopulator;

    public M populate(ApplicantSectionResource section, ApplicationForm form, Model model, BindingResult bindingResult, boolean readOnly) {
        M viewModel = createNew(section, form, readOnly);
        populate(section, form, viewModel, model, bindingResult, readOnly);
        return viewModel;
    }

    protected abstract void populate(ApplicantSectionResource section, ApplicationForm form, M viewModel, Model model, BindingResult bindingResult, boolean readOnly);
    protected abstract M createNew(ApplicantSectionResource section, ApplicationForm form, boolean readOnly);

    public abstract SectionType getSectionType();

    protected NavigationViewModel getNavigationViewModel(ApplicantSectionResource applicantSection) {
        return navigationPopulator.addNavigation(applicantSection.getSection(),
                applicantSection.getApplication().getId(),
                applicantSection.getCurrentApplicant() != null ? SectionType.sectionsNotRequiredForOrganisationType(applicantSection.getCurrentApplicant().getOrganisation().getOrganisationType()) : Collections.emptyList());

    }
}
