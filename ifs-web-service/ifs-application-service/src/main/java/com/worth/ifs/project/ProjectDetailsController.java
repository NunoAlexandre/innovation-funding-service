package com.worth.ifs.project;

import com.worth.ifs.address.resource.AddressResource;
import com.worth.ifs.address.resource.AddressType;
import com.worth.ifs.address.service.AddressRestService;
import com.worth.ifs.application.form.AddressForm;
import com.worth.ifs.application.resource.ApplicationResource;
import com.worth.ifs.application.service.ApplicationService;
import com.worth.ifs.application.service.CompetitionService;
import com.worth.ifs.application.service.OrganisationService;
import com.worth.ifs.application.service.ProjectService;
import com.worth.ifs.commons.rest.RestResult;
import com.worth.ifs.commons.security.UserAuthenticationService;
import com.worth.ifs.commons.service.ServiceResult;
import com.worth.ifs.competition.resource.CompetitionResource;
import com.worth.ifs.controller.BindingResultTarget;
import com.worth.ifs.model.OrganisationDetailsModelPopulator;
import com.worth.ifs.organisation.resource.OrganisationAddressResource;
import com.worth.ifs.project.resource.ProjectResource;
import com.worth.ifs.project.viewmodel.ProjectDetailsAddressViewModel;
import com.worth.ifs.project.viewmodel.ProjectDetailsAddressViewModelForm;
import com.worth.ifs.project.viewmodel.ProjectDetailsStartDateForm;
import com.worth.ifs.project.viewmodel.ProjectDetailsStartDateViewModel;
import com.worth.ifs.user.resource.OrganisationResource;
import com.worth.ifs.user.resource.ProcessRoleResource;
import com.worth.ifs.user.resource.UserResource;
import com.worth.ifs.user.service.UserService;
import com.worth.ifs.util.CookieUtil;
import com.worth.ifs.util.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static com.worth.ifs.address.resource.AddressType.OPERATING;
import static com.worth.ifs.address.resource.AddressType.REGISTERED;
import static com.worth.ifs.controller.RestFailuresToValidationErrorBindingUtils.bindAnyErrorsToField;

/**
 * This controller will handle all requests that are related to project details.
 */
@Controller
@RequestMapping("/project")
public class ProjectDetailsController {
    static final String FORM_ATTR_NAME = "form";
    private static final String ADD_TYPE_ATTR_NAME = "addressType";
    private static final String MANUAL_ADDRESS = "manual-address";
    private static final String SEARCH_ADDRESS = "search-address";
    private static final String SELECT_ADDRESS = "select-address";
    private static final String SELECTED_POSTCODE = "selectedPostcode";
    private static final String BINDING_RESULT_PROJECT_LOCATION_FORM = "org.springframework.validation.BindingResult.form";

	@Autowired
    private ProjectService projectService;

    @Autowired
    private UserService userService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private OrganisationService organisationService;

    @Autowired
    private CompetitionService competitionService;
    
    @Autowired
    private OrganisationDetailsModelPopulator organisationDetailsModelPopulator;
    
    @Autowired
    private UserAuthenticationService userAuthenticationService;

    @Autowired
    private AddressRestService addressRestService;

    private Validator validator;

    @Autowired
    public void setValidator(Validator validator) {
        this.validator = validator;
    }

    @RequestMapping(value = "/{projectId}/details", method = RequestMethod.GET)
    public String projectDetail(Model model, @PathVariable("projectId") final Long projectId, HttpServletRequest request) {
        ProjectResource projectResource = projectService.getById(projectId);
        ApplicationResource applicationResource = applicationService.getById(projectId);
        CompetitionResource competitionResource = competitionService.getById(applicationResource.getCompetition());
        UserResource user = userAuthenticationService.getAuthenticatedUser(request);
        
        organisationDetailsModelPopulator.populateModel(model, projectId);
        
        model.addAttribute("project", projectResource);
        model.addAttribute("currentUser", user);
        model.addAttribute("currentOrganisation", user.getOrganisations().get(0));
        model.addAttribute("app", applicationResource);
        model.addAttribute("competition", competitionResource);
        return "project/detail";
    }

