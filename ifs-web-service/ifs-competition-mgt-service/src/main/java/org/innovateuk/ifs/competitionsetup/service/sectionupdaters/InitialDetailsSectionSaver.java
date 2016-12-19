package org.innovateuk.ifs.competitionsetup.service.sectionupdaters;

import org.innovateuk.ifs.application.service.CategoryService;
import org.innovateuk.ifs.application.service.CompetitionService;
import org.innovateuk.ifs.application.service.MilestoneService;
import org.innovateuk.ifs.category.resource.CategoryResource;
import org.innovateuk.ifs.commons.error.Error;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.competition.resource.CompetitionSetupSection;
import org.innovateuk.ifs.competition.resource.MilestoneResource;
import org.innovateuk.ifs.competition.resource.MilestoneType;
import org.innovateuk.ifs.competitionsetup.form.CompetitionSetupForm;
import org.innovateuk.ifs.competitionsetup.form.InitialDetailsForm;
import org.innovateuk.ifs.competitionsetup.service.CompetitionSetupMilestoneService;
import org.innovateuk.ifs.competitionsetup.form.MilestoneRowForm;
import org.apache.commons.collections4.map.LinkedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.innovateuk.ifs.commons.error.Error.fieldError;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceFailure;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static java.util.Collections.singletonList;
import static org.codehaus.groovy.runtime.InvokerHelper.asList;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

/**
 * Competition setup section saver for the initial details section.
 */
@Service
public class InitialDetailsSectionSaver extends AbstractSectionSaver implements CompetitionSetupSectionSaver {

	private static Log LOG = LogFactory.getLog(InitialDetailsSectionSaver.class);
    public final static String OPENINGDATE_FIELDNAME = "openingDate";

	@Autowired
	private CompetitionService competitionService;

    @Autowired
    private MilestoneService milestoneService;

	@Autowired
	private CompetitionSetupMilestoneService competitionSetupMilestoneService;

	@Autowired
	private CategoryService categoryService;

	@Override
	public CompetitionSetupSection sectionToSave() {
		return CompetitionSetupSection.INITIAL_DETAILS;
	}

	@Override
	protected ServiceResult<Void> doSaveSection(CompetitionResource competition, CompetitionSetupForm competitionSetupForm) {
		
		InitialDetailsForm initialDetailsForm = (InitialDetailsForm) competitionSetupForm;
        if (!competition.isSetupAndAfterNotifications()) {
            competition.setExecutive(initialDetailsForm.getExecutiveUserId());
            competition.setLeadTechnologist(initialDetailsForm.getLeadTechnologistUserId());

            if (!Boolean.TRUE.equals(competition.getSetupComplete())) {

                competition.setName(initialDetailsForm.getTitle());

                if (shouldTryToSaveStartDate(initialDetailsForm)) {
                    try {
                        LocalDateTime startDate = LocalDateTime.of(initialDetailsForm.getOpeningDateYear(),
                                initialDetailsForm.getOpeningDateMonth(), initialDetailsForm.getOpeningDateDay(), 0, 0);
                        competition.setStartDate(startDate);

                        List<Error> errors = saveOpeningDateAsMilestone(startDate, competition.getId(), initialDetailsForm.isMarkAsCompleteAction());
                        if (!errors.isEmpty()) {
                            return serviceFailure(errors);
                        }

                    } catch (Exception e) {
                        LOG.error(e.getMessage());

                        return serviceFailure(asList(fieldError(OPENINGDATE_FIELDNAME, null, "competition.setup.opening.date.not.able.to.save")));
                    }
                }


                competition.setCompetitionType(initialDetailsForm.getCompetitionTypeId());
                competition.setInnovationSector(initialDetailsForm.getInnovationSectorCategoryId());

                if (competition.getInnovationSector() != null) {
                    List<CategoryResource> children = categoryService.getCategoryByParentId(competition.getInnovationSector());
                    List<CategoryResource> matchingChildren =
                            children.stream().filter(child -> initialDetailsForm.getInnovationAreaCategoryIds().contains(child.getId())).collect(Collectors.toList());

                    if (matchingChildren.isEmpty() && initialDetailsForm.isMarkAsCompleteAction()) {
                        return serviceFailure(asList(fieldError("innovationAreaCategoryIds",
                                initialDetailsForm.getInnovationAreaCategoryIds(),
                                "competition.setup.innovation.area.must.be.selected",
                                singletonList(children.stream().map(child -> child.getName()).collect(Collectors.joining(", "))))));
                    }
                }
                competition.setInnovationAreas(initialDetailsForm.getInnovationAreaCategoryIds().stream().collect(Collectors.toSet()));
            }
            return competitionService.update(competition).andOnSuccess(() -> {
                if (initialDetailsForm.isMarkAsCompleteAction() && Boolean.FALSE.equals(competition.getSetupComplete())) {
                    return competitionService.initApplicationFormByCompetitionType(competition.getId(), initialDetailsForm.getCompetitionTypeId());
                } else {
                    return serviceSuccess();
                }
            });
        } else {
            return serviceFailure(new Error("Initial details section is not editable after notifications", BAD_REQUEST));
        }
   }

