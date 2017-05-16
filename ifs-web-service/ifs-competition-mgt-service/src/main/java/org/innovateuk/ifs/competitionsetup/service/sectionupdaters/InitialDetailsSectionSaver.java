package org.innovateuk.ifs.competitionsetup.service.sectionupdaters;

import org.apache.commons.collections4.map.LinkedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.innovateuk.ifs.application.service.CompetitionService;
import org.innovateuk.ifs.category.resource.InnovationAreaResource;
import org.innovateuk.ifs.category.service.CategoryRestService;
import org.innovateuk.ifs.commons.error.Error;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.competition.resource.CompetitionSetupSection;
import org.innovateuk.ifs.competition.resource.MilestoneResource;
import org.innovateuk.ifs.competition.resource.MilestoneType;
import org.innovateuk.ifs.competition.service.MilestoneRestService;
import org.innovateuk.ifs.competitionsetup.form.CompetitionSetupForm;
import org.innovateuk.ifs.competitionsetup.form.InitialDetailsForm;
import org.innovateuk.ifs.competitionsetup.form.MilestoneRowForm;
import org.innovateuk.ifs.competitionsetup.service.CompetitionSetupMilestoneService;
import org.innovateuk.ifs.competitionsetup.utils.CompetitionUtils;
import org.innovateuk.ifs.user.service.UserService;
import org.innovateuk.ifs.util.TimeZoneUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.innovateuk.ifs.commons.error.Error.fieldError;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceFailure;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.competitionsetup.controller.CompetitionSetupController.OPEN_SECTOR_ID;
import static org.innovateuk.ifs.user.resource.UserRoleType.COMP_ADMIN;
import static org.innovateuk.ifs.user.resource.UserRoleType.COMP_TECHNOLOGIST;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

/**
 * Competition setup section saver for the initial details section.
 */
@Service
public class InitialDetailsSectionSaver extends AbstractSectionSaver implements CompetitionSetupSectionSaver {

    public final static String OPENINGDATE_FIELDNAME = "openingDate";
	private static Log LOG = LogFactory.getLog(InitialDetailsSectionSaver.class);
	@Autowired
	private CompetitionService competitionService;

    @Autowired
    private MilestoneRestService milestoneRestService;

	@Autowired
	private CompetitionSetupMilestoneService competitionSetupMilestoneService;

	@Autowired
	private CategoryRestService categoryRestService;

	@Autowired
    private UserService userService;

	@Override
	public CompetitionSetupSection sectionToSave() {
		return CompetitionSetupSection.INITIAL_DETAILS;
	}

