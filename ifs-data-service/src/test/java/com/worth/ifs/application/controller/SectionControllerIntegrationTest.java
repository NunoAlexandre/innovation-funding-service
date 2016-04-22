package com.worth.ifs.application.controller;

import com.worth.ifs.BaseControllerIntegrationTest;
import com.worth.ifs.application.domain.Section;
import com.worth.ifs.application.repository.ApplicationRepository;
import com.worth.ifs.application.repository.SectionRepository;
import com.worth.ifs.application.resource.QuestionApplicationCompositeId;
import com.worth.ifs.application.resource.SectionResource;
import com.worth.ifs.application.transactional.QuestionService;
import com.worth.ifs.application.transactional.SectionService;
import com.worth.ifs.commons.rest.RestResult;
import com.worth.ifs.commons.rest.ValidationMessages;
import com.worth.ifs.security.SecuritySetter;
import com.worth.ifs.user.resource.UserResource;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.Rollback;

import java.util.List;

import static com.worth.ifs.security.SecuritySetter.addBasicSecurityUser;
import static com.worth.ifs.user.builder.UserResourceBuilder.newUserResource;
import static org.junit.Assert.*;

@Rollback
public class SectionControllerIntegrationTest extends BaseControllerIntegrationTest<SectionController> {

    @Autowired
    SectionRepository sectionRepository;
    @Autowired
    ApplicationRepository applicationRepository;
    @Autowired
    private QuestionService questionService;
    @Autowired
    SectionService sectionService;

    private Section section;
    private Long applicationId;
    private Section excludedSections;
    private Long sectionId;
    private Long excludedSectionId;
    private Long leadApplicantId;
    private Long collaboratorIdOne;
    private Long collaboratorOneProcessRoleId;
    private Long leadApplicantProcessRole;
    private long leadApplicantOrganisationId;
    private long collaboratorOneOrganisationId;
    private long sectionIdYourFinances;
    private long sectionIdLabour;



    @Before
    public void setUp() throws Exception {
        sectionId = 1L;
        excludedSectionId = 2L;
        applicationId = 1L;
        section = sectionRepository.findOne(sectionId);
        excludedSections = section = sectionRepository.findOne(excludedSectionId);

        leadApplicantId = 1L;
        leadApplicantProcessRole = 1L;
        leadApplicantOrganisationId = 3L;

        collaboratorIdOne = 8L;
        collaboratorOneProcessRoleId = 9L;
        collaboratorOneOrganisationId = 6L;

        sectionIdYourFinances = 7L;
        sectionIdLabour = 9L;
        addBasicSecurityUser();
    }

    @Autowired
    @Override
    protected void setControllerUnderTest(SectionController controller) {
        this.controller = controller;
    }

    @Test
    public void testGetById() throws Exception {
        SectionResource section = controller.getById(sectionId).getSuccessObject();
        assertEquals("Project details", section.getName());

        section = controller.getById(2L).getSuccessObject();
        assertEquals("Application questions", section.getName());
    }

    /**
     * Check if all sections under Your-Finances is marked-as-complete.
     */
    @Test
    public void testChildSectionsAreCompleteForAllOrganisations() throws Exception {
        excludedSections = null;

        section = sectionRepository.findOne(sectionIdYourFinances);
        assertEquals("Your finances", section.getName());
        assertTrue(section.hasChildSections());
        assertEquals(7, section.getChildSections().size());
        assertTrue(sectionService.childSectionsAreCompleteForAllOrganisations(section, applicationId, excludedSections).getSuccessObject());
        assertEquals(8, controller.getCompletedSections(applicationId, 3L).getSuccessObject().size());

        // Mark one question as incomplete.
        questionService.markAsInComplete(new QuestionApplicationCompositeId(28L, applicationId), leadApplicantProcessRole);
        assertFalse(questionService.isMarkedAsComplete(questionService.getQuestionById(21L).getSuccessObject(), applicationId, leadApplicantOrganisationId).getSuccessObject());

        assertFalse(sectionService.childSectionsAreCompleteForAllOrganisations(section, applicationId, excludedSections).getSuccessObject());
        assertEquals(7, controller.getCompletedSections(applicationId, leadApplicantOrganisationId).getSuccessObject().size());

        UserResource collaborator = newUserResource().withId(collaboratorIdOne).build();
        SecuritySetter.swapOutForUser(collaborator);
        assertEquals(8, controller.getCompletedSections(applicationId, collaboratorOneOrganisationId).getSuccessObject().size());

        section = sectionRepository.findOne(11L);
        assertEquals("Materials", section.getName());
        assertFalse(section.hasChildSections());
    }

    @Test
    @Rollback
    public void testMarkAsComplete(){
        RestResult<List<ValidationMessages>> result = controller.markAsComplete(sectionIdYourFinances, applicationId, leadApplicantProcessRole);
        assertTrue(result.isSuccess());
        List<ValidationMessages> validationMessages = result.getSuccessObject();
        assertEquals(2, validationMessages.size());

        ValidationMessages messages = validationMessages.get(0);
        assertEquals(1, messages.getErrors().size());
        assertEquals(new Long(54), messages.getObjectId());
        assertEquals("costItem", messages.getObjectName());
        assertTrue(messages.getErrors().stream()
                .filter(e -> "".equals(e.getErrorKey()))
                .filter(e -> "You should provide at least one Source of funding".equals(e.getErrorMessage()))
                .filter(e -> HttpStatus.NOT_ACCEPTABLE.equals(e.getStatusCode()))
                .findAny().isPresent());

        messages = validationMessages.get(1);
        assertEquals(1, messages.getErrors().size());
        assertEquals(new Long(35), messages.getObjectId());
        assertEquals("question", messages.getObjectName());
        assertTrue(messages.getErrors().stream()
                .filter(e -> "".equals(e.getErrorKey()))
                .filter(e -> "You should provide at least one Source of funding".equals(e.getErrorMessage()))
                .filter(e -> HttpStatus.NOT_ACCEPTABLE.equals(e.getStatusCode()))
                .findAny().isPresent());
    }
}