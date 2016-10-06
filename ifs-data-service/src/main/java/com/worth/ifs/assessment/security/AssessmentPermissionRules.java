package com.worth.ifs.assessment.security;

import com.worth.ifs.assessment.domain.Assessment;
import com.worth.ifs.assessment.mapper.AssessmentMapper;
import com.worth.ifs.assessment.repository.AssessmentRepository;
import com.worth.ifs.assessment.resource.AssessmentOutcomes;
import com.worth.ifs.assessment.resource.AssessmentResource;
import com.worth.ifs.assessment.resource.AssessmentStates;
import com.worth.ifs.security.BasePermissionRules;
import com.worth.ifs.commons.security.PermissionRule;
import com.worth.ifs.commons.security.PermissionRules;
import com.worth.ifs.user.domain.User;
import com.worth.ifs.user.mapper.UserMapper;
import com.worth.ifs.user.resource.UserResource;
import com.worth.ifs.workflow.domain.ActivityState;
import com.worth.ifs.workflow.domain.ProcessOutcome;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Provides the permissions around CRUD operations for {@link com.worth.ifs.assessment.domain.Assessment} resources.
 */
@Component
@PermissionRules
public class AssessmentPermissionRules extends BasePermissionRules {
    @Autowired
    AssessmentMapper mapper;

    @Autowired
    AssessmentRepository assessmentRepository;

    @PermissionRule(value = "READ", description = "Assessors can read Assessments")
    public boolean userCanReadAssessment(final AssessmentResource assessment, final UserResource user) {
        return isAssessorForAssessment(assessment, user);
    }

    @PermissionRule(value = "UPDATE", description = "Only owners can update Assessments")
    public boolean userCanUpdateAssessment(final AssessmentResource assessment, final UserResource user) {
        return isAssessorForAssessment(assessment, user);
    }

    private boolean isAssessorForAssessment(final AssessmentResource assessment, final UserResource user) {
        Long assessmentUser = processRoleRepository.findOne(assessment.getProcessRole()).getUser().getId();
        return user.getId().equals(assessmentUser) && !assessorHasRejectedAssessment(assessment);
    }

    private boolean assessorHasRejectedAssessment(final AssessmentResource assessmentResource) {
        Assessment assessment = assessmentRepository.findOne(assessmentResource.getId());

        if(assessment.getLastOutcome()!=null) {
            return assessment.getLastOutcome().getOutcomeType().equals(AssessmentOutcomes.REJECT.getType());
        }
        else {
            return false;
        }
    }
}
