package com.worth.ifs.assessment.controller;

import com.worth.ifs.assessment.resource.AssessmentResource;
import com.worth.ifs.assessment.transactional.AssessmentService;
import com.worth.ifs.commons.rest.RestResult;
import com.worth.ifs.workflow.resource.ProcessOutcomeResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

/**
 * Exposes CRUD operations through a REST API to manage {@link com.worth.ifs.assessment.domain.Assessment} related data.
 */
@RestController
@RequestMapping("/assessment")
public class AssessmentController {

    @Autowired
    private AssessmentService assessmentService;

    @RequestMapping(value= "/{id}", method = GET)
    public RestResult<AssessmentResource> findById(@PathVariable("id") Long id) {
        return assessmentService.findById(id).toGetResponse();
    }

    @RequestMapping(value= "/user/{userId}/competition/{competitionId}", method = GET)
    public RestResult<List<AssessmentResource>> findByUserAndCompetition(@PathVariable("userId") Long userId, @PathVariable("competitionId") Long competitionId ) {
        return assessmentService.findByUserAndCompetition(userId, competitionId).toGetResponse();
    }

    @RequestMapping(value= "/{id}/recommend", method = PUT)
    public RestResult<Void> recommend(@PathVariable("id") Long id,@RequestBody ProcessOutcomeResource processOutcome) {
        return assessmentService.recommend(id, processOutcome).toPutResponse();
    }

    @RequestMapping(value= "/{id}/rejectInvitation", method = PUT)
    public RestResult<Void> rejectInvitation(@PathVariable("id") Long id,@RequestBody ProcessOutcomeResource processOutcome) {
        return assessmentService.rejectInvitation(id, processOutcome).toPutResponse();
    }

}
