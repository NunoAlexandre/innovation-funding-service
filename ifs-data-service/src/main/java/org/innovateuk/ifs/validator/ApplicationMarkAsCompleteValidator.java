package org.innovateuk.ifs.validator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.innovateuk.ifs.application.domain.Application;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.time.LocalDate;

import static org.innovateuk.ifs.commons.rest.ValidationMessages.rejectValue;

/**
 * Validates the inputs in the application details, if valid on the markAsComplete action
 *
 */
public class ApplicationMarkAsCompleteValidator implements Validator {
    private static final Log LOG = LogFactory.getLog(ApplicationMarkAsCompleteValidator.class);

    @Override
    public boolean supports(Class<?> clazz) {
        //Check subclasses for in case we receive hibernate proxy class.
        return Application.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        LocalDate currentDate = LocalDate.now();

        LOG.debug("do ApplicationMarkAsComplete Validation");

        Application application = (Application) target;

        if (StringUtils.isEmpty(application.getName())) {
            LOG.debug("MarkAsComplete application details validation message for name: " + application.getName());
            rejectValue(errors, "name", "validation.project.name.must.not.be.empty");
        }

        if (StringUtils.isEmpty(application.getStartDate()) || (application.getStartDate().isBefore(currentDate))) {
            LOG.debug("MarkAsComplete application details validation message for start date: " + application.getStartDate());
            rejectValue(errors, "startDate", "validation.project.start.date.not.in.future");
        }

        if (StringUtils.isEmpty(application.getDurationInMonths()) || application.getDurationInMonths() < 1 || application.getDurationInMonths() > 36) {
            LOG.debug("MarkAsComplete application details validation message for duration in months: " + application.getDurationInMonths());
            rejectValue(errors, "durationInMonths", "validation.project.duration.range.invalid");
        }

        if (!applicationInnovationAreaIsInCorrectState(application)) {
            LOG.debug("MarkAsComplete application details validation message for innovation area: " + application.getInnovationArea());
            rejectValue(errors, "innovationArea", "validation.application.innovationarea.category.required");
        }

        if (application.getResearchCategory() == null) {
            LOG.debug("MarkAsComplete application details validation message for research category: " + application.getResearchCategory());
            rejectValue(errors, "researchCategory", "validation.application.research.category.required");
        }

        if (application.getResubmission() != null) {
            if (application.getResubmission()) {
                if (StringUtils.isEmpty(application.getPreviousApplicationNumber())) {
                    LOG.debug("MarkAsComplete application details validation message for previous application number: " + application.getPreviousApplicationNumber());
                    rejectValue(errors, "previousApplicationNumber", "validation.application.previous.application.number.required");
                }
                if (StringUtils.isEmpty(application.getPreviousApplicationTitle())) {
                    LOG.debug("MarkAsComplete application details validation message for previous application title: " + application.getPreviousApplicationTitle());
                    rejectValue(errors, "previousApplicationTitle", "validation.application.previous.application.title.required");
                }
            }
        } else {
            LOG.debug("MarkAsComplete application details validation message for resubmission indicator: " + application.getResubmission());
            rejectValue(errors, "resubmission", "validation.application.must.indicate.resubmission.or.not");
        }
    }

    private boolean applicationInnovationAreaIsInCorrectState(Application application) {
        return application.getNoInnovationAreaApplicable() == true || (application.getNoInnovationAreaApplicable() == false && application.getInnovationArea() !=null);
    }
}
