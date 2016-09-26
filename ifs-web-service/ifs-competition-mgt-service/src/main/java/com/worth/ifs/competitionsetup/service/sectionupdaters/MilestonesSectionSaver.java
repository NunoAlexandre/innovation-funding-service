package com.worth.ifs.competitionsetup.service.sectionupdaters;

import com.worth.ifs.application.service.MilestoneService;
import com.worth.ifs.commons.error.Error;
import com.worth.ifs.competition.resource.CompetitionResource;
import com.worth.ifs.competition.resource.CompetitionSetupSection;
import com.worth.ifs.competition.resource.MilestoneResource;
import com.worth.ifs.competition.resource.MilestoneType;
import com.worth.ifs.competitionsetup.form.CompetitionSetupForm;
import com.worth.ifs.competitionsetup.form.MilestonesForm;
import com.worth.ifs.competitionsetup.model.MilestoneEntry;
import com.worth.ifs.competitionsetup.service.CompetitionSetupMilestoneService;
import org.apache.commons.collections4.map.LinkedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.worth.ifs.commons.error.Error.fieldError;
import static org.codehaus.groovy.runtime.InvokerHelper.asList;

/**
 * Competition setup section saver for the milestones section.
 */
@Service
public class MilestonesSectionSaver extends AbstractSectionSaver implements CompetitionSetupSectionSaver {

    private static Log LOG = LogFactory.getLog(MilestonesSectionSaver.class);

    @Autowired
    private MilestoneService milestoneService;

    @Autowired
    private CompetitionSetupMilestoneService competitionSetupMilestoneService;

	@Override
	public CompetitionSetupSection sectionToSave() {
		return CompetitionSetupSection.MILESTONES;
	}

	@Override
	public List<Error> saveSection(CompetitionResource competition, CompetitionSetupForm competitionSetupForm) {

        MilestonesForm milestonesForm = (MilestonesForm) competitionSetupForm;
        LinkedMap<String, MilestoneEntry> milestoneEntries = milestonesForm.getMilestoneEntries();

        List<Error> errors = returnErrorsFoundOnSave(milestoneEntries, competition.getId());
        if(!errors.isEmpty()) {
            competitionSetupMilestoneService.sortMilestones(milestonesForm);
            return errors;
        }

        return Collections.emptyList();
    }

    @Override
    public List<Error> autoSaveSectionField(CompetitionResource competitionResource, String fieldName, String value, Optional<Long> ObjectId) {
        return updateMilestoneWithValueByFieldname2(competitionResource, fieldName, value);
    }

    private List<Error> returnErrorsFoundOnSave(LinkedMap<String, MilestoneEntry> milestoneEntries, Long competitionId){
        List<MilestoneResource> milestones = milestoneService.getAllMilestonesByCompetitionId(competitionId);

        List<Error> errors = competitionSetupMilestoneService.validateMilestoneDates(milestoneEntries);

        if(!errors.isEmpty()) {
            return errors;
        }

        return competitionSetupMilestoneService.updateMilestonesForCompetition(milestones, milestoneEntries, competitionId);
    }

    @Override
    public boolean supportsForm(Class<? extends CompetitionSetupForm> clazz) { return MilestonesForm.class.equals(clazz); }

    @Override
    protected List<Error> updateCompetitionResourceWithAutoSave(List<Error> errors, CompetitionResource competitionResource, String fieldName, String value) {
      return  Collections.emptyList();
    }

    private List<Error> updateMilestoneWithValueByFieldname(CompetitionResource competitionResource, String fieldName, String value) {
        List<Error> errors = new ArrayList<>();
        try{
            MilestoneResource milestone = milestoneService.getMilestoneByTypeAndCompetitionId(
                    MilestoneType.valueOf(getMilestoneTypeFromFieldName(fieldName)), competitionResource.getId());

            errors.addAll(validateMilestoneDate(milestone, getDateFromFieldValue(value)));

            if(!errors.isEmpty()) {
                return errors;
            }
            milestoneService.updateMilestone(milestone);
        }catch(Exception ex){
            LOG.error(ex.getMessage());
            return makeErrorList();
        }
        return  errors;
    }

    private List<Error> updateMilestoneWithValueByFieldname2(CompetitionResource competitionResource, String fieldName, String value) {
        List<Error> errors = new ArrayList<>();
        try{
            MilestoneResource milestone = milestoneService.getMilestoneByTypeAndCompetitionId(
                    MilestoneType.valueOf(getMilestoneTypeFromFieldName(fieldName)), competitionResource.getId());

           //past value
            Error error = validate2(value, milestone);

            if(!errors.isEmpty()) {
                return errors;
            }
            milestoneService.updateMilestone(milestone);
        }catch(Exception ex){
            LOG.error(ex.getMessage());
            return makeErrorList();
        }
        return  errors;
    }

    private LocalDateTime getDateFromFieldValue(String value) {
        try {
            String[] dateParts = value.split("-");
            return LocalDateTime.of(
                    Integer.parseInt(dateParts[2]),
                    Integer.parseInt(dateParts[1]),
                    Integer.parseInt(dateParts[0]),
                    0, 0, 0);
        } catch (Exception e) {
            LOG.error("Invalid milestone on autosave " + e.getMessage());
            return null;
        }
    }

    private Error validate2(String value, MilestoneResource milestone) {
        Error error = null;
        String[] dateParts = value.split("-");
        Integer day = Integer.parseInt(dateParts[0]);
        Integer month = Integer.parseInt(dateParts[1]);
        Integer year = Integer.parseInt(dateParts[2]);

        if(day == null || month == null || year == null || !competitionSetupMilestoneService.isMilestoneDateValid(day, month, year)) {

                error = new Error("error.milestone.invalid2", HttpStatus.BAD_REQUEST);

        }
        else { //catch validate dates
            milestone.setDate(LocalDateTime.of(year, month, day, 0,0));
        }

        return error;
    }

        private List<Error> validateMilestoneDate(MilestoneResource milestone, LocalDateTime milestoneDate) {
        if (milestoneDate.isBefore(LocalDateTime.now())){
            return asList(fieldError("", milestoneDate.toString(), "competition.setup.milestone.date.not.in.future"));
        }
        milestone.setDate(milestoneDate);

        return Collections.emptyList();
    }

    private List<Error> makeErrorList()  {
        return asList(fieldError("", null, "competition.setup.milestone.date.not.able.to.save"));
    }

    private String getMilestoneTypeFromFieldName(String fieldName) {
        Pattern typePattern = Pattern.compile("\\[(.*?)\\]");
        Matcher typeMatcher = typePattern.matcher(fieldName);
        typeMatcher.find();
        return typeMatcher.group(1);
    }
}
