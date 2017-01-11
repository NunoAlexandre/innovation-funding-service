package org.innovateuk.ifs.assessment.controller;

import org.innovateuk.ifs.assessment.resource.AssessorProfileResource;
import org.innovateuk.ifs.assessment.transactional.AssessorService;
import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.registration.resource.UserRegistrationResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * Exposes CRUD operations through a REST API to manage assessor related data.
 */
@RestController
@RequestMapping("/assessor")
public class AssessorController {
    @Autowired
    private AssessorService assessorService;

    @RequestMapping(value = "/register/{hash}", method = POST)
    public RestResult<Void> registerAssessorByHash(@PathVariable("hash") String hash, @Valid @RequestBody UserRegistrationResource userResource) {
        return assessorService.registerAssessorByHash(hash, userResource).toPostResponse();
    }

    @RequestMapping(value = "/profile/{assessorId}", method = GET)
    public RestResult<AssessorProfileResource> getAssessorProfile(@PathVariable("assessorId") Long assessorId) {
        return assessorService.getAssessorProfile(assessorId).toGetResponse();
    }
}
