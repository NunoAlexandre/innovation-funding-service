package com.worth.ifs.documentation;

import java.time.LocalDate;

import com.worth.ifs.assessment.builder.AssessmentResourceBuilder;
import com.worth.ifs.assessment.resource.AssessmentResource;
import com.worth.ifs.assessment.resource.AssessmentStates;
import com.worth.ifs.workflow.resource.ProcessEvent;

import org.springframework.restdocs.payload.FieldDescriptor;

import static com.worth.ifs.assessment.builder.AssessmentBuilder.newAssessment;
import static com.worth.ifs.assessment.builder.AssessmentResourceBuilder.newAssessmentResource;
import static com.worth.ifs.assessment.builder.ProcessOutcomeBuilder.newProcessOutcome;
import static com.worth.ifs.assessment.builder.ProcessOutcomeResourceBuilder.newProcessOutcomeResource;
import static com.worth.ifs.user.builder.ProcessRoleBuilder.newProcessRole;
import static com.worth.ifs.user.builder.ProcessRoleResourceBuilder.newProcessRoleResource;
import static java.util.Arrays.asList;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

public class AssessmentDocs {
    public static final FieldDescriptor[] assessmentFields = {
            fieldWithPath("id").description("Id of the assessment"),
            fieldWithPath("startDate").description("start date of the assessment"),
            fieldWithPath("endDate").description("end date of the assessment"),
            fieldWithPath("processOutcomes").description("outcomes of the assessment process"),
            fieldWithPath("lastModified").description("last modified"),
            fieldWithPath("status").description("current status of the assessment process"),
            fieldWithPath("event").description("currently not used"),
            fieldWithPath("processRole").description("process role of the assigned assessor")
    };

    public static final AssessmentResourceBuilder assessmentResourceBuilder = newAssessmentResource()
            .withId(1L)
            .withStartDate(LocalDate.now())
            .withEndDate(LocalDate.now().plusDays(14))
            .withProcessOutcome(asList(1L, 2L))
            .withProcessStatus(AssessmentStates.OPEN)
            .withProcessEvent(ProcessEvent.ASSESSMENT)
            .withProcessRole(newProcessRoleResource().build());
}