    @RequestMapping(value = "/{projectId}/details/start-date", method = RequestMethod.GET)
    public String viewStartDate(Model model, @PathVariable("projectId") final Long projectId,
                                @ModelAttribute(FORM_ATTR_NAME) ProjectDetailsStartDateForm form) {
    	
    	ProjectResource project = projectService.getById(projectId);
    	model.addAttribute("model", new ProjectDetailsStartDateViewModel(project));
        LocalDate defaultStartDate = project.getTargetStartDate().withDayOfMonth(1);
        form.setProjectStartDate(defaultStartDate);
        return "project/details-start-date";
    }

    @RequestMapping(value = "/{projectId}/details/start-date", method = RequestMethod.POST)
    public String updateStartDate(@PathVariable("projectId") final Long projectId,
                                  @ModelAttribute(FORM_ATTR_NAME) ProjectDetailsStartDateForm form,
                                  Model model,
                                  BindingResult bindingResult,
                                  HttpServletResponse response) {

        ServiceResult<Void> updateResult = projectService.updateProjectStartDate(projectId, form.getProjectStartDate());
        return handleErrorsOrRedirectToProjectOverview("projectStartDate", projectId, model, response, form, bindingResult, updateResult, () -> viewStartDate(model, projectId, form));
    }

    @RequestMapping(value = "/{projectId}/details/project-address", method = RequestMethod.GET)
    public String viewAddress(Model model,
                              HttpServletRequest request,
                              @ModelAttribute(FORM_ATTR_NAME) ProjectDetailsAddressViewModelForm form,
                              @PathVariable("projectId") final Long projectId) {
        ProjectResource project = projectService.getById(projectId);
        ProjectDetailsAddressViewModel projectDetailsAddressViewModel = loadDataIntoModel(project);
        form.setAddressType(project.getAddressType());
        model.addAttribute("model", projectDetailsAddressViewModel);
        return "project/details-address";
    }

    private String viewCurrentAddressForm(Model model, ProjectDetailsAddressViewModelForm form, ProjectResource project){
        ProjectDetailsAddressViewModel projectDetailsAddressViewModel = loadDataIntoModel(project);
        processAddressLookupFields(form);
        model.addAttribute("model", projectDetailsAddressViewModel);
        return "project/details-address";
    }

    @RequestMapping(value = "/{projectId}/details/project-address", method = RequestMethod.POST)
    public String updateAddress(Model model,
                                HttpServletRequest request,
                                HttpServletResponse response,
                                @ModelAttribute(FORM_ATTR_NAME) ProjectDetailsAddressViewModelForm form,
                                BindingResult bindingResult,
                                @PathVariable("projectId") final Long projectId) {
        ProjectResource projectResource = projectService.getById(projectId);
        OrganisationResource leadOrganisation = getLeadOrganisation(projectId);
        AddressResource newAddressResource = null;
        switch (form.getAddressType()) {
            case REGISTERED:
            case OPERATING:
            case PROJECT:
                Optional<OrganisationAddressResource> organisationAddressResource = getAddress(leadOrganisation, form.getAddressType());
                if (organisationAddressResource.isPresent()) {
                    newAddressResource = organisationAddressResource.get().getAddress();
                }
                break;
            case ADD_NEW:
                newAddressResource = form.getAddressForm().getSelectedPostcode();
                break;
            default:
                newAddressResource = null;
                break;
        }
        projectResource.setAddressType(form.getAddressType());
        projectResource.setAddress(newAddressResource);
        ServiceResult<Void> updateResult = projectService.updateAddress(projectId, form.getAddressType(), newAddressResource);
        return handleErrorsOrRedirectToProjectOverview("", projectId, model, response, form, bindingResult, updateResult, () -> viewAddress(model, request, form, projectId));
    }