    @Override
	protected ServiceResult<Void> doSaveSection(CompetitionResource competition, CompetitionSetupForm competitionSetupForm) {
		InitialDetailsForm initialDetailsForm = (InitialDetailsForm) competitionSetupForm;
        if (!competition.isSetupAndAfterNotifications()) {
            List<Error> errors = saveAssignedUsers(competition, initialDetailsForm);

            if (errors.isEmpty() && !competition.getSetupComplete()) {
                errors = doSetupComplete(competition, initialDetailsForm);
            }

            if (!errors.isEmpty()) {
                return serviceFailure(errors);
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

    private List<Error> doSetupComplete(final CompetitionResource competition, final InitialDetailsForm initialDetailsForm) {
        List<Error> errors = new ArrayList<>();

        competition.setName(initialDetailsForm.getTitle());

        if (shouldTryToSaveStartDate(initialDetailsForm)) {
            ZonedDateTime startDate = initialDetailsForm.getOpeningDate();
            competition.setStartDate(startDate);

            errors.addAll(saveOpeningDateAsMilestone(startDate, competition.getId(), initialDetailsForm.isMarkAsCompleteAction()));
        }

        competition.setCompetitionType(initialDetailsForm.getCompetitionTypeId());
        competition.setInnovationSector(initialDetailsForm.getInnovationSectorCategoryId());

        List<Long> innovationAreas = initialDetailsForm.getInnovationAreaCategoryIds();

        if (competition.getInnovationSector() != null) {
            List<InnovationAreaResource> allInnovationAreas = categoryRestService.getInnovationAreas().getSuccessObjectOrThrowException();
            List<Long> allInnovationAreasIds = getAllInnovationAreaIds(allInnovationAreas).collect(Collectors.toList());
            List<Long> newInnovationAreaIds = initialDetailsForm.getInnovationAreaCategoryIds();

            if(OPEN_SECTOR_ID.equals(competition.getInnovationSector()) && newInnovationAreaIds.contains(CompetitionUtils.ALL_INNOVATION_AREAS)) {
                innovationAreas = allInnovationAreasIds;
            }
            errors.addAll(checkInnovationAreaData(competition, allInnovationAreas, allInnovationAreasIds,
                    newInnovationAreaIds, initialDetailsForm.isMarkAsCompleteAction()));
        }

        competition.setInnovationAreas(innovationAreas.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toSet()));

        return errors;
    }

    private List<Error> checkInnovationAreaData(CompetitionResource competition,
                                                List<InnovationAreaResource> allInnovationAreas,
                                                List<Long> allInnovationAreasIds,
                                                List<Long> innovationAreaIds, boolean isMarkAsComplete) {
        if(OPEN_SECTOR_ID.equals(competition.getInnovationSector())) {
            if(!innovationAreaIds.contains(CompetitionUtils.ALL_INNOVATION_AREAS)) {
                boolean foundNotMatchingId = innovationAreaIds.stream().anyMatch(areaId -> !allInnovationAreasIds.contains(areaId));

                if (foundNotMatchingId && isMarkAsComplete) {
                    return buildInnovationError(innovationAreaIds, allInnovationAreas);
                }
            }
        } else {
            List<InnovationAreaResource> children = categoryRestService.getInnovationAreasBySector(competition.getInnovationSector()).getSuccessObjectOrThrowException();
            List<Long> childrenIds = children.stream().map(InnovationAreaResource::getId).collect(Collectors.toList());

            boolean foundNotMatchingId = innovationAreaIds.stream().anyMatch(areaId -> !childrenIds.contains(areaId));

            if (foundNotMatchingId && isMarkAsComplete) {
                return buildInnovationError(innovationAreaIds, children);
            }
        }

        return emptyList();
    }

    private List<Error> buildInnovationError(List<Long> innovationAreaCategoryIds, List<InnovationAreaResource> allInnovationAreas) {
        return asList(fieldError("innovationAreaCategoryIds",
                innovationAreaCategoryIds,
                "competition.setup.innovation.area.must.be.selected",
                singletonList(allInnovationAreas.stream().map(child -> child.getName()).collect(Collectors.joining(", ")))));
    }

    private Stream<Long> getAllInnovationAreaIds(List<InnovationAreaResource> allInnovationAreas) {
        return allInnovationAreas.stream().map(InnovationAreaResource::getId);
    }

    private List<Error> saveAssignedUsers(final CompetitionResource competition, InitialDetailsForm initialDetailsForm) {
        if (userService.existsAndHasRole(initialDetailsForm.getExecutiveUserId(), COMP_ADMIN)) {
            competition.setExecutive(initialDetailsForm.getExecutiveUserId());
        } else if (initialDetailsForm.getExecutiveUserId() != null) {
            return asList(fieldError("executiveUserId",
                    initialDetailsForm.getExecutiveUserId(),
                    "competition.setup.invalid.comp.exec"));
        }

        if (userService.existsAndHasRole(initialDetailsForm.getLeadTechnologistUserId(), COMP_TECHNOLOGIST)) {
            competition.setLeadTechnologist(initialDetailsForm.getLeadTechnologistUserId());
        } else if (initialDetailsForm.getLeadTechnologistUserId() != null) {
            return asList(fieldError("leadTechnologistUserId",
                    initialDetailsForm.getLeadTechnologistUserId(),
                    "competition.setup.invalid.comp.technologist"));
        }

        return emptyList();
    }

    private boolean shouldTryToSaveStartDate(InitialDetailsForm initialDetailsForm) {
       return initialDetailsForm.isMarkAsCompleteAction() ||
               (initialDetailsForm.getOpeningDateYear() != null &&
               initialDetailsForm.getOpeningDateMonth() != null &&
               initialDetailsForm.getOpeningDateDay() != null);
   }

	private List<Error> validateOpeningDate(ZonedDateTime openingDate) {
	    if(openingDate.getYear() > 9999) {
            return asList(fieldError(OPENINGDATE_FIELDNAME, openingDate.toString(), "validation.initialdetailsform.openingdateyear.range"));
        }

        if (openingDate.isBefore(ZonedDateTime.now())) {
            return asList(fieldError(OPENINGDATE_FIELDNAME, openingDate.toString(), "competition.setup.opening.date.not.in.future"));
        }

        return Collections.emptyList();
    }

	private List<Error> saveOpeningDateAsMilestone(ZonedDateTime openingDate, Long competitionId, boolean isMarkAsCompleteAction) {
		if (isMarkAsCompleteAction) {
			List<Error> errors = validateOpeningDate(openingDate);
			if (!errors.isEmpty()) {
				return errors;
			}
		}

	    MilestoneRowForm milestoneEntry = new MilestoneRowForm(MilestoneType.OPEN_DATE, openingDate);


        List<MilestoneResource> milestones = milestoneRestService.getAllMilestonesByCompetitionId(competitionId).getSuccessObjectOrThrowException();
        if(milestones.isEmpty()) {
            milestones = competitionSetupMilestoneService.createMilestonesForCompetition(competitionId).getSuccessObjectOrThrowException();
        }
        milestones.sort(Comparator.comparing(MilestoneResource::getType));

		LinkedMap<String, MilestoneRowForm> milestoneEntryMap = new LinkedMap<>();
		milestoneEntryMap.put(MilestoneType.OPEN_DATE.name(), milestoneEntry);

        ServiceResult<Void> result = competitionSetupMilestoneService.updateMilestonesForCompetition(milestones, milestoneEntryMap, competitionId);
        if(result.isFailure()) {
            return result.getErrors();
        }
		return emptyList();
	}

    @Override
    protected ServiceResult<Void> handleIrregularAutosaveCase(CompetitionResource competitionResource, String fieldName, String value, Optional<Long> questionId) {
        if("openingDate".equals(fieldName)) {
            try {
                String[] dateParts = value.split("-");
                ZonedDateTime startDate = TimeZoneUtil.fromUkTimeZone(
                        Integer.parseInt(dateParts[2]),
                        Integer.parseInt(dateParts[1]),
                        Integer.parseInt(dateParts[0]));
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
        } else if( fieldName.equals("autosaveInnovationAreaIds")) {
            processInnovationAreas(value, competitionResource);
            return competitionService.update(competitionResource);
        }
        return super.handleIrregularAutosaveCase(competitionResource, fieldName, value, questionId);
    }

    private void processInnovationAreas(String inputValue, CompetitionResource competitionResource) {
        List<String> valueList = Arrays.asList(inputValue.split("\\s*,\\s*"));
        Set<Long> valueSet = valueList.stream().map(Long::parseLong).collect(Collectors.toSet());

        if(valueSet.contains(CompetitionUtils.ALL_INNOVATION_AREAS)) {
            List<InnovationAreaResource> allInnovationAreas = categoryRestService.getInnovationAreas().getSuccessObjectOrThrowException();
            valueSet = getAllInnovationAreaIds(allInnovationAreas).collect(Collectors.toSet());
        }

        competitionResource.setInnovationAreas(valueSet);
    }

	@Override
	public boolean supportsForm(Class<? extends CompetitionSetupForm> clazz) {
		return InitialDetailsForm.class.equals(clazz);
	}
}
