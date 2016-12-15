package org.innovateuk.ifs.documentation;

import org.springframework.restdocs.payload.FieldDescriptor;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

public class AvailableAssessorResourceDocs {

    public static final FieldDescriptor[] availableAssessorResourceFields = {
            fieldWithPath("firstName").description("First name of the assessor"),
            fieldWithPath("lastName").description("Last name of the assessor"),
            fieldWithPath("innovationArea").description("Innovation area of the assessor"),
            fieldWithPath("compliant").description("Flag to signify if the assessor is compliant. An assessor is compliant if, and only if they’ve completed their Skills and Business Type, and they’ve completed their DoI, and they’ve signed a Contract."),
            fieldWithPath("email").description("E-mail address of the assessor"),
            fieldWithPath("businessType").description("Assessor type (business or academic)"),
            fieldWithPath("added").description("Flag to signify if the assessor has been added to the assessor invite list for the competition requested")
    };
}