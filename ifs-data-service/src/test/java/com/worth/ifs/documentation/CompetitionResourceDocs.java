package com.worth.ifs.documentation;

import com.worth.ifs.competition.builder.CompetitionResourceBuilder;
import org.springframework.restdocs.payload.FieldDescriptor;

import java.time.LocalDateTime;

import static com.google.common.primitives.Longs.asList;
import static com.worth.ifs.competition.builder.CompetitionResourceBuilder.newCompetitionResource;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

public class CompetitionResourceDocs {
    public static final FieldDescriptor[] competitionResourceFields = {
            fieldWithPath("id").description("Id of the competitionResource"),
            fieldWithPath("sections").description("List of section ids belonging to the competition"),
            fieldWithPath("name").description("name of the competition"),
            fieldWithPath("description").description("description of the competition"),
            fieldWithPath("startDate").description("date the competition opens for submissions"),
            fieldWithPath("endDate").description("date the submissions phase of the competition closes"),
            fieldWithPath("assessmentStartDate").description("date on which the assessments start"),
            fieldWithPath("assessmentEndDate").description("date on which all the assessments should be finished"),
            fieldWithPath("fundersPanelEndDate").description("date on which the funders panel ended"),
            fieldWithPath("assessorFeedbackDate").description("date on which applicants can expect to receive feedback from the assessments"),
            fieldWithPath("competitionStatus").description("the current status of the competition"),
            fieldWithPath("maxResearchRatio").description("maximum ratio of research participation"),
            fieldWithPath("academicGrantPercentage").description("grant claim percentage for the academics"),
            fieldWithPath("milestones").description("List of milestone ids"),
            fieldWithPath("competitionType").description("the competition type this competition belongs to"),
            fieldWithPath("executive").description("the user id of the competition executive"),
            fieldWithPath("leadTechnologist").description("the user id of the competition leadTechnologist"),
            fieldWithPath("innovationSector").description("the Innovation sector this competition belongs to"),
            fieldWithPath("innovationSectorName").description("the Innovation sector name this competition belongs to"),
            fieldWithPath("innovationArea").description("the Innovation area this competition belongs to"),
            fieldWithPath("innovationAreaName").description("the Innovation area name this competition belongs to"),
            fieldWithPath("pafCode").description("the paf code entered during competition setup"),
            fieldWithPath("budgetCode").description("the budget code entered during competition setup"),
            fieldWithPath("code").description("the unique competition code entered during competition setup"),
            fieldWithPath("sectionSetupStatus").description("the completion status of competition setup sections"),
            fieldWithPath("activityCode").description("the activity code entered during competition setup"),
            fieldWithPath("innovateBudget").description("the innovate budget entered during competition setup"),
            fieldWithPath("coFunders").description("the co-funders entered during competition setup"),
            fieldWithPath("coFundersBudget").description("the co-funders budget entered during competition setup")
    };

    public static final CompetitionResourceBuilder competitionResourceBuilder = newCompetitionResource()
            .withId(1L)
            .withSections(asList(1L, 2L, 3L))
            .withName("competition name")
            .withDescription("competition description")
            .withStartDate(LocalDateTime.now())
            .withEndDate(LocalDateTime.now().plusDays(30))
            .withAssessmentStartDate(LocalDateTime.now().plusDays(32))
            .withAssessmentEndDate(LocalDateTime.now().plusDays(44))
            .withAssessorFeedbackDate(LocalDateTime.now().plusDays(56))
            .withMaxResearchRatio(20)
            .withAcademicGrantClaimPercentage(100)
            .withCompetitionCode("COMP-1")
            .withCompetitionType(1L)
            .withExecutive(1L)
            .withLeadTechnologist(1L)
            .withInnovationArea(1L)
            .withInnovationAreaName("Tech")
            .withInnovationSector(2L)
            .withInnovationSectorName("IT")
            .withPafCode("PAF-123")
            .withBudgetCode("BUDGET-456")
            .withActitiyCode("Activity-Code")
            .withInnovateBudget("INNOVATE-Budget")
            .withCoFunders("Cofunder, Cofunder")
            .withCoFundersBudget("1234, 23423");
}
