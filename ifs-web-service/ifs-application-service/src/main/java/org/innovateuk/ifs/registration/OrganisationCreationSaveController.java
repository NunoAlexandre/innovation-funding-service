package org.innovateuk.ifs.registration;

import org.innovateuk.ifs.address.resource.AddressResource;
import org.innovateuk.ifs.application.service.OrganisationService;
import org.innovateuk.ifs.invite.service.InviteOrganisationRestService;
import org.innovateuk.ifs.invite.service.InviteRestService;
import org.innovateuk.ifs.organisation.resource.OrganisationSearchResult;
import org.innovateuk.ifs.registration.form.OrganisationCreationForm;
import org.innovateuk.ifs.user.resource.OrganisationResource;
import org.innovateuk.ifs.user.resource.OrganisationTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.innovateuk.ifs.address.resource.OrganisationAddressType.OPERATING;
import static org.innovateuk.ifs.address.resource.OrganisationAddressType.REGISTERED;
import static org.innovateuk.ifs.commons.rest.RestResult.restFailure;
import static org.innovateuk.ifs.registration.AbstractAcceptInviteController.INVITE_HASH;

/**
 * TODO: Add description
 */
@Controller
@RequestMapping(AbstractOrganisationCreationController.BASE_URL)
@PreAuthorize("permitAll")
public class OrganisationCreationSaveController extends AbstractOrganisationCreationController {

    @Autowired
    private OrganisationService organisationService;

    @Autowired
    private InviteRestService inviteRestService;

    @Autowired
    private InviteOrganisationRestService inviteOrganisationRestService;

    /**
     * Confirm the company details (user input, not from company-house)
     */
    @GetMapping("/" + CONFIRM_ORGANISATION)
    public String confirmCompany(@ModelAttribute(name = ORGANISATION_FORM, binding = false) OrganisationCreationForm organisationForm,
                                 Model model,
                                 HttpServletRequest request) throws IOException {
        organisationForm = getFormDataFromCookie(organisationForm, model, request);
        addOrganisationType(organisationForm, organisationTypeIdFromCookie(request));
        addSelectedOrganisation(organisationForm, model);
        model.addAttribute(ORGANISATION_FORM, organisationForm);
        return TEMPLATE_PATH + "/" + CONFIRM_ORGANISATION;
    }

    @PostMapping("/save-organisation")
    public String saveOrganisation(@ModelAttribute(name = ORGANISATION_FORM, binding = false) OrganisationCreationForm organisationForm, Model model, HttpServletRequest request, HttpServletResponse response) throws IOException {
        organisationForm = getFormDataFromCookie(organisationForm, model, request);
        OrganisationSearchResult selectedOrganisation = addSelectedOrganisation(organisationForm, model);
        AddressResource address = organisationForm.getAddressForm().getSelectedPostcode();

        OrganisationResource organisationResource = new OrganisationResource();
        organisationResource.setName(organisationForm.getOrganisationName());
        organisationResource.setOrganisationType(organisationForm.getOrganisationType().getId());

        if (!OrganisationTypeEnum.RESEARCH.getId().equals(organisationForm.getOrganisationType().getId())) {
            organisationResource.setCompanyHouseNumber(organisationForm.getSearchOrganisationId());
        }

        organisationResource = saveNewOrganisation(organisationResource, request);
        if (address != null && !organisationForm.isUseSearchResultAddress()) {
            organisationService.addAddress(organisationResource, address, OPERATING);
        }
        if (selectedOrganisation != null && selectedOrganisation.getOrganisationAddress() != null) {
            organisationService.addAddress(organisationResource, selectedOrganisation.getOrganisationAddress(), REGISTERED);
        }
        cookieUtil.saveToCookie(response, ORGANISATION_ID, String.valueOf(organisationResource.getId()));
        return "redirect:" + RegistrationController.BASE_URL;
    }

    private OrganisationResource saveNewOrganisation(OrganisationResource organisationResource, HttpServletRequest request) {
        organisationResource = organisationService.saveForAnonymousUserFlow(organisationResource);
        linkOrganisationToInvite(organisationResource, request);
        return organisationResource;
    }

    /**
     * If current user is a invitee, then link the organisation that is created, to the InviteOrganisation.
     */
    private void linkOrganisationToInvite(OrganisationResource organisationResource, HttpServletRequest request) {
        String cookieHash = cookieUtil.getCookieValue(request, INVITE_HASH);
        if (isNotBlank(cookieHash)) {
            final OrganisationResource finalOrganisationResource = organisationResource;

            inviteRestService.getInviteByHash(cookieHash).andOnSuccess(
                    s ->
                            inviteOrganisationRestService.getByIdForAnonymousUserFlow(s.getInviteOrganisation()).handleSuccessOrFailure(
                                    f -> restFailure(HttpStatus.NOT_FOUND),
                                    i -> {
                                        if (i.getOrganisation() == null) {
                                            i.setOrganisation(finalOrganisationResource.getId());
                                            // Save the created organisation Id, so the next invitee does not have to..
                                            return inviteOrganisationRestService.put(i);
                                        }
                                        return restFailure(HttpStatus.ALREADY_REPORTED);
                                    }
                            )
            );
        }
    }
}
