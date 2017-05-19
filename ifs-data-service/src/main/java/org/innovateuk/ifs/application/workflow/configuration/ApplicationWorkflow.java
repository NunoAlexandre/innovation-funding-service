package org.innovateuk.ifs.application.workflow.configuration;

import org.innovateuk.ifs.application.resource.ApplicationOutcome;
import org.innovateuk.ifs.application.resource.ApplicationState;
import org.innovateuk.ifs.application.workflow.actions.MarkIneligibleAction;
import org.innovateuk.ifs.workflow.WorkflowStateMachineListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import java.util.LinkedHashSet;

import static java.util.Arrays.asList;

/**
 * The workflow for an {@link org.innovateuk.ifs.application.domain.Application}. Describes the possible states and
 * transitions through an Application's lifecycle.
 */
@Configuration
@EnableStateMachine(name = "applicationProcessStateMachine")
public class ApplicationWorkflow extends StateMachineConfigurerAdapter<ApplicationState, ApplicationOutcome> {

    @Autowired
    private MarkIneligibleAction markIneligibleAction;

    @Override
    public void configure(StateMachineConfigurationConfigurer<ApplicationState, ApplicationOutcome> config) throws Exception {
        config.withConfiguration().listener(new WorkflowStateMachineListener<>());
    }

    @Override
    public void configure(StateMachineStateConfigurer<ApplicationState, ApplicationOutcome> states) throws Exception {
        states.withStates()
                .initial(ApplicationState.CREATED)
                .states(new LinkedHashSet<>(asList(ApplicationState.values())));
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<ApplicationState, ApplicationOutcome> transitions) throws Exception {
        transitions
                .withExternal()
                    .source(ApplicationState.CREATED)
                    .event(ApplicationOutcome.OPENED)
                    .target(ApplicationState.OPEN)
                .and()
                .withExternal()
                    .source(ApplicationState.OPEN)
                    .event(ApplicationOutcome.SUBMITTED)
                    .target(ApplicationState.SUBMITTED)
                .and()
                .withExternal()
                    .source(ApplicationState.SUBMITTED)
                    .event(ApplicationOutcome.MARK_INELIGIBLE)
                    .action(markIneligibleAction)
                    .target(ApplicationState.INELIGIBLE)
                .and()
                .withExternal()
                    .source(ApplicationState.INELIGIBLE)
                    .event(ApplicationOutcome.INFORM_INELIGIBLE)
                    .target(ApplicationState.INELIGIBLE_INFORMED)
                .and()
                .withExternal()
                    .source(ApplicationState.INELIGIBLE)
                    .event(ApplicationOutcome.REINSTATE_INELIGIBLE)
                    .target(ApplicationState.SUBMITTED)
                .and()
                .withExternal()
                    .source(ApplicationState.INELIGIBLE_INFORMED)
                    .event(ApplicationOutcome.REINSTATE_INELIGIBLE)
                    .target(ApplicationState.SUBMITTED)
                .and()
                .withExternal()
                    .source(ApplicationState.SUBMITTED)
                    .event(ApplicationOutcome.APPROVED)
                    .target(ApplicationState.APPROVED)
                .and()
                .withExternal()
                    .source(ApplicationState.SUBMITTED)
                    .event(ApplicationOutcome.REJECTED)
                    .target(ApplicationState.REJECTED);
    }
}
