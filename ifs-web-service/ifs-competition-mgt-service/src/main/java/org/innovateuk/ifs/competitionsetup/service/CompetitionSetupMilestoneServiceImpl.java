package org.innovateuk.ifs.competitionsetup.service;

import org.apache.commons.collections4.map.LinkedMap;
import org.innovateuk.ifs.application.service.MilestoneService;
import org.innovateuk.ifs.commons.error.Error;
import org.innovateuk.ifs.competition.resource.MilestoneResource;
import org.innovateuk.ifs.competition.resource.MilestoneType;
import org.innovateuk.ifs.competitionsetup.form.MilestonesForm;
import org.innovateuk.ifs.competitionsetup.form.MilestoneRowForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class CompetitionSetupMilestoneServiceImpl implements CompetitionSetupMilestoneService {

    @Autowired
    private MilestoneService milestoneService;

    @Override
    public List<MilestoneResource> createMilestonesForCompetition(Long competitionId) {
        List<MilestoneResource> newMilestones = new ArrayList<>();
        Stream.of(MilestoneType.presetValues()).forEach(type -> {
            MilestoneResource newMilestone = milestoneService.create(type, competitionId);
            newMilestones.add(newMilestone);
        });
        return newMilestones;
    }

    @Override
    public List<Error> updateMilestonesForCompetition(List<MilestoneResource> milestones, Map<String, MilestoneRowForm> milestoneEntries, Long competitionId) {
        List<MilestoneResource> updatedMilestones = new ArrayList();

        milestones.forEach(milestoneResource -> {
            MilestoneRowForm milestoneWithUpdate = milestoneEntries.getOrDefault(milestoneResource.getType().name(), null);

            if(milestoneWithUpdate != null) {
                LocalDateTime temp = milestoneWithUpdate.getMilestoneAsDateTime();
                if (temp != null) {
                    milestoneResource.setDate(temp);
                    updatedMilestones.add(milestoneResource);
                }
            }
        });

        return milestoneService.updateMilestones(updatedMilestones);
    }

    @Override
    public List<Error> validateMilestoneDates(Map<String, MilestoneRowForm> milestonesFormEntries) {
        List<Error> errors =  new ArrayList<>();
        milestonesFormEntries.values().forEach(milestone -> {

            Integer day = milestone.getDay();
            Integer month = milestone.getMonth();
            Integer year = milestone.getYear();

            if(day == null || month == null || year == null || !isMilestoneDateValid(day, month, year)) {
                if(errors.isEmpty()) {
                    errors.add(new Error("error.milestone.invalid", HttpStatus.BAD_REQUEST));
                }
            }
        });
        return errors;
    }

    @Override
    public Boolean isMilestoneDateValid(Integer day, Integer month, Integer year) {
        try{
            LocalDateTime.of(year, month, day, 0,0);
            if (year > 9999) {
                    return false;
            }
            return true;
        }
        catch(DateTimeException dte){
            return false;
        }
    }

    public void sortMilestones(MilestonesForm milestoneForm) {
        LinkedMap<String, MilestoneRowForm> milestoneEntries = milestoneForm.getMilestoneEntries();
        milestoneForm.setMilestoneEntries(sortMilestoneEntries(milestoneEntries.values()));
    }

    private LinkedMap<String, MilestoneRowForm> sortMilestoneEntries(Collection<MilestoneRowForm> milestones) {
        List<MilestoneRowForm> sortedMilestones = milestones.stream()
                .sorted((o1, o2) -> o1.getMilestoneType().ordinal() - o2.getMilestoneType().ordinal())
                .collect(Collectors.toList());

        LinkedMap<String, MilestoneRowForm> milestoneFormEntries = new LinkedMap<>();
        sortedMilestones.stream().forEachOrdered(milestone ->
                milestoneFormEntries.put(milestone.getMilestoneType().name(), milestone)
        );

        return milestoneFormEntries;
    }
}
