package org.innovateuk.ifs.commons.service;

import org.innovateuk.ifs.address.resource.AddressResource;
import org.innovateuk.ifs.alert.resource.AlertResource;
import org.innovateuk.ifs.application.resource.ApplicationResource;
import org.innovateuk.ifs.application.resource.QuestionResource;
import org.innovateuk.ifs.application.resource.QuestionStatusResource;
import org.innovateuk.ifs.application.resource.SectionResource;
import org.innovateuk.ifs.assessment.resource.AssessmentResource;
import org.innovateuk.ifs.assessment.resource.AssessorFormInputResponseResource;
import org.innovateuk.ifs.category.resource.CategoryResource;
import org.innovateuk.ifs.commons.rest.ValidationMessages;
import org.innovateuk.ifs.competition.resource.*;
import org.innovateuk.ifs.finance.resource.ApplicationFinanceResource;
import org.innovateuk.ifs.finance.resource.FinanceRowMetaFieldResource;
import org.innovateuk.ifs.finance.resource.FinanceRowResource;
import org.innovateuk.ifs.finance.resource.ProjectFinanceResource;
import org.innovateuk.ifs.finance.resource.cost.FinanceRowItem;
import org.innovateuk.ifs.form.resource.FormInputResource;
import org.innovateuk.ifs.form.resource.FormInputResponseResource;
import org.innovateuk.ifs.invite.resource.*;
import org.innovateuk.ifs.project.resource.PartnerOrganisationResource;
import org.innovateuk.ifs.project.resource.ProjectResource;
import org.innovateuk.ifs.project.resource.ProjectUserResource;
import org.innovateuk.ifs.user.resource.*;
import org.springframework.core.ParameterizedTypeReference;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A utility for commonly used ParameterizedTypeReferences
 */
public final class ParameterizedTypeReferences {

    private ParameterizedTypeReferences() {}

    /**
     * Basic types
     */

    public static ParameterizedTypeReference<List<Long>> longsListType() {
        return new ParameterizedTypeReference<List<Long>>() {};
    }

    public static ParameterizedTypeReference<Set<Long>> longsSetType() {
        return new ParameterizedTypeReference<Set<Long>>() {};
    }

    public static ParameterizedTypeReference<List<String>> stringsListType() {
        return new ParameterizedTypeReference<List<String>>() {};
    }


    public static ParameterizedTypeReference<Map<Long, Set<Long>>> mapOfLongToLongsSetType() {
        return new ParameterizedTypeReference<Map<Long, Set<Long>>>() {};
    }


    /**
     * IFS types
     */

    public static ParameterizedTypeReference<List<AffiliationResource>> affiliationResourceListType() {
        return new ParameterizedTypeReference<List<AffiliationResource>>() {};
    }

    public static ParameterizedTypeReference<List<AlertResource>> alertResourceListType() {
        return new ParameterizedTypeReference<List<AlertResource>>() {};
    }

    public static ParameterizedTypeReference<List<AssessorCreatedInviteResource>> assessorCreatedInviteResourceListType() {
        return new ParameterizedTypeReference<List<AssessorCreatedInviteResource>>() {};
    }

    public static ParameterizedTypeReference<List<AssessorInviteOverviewResource>> assessorInviteOverviewResourceListType() {
        return new ParameterizedTypeReference<List<AssessorInviteOverviewResource>>() {};
    }

    public static ParameterizedTypeReference<List<ApplicationResource>> applicationResourceListType() {
        return new ParameterizedTypeReference<List<ApplicationResource>>() {};
    }

    public static ParameterizedTypeReference<List<AssessorFormInputResponseResource>> assessorFormInputResponseResourceListType() {
        return new ParameterizedTypeReference<List<AssessorFormInputResponseResource>>() {};
    }

    public static ParameterizedTypeReference<List<ProcessRoleResource>> processRoleResourceListType() {
        return new ParameterizedTypeReference<List<ProcessRoleResource>>() {};
    }

    public static ParameterizedTypeReference<List<UserResource>> userListType() {
        return new ParameterizedTypeReference<List<UserResource>>() {};
    }

    public static ParameterizedTypeReference<List<CompetitionResource>> competitionResourceListType() {
        return new ParameterizedTypeReference<List<CompetitionResource>>() {};
    }

    public static ParameterizedTypeReference<List<CompetitionSearchResultItem>> competitionSearchResultItemListType() {
        return new ParameterizedTypeReference<List<CompetitionSearchResultItem>>() {};
    }

    public static ParameterizedTypeReference<List<CategoryResource>> categoryResourceListType() {
        return new ParameterizedTypeReference<List<CategoryResource>>() {};
    }

    public static ParameterizedTypeReference<List<CompetitionTypeResource>> competitionTypeResourceListType() {
        return new ParameterizedTypeReference<List<CompetitionTypeResource>>() {};
    }

