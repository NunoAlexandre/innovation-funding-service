package org.innovateuk.ifs.user.controller;

import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.organisation.transactional.OrganisationInitialCreationService;
import org.innovateuk.ifs.organisation.transactional.OrganisationService;
import org.innovateuk.ifs.user.domain.Organisation;
import org.innovateuk.ifs.user.resource.OrganisationResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

/**
 * This RestController exposes CRUD operations to both the
 * {@link org.innovateuk.ifs.user.service.OrganisationRestServiceImpl} and other REST-API users
 * to manage {@link Organisation} related data.
 */
@RestController
@RequestMapping("/organisation")
public class OrganisationController {

    @Autowired
    private OrganisationService organisationService;

    @Autowired
    private OrganisationInitialCreationService organisationCreationService;

    @GetMapping("/findByApplicationId/{applicationId}")
    public RestResult<Set<OrganisationResource>> findByApplicationId(@PathVariable("applicationId") final Long applicationId) {
        return organisationService.findByApplicationId(applicationId).toGetResponse();
    }

    @GetMapping("/findById/{organisationId}")
    public RestResult<OrganisationResource> findById(@PathVariable("organisationId") final Long organisationId) {
        return organisationService.findById(organisationId).toGetResponse();
    }

    @GetMapping("/getPrimaryForUser/{userId}")
    public RestResult<OrganisationResource> getPrimaryForUser(@PathVariable("userId") final Long userId) {
        return organisationService.getPrimaryForUser(userId).toGetResponse();
    }

    @PostMapping("/createOrMatch")
    public RestResult<OrganisationResource> createOrMatch(@RequestBody OrganisationResource organisation) {
        return organisationCreationService.createOrMatch(organisation).toPostCreateResponse();
    }

    @PostMapping("/createAndLinkByInvite")
    public RestResult<OrganisationResource> createAndLinkByInvite(@RequestBody OrganisationResource organisation,
                                                          @RequestParam("inviteHash") String inviteHash) {
        return organisationCreationService.createAndLinkByInvite(organisation, inviteHash).toPostCreateResponse();
    }

    @PostMapping("/create")
    public RestResult<OrganisationResource> create(@RequestBody OrganisationResource organisation) {
        return organisationService.create(organisation).toPostCreateResponse();
    }

    @PutMapping("/update")
    public RestResult<OrganisationResource> saveResource(@RequestBody OrganisationResource organisationResource) {
        return organisationService.update(organisationResource).toPutWithBodyResponse();
    }

    @PostMapping("/updateNameAndRegistration/{organisationId}")
    public RestResult<OrganisationResource> updateNameAndRegistration(@PathVariable("organisationId") Long organisationId, @RequestParam(value = "name") String name, @RequestParam(value = "registration") String registration) {
        return organisationService.updateOrganisationNameAndRegistration(organisationId, name, registration).toPostCreateResponse();
    }
}