   private boolean shouldTryToSaveStartDate(InitialDetailsForm initialDetailsForm) {
       return initialDetailsForm.isMarkAsCompleteAction() ||
               (initialDetailsForm.getOpeningDateYear() != null &&
               initialDetailsForm.getOpeningDateMonth() != null &&
               initialDetailsForm.getOpeningDateDay() != null);
   }

	private List<Error> validateOpeningDate(LocalDateTime openingDate) {
	    if(openingDate.getYear() > 9999) {
            return asList(fieldError(OPENINGDATE_FIELDNAME, openingDate.toString(), "validation.initialdetailsform.openingdateyear.range"));
        }

        if (openingDate.isBefore(LocalDateTime.now())) {
            return asList(fieldError(OPENINGDATE_FIELDNAME, openingDate.toString(), "competition.setup.opening.date.not.in.future"));
        }

        return Collections.emptyList();
    }

	private List<Error> saveOpeningDateAsMilestone(LocalDateTime openingDate, Long competitionId, boolean isMarkAsCompleteAction) {
		if (isMarkAsCompleteAction) {
			List<Error> errors = validateOpeningDate(openingDate);
			if (!errors.isEmpty()) {
				return errors;
			}
		}

	    MilestoneRowForm milestoneEntry = new MilestoneRowForm(MilestoneType.OPEN_DATE, openingDate);


        List<MilestoneResource> milestones = milestoneService.getAllMilestonesByCompetitionId(competitionId);
        if(milestones.isEmpty()) {
            milestones = competitionSetupMilestoneService.createMilestonesForCompetition(competitionId);
        }
        milestones.sort((c1, c2) -> c1.getType().compareTo(c2.getType()));

		LinkedMap<String, MilestoneRowForm> milestoneEntryMap = new LinkedMap<>();
		milestoneEntryMap.put(MilestoneType.OPEN_DATE.name(), milestoneEntry);

		return competitionSetupMilestoneService.updateMilestonesForCompetition(milestones, milestoneEntryMap, competitionId);
	}

    @Override
    protected ServiceResult<Void> handleIrregularAutosaveCase(CompetitionResource competitionResource, String fieldName, String value, Optional<Long> questionId) {
        if("openingDate".equals(fieldName)) {
            try {
                String[] dateParts = value.split("-");
                LocalDateTime startDate = LocalDateTime.of(
                        Integer.parseInt(dateParts[2]),
                        Integer.parseInt(dateParts[1]),
                        Integer.parseInt(dateParts[0]),
                        0, 0, 0);
                competitionResource.setStartDate(startDate);


                List<Error> errors = saveOpeningDateAsMilestone(startDate, competitionResource.getId(), false);
                if(!errors.isEmpty()) {
                    return serviceFailure(errors);
                } else {
                    return competitionService.update(competitionResource);
                }
            } catch (Exception e) {
                LOG.error(e.getMessage());
                return serviceFailure(fieldError(OPENINGDATE_FIELDNAME, null, "competition.setup.opening.date.not.able.to.save"));
            }
        } else if( "innovationAreaCategoryId".equals(fieldName)) {
            processInnovationSector(value, competitionResource);
            return competitionService.update(competitionResource);
        }
        return super.handleIrregularAutosaveCase(competitionResource, fieldName, value, questionId);
    }

    private void processInnovationSector(String inputValue, CompetitionResource competitionResource) {
        Long value = Long.parseLong(inputValue);
        if (competitionResource.getInnovationAreas().contains(value)) {
            competitionResource.getInnovationAreas().remove(value);
        } else {
            competitionResource.getInnovationAreas().add(value);
        }
    }

	@Override
	public boolean supportsForm(Class<? extends CompetitionSetupForm> clazz) {
		return InitialDetailsForm.class.equals(clazz);
	}
}
