package com.worth.ifs.project;

import com.google.common.net.UrlEscapers;
import com.worth.ifs.address.resource.AddressResource;
import com.worth.ifs.address.resource.AddressType;
import com.worth.ifs.address.service.AddressRestService;
import com.worth.ifs.application.form.AddressForm;
import com.worth.ifs.application.resource.ApplicationResource;
import com.worth.ifs.application.service.*;
import com.worth.ifs.commons.rest.RestResult;
import com.worth.ifs.commons.security.UserAuthenticationService;
import com.worth.ifs.commons.service.ServiceResult;
import com.worth.ifs.competition.resource.CompetitionResource;
import com.worth.ifs.controller.BindingResultTarget;
import com.worth.ifs.model.OrganisationDetailsModelPopulator;
import com.worth.ifs.organisation.resource.OrganisationAddressResource;
import com.worth.ifs.project.resource.ProjectResource;
import com.worth.ifs.project.viewmodel.ProjectDetailsAddressViewModel;
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
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static com.worth.ifs.controller.RestFailuresToValidationErrorBindingUtils.bindAnyErrorsToField;

/**
 * This controller will handle all requests that are related to project details.
 */
@Controller
@RequestMapping("/project")
public class ProjectDetailsController {
    private static final String MANUAL_ADDRESS = "manual-address";
    private static final String SEARCH_ADDRESS = "search-address";
    private static final String SELECT_ADDRESS = "select-address";

    private static final String ADDRESS_USE_ORG = "address-use-org";
    private static final String ADDRESS_USE_OP = "address-use-operating";
    private static final String ADDRESS_USE_ADD = "address-add-project";

    public static final String PROJECT_LOCATION_FORM = "projectLocationForm";
    public static final String PROJECT_START_DATE_FORM = "form";

    private static final String SELECTED_POSTCODE = "selectedPostcode";

    private static final String BINDING_RESULT_PROJECT_LOCATION_FORM = "org.springframework.validation.BindingResult.projectLocationForm";

