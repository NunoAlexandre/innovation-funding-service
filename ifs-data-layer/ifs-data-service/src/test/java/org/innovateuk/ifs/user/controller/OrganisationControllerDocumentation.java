package org.innovateuk.ifs.user.controller;

import org.innovateuk.ifs.BaseControllerMockMVCTest;
import org.innovateuk.ifs.user.resource.OrganisationResource;
import org.junit.Test;

import java.util.Set;

import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.documentation.OrganisationDocs.organisationResourceBuilder;
import static org.innovateuk.ifs.documentation.OrganisationDocs.organisationResourceFields;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class OrganisationControllerDocumentation extends BaseControllerMockMVCTest<OrganisationController> {
    @Override
    protected OrganisationController supplyControllerUnderTest() {
        return new OrganisationController();
    }

    @Test
    public void findByApplicationId() throws Exception {
        long applicationId = 1L;
        Set<OrganisationResource> organisationResourceSet = organisationResourceBuilder.buildSet(1);
        when(organisationServiceMock.findByApplicationId(applicationId)).thenReturn(serviceSuccess(organisationResourceSet));

        mockMvc.perform(get("/organisation/findByApplicationId/{applicationId}", applicationId))
                .andExpect(status().isOk())
                .andDo(document("organisation/{method-name}",
                        pathParameters(
                                parameterWithName("applicationId").description("Identifier of the application to find the organisations for")
                        ),
                        responseFields(
                                fieldWithPath("[]").description("List of organisations for the selected application")
                        )
                ));
    }

    @Test
    public void findById() throws Exception {
        long organisationId = 1L;
        OrganisationResource organisationResource = organisationResourceBuilder.build();
        when(organisationServiceMock.findById(organisationId)).thenReturn(serviceSuccess(organisationResource));

        mockMvc.perform(get("/organisation/findById/{organisationId}", organisationId))
                .andExpect(status().isOk())
                .andDo(document("organisation/{method-name}",
                        pathParameters(
                                parameterWithName("organisationId").description("Identifier of the organisation to find")
                        ),
                        responseFields(organisationResourceFields)
                ));
    }

    @Test
    public void getPrimaryForUser() throws Exception {
        long userId = 1L;
        OrganisationResource organisationResource = organisationResourceBuilder.build();

        when(organisationServiceMock.getPrimaryForUser(userId)).thenReturn(serviceSuccess(organisationResource));

        mockMvc.perform(get("/organisation/getPrimaryForUser/{userId}", userId))
                .andExpect(status().isOk())
                .andDo(document("organisation/{method-name}",
                        pathParameters(
                                parameterWithName("userId").description("Identifier of the user to find the primary organisation for")
                        ),
                        responseFields(organisationResourceFields)
                ));
    }

    @Test
    public void createOrMatch() throws Exception {
        OrganisationResource organisationResource = organisationResourceBuilder.build();

        when(organisationInitialCreationServiceMock.createOrMatch(organisationResource)).thenReturn(serviceSuccess(organisationResource));

        mockMvc.perform(post("/organisation/createOrMatch")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(organisationResource)))
                .andExpect(status().isCreated())
                .andDo(document("organisation/{method-name}",
                        requestFields(organisationResourceFields),
                        responseFields(organisationResourceFields)
                ));
    }

    @Test
    public void saveResource() throws Exception {
        OrganisationResource organisationResource = organisationResourceBuilder.build();

        when(organisationServiceMock.update(organisationResource)).thenReturn(serviceSuccess(organisationResource));

        mockMvc.perform(put("/organisation/update")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(organisationResource)))
                .andExpect(status().isOk())
                .andDo(document("organisation/{method-name}",
                        requestFields(organisationResourceFields),
                        responseFields(organisationResourceFields)
                ));
    }

    @Test
    public void createAndLinkByInvite() throws Exception {
        String inviteHash = "123abc";
        OrganisationResource organisationResource = organisationResourceBuilder.build();

        when(organisationInitialCreationServiceMock.createAndLinkByInvite(organisationResource, inviteHash)).thenReturn(serviceSuccess(organisationResource));

        mockMvc.perform(post("/organisation/createAndLinkByInvite")
                .param("inviteHash", inviteHash)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(organisationResource)))
                .andExpect(status().isCreated())
                .andDo(document("organisation/{method-name}",
                        requestParameters(
                                parameterWithName("inviteHash").description("The hash for the invite that the found or created organisation has to be linked to")
                        ),
                        requestFields(organisationResourceFields),
                        responseFields(organisationResourceFields)
                ));
    }

    @Test
    public void updateNameAndRegistration() throws Exception {
        long organisationId = 1L;
        String name = "name";
        String registration = "registration";
        OrganisationResource organisationResource = organisationResourceBuilder.build();

        when(organisationServiceMock.updateOrganisationNameAndRegistration(organisationId, name, registration)).thenReturn(serviceSuccess(organisationResource));

        mockMvc.perform(post("/organisation/updateNameAndRegistration/{organisationId}", organisationId)
                .param("name", name)
                .param("registration", registration))
                .andExpect(status().isCreated())
                .andDo(document("organisation/{method-name}",
                        pathParameters(
                                parameterWithName("organisationId").description("The identifier of the organisation being updated")
                        ),
                        requestParameters(
                                parameterWithName("name").description("The name of the organisation"),
                                parameterWithName("registration").description("The companies house number")
                        ),
                        responseFields(organisationResourceFields)
                ));
    }
}