    @RequestMapping(value = "/{projectId}/details/project-address", params = SEARCH_ADDRESS, method = RequestMethod.POST)
    public String searchAddress(Model model,
                                HttpServletRequest request,
                                HttpServletResponse response,
                                @PathVariable("projectId") Long projectId,
                                @ModelAttribute(FORM_ATTR_NAME) ProjectDetailsAddressViewModelForm form) {
        form.getAddressForm().setSelectedPostcodeIndex(null);
        form.getAddressForm().setTriedToSearch(true);
        form.setAddressType(AddressType.valueOf(form.getAddressType().name()));
        ProjectResource project = projectService.getById(projectId);
        return viewCurrentAddressForm(model, form, project);
    }

    @RequestMapping(value = "/{projectId}/details/project-address", params = SELECT_ADDRESS, method = RequestMethod.POST)
    public String selectAddress(Model model, HttpServletRequest request,
                                @PathVariable("projectId") Long projectId,
                                @ModelAttribute(FORM_ATTR_NAME) ProjectDetailsAddressViewModelForm form,
                                HttpServletResponse response) {
        form.getAddressForm().setSelectedPostcode(null);
        CookieUtil.saveToCookie(response, FORM_ATTR_NAME, JsonUtil.getSerializedObject(form));
        CookieUtil.saveToCookie(response, ADD_TYPE_ATTR_NAME, form.getAddressType().name());
        ProjectResource project = projectService.getById(projectId);
        return viewCurrentAddressForm(model, form, project);
    }

    @RequestMapping(value = "/{projectId}/details/project-address", params = MANUAL_ADDRESS, method = RequestMethod.POST)
    public String manualAddress(Model model, HttpServletRequest request, HttpServletResponse response,
                                @ModelAttribute(FORM_ATTR_NAME) ProjectDetailsAddressViewModelForm form,
                                @PathVariable("projectId") Long projectId) {
        AddressForm addressForm = form.getAddressForm();
        addressForm.setManualAddress(true);
        CookieUtil.saveToCookie(response, FORM_ATTR_NAME, JsonUtil.getSerializedObject(addressForm));
        CookieUtil.saveToCookie(response, ADD_TYPE_ATTR_NAME, form.getAddressType().name());
        ProjectResource project = projectService.getById(projectId);
        return viewCurrentAddressForm(model, form, project);
    }

    private String handleErrorsOrRedirectToProjectOverview(
            String fieldName, long projectId, Model model,HttpServletResponse response,
            BindingResultTarget form, BindingResult bindingResult,
            ServiceResult<?> result,
            Supplier<String> viewSupplier) {
        if (result.isFailure()) {
            bindAnyErrorsToField(result, fieldName, bindingResult, form);
            model.addAttribute(FORM_ATTR_NAME, form);
            return viewSupplier.get();
        }

        return redirectToProjectDetails(response, projectId);
    }

    private String redirectToProjectDetails(HttpServletResponse response, long projectId) {
        CookieUtil.removeCookie(response, FORM_ATTR_NAME);
        CookieUtil.removeCookie(response, ADD_TYPE_ATTR_NAME);
        return "redirect:/project/" + projectId + "/details";
    }

    private Optional<OrganisationAddressResource> getAddress(final OrganisationResource organisation, final AddressType addressType) {
        return organisation.getAddresses().stream().filter(a -> addressType.equals(a.getAddressType())).findFirst();
    }

    private OrganisationResource getLeadOrganisation(final Long projectId){
        ApplicationResource application = applicationService.getById(projectId);
        ProcessRoleResource leadApplicantProcessRole = userService.getLeadApplicantProcessRoleOrNull(application);
        return organisationService.getOrganisationById(leadApplicantProcessRole.getOrganisation());
    }

    /**
     * Get the list of postcode options, with the entered postcode. Add those results to the form.
     */
    private void addAddressOptions(ProjectDetailsAddressViewModelForm projectDetailsAddressViewModelForm) {
        if (StringUtils.hasText(projectDetailsAddressViewModelForm.getAddressForm().getPostcodeInput())) {
            AddressForm addressForm = projectDetailsAddressViewModelForm.getAddressForm();
            addressForm.setPostcodeOptions(searchPostcode(projectDetailsAddressViewModelForm.getAddressForm().getPostcodeInput()));
            addressForm.setPostcodeInput(projectDetailsAddressViewModelForm.getAddressForm().getPostcodeInput());
        }
    }

