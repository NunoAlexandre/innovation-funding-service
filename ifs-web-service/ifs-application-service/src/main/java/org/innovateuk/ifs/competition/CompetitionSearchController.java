package org.innovateuk.ifs.competition;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

/**
 * Controller that serves pages of competition public content based on search criteria.
 */
@Controller
@RequestMapping("/competition/search")
public class CompetitionSearchController {
    private static final String KEYWORDS_KEY = "keywords";
    private static final String INNOVATION_AREA_ID_KEY = "innovationAreaId";
    private static final String PAGE_NUMBER_KEY = "page";

    private static final String TEMPLATE_FOLDER = "competition/";


    @Autowired
    private CompetitionSearchPopulator itemSearchPopulator;

    @GetMapping
    public String publicContentSearch(Model model, @RequestParam(KEYWORDS_KEY) Optional<String> keywords,
                                      @RequestParam(INNOVATION_AREA_ID_KEY) Optional<Long> innovationAreaId,
                                      @RequestParam(PAGE_NUMBER_KEY) Optional<Long> pageNumber) {

        model.addAttribute("model", itemSearchPopulator.createItemSearchViewModel(innovationAreaId, keywords,  pageNumber));

        return TEMPLATE_FOLDER+"search";
    }
}
