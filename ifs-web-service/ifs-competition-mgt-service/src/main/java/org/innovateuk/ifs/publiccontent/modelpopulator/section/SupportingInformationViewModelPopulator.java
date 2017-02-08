package org.innovateuk.ifs.publiccontent.modelpopulator.section;


import org.innovateuk.ifs.competition.publiccontent.resource.PublicContentSectionType;
import org.innovateuk.ifs.publiccontent.modelpopulator.AbstractPublicContentGroupViewModelPopulator;
import org.innovateuk.ifs.publiccontent.modelpopulator.PublicContentViewModelPopulator;
import org.innovateuk.ifs.publiccontent.viewmodel.section.SupportingInformationViewModel;
import org.springframework.stereotype.Service;


@Service
public class SupportingInformationViewModelPopulator extends AbstractPublicContentGroupViewModelPopulator<SupportingInformationViewModel> implements PublicContentViewModelPopulator<SupportingInformationViewModel> {

    @Override
    protected SupportingInformationViewModel createInitial() {
        return new SupportingInformationViewModel();
    }

    @Override
    protected PublicContentSectionType getType() {
        return PublicContentSectionType.SUPPORTING_INFORMATION;
    }
}
