package org.innovateuk.ifs.competitionsetup.service.formpopulator.application;

import org.innovateuk.ifs.application.resource.QuestionResource;
import org.innovateuk.ifs.application.service.QuestionService;
import org.innovateuk.ifs.commons.error.exception.ObjectNotFoundException;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.competition.resource.CompetitionSetupQuestionResource;
import org.innovateuk.ifs.competitionsetup.form.CompetitionSetupForm;
import org.innovateuk.ifs.competitionsetup.form.application.ApplicationProjectForm;
import org.innovateuk.ifs.competitionsetup.service.CompetitionSetupQuestionService;
import org.innovateuk.ifs.form.service.FormInputService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;

import static org.innovateuk.ifs.application.builder.QuestionResourceBuilder.newQuestionResource;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationProjectFormPopulatorTest {

    @InjectMocks
	private ApplicationProjectFormPopulator populator;

    @Mock
    private CompetitionSetupQuestionService competitionSetupQuestionService;

    @Mock
    private QuestionService questionService;

    @Mock
    private FormInputService formInputService;

    private Long questionId = 7890L;
    private Long questionNotFoundId = 12904L;
    private QuestionResource questionResource = newQuestionResource().withId(questionId).build();
    private CompetitionResource competitionResource;


	@Test
    public void testPopulateFormWithoutErrors() {
        CompetitionSetupQuestionResource resource = new CompetitionSetupQuestionResource();
        when(questionService.getById(questionId)).thenReturn(questionResource);
        when(competitionSetupQuestionService.getQuestion(questionId)).thenReturn(serviceSuccess(resource));

        CompetitionSetupForm result = populator.populateForm(competitionResource, Optional.of(questionId));

        assertTrue(result instanceof ApplicationProjectForm);
        ApplicationProjectForm form = (ApplicationProjectForm) result;
        assertEquals(form.getQuestion(), resource);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testPopulateFormWithErrors() {
        when(competitionSetupQuestionService.getQuestion(questionNotFoundId)).thenThrow(new ObjectNotFoundException());
        CompetitionSetupForm result = populator.populateForm(competitionResource, Optional.of(questionNotFoundId));
        assertEquals(null, result);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testPopulateFormWithNoObjectIdErrors() {
        CompetitionSetupForm result = populator.populateForm(competitionResource, Optional.empty());
        assertEquals(null, result);
    }

}
