package org.innovateuk.ifs.competition.domain;

import org.innovateuk.ifs.competition.domain.Competition.DateProvider;
import org.innovateuk.ifs.competition.resource.CompetitionStatus;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CompetitionStatusTest {

	private Competition competition;
    
    private DateProvider dateProvider;

    private LocalDateTime currentDate;
    private LocalDateTime future;
    private LocalDateTime past;
	
    @Before
    public void setUp() {
    	currentDate = LocalDateTime.now();
    	future = currentDate.plusNanos(1);
    	past = currentDate.minusNanos(1);
    	
    	dateProvider = mock(DateProvider.class);
    	when(dateProvider.provideDate()).thenReturn(currentDate);
    	
    	competition = new Competition();
        competition.setSetupComplete(true);
    	competition.setDateProvider(dateProvider);
    }

    @Test
    public void competitionInSetup(){
        competition.setSetupComplete(false);

        assertEquals(CompetitionStatus.COMPETITION_SETUP, competition.getCompetitionStatus());
        assertFalse(competition.getCompetitionStatus().isFeedbackReleased());
    }

    @Test
    public void competitionStatusReadyToOpen(){
    	competition.setStartDate(future);
        
        assertEquals(CompetitionStatus.READY_TO_OPEN, competition.getCompetitionStatus());
        assertFalse(competition.getCompetitionStatus().isFeedbackReleased());
    }

    @Test
    public void competitionStatusOpenIfStartDateInPastAndEndDateInFuture(){
    	competition.setStartDate(past);
    	competition.setEndDate(future);

        assertEquals(CompetitionStatus.OPEN, competition.getCompetitionStatus());
        assertFalse(competition.getCompetitionStatus().isFeedbackReleased());
    }
    
    @Test
    public void competitionStatusOpenIfStartDateMetAndEndDateInFuture(){
    	competition.setStartDate(currentDate);
    	competition.setEndDate(future);

        assertEquals(CompetitionStatus.OPEN, competition.getCompetitionStatus());
        assertFalse(competition.getCompetitionStatus().isFeedbackReleased());
    }
    
    @Test
    public void competitionStatusInAssessmentIfEndDateInPastAndAssessmentEndDateInFuture(){
    	competition.setStartDate(past);
    	competition.setEndDate(past);
        competition.notifyAssessors(past);
    	competition.setFundersPanelDate(future);

        assertEquals(CompetitionStatus.IN_ASSESSMENT, competition.getCompetitionStatus());
        assertFalse(competition.getCompetitionStatus().isFeedbackReleased());
    }
    
    @Test
    public void competitionStatusInAssessmentIfEndDateMetAndAssessmentEndDateInFuture(){
    	competition.setStartDate(past);
    	competition.setEndDate(currentDate);
        competition.notifyAssessors(past);
    	competition.setFundersPanelDate(future);

        assertEquals(CompetitionStatus.IN_ASSESSMENT, competition.getCompetitionStatus());
        assertFalse(competition.getCompetitionStatus().isFeedbackReleased());
    }
    
    @Test
    public void competitionStatusFundersPanelIfFundersPanelDateInPastAndFundersPanelEndDateInFuture(){
    	competition.setStartDate(past);
    	competition.setEndDate(past);
        competition.notifyAssessors(past);
        competition.closeAssessment(past);
    	competition.setFundersPanelDate(past);
    	competition.setFundersPanelEndDate(future);

        assertEquals(CompetitionStatus.FUNDERS_PANEL, competition.getCompetitionStatus());
        assertFalse(competition.getCompetitionStatus().isFeedbackReleased());
    }
    
    @Test
    public void competitionStatusFundersPanelIfFundersPanelDateMetAndFundersPanelEndDateInFuture(){
    	competition.setStartDate(past);
    	competition.setEndDate(past);
        competition.notifyAssessors(past);
        competition.closeAssessment(past);
    	competition.setFundersPanelDate(currentDate);
    	competition.setFundersPanelEndDate(future);

        assertEquals(CompetitionStatus.FUNDERS_PANEL, competition.getCompetitionStatus());
        assertFalse(competition.getCompetitionStatus().isFeedbackReleased());
    }
    
    @Test
    public void competitionStatusFundersPanelIfFundersPanelDateInPastAndFundersPanelEndDateNotSet(){
    	competition.setStartDate(past);
    	competition.setEndDate(past);
        competition.notifyAssessors(past);
        competition.closeAssessment(past);
    	competition.setFundersPanelDate(past);

        assertEquals(CompetitionStatus.FUNDERS_PANEL, competition.getCompetitionStatus());
        assertFalse(competition.getCompetitionStatus().isFeedbackReleased());
    }
    
    @Test
    public void competitionStatusFundersPanelIfFundersPanelDateMetAndFundersPanelEndDateNotSet(){
    	competition.setStartDate(past);
    	competition.setEndDate(past);
        competition.notifyAssessors(past);
        competition.closeAssessment(past);
    	competition.setFundersPanelDate(currentDate);

        assertEquals(CompetitionStatus.FUNDERS_PANEL, competition.getCompetitionStatus());
        assertFalse(competition.getCompetitionStatus().isFeedbackReleased());
    }
    
    @Test
    public void competitionStatusAssessorFeedbackIfFundersPanelEndDateInPastAndAssessorFeedbackDateInFuture(){
    	competition.setStartDate(past);
    	competition.setEndDate(past);
    	competition.setFundersPanelDate(past);
    	competition.setFundersPanelEndDate(past);
        competition.notifyAssessors(past);
        competition.closeAssessment(past);
    	competition.setAssessorFeedbackDate(future);

        assertEquals(CompetitionStatus.ASSESSOR_FEEDBACK, competition.getCompetitionStatus());
        assertTrue(competition.getCompetitionStatus().isFeedbackReleased());
    }
    
    @Test
    public void competitionStatusAssessorFeedbackIfFundersPanelEndDateMetAndAssessorFeedbackDateInFuture(){
    	competition.setStartDate(past);
    	competition.setEndDate(past);
    	competition.setFundersPanelDate(past);
        competition.notifyAssessors(past);
        competition.closeAssessment(past);
    	competition.setFundersPanelEndDate(currentDate);
    	competition.setAssessorFeedbackDate(future);

        assertEquals(CompetitionStatus.ASSESSOR_FEEDBACK, competition.getCompetitionStatus());
        assertTrue(competition.getCompetitionStatus().isFeedbackReleased());
    }
    
    @Test
    public void competitionStatusAssessorFeedbackIfFundersPanelEndDateInPastAndAssessorFeedbackDateNotSet(){
    	competition.setStartDate(past);
    	competition.setEndDate(past);
    	competition.setFundersPanelDate(past);
        competition.notifyAssessors(past);
        competition.closeAssessment(past);
    	competition.setFundersPanelEndDate(past);

        assertEquals(CompetitionStatus.ASSESSOR_FEEDBACK, competition.getCompetitionStatus());
        assertTrue(competition.getCompetitionStatus().isFeedbackReleased());
    }
    
    @Test
    public void competitionStatusAssessorFeedbackIfFundersPanelEndDateMetAndAssessorFeedbackDateNotSet(){
    	competition.setStartDate(past);
    	competition.setEndDate(past);
    	competition.setFundersPanelDate(past);
        competition.notifyAssessors(past);
        competition.closeAssessment(past);
    	competition.setFundersPanelEndDate(currentDate);
    	competition.setAssessorFeedbackDate(future);

        assertEquals(CompetitionStatus.ASSESSOR_FEEDBACK, competition.getCompetitionStatus());
        assertTrue(competition.getCompetitionStatus().isFeedbackReleased());
    }
    
    @Test
    public void competitionStatusProjectSetupIfReleaseFeedbackDateInPast(){
    	competition.setStartDate(past);
    	competition.setEndDate(past);
        competition.setFundersPanelDate(past);
        competition.notifyAssessors(past);
        competition.closeAssessment(past);
    	competition.setFundersPanelEndDate(past);
    	competition.setReleaseFeedbackDate(past);

        assertEquals(CompetitionStatus.PROJECT_SETUP, competition.getCompetitionStatus());
        assertTrue(competition.getCompetitionStatus().isFeedbackReleased());
    }
    
    @Test
    public void competitionStatusProjectSetupIfReleaseFeedbackDateMet(){
    	competition.setStartDate(past);
    	competition.setEndDate(past);
        competition.setFundersPanelDate(past);
        competition.notifyAssessors(past);
        competition.closeAssessment(past);
    	competition.setFundersPanelEndDate(past);
    	competition.setReleaseFeedbackDate(currentDate);

        assertEquals(CompetitionStatus.PROJECT_SETUP, competition.getCompetitionStatus());
        assertTrue(competition.getCompetitionStatus().isFeedbackReleased());
    }

    /**
     * By default the competition status of a new competition should be COMPETITION_SETUP. When this state is finished, the status is changed to
     * COMPETITION_SETUP_FINISHED, then the other statusses are used.
     */
    @Test
    public void competitionStatusProjectSetupForNewCompetition(){
        Competition newCompetition = new Competition();
        assertEquals(CompetitionStatus.COMPETITION_SETUP, newCompetition.getCompetitionStatus());
        assertFalse(competition.getCompetitionStatus().isFeedbackReleased());
    }
    
}
