package org.innovateuk.ifs.invite.controller;

import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.invite.resource.InviteProjectResource;
import org.innovateuk.ifs.invite.transactional.InviteProjectService;
import org.innovateuk.ifs.user.resource.UserResource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

import static org.innovateuk.ifs.invite.resource.InviteProjectConstants.*;

/**
 * Project Invite controller to handle RESTful service related to Project Invite
 */

@RestController
@RequestMapping(PROJECT_INVITE_BASE_URL)
public class InviteProjectController {

    private static final Log LOG = LogFactory.getLog(InviteProjectController.class);

    @Autowired
    private InviteProjectService inviteProjectService;

    @RequestMapping(value = PROJECT_INVITE_SAVE, method = RequestMethod.POST)
    public RestResult<Void> saveProjectInvites(@RequestBody @Valid InviteProjectResource inviteProjectResource) {

        return inviteProjectService.saveProjectInvite(inviteProjectResource).toPostResponse();
    }

    @RequestMapping(value = GET_INVITE_BY_HASH + "{hash}", method = RequestMethod.GET)
    public RestResult<InviteProjectResource> getProjectInviteByHash(@PathVariable("hash") String hash) {
        return inviteProjectService.getInviteByHash(hash).toGetResponse();
    }

    @RequestMapping(value = GET_PROJECT_INVITE_LIST + "{projectId}", method = RequestMethod.GET)
    public RestResult<List<InviteProjectResource>> getInvitesByProject(@PathVariable("projectId") Long projectId) {
        return inviteProjectService.getInvitesByProject(projectId).toGetResponse();
    }

    @RequestMapping(value = ACCEPT_INVITE  + "{hash}/{userId}", method = RequestMethod.PUT)
    public RestResult<Void> acceptInvite( @PathVariable("hash") String hash, @PathVariable("userId") Long userId) {
        return inviteProjectService.acceptProjectInvite(hash, userId).toPostResponse();
    }

    @RequestMapping(value = CHECK_EXISTING_USER_URL + "{hash}", method = RequestMethod.GET)
    public RestResult<Boolean> checkExistingUser( @PathVariable("hash") String hash) {
        return inviteProjectService.checkUserExistingByInviteHash(hash).toGetResponse();
    }

    @RequestMapping(value = GET_USER_BY_HASH_MAPPING + "{hash}", method = RequestMethod.GET)
    public RestResult<UserResource> getUser( @PathVariable("hash") String hash) {
        return inviteProjectService.getUserByInviteHash(hash).toGetResponse();
    }

}
