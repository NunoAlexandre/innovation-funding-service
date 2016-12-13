package org.innovateuk.ifs.project.gol.workflow.configuration;

import org.innovateuk.ifs.project.domain.Project;
import org.innovateuk.ifs.project.domain.ProjectDetailsProcess;
import org.innovateuk.ifs.project.domain.ProjectUser;
import org.innovateuk.ifs.project.gol.domain.GOLProcess;
import org.innovateuk.ifs.project.gol.repository.GrantOfferLetterProcessRepository;
import org.innovateuk.ifs.project.gol.resource.GOLOutcomes;
import org.innovateuk.ifs.project.gol.resource.GOLState;
import org.innovateuk.ifs.project.repository.ProjectRepository;
import org.innovateuk.ifs.project.repository.ProjectUserRepository;
import org.innovateuk.ifs.project.resource.ProjectDetailsState;
import org.innovateuk.ifs.workflow.BaseWorkflowEventHandler;
import org.innovateuk.ifs.workflow.domain.ActivityType;
import org.innovateuk.ifs.workflow.repository.ProcessRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.repository.CrudRepository;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.stereotype.Component;

import static org.innovateuk.ifs.project.gol.resource.GOLOutcomes.PROJECT_CREATED;
import static org.innovateuk.ifs.project.gol.resource.GOLOutcomes.GOL_SENT;
import static org.innovateuk.ifs.project.gol.resource.GOLOutcomes.GOL_SIGNED;
import static org.innovateuk.ifs.project.gol.resource.GOLOutcomes.GOL_APPROVED;
import static org.innovateuk.ifs.project.gol.resource.GOLOutcomes.GOL_REJECTED;
import static org.innovateuk.ifs.workflow.domain.ActivityType.PROJECT_SETUP_GRANT_OFFER_LETTER;
/**
 * {@code GOLWorkflowService} is the entry point for triggering the workflow.
 *
 * Based on the GOL's current state, the next one is tried to transition to by triggering an event.
 *
 */
@Component
public class GOLWorkflowHandler extends BaseWorkflowEventHandler<GOLProcess, GOLState, GOLOutcomes, Project, ProjectUser> {

    @Autowired
    @Qualifier("golStateMachine")
    private StateMachine<GOLState, GOLOutcomes> stateMachine;

    @Autowired
    private GrantOfferLetterProcessRepository grantOfferLetterProcessRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectUserRepository projectUserRepository;

    public boolean projectCreated(Project project, ProjectUser originalLeadApplicantProjectUser) {
        return fireEvent(projectCreatedEvent(project, originalLeadApplicantProjectUser), GOLState.PENDING);
    }

    public boolean grantOfferLetterSent(Project project, ProjectUser projectUser) {
        return fireEvent(mandatoryValueAddedEvent(project, projectUser, GOL_SENT), project);
    }

    public boolean grantOfferLetterSigned(Project project, ProjectUser projectUser) {
        return fireEvent(mandatoryValueAddedEvent(project, projectUser, GOL_SIGNED), project);
    }

    public boolean grantOfferLetterRejected(Project project, ProjectUser projectUser) {
        return fireEvent(mandatoryValueAddedEvent(project, projectUser, GOL_REJECTED), project);
    }

    public boolean grantOfferLetterApproved(Project project, ProjectUser projectUser) {
        return fireEvent(mandatoryValueAddedEvent(project, projectUser, GOL_APPROVED), project);
    }

    public boolean isSendAllowed(Project project) {
        GOLProcess process = getCurrentProcess(project);
        return process != null && GOLState.PENDING.equals(process.getActivityState());
    }

    public boolean isAlreadySent(Project project) {
        GOLProcess process = getCurrentProcess(project);
        return process != null && !GOLState.PENDING.equals(process.getActivityState());
    }

    public boolean isApproved(Project project) {
        GOLProcess process = getCurrentProcess(project);
        return process != null && GOLState.APPROVED.equals(process.getActivityState());
    }

    public boolean isReadyToApprove(Project project) {
        GOLProcess process = getCurrentProcess(project);
        return process != null && GOLState.READY_TO_APPROVE.equals(process.getActivityState());
    }

    public boolean isSent(Project project) {
        GOLProcess process = getCurrentProcess(project);
        return process != null && GOLState.SENT.equals(process.getActivityState());
    }


    public boolean grantOfferLetterSent(Project project) {
        GOLProcess process = getCurrentProcess(project);
        if(process == null)
            return false;
        ProjectUser projectUser = process.getParticipant();
        if(projectUser == null)
            return false;
        return grantOfferLetterSent(project, projectUser);
    }

    public boolean approve(Project project) {
        GOLProcess process = getCurrentProcess(project);
        if(process == null)
            return false;
        ProjectUser projectUser = process.getParticipant();
        if(projectUser == null)
            return false;
        return grantOfferLetterApproved(project, projectUser);
    }

    public boolean sign(Project project) {
        GOLProcess process = getCurrentProcess(project);
        if(process == null)
            return false;
        ProjectUser projectUser = process.getParticipant();
        if(projectUser == null)
            return false;
        return grantOfferLetterSigned(project, projectUser);
    }

    @Override
    protected GOLProcess createNewProcess(Project target, ProjectUser participant) {
        return new GOLProcess(participant, target, null);
    }

    @Override
    protected ActivityType getActivityType() {
        return PROJECT_SETUP_GRANT_OFFER_LETTER;
    }

    @Override
    protected ProcessRepository<GOLProcess> getProcessRepository() {
        return grantOfferLetterProcessRepository;
    }

    @Override
    protected CrudRepository<Project, Long> getTargetRepository() {
        return projectRepository;
    }

    @Override
    protected CrudRepository<ProjectUser, Long> getParticipantRepository() {
        return projectUserRepository;
    }

    @Override
    protected StateMachine<GOLState, GOLOutcomes> getStateMachine() {
        return stateMachine;
    }

    @Override
    protected GOLProcess getOrCreateProcess(Message<GOLOutcomes> message) {
        return getOrCreateProcessCommonStrategy(message);
    }

    private MessageBuilder<GOLOutcomes> projectCreatedEvent(Project project, ProjectUser originalLeadApplicantProjectUser) {
        return MessageBuilder
                .withPayload(PROJECT_CREATED)
                .setHeader("target", project)
                .setHeader("participant", originalLeadApplicantProjectUser);
    }

    private MessageBuilder<GOLOutcomes> mandatoryValueAddedEvent(Project project, ProjectUser projectUser,
                                                                 GOLOutcomes event) {
        return MessageBuilder
                .withPayload(event)
                .setHeader("target", project)
                .setHeader("participant", projectUser);
    }
}
