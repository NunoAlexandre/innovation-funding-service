package com.worth.ifs.invite.controller;

import com.worth.ifs.commons.rest.RestResult;
import com.worth.ifs.invite.resource.InviteOrganisationResource;
import com.worth.ifs.invite.resource.InviteResource;
import com.worth.ifs.invite.resource.InviteResultsResource;
import com.worth.ifs.invite.transactional.InviteService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * InviteController is to handle the REST calls from the web-service and contains the handling of all call involving the Invite and InviteOrganisations.
 */

@RestController
@RequestMapping("/invite")
public class InviteController {
    private static final Log LOG = LogFactory.getLog(InviteController.class);
    @Autowired
    private InviteService inviteService;

    @RequestMapping("/createApplicationInvites")
    public RestResult<InviteResultsResource> createApplicationInvites(@RequestBody InviteOrganisationResource inviteOrganisationResource) {
        return inviteService.createApplicationInvites(inviteOrganisationResource).toPostCreateResponse();
    }

    @RequestMapping("/getInviteByHash/{hash}")
    public RestResult<InviteResource> getInviteByHash(@PathVariable("hash") String hash) {
        return inviteService.getInviteByHash(hash).toGetResponse();
    }

    @RequestMapping("/getInviteOrganisationByHash/{hash}")
    public RestResult<InviteOrganisationResource> getInviteOrganisationByHash(@PathVariable("hash") String hash) {
        return inviteService.getInviteOrganisationByHash(hash).toGetResponse();
    }


    @RequestMapping("/getInvitesByApplicationId/{applicationId}")
    public RestResult<Set<InviteOrganisationResource>> getInvitesByApplication(@PathVariable("applicationId") Long applicationId) {
        return inviteService.getInvitesByApplication(applicationId).toGetResponse();
    }

    @RequestMapping(value = "/saveInvites", method = RequestMethod.POST)
    public RestResult<InviteResultsResource> saveInvites(@RequestBody List<InviteResource> inviteResources) {
        return inviteService.saveInvites(inviteResources).toPostCreateResponse();
    }

    @RequestMapping(value = "/acceptInvite/{hash}/{userId}", method = RequestMethod.PUT)
    public RestResult<Void> acceptInvite( @PathVariable("hash") String hash, @PathVariable("userId") Long userId) {
        return inviteService.acceptInvite(hash, userId).toPutResponse();
    }

    @RequestMapping(value = "/checkExistingUser/{hash}", method = RequestMethod.GET)
    public RestResult<Void> checkExistingUser( @PathVariable("hash") String hash) {
        return inviteService.checkUserExistingByInviteHash(hash).toGetResponse();
    }

}
