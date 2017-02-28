package org.innovateuk.ifs.documentation;

import org.innovateuk.ifs.application.builder.ApplicationResourceBuilder;
import org.innovateuk.ifs.application.constant.ApplicationStatusConstants;
import org.innovateuk.ifs.category.resource.ResearchCategoryResource;
import org.innovateuk.ifs.competition.resource.CompetitionStatus;
import org.springframework.restdocs.payload.FieldDescriptor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hibernate.validator.internal.util.CollectionHelper.asSet;
import static org.innovateuk.ifs.application.builder.ApplicationResourceBuilder.newApplicationResource;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

public class ApplicationDocs {
    public static final FieldDescriptor[] applicationResourceFields = {
            fieldWithPath("id").description("Id of the application"),
            fieldWithPath("name").description("Name of the application"),
            fieldWithPath("startDate").description("Estimated timescales: project start date"),
            fieldWithPath("submittedDate").description("The date the applicant has submitted this application."),
            fieldWithPath("durationInMonths").description("Estimated timescales: project duration in months"),
            fieldWithPath("applicationStatus").description("ApplicationStatus Id"),
            fieldWithPath("applicationStatusName").description("ApplicationStatus name"),
            fieldWithPath("stateAidAgreed").description("Flag indicating if the user has accepted that they are eligible for state aid"),
            fieldWithPath("competition").description("Competition Id"),
            fieldWithPath("competitionName").description("Competition Name"),
            fieldWithPath("competitionStatus").description("Competition Status"),
            fieldWithPath("assessorFeedbackFileEntry").description("Uploaded Assessor Feedback for the Application"),
            fieldWithPath("completion").description("percentage of completion of the application"),
            fieldWithPath("resubmission").description("indicator that this application is a resubmission"),
            fieldWithPath("previousApplicationNumber").description("the application number of the previous submission"),
            fieldWithPath("previousApplicationTitle").description("the application title of the previous submission"),
            fieldWithPath("researchCategories").description("list research categories")
    };

    public static final ApplicationResourceBuilder applicationResourceBuilder = newApplicationResource()
            .withId(1L)
            .withName("application name")
            .withStartDate(LocalDate.now())
            .withSubmittedDate(LocalDateTime.now())
            .withDurationInMonths(1L)
            .withApplicationStatus(ApplicationStatusConstants.OPEN)
            .withCompetition(1L)
            .withCompetitionName("competition name")
            .withCompetitionStatus(CompetitionStatus.PROJECT_SETUP)
            .withAssessorFeedbackFileEntry(123L)
            .withCompletion(new BigDecimal(30L))
            .withResearchCategories(asSet(new ResearchCategoryResource()));
}
