package com.worth.ifs.project.workflow.projectdetails.configuration;

import com.worth.ifs.project.resource.ProjectDetailsOutcomes;
import com.worth.ifs.project.resource.ProjectDetailsState;
import com.worth.ifs.project.workflow.projectdetails.actions.ReadyToSubmitProjectDetailsAction;
import com.worth.ifs.project.workflow.projectdetails.actions.SubmitProjectDetailsAction;
import com.worth.ifs.project.workflow.projectdetails.guards.ProjectDetailsSuppliedGuard;
import com.worth.ifs.workflow.WorkflowStateMachineListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import java.util.LinkedHashSet;

import static com.worth.ifs.project.resource.ProjectDetailsOutcomes.SUBMIT;
import static com.worth.ifs.project.resource.ProjectDetailsState.*;
import static java.util.Arrays.asList;

/**
 * Describes the workflow for the Project Details section for Project Setup.
 */
@Configuration
@EnableStateMachine(name = "projectDetailsStateMachine")
public class ProjectDetailsWorkflow extends StateMachineConfigurerAdapter<ProjectDetailsState, ProjectDetailsOutcomes> {

    @Override
    public void configure(StateMachineConfigurationConfigurer<ProjectDetailsState, ProjectDetailsOutcomes> config) throws Exception {
        config.withConfiguration().listener(new WorkflowStateMachineListener());

    }

    @Override
    public void configure(StateMachineStateConfigurer<ProjectDetailsState, ProjectDetailsOutcomes> states) throws Exception {
        states.withStates()
                .initial(PENDING)
                .states(new LinkedHashSet<>(asList(ProjectDetailsState.values())));
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<ProjectDetailsState, ProjectDetailsOutcomes> transitions) throws Exception {
        transitions
                .withExternal()
                    .source(PENDING).target(READY_TO_SUBMIT)
                    .action(readyToSubmitProjectDetailsAction())
                    .guard(projectDetailsSuppliedGuard())
                    .and()
                .withExternal()
                    .source(READY_TO_SUBMIT).target(SUBMITTED)
                    .event(SUBMIT)
                    .action(submitProjectDetailsAction())
                    .guard(projectDetailsSuppliedGuard());
    }

    @Bean
    public ReadyToSubmitProjectDetailsAction readyToSubmitProjectDetailsAction() {
        return new ReadyToSubmitProjectDetailsAction();
    }

    @Bean
    public SubmitProjectDetailsAction submitProjectDetailsAction() {
        return new SubmitProjectDetailsAction();
    }

    @Bean
    public ProjectDetailsSuppliedGuard projectDetailsSuppliedGuard() {
        return new ProjectDetailsSuppliedGuard();
    }
}