    public static final String USE_SEARCH_RESULT_ADDRESS = "useSearchResultAddress";

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
    private AddressService addressService;

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
                                @ModelAttribute(PROJECT_START_DATE_FORM) ProjectDetailsStartDateForm form) {
    	
    	ProjectResource project = projectService.getById(projectId);
    	model.addAttribute("model", new ProjectDetailsStartDateViewModel(project));
        LocalDate defaultStartDate = project.getTargetStartDate().withDayOfMonth(1);
        form.setProjectStartDate(defaultStartDate);
        return "project/details-start-date";
    }

    @RequestMapping(value = "/{projectId}/details/start-date", method = RequestMethod.POST)
    public String updateStartDate(@PathVariable("projectId") final Long projectId,
                                  @ModelAttribute(PROJECT_START_DATE_FORM) ProjectDetailsStartDateForm form,
                                  Model model,
                                  BindingResult bindingResult) {

        ServiceResult<Void> updateResult = projectService.updateProjectStartDate(projectId, form.getProjectStartDate());
        return handleErrorsOrRedirectToProjectOverview("projectStartDate", projectId, model, form, bindingResult, updateResult, () -> viewStartDate(model, projectId, form));
    }

    @RequestMapping(value = "/{projectId}/details/project-address", method = RequestMethod.GET)
    public String viewAddress(Model model, @PathVariable("projectId") final Long projectId, HttpServletRequest request,
                              @ModelAttribute(PROJECT_LOCATION_FORM) ProjectDetailsAddressViewModel.ProjectDetailsAddressViewModelForm form) {
        form = getFormDataFromCookie(form, model, request);

        ProjectResource project = projectService.getById(projectId);
        ProjectDetailsAddressViewModel projectDetailsAddressViewModel = new ProjectDetailsAddressViewModel(project);
        OrganisationResource leadOrganisation = getLeadOrganisation(projectId);
        List<Long> existingAddresses = new ArrayList<>();
        Optional<OrganisationAddressResource> registeredAddress = getAddress(leadOrganisation, AddressType.REGISTERED);
        if(registeredAddress.isPresent()){
            AddressResource addressResource = registeredAddress.get().getAddress();
            projectDetailsAddressViewModel.setRegisteredAddress(addressResource);
            existingAddresses.add(addressResource.getId());
            if(addressResource.getId().equals(project.getAddress())){
                form.setProjectAddressGroup(ADDRESS_USE_ORG);
            }
        }

        Optional<OrganisationAddressResource> operatingAddress = getAddress(leadOrganisation, AddressType.OPERATING);
        if(operatingAddress.isPresent()){
            AddressResource addressResource = operatingAddress.get().getAddress();
            projectDetailsAddressViewModel.setOperatingAddress(operatingAddress.get().getAddress());
            existingAddresses.add(addressResource.getId());
            if(addressResource.getId().equals(project.getAddress())){
                form.setProjectAddressGroup(ADDRESS_USE_OP);
            }
        }

        if(project.getAddress() != null && !existingAddresses.contains(project.getAddress())){
            AddressResource addressResource = addressService.getById(project.getAddress()).getSuccessObjectOrThrowException();
            projectDetailsAddressViewModel.setProjectAddress(addressResource);
            if(addressResource.getId().equals(project.getAddress())){
                form.setProjectAddressGroup(ADDRESS_USE_ADD);
            }
        }

        addAddressOptions(form);
        addSelectedAddress(form);

        model.addAttribute("model", projectDetailsAddressViewModel);
        return "project/details-address";
    }

    @RequestMapping(value = "/{projectId}/details/project-address", method = RequestMethod.POST)
    public String updateAddress(@PathVariable("projectId") final Long projectId,
                                @ModelAttribute(PROJECT_LOCATION_FORM) ProjectDetailsAddressViewModel.ProjectDetailsAddressViewModelForm form,
                                Model model,
                                HttpServletRequest request,
                                BindingResult bindingResult) {
        ProjectResource project = projectService.getById(projectId);
        ProjectDetailsAddressViewModel projectDetailsAddressViewModel = new ProjectDetailsAddressViewModel(project);
        OrganisationResource leadOrganisation = getLeadOrganisation(projectId);

        Long selectedAddressId = null;

        switch (form.getProjectAddressGroup()) {
            case ADDRESS_USE_OP:
                Optional<OrganisationAddressResource> operatingAddress = getAddress(leadOrganisation, AddressType.OPERATING);
                if (operatingAddress.isPresent()) {
                    selectedAddressId = operatingAddress.get().getAddress().getId();
                }
                break;
            case ADDRESS_USE_ORG:
                Optional<OrganisationAddressResource> registeredAddress = getAddress(leadOrganisation, AddressType.REGISTERED);
                if (registeredAddress.isPresent()) {
                    selectedAddressId = registeredAddress.get().getAddress().getId();
                }
                break;
            default:
                // Save new project address and assign id
                break;
        }
        ServiceResult<Void> updateResult = projectService.updateAddress(projectId, selectedAddressId);
        return handleErrorsOrRedirectToProjectOverview("projectStartDate", projectId, model, form, bindingResult, updateResult, () -> viewAddress(model, projectId, request, form));
    }

    @RequestMapping(value = "/{projectId}/details/project-address", params = SEARCH_ADDRESS, method = RequestMethod.POST)
    public String searchAddress(@ModelAttribute(PROJECT_LOCATION_FORM) ProjectDetailsAddressViewModel.ProjectDetailsAddressViewModelForm form,
                                HttpServletResponse response) {
        form.getAddressForm().setSelectedPostcodeIndex(null);
        form.getAddressForm().setTriedToSearch(true);
        CookieUtil.saveToCookie(response, PROJECT_LOCATION_FORM, JsonUtil.getSerializedObject(form));
        return "project/details-address";
    }

    @RequestMapping(value = "/{projectId}/details/project-address", params = SELECT_ADDRESS, method = RequestMethod.POST)
    public String selectAddress(@ModelAttribute(PROJECT_LOCATION_FORM) ProjectDetailsAddressViewModel.ProjectDetailsAddressViewModelForm form,
                                HttpServletResponse response) {
        form.getAddressForm().setSelectedPostcode(null);
        CookieUtil.saveToCookie(response, PROJECT_LOCATION_FORM, JsonUtil.getSerializedObject(form));
        return "project/details-address";
    }

    @RequestMapping(value = "/{projectId}/details/project-address", params = MANUAL_ADDRESS, method = RequestMethod.POST)
    public String manualAddress(@ModelAttribute(PROJECT_LOCATION_FORM) ProjectDetailsAddressViewModel.ProjectDetailsAddressViewModelForm form, HttpServletResponse response) {
        form.setAddressForm(new AddressForm());
        form.getAddressForm().setManualAddress(true);
        CookieUtil.saveToCookie(response, PROJECT_LOCATION_FORM, JsonUtil.getSerializedObject(form));
        return "project/details-address";
    }

    private String handleErrorsOrRedirectToProjectOverview(
            String fieldName, long projectId, Model model,
            BindingResultTarget form, BindingResult bindingResult,
            ServiceResult<?> result,
            Supplier<String> viewSupplier) {
        if (result.isFailure()) {
            bindAnyErrorsToField(result, fieldName, bindingResult, form);
            model.addAttribute(PROJECT_LOCATION_FORM, form);
            return viewSupplier.get();
        }

        return redirectToProjectDetails(projectId);
    }

    private String redirectToProjectDetails(long projectId) {
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
    private void addAddressOptions(ProjectDetailsAddressViewModel.ProjectDetailsAddressViewModelForm projectDetailsAddressViewModelForm) {
        if (StringUtils.hasText(projectDetailsAddressViewModelForm.getAddressForm().getPostcodeInput())) {
            AddressForm addressForm = projectDetailsAddressViewModelForm.getAddressForm();
            addressForm.setPostcodeOptions(searchPostcode(projectDetailsAddressViewModelForm.getAddressForm().getPostcodeInput()));
            addressForm.setPostcodeInput(projectDetailsAddressViewModelForm.getAddressForm().getPostcodeInput());
            projectDetailsAddressViewModelForm.setAddressForm(addressForm);
        }
    }

    /**
     * if user has selected a address from the dropdown, get it from the list, and set it as selected.
     */
    private void addSelectedAddress(ProjectDetailsAddressViewModel.ProjectDetailsAddressViewModelForm projectDetailsAddressViewModelForm) {
        AddressForm addressForm = projectDetailsAddressViewModelForm.getAddressForm();
        if (StringUtils.hasText(addressForm.getSelectedPostcodeIndex()) && addressForm.getSelectedPostcode() == null) {
            addressForm.setSelectedPostcode(addressForm.getPostcodeOptions().get(Integer.parseInt(addressForm.getSelectedPostcodeIndex())));
            projectDetailsAddressViewModelForm.setAddressForm(addressForm);
        }
    }

    private List<AddressResource> searchPostcode(String postcodeInput) {
        RestResult<List<AddressResource>> addressLookupRestResult = addressRestService.doLookup(postcodeInput);
        return addressLookupRestResult.handleSuccessOrFailure(
                failure -> new ArrayList<>(),
                addresses -> addresses);
    }

    private String escapePathVariable(final String input){
        return UrlEscapers.urlPathSegmentEscaper().escape(input);
    }

    private ProjectDetailsAddressViewModel.ProjectDetailsAddressViewModelForm getFormDataFromCookie(@ModelAttribute(PROJECT_LOCATION_FORM) ProjectDetailsAddressViewModel.ProjectDetailsAddressViewModelForm form, Model model, HttpServletRequest request) {
        BindingResult bindingResult;// Merge information from cookie into ModelAttribute.
        String formJson = CookieUtil.getCookieValue(request, PROJECT_LOCATION_FORM);

        if (StringUtils.hasText(formJson)) {
            form = JsonUtil.getObjectFromJson(formJson, ProjectDetailsAddressViewModel.ProjectDetailsAddressViewModelForm.class);
            bindingResult = new BeanPropertyBindingResult(form, PROJECT_LOCATION_FORM);
            validator.validate(form, bindingResult);
            model.addAttribute(PROJECT_LOCATION_FORM, bindingResult);
            BindingResult addressBindingResult = new BeanPropertyBindingResult(form.getAddressForm().getSelectedPostcode(), SELECTED_POSTCODE);
            addressFormValidate(form, bindingResult, addressBindingResult);
        }
        return form;
    }

    private void addressFormValidate(@Valid @ModelAttribute(PROJECT_LOCATION_FORM) ProjectDetailsAddressViewModel.ProjectDetailsAddressViewModelForm form, BindingResult bindingResult, BindingResult addressBindingResult) {
        if (form.getProjectAddressGroup().equals(ADDRESS_USE_ADD)) {
            if (form.getAddressForm().getSelectedPostcode() != null) {
                validator.validate(form.getAddressForm().getSelectedPostcode(), addressBindingResult);
            } else if (!form.getAddressForm().isManualAddress()) {
                bindingResult.rejectValue(USE_SEARCH_RESULT_ADDRESS, "NotEmpty", "You should either fill in your address, or use the registered or operating address as your project address.");
            }
        }
    }
}
