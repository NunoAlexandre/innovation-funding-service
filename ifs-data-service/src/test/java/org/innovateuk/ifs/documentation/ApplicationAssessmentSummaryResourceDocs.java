package org.innovateuk.ifs.documentation;

import org.innovateuk.ifs.application.builder.ApplicationAssessmentSummaryResourceBuilder;
import org.springframework.restdocs.payload.FieldDescriptor;

import static java.util.Arrays.asList;
import static org.innovateuk.ifs.application.builder.ApplicationAssessmentSummaryResourceBuilder.newApplicationAssessmentSummaryResource;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

public class ApplicationAssessmentSummaryResourceDocs {

    public static final FieldDescriptor[] applicationAssessmentSummaryFields = {
            fieldWithPath("id").description("Id of the application"),
            fieldWithPath("name").description("Name of the application"),
            fieldWithPath("competitionId").description("Id of the competition"),
            fieldWithPath("competitionName").description("Name of the competition"),
            fieldWithPath("leadOrganisation").description("The lead organisation"),
            fieldWithPath("partnerOrganisations").description("List of partner organisation names")
    };

    public static final ApplicationAssessmentSummaryResourceBuilder applicationAssessmentSummaryResourceBuilder = newApplicationAssessmentSummaryResource()
            .withId(1L)
            .withName("Progressive machines")
            .withCompetitionId(2L)
            .withCompetitionName("Connected digital additive manufacturing")
            .withLeadOrganisation("Liquid Dynamics")
            .withPartnerOrganisations(asList("Acme Ltd.", "IO systems"));

}