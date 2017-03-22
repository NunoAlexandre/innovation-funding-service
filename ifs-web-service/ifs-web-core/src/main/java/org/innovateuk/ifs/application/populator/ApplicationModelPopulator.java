package org.innovateuk.ifs.application.populator;

import org.innovateuk.ifs.application.UserApplicationRole;
import org.innovateuk.ifs.application.finance.view.ApplicationFinanceOverviewModelManager;
import org.innovateuk.ifs.application.finance.view.FinanceHandler;
import org.innovateuk.ifs.application.form.ApplicationForm;
import org.innovateuk.ifs.application.form.Form;
import org.innovateuk.ifs.application.resource.ApplicationResource;
import org.innovateuk.ifs.application.resource.QuestionResource;
import org.innovateuk.ifs.application.resource.QuestionType;
import org.innovateuk.ifs.application.resource.SectionResource;
import org.innovateuk.ifs.application.service.OrganisationService;
import org.innovateuk.ifs.application.service.QuestionService;
import org.innovateuk.ifs.application.service.SectionService;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.user.resource.OrganisationResource;
import org.innovateuk.ifs.user.resource.ProcessRoleResource;
import org.innovateuk.ifs.user.resource.UserResource;
import org.innovateuk.ifs.user.service.ProcessRoleService;
import org.innovateuk.ifs.user.service.UserRestService;
import org.innovateuk.ifs.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.innovateuk.ifs.util.CollectionFunctions.simpleFindFirst;

@Component
public class ApplicationModelPopulator {
    public static final String MODEL_ATTRIBUTE_FORM = "form";

    @Autowired
    protected UserService userService;

    @Autowired
    protected QuestionService questionService;

    @Autowired
    protected ProcessRoleService processRoleService;

    @Autowired
    protected SectionService sectionService;

    @Autowired
    protected ApplicationFinanceOverviewModelManager applicationFinanceOverviewModelManager;

    @Autowired
    protected OrganisationService organisationService;

    @Autowired
    protected FinanceHandler financeHandler;

    @Autowired
    private ApplicationSectionAndQuestionModelPopulator applicationSectionAndQuestionModelPopulator;

    @Autowired
    protected UserRestService userRestService;

    public void addApplicationAndSections(ApplicationResource application,
                                          CompetitionResource competition,
                                          UserResource user,
                                          Optional<SectionResource> section,
                                          Optional<Long> currentQuestionId,
                                          Model model,
                                          ApplicationForm form,
                                          List<ProcessRoleResource> userApplicationRoles) {

        addApplicationDetails(application, competition, user, section, currentQuestionId, model, form, userApplicationRoles);

        model.addAttribute("completedQuestionsPercentage", application.getCompletion());
        applicationSectionAndQuestionModelPopulator.addSectionDetails(model, section);
    }

    public ApplicationResource addApplicationDetails(ApplicationResource application,
                                                     CompetitionResource competition,
                                                     UserResource user,
                                                     Optional<SectionResource> section,
                                                     Optional<Long> currentQuestionId,
                                                     Model model,
                                                     ApplicationForm form,
                                                     List<ProcessRoleResource> userApplicationRoles) {

        model.addAttribute("currentApplication", application);
        model.addAttribute("currentCompetition", competition);

        Optional<OrganisationResource> userOrganisation = getUserOrganisation(user.getId(), userApplicationRoles);
        model.addAttribute("userOrganisation", userOrganisation.orElse(null));

        if(form == null){
            form = new ApplicationForm();
        }
        form.setApplication(application);

        Map<Long, Set<Long>> completedSectionsByOrganisation = sectionService.getCompletedSectionsByOrganisation(application.getId());

        applicationSectionAndQuestionModelPopulator.addQuestionsDetails(model, application, form);
        addUserDetails(model, user, userApplicationRoles);
        addApplicationFormDetailInputs(application, form);
        applicationSectionAndQuestionModelPopulator.addMappedSectionsDetails(model, competition, section, userOrganisation, completedSectionsByOrganisation);

        applicationSectionAndQuestionModelPopulator.addAssignableDetails(model, application, userOrganisation.orElse(null), user, section, currentQuestionId);
        applicationSectionAndQuestionModelPopulator.addCompletedDetails(model, application, userOrganisation, completedSectionsByOrganisation);

        model.addAttribute(MODEL_ATTRIBUTE_FORM, form);
        return application;
    }


