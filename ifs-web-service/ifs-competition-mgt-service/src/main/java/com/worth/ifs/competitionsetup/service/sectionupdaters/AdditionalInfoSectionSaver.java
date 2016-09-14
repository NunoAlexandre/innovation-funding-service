package com.worth.ifs.competitionsetup.service.sectionupdaters;

import com.worth.ifs.application.service.CompetitionService;
import com.worth.ifs.commons.error.Error;
import com.worth.ifs.competition.resource.CompetitionFunderResource;
import com.worth.ifs.competition.resource.CompetitionResource;
import com.worth.ifs.competition.resource.CompetitionSetupSection;
import com.worth.ifs.competitionsetup.form.AdditionalInfoForm;
import com.worth.ifs.competitionsetup.form.CompetitionSetupForm;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.el.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.codehaus.groovy.runtime.InvokerHelper.asList;

/**
 * Competition setup section saver for the additional info section.
 */
@Service
public class AdditionalInfoSectionSaver extends AbstractSectionSaver implements CompetitionSetupSectionSaver {

    private static final Log LOG = LogFactory.getLog(AdditionalInfoSectionSaver.class);

    @Autowired
    private CompetitionService competitionService;

	@Override
	public CompetitionSetupSection sectionToSave() {
		return CompetitionSetupSection.ADDITIONAL_INFO;
	}

	@Override
	public List<Error> saveSection(CompetitionResource competition, CompetitionSetupForm competitionSetupForm) {
		AdditionalInfoForm additionalInfoForm = (AdditionalInfoForm) competitionSetupForm;
		competition.setActivityCode(additionalInfoForm.getActivityCode());
		competition.setInnovateBudget(additionalInfoForm.getInnovateBudget());
		competition.setBudgetCode(additionalInfoForm.getBudgetCode());
		competition.setPafCode(additionalInfoForm.getPafNumber());
		additionalInfoForm.setCompetitionCode(competition.getCode());
		competition.setFunders(new ArrayList());
		additionalInfoForm.getFunders().forEach(funder -> {
            CompetitionFunderResource competitionFunderResource = new CompetitionFunderResource();
            competitionFunderResource.setFunder(funder.getFunder());
            competitionFunderResource.setFunderBudget(funder.getFunderBudget());
            competitionFunderResource.setCoFunder(funder.getCoFunder());
			competition.getFunders().add(competitionFunderResource);
		});

		competitionService.update(competition);

		return Collections.emptyList();
	}

	@Override
	public List<Error> autoSaveSectionField(CompetitionResource competitionResource, String fieldName, String value, Optional<Long> objectId) {
        return performAutoSaveField(competitionResource, fieldName, value);
	}

	@Override
	protected List<Error> updateCompetitionResourceWithAutoSave(List<Error> errors, CompetitionResource competitionResource, String fieldName, String value) throws ParseException {
		switch (fieldName) {
			case "pafNumber":
				competitionResource.setPafCode(value);
				break;
			case "budgetCode":
				competitionResource.setBudgetCode(value);
				break;
			case "activityCode":
				competitionResource.setActivityCode(value);
				break;
			case "competitionCode":
				competitionResource.setCode(value);
				break;
			case "removeFunder":
				int index = Integer.valueOf(value);
				if (index > 0 && competitionResource.getFunders().size() > index) {
					competitionResource.getFunders().remove(index);
				} else {
					return asList(new Error("Funder could not be removed", HttpStatus.BAD_REQUEST));
				}
				break;
			default:
				errors = tryUpdateFunders(competitionResource, fieldName, value);
		}

		return errors;
	}

	private Integer getFunderIndex(String fieldName) throws ParseException {
	    return Integer.parseInt(fieldName.substring(fieldName.indexOf("[") + 1, fieldName.indexOf("]")));
    }

    private List<Error> tryUpdateFunders(CompetitionResource competitionResource, String fieldName, String value) {
        Integer index;
        CompetitionFunderResource funder;

        try {
            index = getFunderIndex(fieldName);
            if(index >= competitionResource.getFunders().size()) {
                addNotSavedFunders(competitionResource, index);
            }

            funder = competitionResource.getFunders().get(index);

            if(fieldName.endsWith("funder")) {
                funder.setFunder(value);
            } else if(fieldName.endsWith("funderBudget")) {
                funder.setFunderBudget(new BigDecimal(value));
			} else {
               return asList(new Error("Field not found", HttpStatus.BAD_REQUEST));
            }
        } catch (ParseException e) {
            return asList(new Error("Field not found", HttpStatus.BAD_REQUEST));
        }

        competitionResource.getFunders().set(index, funder);

        return Collections.emptyList();
    }

    private void addNotSavedFunders(CompetitionResource competitionResource, Integer index) {
        Integer currentIndexNotUsed = competitionResource.getFunders().size();

        for(Integer i = currentIndexNotUsed; i <= index; i++) {
            CompetitionFunderResource competitionFunderResource = new CompetitionFunderResource();
            competitionFunderResource.setCoFunder(true);
            competitionResource.getFunders().add(i, competitionFunderResource);
        }
    }


    @Override
	public boolean supportsForm(Class<? extends CompetitionSetupForm> clazz) {
		return AdditionalInfoForm.class.equals(clazz);
	}

}
