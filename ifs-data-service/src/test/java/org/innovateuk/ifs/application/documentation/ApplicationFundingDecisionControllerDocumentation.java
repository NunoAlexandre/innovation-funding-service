package org.innovateuk.ifs.application.documentation;

import org.innovateuk.ifs.BaseControllerMockMVCTest;
import org.innovateuk.ifs.application.controller.ApplicationFundingDecisionController;
import org.innovateuk.ifs.application.resource.FundingDecision;
import org.innovateuk.ifs.application.resource.NotificationResource;
import org.innovateuk.ifs.util.MapFunctions;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;

import java.util.Map;

import static java.util.Arrays.asList;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.documentation.NotificationResourceDocs.notificationResourceFields;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;

public class ApplicationFundingDecisionControllerDocumentation extends BaseControllerMockMVCTest<ApplicationFundingDecisionController> {

    private RestDocumentationResultHandler document;

    @Override
    protected ApplicationFundingDecisionController supplyControllerUnderTest() {
        return new ApplicationFundingDecisionController();
    }

    @Before
    public void setup(){
        this.document = document("applicationfunding/{method-name}",
                preprocessResponse(prettyPrint()));
    }
    
    @Test
    public void makeFundingDecision() throws Exception {
    	Long competitionId = 1L;
        Map<Long, FundingDecision> decision = MapFunctions.asMap(1L, FundingDecision.FUNDED, 2L, FundingDecision.UNFUNDED);

        when(applicationFundingServiceMock.makeFundingDecision(competitionId, decision)).thenReturn(serviceSuccess());
        when(applicationFundingServiceMock.notifyLeadApplicantsOfFundingDecisions(competitionId, decision)).thenReturn(serviceSuccess());
        when(projectServiceMock.createProjectsFromFundingDecisions(decision)).thenReturn(serviceSuccess());

        mockMvc.perform(post("/applicationfunding/1/submit")
	        		.contentType(MediaType.APPLICATION_JSON)
	    			.content(objectMapper.writeValueAsString(decision)))
                .andDo( this.document.snippets());
    }
    
    @Test
    public void saveFundingDecisionData() throws Exception {
        Long competitionId = 1L;
        Map<Long, FundingDecision> decision = MapFunctions.asMap(1L, FundingDecision.FUNDED, 2L, FundingDecision.UNFUNDED);

        when(applicationFundingServiceMock.saveFundingDecisionData(competitionId, decision)).thenReturn(serviceSuccess());

        mockMvc.perform(put("/applicationfunding/1")
        			.contentType(MediaType.APPLICATION_JSON)
        			.content(objectMapper.writeValueAsString(decision)))
        		.andDo( this.document.snippets());
    }

    @Test
    public void sendNotifications() throws Exception {
        NotificationResource notification = new NotificationResource("Subject of notification", "Body of notification message.", asList(1L, 2L, 3L));

        when(applicationFundingServiceMock.notifyLeadApplicantsOfFundingDecisions(notification)).thenReturn(serviceSuccess());

        mockMvc.perform(post("/applicationfunding/sendNotifications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(notification)))
                .andDo( this.document.snippets(
                        requestFields(notificationResourceFields)
                ));
    }
}
