package com.worth.ifs.documentation;

import com.worth.ifs.assessment.builder.AssessmentResourceBuilder;
import com.worth.ifs.assessment.resource.AssessmentStates;
import com.worth.ifs.workflow.resource.ProcessEvent;
import org.springframework.restdocs.payload.FieldDescriptor;

import java.time.LocalDate;
import java.util.GregorianCalendar;

import static com.worth.ifs.assessment.builder.AssessmentResourceBuilder.newAssessmentResource;
import static java.util.Arrays.asList;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

public class AssessmentDocs {
    public static final FieldDescriptor[] assessmentFields = {
            fieldWithPath("id").description("Id of the assessment"),
            fieldWithPath("event").description("currently not used"),
            fieldWithPath("status").description("current status of the assessment process"),
            fieldWithPath("lastModified").description("last modified"),
            fieldWithPath("startDate").description("start date of the assessment"),
            fieldWithPath("endDate").description("end date of the assessment"),
            fieldWithPath("processOutcomes").description("outcomes of the assessment process"),
            fieldWithPath("processRole").description("process role of the assigned assessor"),
            fieldWithPath("submitted").description("the assessment is submitted"),
            fieldWithPath("started").description("the assessment is started"),
            fieldWithPath("application").description("the id of the application being assessed"),
            fieldWithPath("competition").description("the competition id of the application being assessed"),
    };

    public static final AssessmentResourceBuilder assessmentResourceBuilder = newAssessmentResource()
            .withId(1L)
            .withStartDate(LocalDate.now())
            .withEndDate(LocalDate.now().plusDays(14))
            .withProcessOutcome(asList(1L, 2L))
            .withProcessStatus(AssessmentStates.OPEN)
            .withProcessEvent(ProcessEvent.ASSESSMENT)
            .withStarted(true)
            .withSubmitted(false)
            .withLastModifiedDate(GregorianCalendar.getInstance())
            .withProcessRole(1L)
            .withApplication(2L);
}
