package com.worth.ifs.documentation;

import com.worth.ifs.bankdetails.builder.BankDetailsResourceBuilder;
import org.springframework.restdocs.payload.FieldDescriptor;

import static com.worth.ifs.bankdetails.builder.BankDetailsResourceBuilder.newBankDetailsResource;
import static com.worth.ifs.organisation.builder.OrganisationAddressResourceBuilder.newOrganisationAddressResource;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

public class BankDetailsDocs {
    public static final FieldDescriptor[] bankDetailsResourceFields = {
            fieldWithPath("id").description("Id of the bankDetails record"),
            fieldWithPath("project").description("Project that the bank details belong to"),
            fieldWithPath("organisation").description("Organisation to which these bank details belong"),
            fieldWithPath("sortCode").description("Sort code for the bank, identifying a specific branch"),
            fieldWithPath("accountNumber").description("Bank account number"),
            fieldWithPath("organisationAddress").description("Banking address used by organisation"),
            fieldWithPath("organisationTypeName").description("The type of organisation"),
            fieldWithPath("companyName").description("The company name"),
            fieldWithPath("registrationNumber").description("The registration number"),
    };

    @SuppressWarnings("unchecked")
    public static final BankDetailsResourceBuilder bankDetailsResourceBuilder = newBankDetailsResource()
            .withId(1L)
            .withProject(1L)
            .withOrganisation(1L)
            .withSortCode("123456")
            .withAccountNumber("12345678")
            .withOrganiationAddress(newOrganisationAddressResource().build());
}
