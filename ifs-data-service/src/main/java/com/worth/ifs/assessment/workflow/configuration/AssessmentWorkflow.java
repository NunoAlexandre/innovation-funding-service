package com.worth.ifs.assessment.workflow.configuration;

import com.worth.ifs.assessment.resource.AssessmentOutcomes;
import com.worth.ifs.assessment.resource.AssessmentStates;
import com.worth.ifs.assessment.workflow.actions.AcceptAction;
import com.worth.ifs.assessment.workflow.actions.RecommendAction;
import com.worth.ifs.assessment.workflow.actions.RejectAction;
import com.worth.ifs.assessment.workflow.actions.SubmitAction;
import com.worth.ifs.assessment.workflow.guards.AssessmentGuard;
import com.worth.ifs.assessment.workflow.guards.ProcessOutcomeGuard;
import com.worth.ifs.assessment.workflow.guards.SubmitGuard;
import com.worth.ifs.workflow.WorkflowStateMachineListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import java.util.LinkedHashSet;

import static com.worth.ifs.assessment.resource.AssessmentOutcomes.ACCEPT;
import static com.worth.ifs.assessment.resource.AssessmentOutcomes.RECOMMEND;
import static com.worth.ifs.assessment.resource.AssessmentOutcomes.REJECT;
import static com.worth.ifs.assessment.resource.AssessmentStates.*;
import static java.util.Arrays.asList;

/**
 * Describes the workflow for assessment. This is from accepting a competition to submitting the application.
 * A persistent configuration is used, so we can apply different states to different assessments.
 */
@Configuration
@EnableStateMachine(name = "assessmentStateMachine")
public class AssessmentWorkflow extends StateMachineConfigurerAdapter<AssessmentStates, AssessmentOutcomes> {

    @Override
    public void configure(StateMachineConfigurationConfigurer<AssessmentStates, AssessmentOutcomes> config) throws Exception {
        config.withConfiguration().listener(new WorkflowStateMachineListener());

    }

    @Override
    public void configure(StateMachineStateConfigurer<AssessmentStates, AssessmentOutcomes> states) throws Exception {
        states.withStates()
                .initial(PENDING)
                .states(new LinkedHashSet<>(asList(AssessmentStates.values())));
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<AssessmentStates, AssessmentOutcomes> transitions) throws Exception {
        transitions
                .withExternal()
                    .source(PENDING).target(REJECTED)
                    .event(REJECT)
                    .action(rejectAction())
                    .guard(processOutcomeExistsGuard())
                    .and()
                .withExternal()
                    .source(PENDING).target(OPEN)
                    .event(ACCEPT)
                    .action(acceptAction())
                    .guard(assessmentExistsGuard())
                    .and()
                .withExternal()
                    .source(OPEN).target(ASSESSED)
                    .event(RECOMMEND)
                    .action(recommendAction())
                    .guard(assessmentExistsGuard())
                    .and()
                .withExternal()
                    .source(OPEN).target(REJECTED)
                    .event(REJECT)
                    .action(rejectAction())
                    .guard(processOutcomeExistsGuard())
                    .and()
                .withExternal()
                    .source(ASSESSED).target(ASSESSED)
                    .event(RECOMMEND)
                    .action(recommendAction())
                    .guard(assessmentExistsGuard())
                    .and()
                .withExternal()
                     .source(ASSESSED).target(REJECTED)
                     .event(REJECT)
                     .action(rejectAction())
                     .guard(processOutcomeExistsGuard())
                     .and()
                .withExternal()
                    .source(ASSESSED).target(SUBMITTED)
                    .event(AssessmentOutcomes.SUBMIT)
                    .action(submitAction())
                    .guard(submitGuard());
    }

    @Bean
    public RejectAction rejectAction() {
        return new RejectAction();
    }

    @Bean
    public AcceptAction acceptAction() {
        return new AcceptAction();
    }

    @Bean
    public RecommendAction recommendAction() {
        return new RecommendAction();
    }

    @Bean
    SubmitAction submitAction() {
        return new SubmitAction();
    }

    @Bean
    public AssessmentGuard assessmentExistsGuard() {
        return new AssessmentGuard();
    }

    @Bean
    public ProcessOutcomeGuard processOutcomeExistsGuard() {
        return new ProcessOutcomeGuard();
    }

    @Bean
    public SubmitGuard submitGuard() {
        return new SubmitGuard();
    }

}
