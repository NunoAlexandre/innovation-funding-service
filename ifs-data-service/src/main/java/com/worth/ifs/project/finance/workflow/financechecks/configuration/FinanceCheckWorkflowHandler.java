package com.worth.ifs.project.finance.workflow.financechecks.configuration;

import com.worth.ifs.project.domain.PartnerOrganisation;
import com.worth.ifs.project.domain.ProjectUser;
import com.worth.ifs.project.finance.domain.FinanceCheckProcess;
import com.worth.ifs.project.finance.repository.FinanceCheckProcessRepository;
import com.worth.ifs.project.finance.resource.FinanceCheckOutcomes;
import com.worth.ifs.project.finance.resource.FinanceCheckState;
import com.worth.ifs.project.repository.PartnerOrganisationRepository;
import com.worth.ifs.project.repository.ProjectUserRepository;
import com.worth.ifs.workflow.BaseWorkflowEventHandler;
import com.worth.ifs.workflow.domain.ActivityType;
import com.worth.ifs.workflow.repository.ProcessRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.repository.CrudRepository;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.stereotype.Component;

import static com.worth.ifs.project.finance.resource.FinanceCheckOutcomes.*;
import static com.worth.ifs.project.finance.resource.FinanceCheckState.APPROVED;
import static com.worth.ifs.workflow.domain.ActivityType.PROJECT_SETUP_FINANCE_CHECKS;

/**
 * {@code FinanceCheckWorkflowService} is the entry point for triggering the workflow.
 * Based on the Finance Check's current state the next one is tried to transition to by triggering
 * an event.
 */
@Component
public class FinanceCheckWorkflowHandler extends BaseWorkflowEventHandler<FinanceCheckProcess, FinanceCheckState, FinanceCheckOutcomes, PartnerOrganisation, ProjectUser> {

    @Autowired
    @Qualifier("financeCheckStateMachine")
    private StateMachine<FinanceCheckState, FinanceCheckOutcomes> stateMachine;

    @Autowired
    private FinanceCheckProcessRepository financeCheckProcessRepository;

    @Autowired
    private PartnerOrganisationRepository partnerOrganisationRepository;

    @Autowired
    private ProjectUserRepository projectUserRepository;

    public boolean projectCreated(PartnerOrganisation partnerOrganisation, ProjectUser originalLeadApplicantProjectUser) {
        return fireEvent(projectCreatedEvent(partnerOrganisation, originalLeadApplicantProjectUser), FinanceCheckState.PENDING);
    }

    public boolean financeCheckFiguresEdited(PartnerOrganisation partnerOrganisation, ProjectUser projectUser) {
        return fireEvent(financeCheckFiguresEditedEvent(partnerOrganisation, projectUser, FINANCE_CHECK_FIGURES_EDITED), partnerOrganisation);
    }

    public boolean approveFinanceCheckFigures(PartnerOrganisation partnerOrganisation, ProjectUser projectUser) {
        return fireEvent(approveFinanceCheckMessage(projectUser, partnerOrganisation), partnerOrganisation);
    }

    public boolean isApprovalAllowed(PartnerOrganisation partnerOrganisation) {
        return testEvent(approveFinanceCheckMessage(null, partnerOrganisation), partnerOrganisation);
    }

    @Override
    protected FinanceCheckProcess createNewProcess(PartnerOrganisation target, ProjectUser participant) {
        return new FinanceCheckProcess(participant, target, null);
    }

    public boolean isApproved(PartnerOrganisation partnerOrganisation) {
        FinanceCheckProcess process = getCurrentProcess(partnerOrganisation);
        return process != null && APPROVED.equals(process.getActivityState());
    }

    @Override
    protected ActivityType getActivityType() {
        return PROJECT_SETUP_FINANCE_CHECKS;
    }

    @Override
    protected ProcessRepository<FinanceCheckProcess> getProcessRepository() {
        return financeCheckProcessRepository;
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
    protected StateMachine<FinanceCheckState, FinanceCheckOutcomes> getStateMachine() {
        return stateMachine;
    }

    private MessageBuilder<FinanceCheckOutcomes> projectCreatedEvent(PartnerOrganisation partnerOrganisation, ProjectUser originalLeadApplicantProjectUser) {
        return MessageBuilder
                .withPayload(PROJECT_CREATED)
                .setHeader("target", partnerOrganisation)
                .setHeader("participant", originalLeadApplicantProjectUser);
    }

    private MessageBuilder<FinanceCheckOutcomes> financeCheckFiguresEditedEvent(PartnerOrganisation partnerOrganisation, ProjectUser projectUser,
                                                                                FinanceCheckOutcomes event) {
        return MessageBuilder
                .withPayload(event)
                .setHeader("target", partnerOrganisation)
                .setHeader("participant", projectUser);
    }

    private MessageBuilder<FinanceCheckOutcomes> approveFinanceCheckMessage(ProjectUser projectUser, PartnerOrganisation partnerOrganisation) {
        return MessageBuilder
                .withPayload(APPROVE)
                .setHeader("target", partnerOrganisation)
                .setHeader("participant", projectUser);
    }
}