    public static ParameterizedTypeReference<List<QuestionStatusResource>> questionStatusResourceListType() {
        return new ParameterizedTypeReference<List<QuestionStatusResource>>() {};
    }

    public static ParameterizedTypeReference<List<FormInputResource>> formInputResourceListType() {
        return new ParameterizedTypeReference<List<FormInputResource>>() {};
    }

    public static ParameterizedTypeReference<List<QuestionResource>> questionResourceListType() {
        return new ParameterizedTypeReference<List<QuestionResource>>() {};
    }

    public static ParameterizedTypeReference<List<FormInputResponseResource>> formInputResponseListType() {
        return new ParameterizedTypeReference<List<FormInputResponseResource>>() {};
    }

    public static ParameterizedTypeReference<List<FinanceRowMetaFieldResource>> financeRowMetaFieldResourceListType() {
        return new ParameterizedTypeReference<List<FinanceRowMetaFieldResource>>() {};
    }

    public static ParameterizedTypeReference<List<InviteOrganisationResource>> inviteOrganisationResourceListType() {
        return new ParameterizedTypeReference<List<InviteOrganisationResource>>() {};
    }

    public static ParameterizedTypeReference<List<InviteProjectResource>> inviteProjectResourceListType() {
        return new ParameterizedTypeReference<List<InviteProjectResource>>() {};
    }

    public static ParameterizedTypeReference<List<FinanceRowResource>> costListType() {
        return new ParameterizedTypeReference<List<FinanceRowResource>>() {};
    }

    public static ParameterizedTypeReference<List<FinanceRowItem>> costItemListType() {
        return new ParameterizedTypeReference<List<FinanceRowItem>>() {};
    }

    public static ParameterizedTypeReference<List<ApplicationFinanceResource>> applicationFinanceResourceListType() {
        return new ParameterizedTypeReference<List<ApplicationFinanceResource>>() {};
    }

    public static ParameterizedTypeReference<List<ProjectFinanceResource>> projectFinanceResourceListType() {
        return new ParameterizedTypeReference<List<ProjectFinanceResource>>() {};
    }

    public static ParameterizedTypeReference<List<OrganisationTypeResource>> organisationTypeResourceListType() {
        return new ParameterizedTypeReference<List<OrganisationTypeResource>>() {};
    }

    public static ParameterizedTypeReference<List<AddressResource>> addressResourceListType() {
        return new ParameterizedTypeReference<List<AddressResource>>() {};
    }

    public static ParameterizedTypeReference<List<OrganisationResource>> organisationResourceListType() {
        return new ParameterizedTypeReference<List<OrganisationResource>>() {};
    }

    public static ParameterizedTypeReference<List<ProjectResource>> projectResourceListType() {
        return new ParameterizedTypeReference<List<ProjectResource>>() {
        };
    }

    public static ParameterizedTypeReference<List<ProjectUserResource>> projectUserResourceList() {
        return new ParameterizedTypeReference<List<ProjectUserResource>>() {
        };
    }

    public static ParameterizedTypeReference<List<PartnerOrganisationResource>> partnerOrganisationResourceList() {
        return new ParameterizedTypeReference<List<PartnerOrganisationResource>>() {
        };
    }

    public static ParameterizedTypeReference<List<RejectionReasonResource>> rejectionReasonResourceListType() {
        return new ParameterizedTypeReference<List<RejectionReasonResource>>() {
        };
    }

    public static ParameterizedTypeReference<List<ValidationMessages>> validationMessagesListType() {
        return new ParameterizedTypeReference<List<ValidationMessages>>() {};
    }

    public static ParameterizedTypeReference<List<MilestoneResource>> milestoneResourceListType() {
        return new ParameterizedTypeReference<List<MilestoneResource>>() {};
    }

    public static ParameterizedTypeReference<List<CompetitionParticipantResource>> competitionParticipantResourceListType() {
        return new ParameterizedTypeReference<List<CompetitionParticipantResource>>() {};
    }

    public static ParameterizedTypeReference<List<AssessmentResource>> assessmentResourceListType() {
        return new ParameterizedTypeReference<List<AssessmentResource>>() {};
    }

    public static ParameterizedTypeReference<List<EthnicityResource>> ethnicityResourceListType() {
        return new ParameterizedTypeReference<List<EthnicityResource>>() {};
    }

    public static ParameterizedTypeReference<List<AssessorCountOptionResource>> assessorCountOptionResourceListType() {
        return new ParameterizedTypeReference<List<AssessorCountOptionResource>>() {};
    }

    public static ParameterizedTypeReference<List<SectionResource>> sectionResourceListType() {
        return new ParameterizedTypeReference<List<SectionResource>>() {};
    }

    public static ParameterizedTypeReference<List<AvailableAssessorResource>> availableAssessorResourceListType() {
        return new ParameterizedTypeReference<List<AvailableAssessorResource>>() {};
    }
}
