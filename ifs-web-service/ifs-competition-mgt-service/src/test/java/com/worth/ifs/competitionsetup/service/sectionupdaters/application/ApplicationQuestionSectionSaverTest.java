package com.worth.ifs.competitionsetup.service.sectionupdaters.application;

import com.worth.ifs.application.service.CompetitionService;
import com.worth.ifs.commons.error.Error;
import com.worth.ifs.competition.resource.CompetitionResource;
import com.worth.ifs.competition.resource.CompetitionSetupSubsection;
import com.worth.ifs.competitionsetup.form.application.ApplicationQuestionForm;
import com.worth.ifs.competitionsetup.service.CompetitionSetupQuestionService;
import com.worth.ifs.competitionsetup.viewmodel.application.QuestionViewModel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;
import java.util.Optional;

import static com.worth.ifs.competition.builder.CompetitionResourceBuilder.newCompetitionResource;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationQuestionSectionSaverTest {

    @InjectMocks
    private ApplicationQuestionSectionSaver service;

    @Mock
    private CompetitionService competitionService;

    @Mock
    private CompetitionSetupQuestionService competitionSetupQuestionService;

    @Test
    public void testSectionToSave() {
        assertEquals(CompetitionSetupSubsection.QUESTIONS, service.sectionToSave());
    }

    @Test
    public void testSaveCompetitionSetupSection() {
        ApplicationQuestionForm competitionSetupForm = new ApplicationQuestionForm();

        CompetitionResource competition = newCompetitionResource()
                .withCompetitionCode("compcode").build();

        service.saveSection(competition, competitionSetupForm);
    }

    @Test
    public void testAutoSaveCompetitionSetupSection() {
        QuestionViewModel question = new QuestionViewModel();
        question.setTitle("TitleOld");

        when(competitionSetupQuestionService.getQuestion(1L)).thenReturn(question);

        CompetitionResource competition = newCompetitionResource().build();
        competition.setMilestones(asList(10L));

        List<Error> errors = service.autoSaveSectionField(competition, "question.title", "newTitle", Optional.of(1L));

        assertTrue(errors.isEmpty());
        assertEquals("newTitle", question.getTitle());
        verify(competitionSetupQuestionService).updateQuestion(question);
    }

    @Test
    public void testAutoSaveCompetitionSetupSectionErrors() {
        QuestionViewModel question = new QuestionViewModel();
        question.setMaxWords(400);

        when(competitionSetupQuestionService.getQuestion(1L)).thenReturn(question);

        CompetitionResource competition = newCompetitionResource().build();
        competition.setMilestones(asList(10L));

        List<Error> errors = service.autoSaveSectionField(competition, "question.maxWords", "Hihi", Optional.of(1L));

        assertFalse(errors.isEmpty());
        assertEquals(Integer.valueOf(400), question.getMaxWords());
        verify(competitionSetupQuestionService, never()).updateQuestion(question);
    }

    @Test
    public void testAutoSaveCompetitionSetupSectionUnknown() {
        CompetitionResource competition = newCompetitionResource().build();

        List<Error> errors = service.autoSaveSectionField(competition, "notExisting", "Strange!@#1Value", Optional.of(1L));

        assertTrue(!errors.isEmpty());
        verify(competitionService, never()).update(competition);
    }

    @Test
    public void testAutoSaveCompetitionSetupSectionEmptyObjectId() {
        CompetitionResource competition = newCompetitionResource().build();

        List<Error> errors = service.autoSaveSectionField(competition, "notExisting", "Strange!@#1Value", Optional.empty());

        assertTrue(!errors.isEmpty());
        verify(competitionService, never()).update(competition);
    }
}