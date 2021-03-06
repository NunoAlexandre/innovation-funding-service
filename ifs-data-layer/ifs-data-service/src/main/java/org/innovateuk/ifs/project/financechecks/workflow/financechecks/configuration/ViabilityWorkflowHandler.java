package org.innovateuk.ifs.project.financechecks.workflow.financechecks.configuration;

import org.innovateuk.ifs.project.domain.PartnerOrganisation;
import org.innovateuk.ifs.project.domain.ProjectUser;
import org.innovateuk.ifs.project.financechecks.domain.ViabilityProcess;
import org.innovateuk.ifs.project.financechecks.repository.ViabilityProcessRepository;
import org.innovateuk.ifs.project.finance.resource.ViabilityEvent;
import org.innovateuk.ifs.project.finance.resource.ViabilityState;
import org.innovateuk.ifs.project.repository.PartnerOrganisationRepository;
import org.innovateuk.ifs.project.repository.ProjectUserRepository;
import org.innovateuk.ifs.user.domain.User;
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

import static org.innovateuk.ifs.project.finance.resource.ViabilityEvent.ORGANISATION_IS_ACADEMIC;
import static org.innovateuk.ifs.project.finance.resource.ViabilityEvent.PROJECT_CREATED;
import static org.innovateuk.ifs.project.finance.resource.ViabilityEvent.VIABILITY_APPROVED;
import static org.innovateuk.ifs.workflow.domain.ActivityType.PROJECT_SETUP_VIABILITY;

/**
 * {@code ViabilityWorkflowService} is the entry point for triggering the workflow.
 *
 * Based on the current state of Viability, the next one is tried to transition to by triggering an event.
 *
 */
@Component
public class ViabilityWorkflowHandler extends BaseWorkflowEventHandler<ViabilityProcess, ViabilityState, ViabilityEvent, PartnerOrganisation, ProjectUser> {

    @Autowired
    @Qualifier("viabilityStateMachine")
    private StateMachine<ViabilityState, ViabilityEvent> stateMachine;

    @Autowired
    private ViabilityProcessRepository viabilityProcessRepository;

    @Autowired
    private PartnerOrganisationRepository partnerOrganisationRepository;

    @Autowired
    private ProjectUserRepository projectUserRepository;

    public boolean projectCreated(PartnerOrganisation partnerOrganisation, ProjectUser originalLeadApplicantProjectUser) {
        return fireEvent(projectCreatedEvent(partnerOrganisation, originalLeadApplicantProjectUser), ViabilityState.REVIEW);
    }

    public boolean viabilityApproved(PartnerOrganisation partnerOrganisation, User internalUser) {
        return fireEvent(internalUserEvent(partnerOrganisation, internalUser, VIABILITY_APPROVED), partnerOrganisation);
    }

    public boolean organisationIsAcademic(PartnerOrganisation partnerOrganisation, User internalUser) {
        return fireEvent(internalUserEvent(partnerOrganisation, internalUser, ORGANISATION_IS_ACADEMIC), partnerOrganisation);
    }

    public ViabilityProcess getProcess(PartnerOrganisation partnerOrganisation) {
        return getCurrentProcess(partnerOrganisation);
    }

    public ViabilityState getState(PartnerOrganisation partnerOrganisation) {
        ViabilityProcess process = getCurrentProcess(partnerOrganisation);
        return process != null ? process.getActivityState() : ViabilityState.REVIEW;
    }

    @Override
    protected ViabilityProcess createNewProcess(PartnerOrganisation target, ProjectUser participant) {
        return new ViabilityProcess(participant, target, null);
    }

    @Override
    protected ActivityType getActivityType() {
        return PROJECT_SETUP_VIABILITY;
    }

    @Override
    protected ProcessRepository<ViabilityProcess> getProcessRepository() {
        return viabilityProcessRepository;
    }

    @Override
    protected CrudRepository<PartnerOrganisation, Long> getTargetRepository() {
        return partnerOrganisationRepository;
    }

    @Override
    protected CrudRepository<ProjectUser, Long> getParticipantRepository() {
        return projectUserRepository;
    }

    @Override
    protected StateMachine<ViabilityState, ViabilityEvent> getStateMachine() {
        return stateMachine;
    }

    @Override
    protected ViabilityProcess getOrCreateProcess(Message<ViabilityEvent> message) {
        return getOrCreateProcessCommonStrategy(message);
    }

    private MessageBuilder<ViabilityEvent> projectCreatedEvent(PartnerOrganisation partnerOrganisation, ProjectUser originalLeadApplicantProjectUser) {
        return MessageBuilder
                .withPayload(PROJECT_CREATED)
                .setHeader("target", partnerOrganisation)
                .setHeader("participant", originalLeadApplicantProjectUser);
    }

    private MessageBuilder<ViabilityEvent> internalUserEvent(PartnerOrganisation partnerOrganisation, User internalUser,
                                                             ViabilityEvent event) {
        return MessageBuilder
                .withPayload(event)
                .setHeader("target", partnerOrganisation)
                .setHeader("internalParticipant", internalUser);
    }
}

