package org.innovateuk.ifs.application.populator.finance;

import org.innovateuk.ifs.applicant.resource.AbstractApplicantResource;
import org.innovateuk.ifs.application.populator.forminput.AbstractFormInputPopulator;
import org.innovateuk.ifs.application.viewmodel.finance.OrganisationTurnoverViewModel;
import org.innovateuk.ifs.form.resource.FormInputType;
import org.springframework.stereotype.Component;

@Component
public class OrganisationTurnoverPopulator extends AbstractFormInputPopulator<OrganisationTurnoverViewModel> {

    @Override
    public FormInputType type() {
        return FormInputType.ORGANISATION_TURNOVER;
    }

    @Override
    protected void populate(AbstractApplicantResource resource, OrganisationTurnoverViewModel viewModel) {

    }

    @Override
    protected OrganisationTurnoverViewModel createNew() {
        return new OrganisationTurnoverViewModel();
    }
}
