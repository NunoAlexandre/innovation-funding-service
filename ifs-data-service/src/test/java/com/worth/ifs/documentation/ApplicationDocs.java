package com.worth.ifs.documentation;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.worth.ifs.application.builder.ApplicationResourceBuilder;

import org.springframework.restdocs.payload.FieldDescriptor;

import static com.worth.ifs.application.builder.ApplicationResourceBuilder.newApplicationResource;
import static java.util.Arrays.asList;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

public class ApplicationDocs {
    public static final FieldDescriptor[] applicationResourceFields = {
            fieldWithPath("id").description("Id of the application"),
            fieldWithPath("name").description("Name of the application"),
            fieldWithPath("startDate").description("Estimated timescales: project start date"),
            fieldWithPath("submittedDate").description("The date the applicant has submitted this application."),
            fieldWithPath("durationInMonths").description("Estimated timescales: project duration in months"),
            fieldWithPath("processRoles").description("list of ProcessRole Id's"),
            fieldWithPath("applicationStatus").description("ApplicationStatus Id"),
            fieldWithPath("applicationStatusName").description("ApplicationStatus name"),
            fieldWithPath("competition").description("Competition Id"),
            fieldWithPath("competitionName").description("Competition Name"),
            fieldWithPath("applicationFinances").description("list of ApplicationFinance Id's")
    };

    public static final ApplicationResourceBuilder applicationResourceBuilder = newApplicationResource()
            .withId(1L)
            .withName("application name")
            .withStartDate(LocalDate.now())
            .withSubmittedDate(LocalDateTime.now())
            .withDuration(1L)
            .withProcessRoles(asList(1L,2L,3L))
            .withApplicationFinance(asList(1L,2L,3L))
            .withApplicationStatus(1L)
            .withApplicationStatusName("OPEN")
            .withCompetition(1L)
            .withCompetitionName("competition name")
            .withInviteList(asList(1L,2L,3L));

}