    /**
     * if user has selected a address from the dropdown, get it from the list, and set it as selected.
     */
    private void addSelectedAddress(ProjectDetailsAddressViewModelForm projectDetailsAddressViewModelForm) {
        AddressForm addressForm = projectDetailsAddressViewModelForm.getAddressForm();
        if (StringUtils.hasText(addressForm.getSelectedPostcodeIndex()) && addressForm.getSelectedPostcode() == null) {
            addressForm.setSelectedPostcode(addressForm.getPostcodeOptions().get(Integer.parseInt(addressForm.getSelectedPostcodeIndex())));
        }
    }

    private List<AddressResource> searchPostcode(String postcodeInput) {
        RestResult<List<AddressResource>> addressLookupRestResult = addressRestService.doLookup(postcodeInput);
        return addressLookupRestResult.handleSuccessOrFailure(
                failure -> new ArrayList<>(),
                addresses -> addresses);
    }

    private ProjectDetailsAddressViewModel loadDataIntoModel(final ProjectResource project){
        ProjectDetailsAddressViewModel projectDetailsAddressViewModel = new ProjectDetailsAddressViewModel(project);
        OrganisationResource leadOrganisation = getLeadOrganisation(project.getId());

        Optional<OrganisationAddressResource> registeredAddress = getAddress(leadOrganisation, REGISTERED);
        if(registeredAddress.isPresent()){
            projectDetailsAddressViewModel.setRegisteredAddress(registeredAddress.get().getAddress());
        }

        Optional<OrganisationAddressResource> operatingAddress = getAddress(leadOrganisation, OPERATING);
        if(operatingAddress.isPresent()){
            projectDetailsAddressViewModel.setOperatingAddress(operatingAddress.get().getAddress());
        }

        if(project.getAddress() != null){
            projectDetailsAddressViewModel.setProjectAddress(project.getAddress());
        }

        return projectDetailsAddressViewModel;
    }

    private void addressFormValidate(@Valid @ModelAttribute(FORM_ATTR_NAME) ProjectDetailsAddressViewModelForm form, BindingResult bindingResult, BindingResult addressBindingResult) {
        if (form.getAddressForm().getSelectedPostcode() != null) {
            validator.validate(form.getAddressForm().getSelectedPostcode(), addressBindingResult);
        } else if (!form.getAddressForm().isManualAddress()) {
            bindingResult.rejectValue("", "NotEmpty", "You should either fill in your address, or use the registered or operating address as your project address.");
        }
    }

    private void processAddressLookupFields(ProjectDetailsAddressViewModelForm form){
        addAddressOptions(form);
        addSelectedAddress(form);
    }

    private ProjectDetailsAddressViewModelForm getFormDataFromCookie(@ModelAttribute(FORM_ATTR_NAME) ProjectDetailsAddressViewModelForm form, Model model, HttpServletRequest request) {
        BindingResult bindingResult;// Merge information from cookie into ModelAttribute.
        String formJson = CookieUtil.getCookieValue(request, FORM_ATTR_NAME);
        if (StringUtils.hasText(formJson)) {
            form = JsonUtil.getObjectFromJson(formJson, ProjectDetailsAddressViewModelForm.class);
            bindingResult = new BeanPropertyBindingResult(form, FORM_ATTR_NAME);
            validator.validate(form, bindingResult);
            model.addAttribute(BINDING_RESULT_PROJECT_LOCATION_FORM, bindingResult);
            BindingResult addressBindingResult = new BeanPropertyBindingResult(form.getAddressForm().getSelectedPostcode(), SELECTED_POSTCODE);
            addressFormValidate(form, bindingResult, addressBindingResult);
        }
        String addressTypeJson = CookieUtil.getCookieValue(request, ADD_TYPE_ATTR_NAME);
        if(StringUtils.hasText(addressTypeJson)){
            form.setAddressType(AddressType.valueOf(addressTypeJson));
        }
        return form;
    }
}
