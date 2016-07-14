package com.worth.ifs.sil.experian.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.worth.ifs.BaseControllerMockMVCTest;
import com.worth.ifs.bankdetails.resource.experian.AccountDetails;
import com.worth.ifs.bankdetails.resource.experian.Address;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static java.lang.Boolean.FALSE;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests around the SIL email stub
 */
public class ExperianEndpointControllerMockMvcTest extends BaseControllerMockMVCTest<ExperianEndpointController> {

    @Override
    protected ExperianEndpointController supplyControllerUnderTest() {
        ExperianEndpointController endpointController = new ExperianEndpointController();
        ReflectionTestUtils.setField(endpointController, "validateFromSilStub", FALSE);
        return endpointController;
    }

    @Test
    public void testSendMail() throws Exception {
        String sortCode = "123456";
        String accountNumber = "12345678";
        String companyName = "ACME limited";
        String registrationNumber = "12345678";
        String organisation = "";
        String buildingName = "";
        String street = "";
        String locality = "";
        String town = "";
        String postcode = "";

        Address address = new Address(organisation, buildingName, street, locality, town, postcode);
        AccountDetails accountDetails = new AccountDetails(sortCode, accountNumber, companyName, registrationNumber, address);

        String requestBody = new ObjectMapper().writeValueAsString(accountDetails);

        mockMvc.
            perform(
                post("/silstub/experianValidate").
                    header("Content-Type", "application/json").
                    header("IFS_AUTH_TOKEN", "123abc").
                    content(requestBody)
            ).
            andExpect(status().isAccepted()).
            andDo(document("silstub/experianValidate",
                requestHeaders(
                    headerWithName("Content-Type").description("Needs to be application/json"),
                    headerWithName("IFS_AUTH_TOKEN").description("The authentication token for the logged in user")
                )
            ));
    }
}

