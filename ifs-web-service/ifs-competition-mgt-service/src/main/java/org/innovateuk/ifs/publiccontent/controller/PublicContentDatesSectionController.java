package org.innovateuk.ifs.publiccontent.controller;

import org.innovateuk.ifs.publiccontent.form.DatesForm;
import org.innovateuk.ifs.publiccontent.formpopulator.DatesFormPopulator;
import org.innovateuk.ifs.publiccontent.formpopulator.PublicContentFormPopulator;
import org.innovateuk.ifs.publiccontent.modelpopulator.DatesViewModelPopulator;
import org.innovateuk.ifs.publiccontent.modelpopulator.PublicContentViewModelPopulator;
import org.innovateuk.ifs.publiccontent.saver.DatesFormSaver;
import org.innovateuk.ifs.publiccontent.saver.PublicContentFormSaver;
import org.innovateuk.ifs.publiccontent.viewmodel.DatesViewModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller for setup of public content.
 */
@Controller
@RequestMapping("/competition/setup/public-content/dates")
@PreAuthorize("hasAnyAuthority('comp_admin', 'project_finance')")
public class PublicContentDatesSectionController extends AbstractPublicContentSectionController<DatesViewModel, DatesForm> {

    @Autowired
    private DatesFormPopulator datesFormPopulator;

    @Autowired
    private DatesViewModelPopulator datesViewModelPopulator;

    @Autowired
    private DatesFormSaver datesFormSaver;

    @Override
    protected PublicContentViewModelPopulator<DatesViewModel> modelPopulator() {
        return datesViewModelPopulator;
    }

    @Override
    protected PublicContentFormPopulator<DatesForm> formPopulator() {
        return datesFormPopulator;
    }

    @Override
    protected PublicContentFormSaver<DatesForm> formSaver() {
        return datesFormSaver;
    }

}
