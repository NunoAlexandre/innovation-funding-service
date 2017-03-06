package org.innovateuk.ifs.application.controller;

import org.innovateuk.ifs.BaseControllerMockMVCTest;
import org.innovateuk.ifs.application.resource.FundingDecision;
import org.innovateuk.ifs.application.resource.NotificationResource;
import org.innovateuk.ifs.commons.rest.RestErrorResponse;
import org.innovateuk.ifs.util.MapFunctions;
import org.junit.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static java.util.Arrays.asList;
import static org.innovateuk.ifs.commons.error.CommonErrors.internalServerErrorError;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceFailure;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.util.JsonMappingUtil.toJson;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ApplicationFundingDecisionControllerTest extends BaseControllerMockMVCTest<ApplicationFundingDecisionController> {

    @Override
    protected ApplicationFundingDecisionController supplyControllerUnderTest() {
        return new ApplicationFundingDecisionController();
    }

    @Test
    public void applicationFundingDecisionControllerShouldReturnAppropriateStatusCode() throws Exception {
        Long competitionId = 1L;
        Map<Long, FundingDecision> decision = MapFunctions.asMap(1L, FundingDecision.FUNDED, 2L, FundingDecision.UNFUNDED);

        when(applicationFundingServiceMock.makeFundingDecision(competitionId, decision)).thenReturn(serviceSuccess());
        when(applicationFundingServiceMock.notifyLeadApplicantsOfFundingDecisions(competitionId, decision)).thenReturn(serviceSuccess());
        when(projectServiceMock.createProjectsFromFundingDecisions(decision)).thenReturn(serviceSuccess());

        mockMvc.perform(post("/applicationfunding/1/submit")
        			.contentType(MediaType.APPLICATION_JSON)
        			.content(objectMapper.writeValueAsString(decision)))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    public void makeFundingDecisionButErrorOccursSendingNotifications() throws Exception {
        Long competitionId = 1L;
        Map<Long, FundingDecision> decision = MapFunctions.asMap(1L, FundingDecision.FUNDED, 2L, FundingDecision.UNFUNDED);

        when(applicationFundingServiceMock.makeFundingDecision(competitionId, decision)).thenReturn(serviceSuccess());
        when(applicationFundingServiceMock.notifyLeadApplicantsOfFundingDecisions(competitionId, decision)).thenReturn(serviceFailure(internalServerErrorError()));
        when(projectServiceMock.createProjectsFromFundingDecisions(decision)).thenReturn(serviceSuccess());

        mockMvc.perform(post("/applicationfunding/1/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(decision)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().json(toJson(new RestErrorResponse(internalServerErrorError()))));
    }
    
    @Test
    public void testSaveApplicationFundingDecisionData() throws Exception {
        Long competitionId = 1L;
        Map<Long, FundingDecision> decision = MapFunctions.asMap(1L, FundingDecision.FUNDED, 2L, FundingDecision.UNFUNDED);

        when(applicationFundingServiceMock.saveFundingDecisionData(competitionId, decision)).thenReturn(serviceSuccess());

        mockMvc.perform(post("/applicationfunding/1")
        			.contentType(MediaType.APPLICATION_JSON)
        			.content(objectMapper.writeValueAsString(decision)))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    public void testSendNotifications() throws Exception {
        NotificationResource notification = new NotificationResource("Subject of notification", "Body of notification message.", asList(1L, 2L, 3L));

        when(applicationFundingServiceMock.notifyLeadApplicantsOfFundingDecisions(notification)).thenReturn(serviceSuccess());

        mockMvc.perform(post("/applicationfunding/sendNotifications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(notification)))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

}