    public void addApplicationFormDetailInputs(ApplicationResource application, Form form) {
        Map<String, String> formInputs = form.getFormInput();
        formInputs.put("application_details-title", application.getName());
        formInputs.put("application_details-duration", String.valueOf(application.getDurationInMonths()));
        if(application.getStartDate() == null){
            formInputs.put("application_details-startdate_day", "");
            formInputs.put("application_details-startdate_month", "");
            formInputs.put("application_details-startdate_year", "");
        }else{
            formInputs.put("application_details-startdate_day", String.valueOf(application.getStartDate().getDayOfMonth()));
            formInputs.put("application_details-startdate_month", String.valueOf(application.getStartDate().getMonthValue()));
            formInputs.put("application_details-startdate_year", String.valueOf(application.getStartDate().getYear()));
        }
        form.setFormInput(formInputs);
    }


    public void addUserDetails(Model model, UserResource user, List<ProcessRoleResource> userApplicationRoles) {

        ProcessRoleResource leadApplicantProcessRole =
                simpleFindFirst(userApplicationRoles, role -> role.getRoleName().equals(UserApplicationRole.LEAD_APPLICANT.getRoleName())).get();

        boolean userIsLeadApplicant = leadApplicantProcessRole.getUser().equals(user.getId());

        UserResource leadApplicant = userIsLeadApplicant ? user : userService.findById(leadApplicantProcessRole.getUser());

        model.addAttribute("userIsLeadApplicant", userIsLeadApplicant);
        model.addAttribute("leadApplicant", leadApplicant);
    }

    public boolean userIsLeadApplicant(ApplicationResource application, Long userId) {
        return userService.isLeadApplicant(userId, application);
    }

    public void addOrganisationAndUserFinanceDetails(Long competitionId,
                                                     Long applicationId,
                                                     UserResource user,
                                                     Model model,
                                                     ApplicationForm form,
                                                     Long organisationId) {
        model.addAttribute("currentUser", user);

        SectionResource financeSection = sectionService.getFinanceSection(competitionId);
        boolean hasFinanceSection = financeSection != null;

        if(hasFinanceSection) {
            Optional<Long> optionalOrganisationId = Optional.ofNullable(organisationId);
            applicationFinanceOverviewModelManager.addFinanceDetails(model, competitionId, applicationId, optionalOrganisationId);

            List<QuestionResource> costsQuestions = questionService.getQuestionsBySectionIdAndType(financeSection.getId(), QuestionType.COST);
            // NOTE: This code is terrible.  It does nothing if none of below two conditions don't match.  This is not my code RB.
            if(!form.isAdminMode() || optionalOrganisationId.isPresent()) {
                Long organisationType = organisationService.getOrganisationType(user.getId(), applicationId);
                financeHandler.getFinanceModelManager(organisationType).addOrganisationFinanceDetails(model, applicationId, costsQuestions, user.getId(), form, organisationId);
            }
        }
    }

    public void addResearchCategoryId(ApplicationResource application, Model model) {

        model.addAttribute("rsCategoryId", application.getResearchCategories().stream().findFirst().map(cat -> cat.getId()).orElse(null));
    }

    public void addResearchCategoryName(ApplicationResource application, Model model) {

        model.addAttribute("researchCategoryName", application.getResearchCategories().stream().findFirst().map(cat -> cat.getName()).orElse(""));
    }

    public Optional<OrganisationResource> getUserOrganisation(Long userId, List<ProcessRoleResource> userApplicationRoles) {

        return userApplicationRoles.stream()
                .filter(uar -> uar.getUser().equals(userId))
                .map(uar -> organisationService.getOrganisationById(uar.getOrganisationId()))
                .findFirst();
    }

    public void addApplicationInputs(ApplicationResource application, Model model) {
        model.addAttribute("application_title", application.getName());
        model.addAttribute("application_duration", String.valueOf(application.getDurationInMonths()));
        if(application.getStartDate() == null){
            model.addAttribute("application_startdate_day", "");
            model.addAttribute("application_startdate_month", "");
            model.addAttribute("application_startdate_year", "");
        }
        else{
            model.addAttribute("application_startdate_day", String.valueOf(application.getStartDate().getDayOfMonth()));
            model.addAttribute("application_startdate_month", String.valueOf(application.getStartDate().getMonthValue()));
            model.addAttribute("application_startdate_year", String.valueOf(application.getStartDate().getYear()));
        }
    }

}
