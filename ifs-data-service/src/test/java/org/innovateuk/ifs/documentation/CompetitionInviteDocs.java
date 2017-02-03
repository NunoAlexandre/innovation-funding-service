package org.innovateuk.ifs.documentation;

import org.innovateuk.ifs.assessment.builder.CompetitionInviteResourceBuilder;
import org.innovateuk.ifs.invite.builder.AssessorInviteToSendResourceBuilder;
import org.innovateuk.ifs.invite.builder.ExistingUserStagedInviteResourceBuilder;
import org.innovateuk.ifs.invite.builder.NewUserStagedInviteListResourceBuilder;
import org.innovateuk.ifs.invite.builder.NewUserStagedInviteResourceBuilder;
import org.innovateuk.ifs.invite.resource.CompetitionRejectionResource;
import org.springframework.restdocs.payload.FieldDescriptor;

import java.math.BigDecimal;

import static org.innovateuk.ifs.assessment.builder.CompetitionInviteResourceBuilder.newCompetitionInviteResource;
import static org.innovateuk.ifs.category.builder.InnovationAreaResourceBuilder.newInnovationAreaResource;
import static org.innovateuk.ifs.email.builders.EmailContentResourceBuilder.newEmailContentResource;
import static org.innovateuk.ifs.invite.builder.AssessorInviteToSendResourceBuilder.newAssessorInviteToSendResource;
import static org.innovateuk.ifs.invite.builder.ExistingUserStagedInviteResourceBuilder.newExistingUserStagedInviteResource;
import static org.innovateuk.ifs.invite.builder.NewUserStagedInviteListResourceBuilder.newNewUserStagedInviteListResource;
import static org.innovateuk.ifs.invite.builder.NewUserStagedInviteResourceBuilder.newNewUserStagedInviteResource;
import static org.innovateuk.ifs.invite.builder.RejectionReasonResourceBuilder.newRejectionReasonResource;
import static org.innovateuk.ifs.invite.constant.InviteStatus.CREATED;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

public class CompetitionInviteDocs {

    public static final FieldDescriptor[] competitionInviteFields = {
            fieldWithPath("id").description("Id of the competition invite"),
            fieldWithPath("competitionName").description("Name of the competition"),
            fieldWithPath("email").description("Email of the competition invitee"),
            fieldWithPath("hash").description("Hash id of the competition invite"),
            fieldWithPath("status").description("Status of the competition invite"),
            fieldWithPath("acceptsDate").description("Date of assessor accepting"),
            fieldWithPath("deadlineDate").description("Date of assessor deadline"),
            fieldWithPath("briefingDate").description("Date of assessor briefing"),
            fieldWithPath("assessorPay").description("How much will assessors be paid per application they assess"),
            fieldWithPath("innovationArea").description("Innovation area of the invitee")
    };

    public static final FieldDescriptor[] competitionRejectionFields = {
            fieldWithPath("rejectReason").description("Information about why the invite was rejected"),
            fieldWithPath("rejectComment").description("Optional comments about why the invite was rejected"),
    };

    public static final FieldDescriptor[] assessorToSendFields = {
            fieldWithPath("recipient").description("Name of the recipient of the invite"),
            fieldWithPath("competitionId").description("The id of the competition"),
            fieldWithPath("competitionName").description("The name of the competition"),
            fieldWithPath("emailContent").description("Content of the invite email")
    };

    public static final FieldDescriptor[] existingUserStagedInviteResourceFields = {
            fieldWithPath("email").description("Email of the recipient of the invite"),
            fieldWithPath("competitionId").description("The id of the competition"),
    };

    public static final FieldDescriptor[] newUserStagedInviteResourceFields = {
            fieldWithPath("email").description("Email of the recipient of the invite"),
            fieldWithPath("competitionId").description("The id of the competition"),
            fieldWithPath("name").description("Name of the recipient of the invite"),
            fieldWithPath("innovationAreaId").description("The id of the recipient's innovation area")
    };

    public static final CompetitionInviteResourceBuilder competitionInviteResourceBuilder = newCompetitionInviteResource()
            .withIds(1L)
            .withCompetitionName("Connected digital additive manufacturing")
            .withHash("0519d73a-f062-4784-ae86-7a933a7de4c3")
            .withEmail("paul.plum@gmail.com")
            .withStatus(CREATED)
            .withAssessorPay(BigDecimal.valueOf(100L))
            .withInnovationArea(newInnovationAreaResource()
                    .withId(10L)
                    .withName("Emerging Tech and Industries")
                    .withSector(3L)
                    .build());

    public static final CompetitionRejectionResource competitionInviteResource =
            new CompetitionRejectionResource(newRejectionReasonResource()
                    .withId(1L)
                    .build(),
                    "own company");

    public static final AssessorInviteToSendResourceBuilder assessorInviteToSendResourceBuilder = newAssessorInviteToSendResource()
            .withCompetitionId(1L)
            .withCompetitionName("Connected digital additive manufacturing")
            .withEmailContent(newEmailContentResource()
                .withSubject("subject")
                .withPlainText("plain text")
                .withHtmlText("<html>html text</htm>")
                .build())
            .withRecipient("Paul Plum");

    public static final ExistingUserStagedInviteResourceBuilder existingUserStagedInviteResourceBuilder = newExistingUserStagedInviteResource()
            .withEmail("paul.plum@gmail.com")
            .withCompetitionId(1L);

    public static final NewUserStagedInviteResourceBuilder newUserStagedInviteResourceBuilder = newNewUserStagedInviteResource()
            .withEmail("paul.plum@gmail.com")
            .withCompetitionId(1L)
            .withName("Paul Plum")
            .withInnovationAreaId(8L);

    public static final NewUserStagedInviteListResourceBuilder newUserStagedInviteListResourceBuilder = newNewUserStagedInviteListResource()
            .withInvites(newUserStagedInviteResourceBuilder.build(2));
}
