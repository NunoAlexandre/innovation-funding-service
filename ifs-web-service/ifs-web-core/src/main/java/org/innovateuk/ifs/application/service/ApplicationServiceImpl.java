package org.innovateuk.ifs.application.service;

import org.innovateuk.ifs.application.resource.ApplicationResource;
import org.innovateuk.ifs.application.resource.ApplicationState;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.competition.resource.CompetitionStatus;
import org.innovateuk.ifs.competition.service.CompetitionsRestService;
import org.innovateuk.ifs.invite.service.InviteRestService;
import org.innovateuk.ifs.user.resource.OrganisationResource;
import org.innovateuk.ifs.user.resource.ProcessRoleResource;
import org.innovateuk.ifs.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;
import static org.innovateuk.ifs.application.service.Futures.call;

/**
 * This class contains methods to retrieve and store {@link ApplicationResource} related data,
 * through the RestService {@link ApplicationRestService}.
 */
@Service
public class ApplicationServiceImpl implements ApplicationService {

    @Autowired
    private ApplicationRestService applicationRestService;

    @Autowired
    private CompetitionsRestService competitionsRestService;

    @Autowired
    private InviteRestService inviteRestService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrganisationService organisationService;

    @Override
    public ApplicationResource getById(Long applicationId) {
        if (applicationId == null) {
            return null;
        }
        return applicationRestService.getApplicationById(applicationId).getSuccessObjectOrThrowException();
    }

    @Override
    public List<ApplicationResource> getInProgress(Long userId) {
        List<ApplicationResource> applications = applicationRestService.getApplicationsByUserId(userId).getSuccessObjectOrThrowException();
        return applications.stream()
                .filter(this::applicationInProgress)
                .collect(Collectors.toCollection(ArrayList::new));
    }
    
    @Override
    public List<ApplicationResource> getFinished(Long userId) {
        List<ApplicationResource> applications = applicationRestService.getApplicationsByUserId(userId).getSuccessObjectOrThrowException();
        return applications.stream()
                .filter(this::applicationFinished)
                .collect(Collectors.toCollection(ArrayList::new));
    }
    
    private boolean applicationInProgress(ApplicationResource a) {
    	boolean applicationInProgressForOpenCompetition = applicationStateInProgress(a) && competitionOpen(a);
    	boolean applicationSubmittedForCompetitionNotYetFinishedFunding = applicationStateSubmitted(a) && competitionFundingNotYetComplete(a);

    	return applicationInProgressForOpenCompetition || applicationSubmittedForCompetitionNotYetFinishedFunding;
    }
   
    private boolean applicationFinished(ApplicationResource a) {
    	boolean applicationInProgressForClosedCompetition = applicationStateInProgress(a) && competitionClosed(a);
    	boolean applicationSubmittedForCompetitionFinishedFunding = applicationStateSubmitted(a) && competitionFundingComplete(a);
    	boolean applicationFinished = applicationStateFinished(a);
    	
    	return applicationInProgressForClosedCompetition || applicationSubmittedForCompetitionFinishedFunding || applicationFinished;
    }
    
    private boolean applicationStateInProgress(ApplicationResource a) {
    	return appStatusIn(a, ApplicationState.CREATED, ApplicationState.OPEN);
    }

	private boolean applicationStateFinished(ApplicationResource a) {
		return appStatusIn(a, ApplicationState.APPROVED, ApplicationState.REJECTED);
    }
	
    private boolean applicationStateSubmitted(ApplicationResource a) {
    	return a.getApplicationState() == ApplicationState.SUBMITTED;
    }
    
    private boolean competitionOpen(ApplicationResource a) {
    	CompetitionResource competition = competitionsRestService.getCompetitionById(a.getCompetition()).getSuccessObjectOrThrowException();
    	return CompetitionStatus.OPEN.equals(competition.getCompetitionStatus());
    }
    
    private boolean competitionFundingNotYetComplete(ApplicationResource a) {
    	CompetitionResource competition = competitionsRestService.getCompetitionById(a.getCompetition()).getSuccessObjectOrThrowException();
    	return compStatusIn(competition, CompetitionStatus.OPEN, CompetitionStatus.IN_ASSESSMENT, CompetitionStatus.FUNDERS_PANEL);
    }
    
    private boolean competitionFundingComplete(ApplicationResource a) {
    	CompetitionResource competition = competitionsRestService.getCompetitionById(a.getCompetition()).getSuccessObjectOrThrowException();
    	return compStatusIn(competition, CompetitionStatus.ASSESSOR_FEEDBACK, CompetitionStatus.PROJECT_SETUP);
	}
    
    private boolean appStatusIn(ApplicationResource app, ApplicationState... states) {
    	for(ApplicationState state: states) {
    		if (state == app.getApplicationState()) {
    			return true;
    		}
    	}
    	return false;
    }
    
    private boolean compStatusIn(CompetitionResource comp, CompetitionStatus... statuses) {
    	for(CompetitionStatus status: statuses) {
    		if(status.equals(comp.getCompetitionStatus())) {
    			return true;
    		}
    	}
    	return false;
    }
    
    private boolean competitionClosed(ApplicationResource a) {
    	return !competitionOpen(a);
    }
    
    @Override
    public Map<Long, Integer> getProgress(Long userId) {
        List<ApplicationResource> applications = applicationRestService.getApplicationsByUserId(userId).getSuccessObjectOrThrowException();
        return applications.stream()
                .filter(this::applicationInProgress)
                .collect(toMap(ApplicationResource::getId, a -> a.getCompletion().intValue()));
    }

    @Override
    public ApplicationResource createApplication(Long competitionId, Long userId, String applicationName) {
        return applicationRestService.createApplication(competitionId, userId, applicationName).getSuccessObjectOrThrowException();
    }

    @Override
    public ServiceResult<Void> updateState(Long applicationId, ApplicationState state) {
        return applicationRestService.updateApplicationState(applicationId, state).toServiceResult();
    }

    @Override
    public Integer getCompleteQuestionsPercentage(Long applicationId) {
        return call(
                applicationRestService.getCompleteQuestionsPercentage(applicationId)
            ).getSuccessObjectOrThrowException()
            .intValue();

    }

    @Override
    public int getAssignedQuestionsCount(Long applicationId, Long processRoleId){
        return applicationRestService.getAssignedQuestionsCount(applicationId, processRoleId).getSuccessObject().intValue();
    }

    @Override
    public ServiceResult<Void> save(ApplicationResource application) {
        return applicationRestService.saveApplication(application).toServiceResult();
    }

    @Override
    public ServiceResult<ApplicationResource> findByProcessRoleId(Long id) {
        return applicationRestService.findByProcessRoleId(id).toServiceResult();
    }

    @Override
    public OrganisationResource getLeadOrganisation(Long applicationId) {
        ApplicationResource application = getById(applicationId);
        ProcessRoleResource leadApplicantProcessRole = userService.getLeadApplicantProcessRoleOrNull(application);
        return organisationService.getOrganisationById(leadApplicantProcessRole.getOrganisationId());
    }

    @Override
    public ServiceResult<Void> removeCollaborator(Long applicationInviteId) {
        return inviteRestService.removeApplicationInvite(applicationInviteId).toServiceResult();
    }
}
