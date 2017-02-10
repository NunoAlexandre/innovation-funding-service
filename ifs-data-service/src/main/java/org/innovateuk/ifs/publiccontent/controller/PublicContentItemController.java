package org.innovateuk.ifs.publiccontent.controller;

import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.competition.publiccontent.resource.PublicContentItemPageResource;
import org.innovateuk.ifs.competition.publiccontent.resource.PublicContentItemResource;
import org.innovateuk.ifs.publiccontent.transactional.PublicContentItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * Controller for getting and handling Public Content Items
 */
@RestController
@RequestMapping("/public-content/items/")
public class PublicContentItemController {

    @Autowired
    private PublicContentItemService publicContentItemService;

    @RequestMapping(value = "find-by-filter", method = RequestMethod.GET)
    public RestResult<PublicContentItemPageResource> findFilteredItems(@RequestParam(value = "innovationAreaId", required = false) Optional<Long> innovationAreaId,
                                                                       @RequestParam(value = "searchString", required = false) Optional<String> searchString,
                                                                       @RequestParam(value = "pageNumber", required = false) Optional<Integer> pageNumber,
                                                                       @RequestParam(value = "pageSize") Integer pageSize) {
        return publicContentItemService.findFilteredItems(innovationAreaId, searchString, pageNumber, pageSize).toGetResponse();
    }

    @RequestMapping(value = "by-competition-id/{id}", method = RequestMethod.GET)
    public RestResult<PublicContentItemResource> byCompetitionId(@PathVariable("id") final Long competitionId) {
        return publicContentItemService.byCompetitionId(competitionId).toGetResponse();
    }
}
