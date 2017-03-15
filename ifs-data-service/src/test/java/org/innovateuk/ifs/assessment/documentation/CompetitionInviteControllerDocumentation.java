package org.innovateuk.ifs.assessment.documentation;

import org.innovateuk.ifs.BaseControllerMockMVCTest;
import org.innovateuk.ifs.assessment.controller.CompetitionInviteController;
import org.innovateuk.ifs.email.resource.EmailContent;
import org.innovateuk.ifs.invite.domain.ParticipantStatus;
import org.innovateuk.ifs.invite.resource.*;
import org.innovateuk.ifs.user.resource.UserResource;
import org.junit.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;

import java.util.List;
import java.util.Optional;

import static java.lang.Boolean.TRUE;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.documentation.AssessorCreatedInvitePageResourceDocs.assessorCreatedInvitePageResourceBuilder;
import static org.innovateuk.ifs.documentation.AssessorCreatedInvitePageResourceDocs.assessorCreatedInvitePageResourceFields;
import static org.innovateuk.ifs.documentation.AssessorCreatedInviteResourceDocs.assessorCreatedInviteResourceFields;
import static org.innovateuk.ifs.documentation.AssessorInviteOverviewPageResourceDocs.assessorInviteOverviewPageResourceFields;
import static org.innovateuk.ifs.documentation.AssessorInviteOverviewResourceDocs.assessorInviteOverviewResourceFields;
import static org.innovateuk.ifs.documentation.AvailableAssessorPageResourceDocs.availableAssessorPageResourceBuilder;
import static org.innovateuk.ifs.documentation.AvailableAssessorPageResourceDocs.availableAssessorPageResourceFields;
import static org.innovateuk.ifs.documentation.AvailableAssessorResourceDocs.availableAssessorResourceFields;
import static org.innovateuk.ifs.documentation.CompetitionInviteDocs.*;
import static org.innovateuk.ifs.documentation.CompetitionInviteStatisticsResourceDocs.competitionInviteStatisticsResourceBuilder;
import static org.innovateuk.ifs.documentation.CompetitionInviteStatisticsResourceDocs.competitionInviteStatisticsResourceFields;
import static org.innovateuk.ifs.email.builders.EmailContentResourceBuilder.newEmailContentResource;
import static org.innovateuk.ifs.invite.builder.AssessorInviteOverviewPageResourceBuilder.newAssessorInviteOverviewPageResource;
import static org.innovateuk.ifs.invite.builder.AssessorInviteOverviewResourceBuilder.newAssessorInviteOverviewResource;
import static org.innovateuk.ifs.invite.domain.ParticipantStatus.ACCEPTED;
import static org.innovateuk.ifs.user.builder.UserResourceBuilder.newUserResource;
import static org.innovateuk.ifs.util.JsonMappingUtil.toJson;
import static org.mockito.Mockito.*;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CompetitionInviteControllerDocumentation extends BaseControllerMockMVCTest<CompetitionInviteController> {

    @Override
    protected CompetitionInviteController supplyControllerUnderTest() {
        return new CompetitionInviteController();
    }

    @Test
    public void getCreatedInvite() throws Exception {
        long inviteId = 1L;
        AssessorInviteToSendResource resource = assessorInviteToSendResourceBuilder.build();

        when(competitionInviteServiceMock.getCreatedInvite(inviteId)).thenReturn(serviceSuccess(resource));

        mockMvc.perform(get("/competitioninvite/getCreated/{inviteId}", inviteId))
                .andExpect(status().isOk())
                .andDo(document("competitioninvite/{method-name}",
                        pathParameters(
                                parameterWithName("inviteId").description("Id of the created invite being requested")
                        ),
                        responseFields(assessorInviteToSendResourceFields)
                ));
    }

    @Test
    public void getInvite() throws Exception {
        String hash = "invitehash";
        CompetitionInviteResource competitionInviteResource = competitionInviteResourceBuilder.build();

        when(competitionInviteServiceMock.getInvite(hash)).thenReturn(serviceSuccess(competitionInviteResource));

        mockMvc.perform(get("/competitioninvite/getInvite/{hash}", hash))
                .andExpect(status().isOk())
                .andDo(document("competitioninvite/{method-name}",
                        pathParameters(
                                parameterWithName("hash").description("hash of the invite being requested")
                        ),
                        responseFields(competitionInviteFields)
                ));
    }

    @Test
    public void openInvite() throws Exception {
        String hash = "invitehash";
        CompetitionInviteResource competitionInviteResource = competitionInviteResourceBuilder.build();

        when(competitionInviteServiceMock.openInvite(hash)).thenReturn(serviceSuccess(competitionInviteResource));

        mockMvc.perform(post("/competitioninvite/openInvite/{hash}", hash))
                .andExpect(status().isOk())
                .andDo(document("competitioninvite/{method-name}",
                        pathParameters(
                                parameterWithName("hash").description("hash of the invite being opened")
                        ),
                        responseFields(competitionInviteFields)
                ));
    }

    @Test
    public void acceptInvite() throws Exception {
        String hash = "invitehash";
        UserResource user = newUserResource().build();

        login(user);

        when(competitionInviteServiceMock.acceptInvite(hash, user)).thenReturn(serviceSuccess());

        mockMvc.perform(post("/competitioninvite/acceptInvite/{hash}", hash))
                .andExpect(status().isOk())
                .andDo(document("competitioninvite/{method-name}",
                        pathParameters(
                                parameterWithName("hash").description("hash of the invite being accepted")
                        )
                ));
    }

    @Test
    public void rejectInvite() throws Exception {
        String hash = "invitehash";
        CompetitionRejectionResource compRejection = competitionInviteResource;

        when(competitionInviteServiceMock.rejectInvite(hash, compRejection.getRejectReason(), ofNullable(compRejection.getRejectComment()))).thenReturn(serviceSuccess());

        mockMvc.perform(post("/competitioninvite/rejectInvite/{hash}", hash)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(compRejection)))
                .andExpect(status().isOk())
                .andDo(document("competitioninvite/{method-name}",
                        requestFields(competitionRejectionFields),
                        pathParameters(
                                parameterWithName("hash").description("hash of the invite being rejected")
                        )
                ));
    }

    @Test
    public void checkExistingUser() throws Exception {
        String hash = "invitehash";

        when(competitionInviteServiceMock.checkExistingUser(hash)).thenReturn(serviceSuccess(TRUE));

        mockMvc.perform(get("/competitioninvite/checkExistingUser/{hash}", hash))
                .andExpect(status().isOk())
                .andExpect(content().string("true"))
                .andDo(document("competitioninvite/{method-name}",
                        pathParameters(
                                parameterWithName("hash").description("hash of the invite being checked")
                        )
                ));
    }

    @Test
    public void getAvailableAssessors() throws Exception {
        long competitionId = 1L;
        Optional<Long> innovationArea = of(4L);

        Pageable pageable = new PageRequest(0, 20, new Sort(ASC, "firstName"));

        when(competitionInviteServiceMock.getAvailableAssessors(competitionId, pageable, innovationArea))
                .thenReturn(serviceSuccess(availableAssessorPageResourceBuilder.build()));

        mockMvc.perform(get("/competitioninvite/getAvailableAssessors/{competitionId}", competitionId)
                .param("size", "20")
                .param("page", "0")
                .param("sort", "firstName,asc")
                .param("innovationArea", "4"))
                .andExpect(status().isOk())
                .andDo(document("competitioninvite/{method-name}",
                        pathParameters(
                                parameterWithName("competitionId").description("Id of the competition")
                        ),
                        requestParameters(
                                parameterWithName("size").optional()
                                        .description("Maximum number of elements in a single page. Defaults to 20."),
                                parameterWithName("page").optional()
                                        .description("Page number of the paginated data. Starts at 0. Defaults to 0."),
                                parameterWithName("sort").optional()
                                        .description("The property to sort the elements on. For example `sort=firstName,asc`. Defaults to `firstName,asc`"),
                                parameterWithName("innovationArea").optional()
                                        .description("Innovation area ID to filter assessors by.")
                        ),
                        responseFields(availableAssessorPageResourceFields)
                                .andWithPrefix("content[].", availableAssessorResourceFields)
                ));

        verify(competitionInviteServiceMock, only()).getAvailableAssessors(competitionId, pageable, innovationArea);
    }

    @Test
    public void getCreatedInvites() throws Exception {
        long competitionId = 1L;

        Pageable pageable = new PageRequest(0, 20, new Sort(ASC, "name"));

        when(competitionInviteServiceMock.getCreatedInvites(competitionId, pageable)).thenReturn(serviceSuccess(assessorCreatedInvitePageResourceBuilder.build()));

        mockMvc.perform(get("/competitioninvite/getCreatedInvites/{competitionId}", 1L)
                .param("size", "20")
                .param("page", "0")
                .param("sort", "name,asc"))
                .andExpect(status().isOk())
                .andDo(document("competitioninvite/{method-name}",
                        pathParameters(
                                parameterWithName("competitionId").description("Id of the competition")
                        ),
                        requestParameters(
                                parameterWithName("size").optional()
                                        .description("Maximum number of elements in a single page. Defaults to 20."),
                                parameterWithName("page").optional()
                                        .description("Page number of the paginated data. Starts at 0. Defaults to 0."),
                                parameterWithName("sort").optional()
                                        .description("The property to sort the elements on. For example `sort=name,asc`. Defaults to `name,asc`")
                        ),
                        responseFields(assessorCreatedInvitePageResourceFields)
                                .andWithPrefix("content[]", assessorCreatedInviteResourceFields)
                ));

        verify(competitionInviteServiceMock, only()).getCreatedInvites(competitionId, pageable);
    }

    @Test
    public void getInvitationOverview() throws Exception {
        long competitionId = 1L;
        Optional<Long> innovationArea = of(10L);
        Optional<ParticipantStatus> status = of(ACCEPTED);
        Optional<Boolean> compliant = of(TRUE);

        Pageable pageable = new PageRequest(0, 20, new Sort(ASC, "invite.name"));

        List<AssessorInviteOverviewResource> content = newAssessorInviteOverviewResource().build(2);
        AssessorInviteOverviewPageResource expectedPageResource = newAssessorInviteOverviewPageResource()
                .withContent(content)
                .build();

        when(competitionInviteServiceMock.getInvitationOverview(competitionId, pageable, innovationArea, status, compliant))
                .thenReturn(serviceSuccess(expectedPageResource));

        mockMvc.perform(get("/competitioninvite/getInvitationOverview/{competitionId}", 1L)
                .param("size", "20")
                .param("page", "0")
                .param("sort", "invite.name,asc")
                .param("innovationArea", "10")
                .param("status", "ACCEPTED")
                .param("compliant", "1"))
                .andExpect(status().isOk())
                .andDo(document("competitioninvite/{method-name}",
                        pathParameters(
                                parameterWithName("competitionId").description("Id of the competition")
                        ),
                        requestParameters(
                                parameterWithName("size").optional()
                                        .description("Maximum number of elements in a single page. Defaults to 20."),
                                parameterWithName("page").optional()
                                        .description("Page number of the paginated data. Starts at 0. Defaults to 0."),
                                parameterWithName("sort").optional()
                                        .description("The property to sort the elements on. For example `sort=invite.name,asc`. Defaults to `invite.name,asc`"),
                                parameterWithName("innovationArea").optional()
                                        .description("Innovation area ID to filter assessors by."),
                                parameterWithName("status").optional()
                                        .description("Participant status to filter assessors by. Can only be 'ACCEPTED', 'REJECTED' or 'PENDING'."),
                                parameterWithName("compliant").optional()
                                        .description("Flag to filter assessors by their compliance.")

                        ),
                        responseFields(assessorInviteOverviewPageResourceFields)
                                .andWithPrefix("content[].", assessorInviteOverviewResourceFields)
                ));

        verify(competitionInviteServiceMock, only()).getInvitationOverview(competitionId, pageable, innovationArea, status, compliant);
    }

    @Test
    public void getInviteStatistics() throws Exception {
        long competitionId = 1L;
        CompetitionInviteStatisticsResource statisticsResource = competitionInviteStatisticsResourceBuilder.build();

        when(competitionInviteServiceMock.getInviteStatistics(competitionId)).thenReturn(serviceSuccess(statisticsResource));

        mockMvc.perform(get("/competitioninvite/getInviteStatistics/{competitionId}", competitionId))
                .andExpect(status().isOk())
                .andDo(document("competitioninvite/{method-name}",
                        pathParameters(
                                parameterWithName("competitionId").description("Id of the competition the invite stats are for")
                        ),
                        responseFields(competitionInviteStatisticsResourceFields)
                ));
    }

    @Test
    public void inviteUser() throws Exception {
        ExistingUserStagedInviteResource existingUserStagedInviteResource = existingUserStagedInviteResourceBuilder.build();

        when(competitionInviteServiceMock.inviteUser(existingUserStagedInviteResource)).thenReturn(serviceSuccess(competitionInviteResourceBuilder.build()));

        mockMvc.perform(post("/competitioninvite/inviteUser")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(existingUserStagedInviteResource)))
                .andExpect(status().isOk())
                .andDo(document("competitioninvite/{method-name}",
                        requestFields(existingUserStagedInviteResourceFields),
                        responseFields(competitionInviteFields)
                ));

        verify(competitionInviteServiceMock, only()).inviteUser(existingUserStagedInviteResource);
    }

    @Test
    public void inviteNewUser() throws Exception {
        NewUserStagedInviteResource newUserStagedInviteResource = newUserStagedInviteResourceBuilder.build();

        when(competitionInviteServiceMock.inviteUser(newUserStagedInviteResource)).thenReturn(serviceSuccess(competitionInviteResourceBuilder.build()));

        mockMvc.perform(post("/competitioninvite/inviteNewUser")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(newUserStagedInviteResource)))
                .andExpect(status().isOk())
                .andDo(document("competitioninvite/{method-name}",
                        requestFields(newUserStagedInviteResourceFields),
                        responseFields(competitionInviteFields)
                ));

        verify(competitionInviteServiceMock, only()).inviteUser(newUserStagedInviteResource);
    }

    @Test
    public void inviteNewUsers() throws Exception {
        long competitionId = 1L;

        NewUserStagedInviteListResource newUserStagedInviteListResource = newUserStagedInviteListResourceBuilder.build();
        List<NewUserStagedInviteResource> newUserStagedInviteResources = newUserStagedInviteListResource.getInvites();

        when(competitionInviteServiceMock.inviteNewUsers(newUserStagedInviteResources, competitionId)).thenReturn(serviceSuccess());

        mockMvc.perform(post("/competitioninvite/inviteNewUsers/{competitionId}", competitionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(newUserStagedInviteListResource)))
                .andExpect(status().isOk())
                .andDo(document("competitioninvite/{method-name}",
                        pathParameters(
                                parameterWithName("competitionId").description("Id of the competition to invite the users to")
                        ),
                        requestFields(
                                fieldWithPath("invites[]").description("List of new users to be invited to assess the competition")
                        ).andWithPrefix("invites[].", newUserStagedInviteResourceFields)
                ));

        verify(competitionInviteServiceMock, only()).inviteNewUsers(newUserStagedInviteResources, competitionId);
    }

    @Test
    public void deleteInvite() throws Exception {
        String email = "firstname.lastname@email.com";
        long competitionId = 1L;

        when(competitionInviteServiceMock.deleteInvite(email, competitionId)).thenReturn(serviceSuccess());

        mockMvc.perform(delete("/competitioninvite/deleteInvite")
                .param("email", email)
                .param("competitionId", String.valueOf(competitionId)))
                .andExpect(status().isNoContent())
                .andDo(document("competitioninvite/{method-name}",
                        requestParameters(
                                parameterWithName("email").description("Email address of the invite"),
                                parameterWithName("competitionId").description("Id of the competition")
                        )
                ));

        verify(competitionInviteServiceMock, only()).deleteInvite(email, competitionId);
    }

    @Test
    public void sendInvite() throws Exception {
        long inviteId = 1L;

        AssessorInviteSendResource assessorInviteSendResource = assessorInviteSendResourceBuilder.build();
        when(competitionInviteServiceMock.sendInvite(inviteId, assessorInviteSendResource)).thenReturn(serviceSuccess());

        mockMvc.perform(post("/competitioninvite/sendInvite/{inviteId}", inviteId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(assessorInviteSendResource)))
                .andExpect(status().isOk())
                .andDo(document("competitioninvite/{method-name}",
                        pathParameters(
                                parameterWithName("inviteId").description("Id of the created invite being sent")
                        ),
                        requestFields(assessorInviteSendResourceFields)
                ));
    }
}
