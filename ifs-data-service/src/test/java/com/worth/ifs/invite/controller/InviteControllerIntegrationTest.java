package com.worth.ifs.invite.controller;

import com.worth.ifs.BaseControllerIntegrationTest;
import com.worth.ifs.application.constant.ApplicationStatusConstants;
import com.worth.ifs.application.controller.ApplicationController;
import com.worth.ifs.application.controller.QuestionController;
import com.worth.ifs.application.domain.Application;
import com.worth.ifs.application.domain.ApplicationStatus;
import com.worth.ifs.application.resource.ApplicationResource;
import com.worth.ifs.user.domain.ProcessRole;
import com.worth.ifs.user.domain.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;

import java.util.ArrayList;
import java.util.List;

import static com.worth.ifs.security.SecuritySetter.swapOutForUser;
import static org.junit.Assert.assertTrue;

@Ignore
@Rollback
public class InviteControllerIntegrationTest extends BaseControllerIntegrationTest<InviteController> {

    public static final long APPLICATION_ID = 1L;
    private QuestionController questionController;
    private Long leadApplicantProcessRole;
    private Long leadApplicantId;
    private ApplicationController applicationController;
    private InviteOrganisationController inviteOrganisationController;

    @Autowired
    @Override
    protected void setControllerUnderTest(InviteController controller) {
        this.controller = controller;
    }
    @Autowired
    protected void setApplicationController(ApplicationController controller) {
        this.applicationController = controller;
    }

    @Autowired
    protected void setInviteOrganisationController(InviteOrganisationController controller) {
        this.inviteOrganisationController = controller;
    }


    @Before
    public void setUp() throws Exception {
        leadApplicantId = 1L;
        leadApplicantProcessRole = 1L;
        List<ProcessRole> proccessRoles = new ArrayList<>();
        proccessRoles.add(
                new ProcessRole(
                        leadApplicantProcessRole,
                        null,
                        new Application(
                                APPLICATION_ID,
                                "",
                                new ApplicationStatus(
                                        ApplicationStatusConstants.CREATED.getId(),
                                        ApplicationStatusConstants.CREATED.getName()
                                )
                        ),
                        null,
                        null
                )
        );
        User user = new User(leadApplicantId, "steve", "steve.smith@empire.com", "test", "", proccessRoles, "123abc");
        proccessRoles.get(0).setUser(user);
        swapOutForUser(new User(leadApplicantId, "steve", "steve.smith@empire.com", "test", "", proccessRoles, "123abc"));



        assertTrue(applicationController.getApplicationById(APPLICATION_ID).isSuccess());
        ApplicationResource application = applicationController.getApplicationById(APPLICATION_ID).getSuccessObject();
        LOG.info(String.format("Existing application id: %s", application.getId()));
        LOG.info(String.format("Existing application name: %s", application.getName()));


    }

    @After
    public void tearDown() throws Exception {
        swapOutForUser(null);
    }

//    @Test
//    public void addInvites(){
//        List<InviteResource> inviteResources = new ArrayList<>();
//        InviteResource invite1 = new InviteResource("Nico", "nico@email.com", APPLICATION_ID);
//        invite1.setInviteOrganisationName("Worth");
//        InviteResource invite2 = new InviteResource("Brent", "brent@email.com", APPLICATION_ID);
//        invite2.setInviteOrganisationName("Worth");
//        inviteResources.add(invite1);
//        inviteResources.add(invite2);
////        HttpStatus savedStatus = controller.saveInvites(inviteResources).getStatusCode();
//
//        InviteOrganisationResource inviteOrganisation = new InviteOrganisationResource();
//        inviteOrganisation.setId(50L);
//        inviteOrganisation.setOrganisationName("Worth");
//        inviteOrganisation.setInviteResources(inviteResources);
//        inviteOrganisationController.put(inviteOrganisation);
//
//        RestResult savedStatus = controller.createApplicationInvites(inviteOrganisation);
//        LOG.info(String.format("Status of save: %s", savedStatus.getStatusCode().toString()));
//
//        RestResult<Iterable<InviteOrganisationResource>> inviteOrganisationResult = inviteOrganisationController.findAll();
////        RestResult<InviteOrganisationResource> inviteOrganisationResult = inviteOrganisationController.findById(50L);
//        LOG.info("StatusCode: " + inviteOrganisationResult.getStatusCode());
//        Iterable<InviteOrganisationResource> tmp1 = inviteOrganisationResult.getSuccessObject();
//
//        ArrayList<InviteOrganisationResource> tmp2 = Lists.newArrayList(tmp1);
//        LOG.info("StatusCode: "+ tmp2.size());
//
//        RestResult<Set<InviteOrganisationResource>> savedInviteResult = controller.getInvitesByApplication(APPLICATION_ID);
//        LOG.info(String.format("Status of get invites: %s", savedInviteResult.getStatusCode().toString()));
//        assertTrue(savedInviteResult.isSuccess());
//        Set<InviteOrganisationResource> invitesMap = savedInviteResult.getSuccessObject();
//        assertEquals(1, invitesMap.size());
//        assertEquals(2, invitesMap.iterator().next().getInviteResources().size());
//    }

}