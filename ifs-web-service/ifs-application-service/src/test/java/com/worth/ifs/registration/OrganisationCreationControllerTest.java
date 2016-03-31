package com.worth.ifs.registration;

import com.worth.ifs.BaseUnitTest;
import com.worth.ifs.address.resource.AddressResource;
import com.worth.ifs.address.service.AddressRestService;
import com.worth.ifs.application.AcceptInviteController;
import com.worth.ifs.application.resource.ApplicationResource;
import com.worth.ifs.commons.rest.RestResult;
import com.worth.ifs.exception.ErrorControllerAdvice;
import com.worth.ifs.organisation.resource.OrganisationSearchResult;
import com.worth.ifs.registration.form.OrganisationCreationForm;
import com.worth.ifs.user.domain.Organisation;
import com.worth.ifs.user.resource.OrganisationResource;
import com.worth.ifs.user.service.OrganisationSearchRestService;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.Validator;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import java.util.ArrayList;

import static com.worth.ifs.application.builder.ApplicationResourceBuilder.newApplicationResource;
import static com.worth.ifs.user.builder.OrganisationResourceBuilder.newOrganisationResource;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(MockitoJUnitRunner.class)
@TestPropertySource(locations = "classpath:application.properties")
public class OrganisationCreationControllerTest  extends BaseUnitTest {
    @InjectMocks
    private OrganisationCreationController organisationCreationController;
    private ApplicationResource applicationResource;

    @Mock
    private Validator validator;
    @Mock
    private OrganisationSearchRestService organisationSearchRestService;

    @Mock
    private AddressRestService addressRestService;

    private String COMPANY_ID = "08241216";
    private String COMPANY_NAME = "NETWORTHNET LTD";
    private String POSTCODE_LOOKUP = "CH64 3RU";
    private AddressResource address;
    private OrganisationResource organisationResource;
    private Cookie organisationTypeBusiness;
    private Cookie organisationForm;
    private Cookie organisationFormWithPostcodeInput;
    private Cookie organisationFormWithSelectedPostcode;
    private Cookie inviteHash;

    @Before
    public void setUp() {

        // Process mock annotations
        MockitoAnnotations.initMocks(this);

        mockMvc = MockMvcBuilders.standaloneSetup(organisationCreationController, new ErrorControllerAdvice())
                .setViewResolvers(viewResolver())
                .build();

        super.setup();

        this.setupInvites();

        applicationResource = newApplicationResource().withId(6L).withName("some application").build();
        address = new AddressResource("line1", "line2", "line3", "locality", "region", "postcode");
        organisationResource = newOrganisationResource().withId(5L).withName(COMPANY_NAME).build();
        OrganisationSearchResult organisationSearchResult = new OrganisationSearchResult(COMPANY_ID, COMPANY_NAME);
        when(organisationService.getCompanyHouseOrganisation(COMPANY_ID)).thenReturn(organisationSearchResult);
        when(organisationService.save(any(Organisation.class))).thenReturn(organisationResource);
        when(organisationService.save(any(OrganisationResource.class))).thenReturn(organisationResource);
        when(applicationService.createApplication(anyLong(), anyLong(), anyString())).thenReturn(applicationResource);
        when(organisationSearchRestService.getOrganisation(businessOrganisationTypeResource.getId(), COMPANY_ID)).thenReturn(RestResult.restSuccess(organisationSearchResult));
        when(organisationSearchRestService.searchOrganisation(anyLong(), anyString())).thenReturn(RestResult.restSuccess(new ArrayList<>()));
        when(addressRestService.validatePostcode("CH64 3RU")).thenReturn(RestResult.restSuccess(true));

        organisationTypeBusiness = new Cookie("organisationType", "{\"organisationType\":1}");
        organisationForm = new Cookie("organisationForm", "{\"addressForm\":{\"triedToSave\":false,\"postcodeInput\":\"\",\"selectedPostcodeIndex\":null,\"selectedPostcode\":null,\"postcodeOptions\":[],\"manualAddress\":false},\"triedToSave\":false,\"organisationType\":{\"id\":1,\"name\":\"Business\",\"parentOrganisationType\":null},\"organisationSearchName\":null,\"searchOrganisationId\":\""+COMPANY_ID+"\",\"organisationSearching\":false,\"manualEntry\":false,\"useSearchResultAddress\":false,\"organisationSearchResults\":[],\"organisationName\":\"NOMENSA LTD\"}");
        organisationFormWithPostcodeInput = new Cookie("organisationForm", "{\"addressForm\":{\"triedToSave\":false,\"postcodeInput\":\""+POSTCODE_LOOKUP+"\",\"selectedPostcodeIndex\":null,\"selectedPostcode\":null,\"postcodeOptions\":[],\"manualAddress\":false},\"triedToSave\":false,\"organisationType\":{\"id\":1,\"name\":\"Business\",\"parentOrganisationType\":null},\"organisationSearchName\":null,\"searchOrganisationId\":\""+COMPANY_ID+"\",\"organisationSearching\":false,\"manualEntry\":false,\"useSearchResultAddress\":false,\"organisationSearchResults\":[],\"organisationName\":\"NOMENSA LTD\"}");
        organisationFormWithSelectedPostcode = new Cookie("organisationForm", "{\"addressForm\":{\"triedToSave\":false,\"postcodeInput\":\""+POSTCODE_LOOKUP+"\",\"selectedPostcodeIndex\":\"0\",\"selectedPostcode\":null,\"postcodeOptions\":[],\"manualAddress\":false},\"triedToSave\":false,\"organisationType\":{\"id\":1,\"name\":\"Business\",\"parentOrganisationType\":null},\"organisationSearchName\":null,\"searchOrganisationId\":\""+COMPANY_ID+"\",\"organisationSearching\":false,\"manualEntry\":false,\"useSearchResultAddress\":false,\"organisationSearchResults\":[],\"organisationName\":\"NOMENSA LTD\"}");
        inviteHash = new Cookie(AcceptInviteController.INVITE_HASH, INVITE_HASH);
    }

    @Test
    public void testCreateAccountOrganisationType() throws Exception {
        mockMvc.perform(get("/organisation/create/create-organisation-type"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("registration/organisation/create-organisation-type"));
    }

    @Test
    public void testFindBusiness() throws Exception {
        Cookie[] cookies = mockMvc.perform(MockMvcRequestBuilders.get("/organisation/create/find-business"))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.view().name("redirect:/organisation/create/find-organisation"))
                .andReturn().getResponse().getCookies();

        assertEquals(1, cookies.length);
        assertNotNull(cookies[0]);
        assertEquals("{\"organisationType\":1}", cookies[0].getValue());
    }

    @Test
    public void testFindOrganisation() throws Exception {
        Cookie[] cookies = mockMvc.perform(MockMvcRequestBuilders.post("/organisation/create/find-organisation")
                .param("organisationSearchName", "BusinessName")
                .cookie(organisationTypeBusiness)
                .cookie(organisationForm)
                .param("not-in-company-house", "")
        )
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.view().name("redirect:/organisation/create/find-organisation"))
                .andReturn().getResponse().getCookies();

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/organisation/create/find-organisation")
                .cookie(organisationTypeBusiness)
                .cookie(cookies))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andExpect(MockMvcResultMatchers.view().name("registration/organisation/find-organisation"))
                .andExpect(MockMvcResultMatchers.model().attribute("organisationForm", Matchers.hasProperty("manualEntry", Matchers.equalTo(true))))
                .andReturn();
    }

    @Test
    public void testFindBusinessManualAddress() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/organisation/create/find-organisation")
                        .param("organisationSearchName", "BusinessName")
                        .param("manual-address", "")
                        .cookie(organisationTypeBusiness)
                        .cookie(organisationForm)
        )
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.view().name("redirect:/organisation/create/find-organisation"))
                .andExpect(MockMvcResultMatchers.model().attribute("organisationForm", Matchers.isA(OrganisationCreationForm.class)))
                .andExpect(MockMvcResultMatchers.model().attribute("organisationForm", Matchers.hasProperty("manualEntry", Matchers.equalTo(true))));
//                .andExpect(MockMvcResultMatchers.model().attribute("organisationForm", Matchers.hasProperty("manualAddress", Matchers.equalTo(true))));
    }

    @Test
    public void testFindBusinessSearchAddress() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/organisation/create/find-organisation")
                        .cookie(organisationTypeBusiness)
                        .cookie(organisationForm)
                        .param("addressForm.postcodeInput", POSTCODE_LOOKUP)
                        .param("search-address", "")
                        .header("referer", "/organisation/create/find-organisation/")
        )
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.view().name(String.format("redirect:/organisation/create/find-organisation/%s", POSTCODE_LOOKUP)));

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(String.format("/organisation/create/find-organisation/%s", POSTCODE_LOOKUP))
                    .cookie(organisationTypeBusiness)
                    .cookie(organisationForm))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andExpect(MockMvcResultMatchers.view().name("registration/organisation/find-organisation"))
                .andExpect(MockMvcResultMatchers.model().attribute("organisationForm", Matchers.hasProperty("manualEntry", Matchers.equalTo(false))))
                .andReturn();
    }

    @Test
    public void testFindBusinessSearchAddressInvalid() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/organisation/create/find-organisation")
                        .param("postcodeInput", "")
                        .param("search-address", "")
                .cookie(organisationTypeBusiness)
                .header("referer", "/organisation/create/find-organisation/")
        )
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.view().name(String.format("redirect:/organisation/create/find-organisation")));

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(String.format("/organisation/create/find-organisation"))
                .cookie(organisationTypeBusiness)
        )
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andExpect(MockMvcResultMatchers.view().name("registration/organisation/find-organisation"))
                .andExpect(MockMvcResultMatchers.model().attribute("organisationForm", Matchers.hasProperty("manualEntry", Matchers.equalTo(false))))
                .andReturn();
    }

    @Test
    public void testFindBusinessSelectAddress() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/organisation/create/find-organisation")
                        .param("manualEntry", "true")
                        .param("addressForm.postcodeInput", POSTCODE_LOOKUP)
                        .param("addressForm.selectedPostcodeIndex", String.valueOf(0))
                        .param("select-address", "")
                .cookie(organisationTypeBusiness)
                .cookie(organisationForm)
                .header("referer", "/organisation/create/find-organisation/")
        )
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.view().name(String.format("redirect:/organisation/create/find-organisation/%s/0", POSTCODE_LOOKUP)));

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(String.format("/organisation/create/find-organisation/%s/0", POSTCODE_LOOKUP))
                .cookie(organisationTypeBusiness)
                .cookie(organisationForm)
        )
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andExpect(MockMvcResultMatchers.view().name("registration/organisation/find-organisation"))
                .andExpect(MockMvcResultMatchers.model().attribute("organisationForm", Matchers.hasProperty("manualEntry", Matchers.equalTo(false))))
                .andReturn();
    }

    @Test
    public void testFindBusinessSearchCompanyHouse() throws Exception {
        String companyName = "BusinessName";
        Cookie cookie = mockMvc.perform(MockMvcRequestBuilders.post("/organisation/create/find-organisation")
                .cookie(organisationTypeBusiness)
                .param("organisationSearchName", companyName)
                .param("search-organisation", "")
                .header("referer", "/organisation/create/find-organisation/")
        )
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.view().name("redirect:/organisation/create/find-organisation/" + companyName))
                .andExpect(cookie().exists("organisationForm"))
                .andReturn().getResponse().getCookie("organisationForm");

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/organisation/create/find-organisation/" + companyName)
                .cookie(organisationTypeBusiness)
                .cookie(cookie)
        )
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andExpect(MockMvcResultMatchers.view().name("registration/organisation/find-organisation"))
                .andExpect(MockMvcResultMatchers.model().attribute("organisationForm", Matchers.hasProperty("organisationSearching", Matchers.equalTo(true))))
                .andReturn();
    }

    @Test
    public void testFindBusinessSearchCompanyHouseInvalid() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/organisation/create/find-organisation")
                        .param("organisationSearchName", "")
                        .param("search-organisation", "")
                        .cookie(organisationTypeBusiness)
        )
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.view().name("redirect:/organisation/create/find-organisation/"));
    }

    @Test
    public void testFindBusinessConfirmCompanyDetailsInvalid() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/organisation/create/find-organisation")
                .cookie(organisationTypeBusiness)
                .param("organisationName", "")
                .param("save-organisation-details", "")
                .header("referer", "/organisation/create/find-organisation/")
        )
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.view().name("redirect:/organisation/create/find-organisation"))
                .andReturn();

        Cookie cookie = result.getResponse().getCookie("organisationForm");
        assertNotNull(cookie);

        result = mockMvc.perform(MockMvcRequestBuilders.get("/organisation/create/find-organisation")
                .cookie(organisationTypeBusiness)
                .cookie(organisationForm)
                .cookie(cookie)
        )
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andExpect(MockMvcResultMatchers.view().name("registration/organisation/find-organisation"))
//                .andExpect(model().attributeHasFieldErrorCode("companyHouseForm", "organisationName", "NotEmpty"))
                .andReturn();

        ModelAndView mav = result.getModelAndView();
        log.warn("mav");
    }

    @Test
    public void testFindBusinessConfirmCompanyDetails() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/organisation/create/find-organisation")
                        .cookie(organisationTypeBusiness)
                        .param("organisationName", "BusinessName")
                        .param("manualEntry", "true")
                        .param("addressForm.selectedPostcode.addressLine1", "a")
                        .param("addressForm.selectedPostcode.locality", "abc")
                        .param("addressForm.selectedPostcode.region", "def")
                        .param("addressForm.selectedPostcode.postalcode", "abcass")
                        .param("save-organisation-details", "")
                        .header("referer", "/organisation/create/find-organisation/")
        )
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.view().name("redirect:/organisation/create/confirm-organisation"));
    }

    @Test
    public void testCreateOrganisationBusiness() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/organisation/create/find-organisation")
                .cookie(organisationTypeBusiness)
        )
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful());

        mockMvc.perform(MockMvcRequestBuilders.get("/organisation/create/find-organisation")
                .cookie(organisationTypeBusiness)
        )
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andExpect(MockMvcResultMatchers.view().name("registration/organisation/find-organisation"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("organisationForm"));
    }

    @Test
    public void testConfirmBusiness() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/organisation/create/confirm-organisation")
                .param("searchOrganisationId", COMPANY_ID)
                .cookie(organisationTypeBusiness)
                .cookie(organisationForm)
        )
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andExpect(MockMvcResultMatchers.view().name("registration/organisation/confirm-organisation"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("organisationForm"));
    }

    @Test
    public void testSaveOrganisation() throws Exception {
        Cookie[] cookies = mockMvc.perform(MockMvcRequestBuilders.get("/organisation/create/save-organisation")
                .param("searchOrganisationId", COMPANY_ID)
                .cookie(organisationTypeBusiness)
                .cookie(organisationForm)
        )
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.view().name("redirect:/registration/register?organisationId=5"))
                .andExpect(cookie().exists("organisationId"))
                .andExpect(cookie().value("organisationId", "5"))
                .andReturn().getResponse().getCookies();
    }

    @Test
    public void testSaveOrganisationWithInvite() throws Exception {
        Cookie[] cookies = mockMvc.perform(MockMvcRequestBuilders.get("/organisation/create/save-organisation")
                .param("searchOrganisationId", COMPANY_ID)
                .cookie(organisationTypeBusiness)
                .cookie(organisationForm)
                .cookie(inviteHash)
        )
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.view().name("redirect:/registration/register?organisationId=5"))
                .andExpect(cookie().exists("organisationId"))
                .andExpect(cookie().value("organisationId", "5"))
                .andReturn().getResponse().getCookies();
    }

    @Test
    public void testSelectedBusinessGet() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/organisation/create/selected-organisation/" + COMPANY_ID)
                .cookie(organisationTypeBusiness)
                .cookie(organisationForm)
                .header("referer", "/organisation/create/selected-organisation/")
        )
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testSelectedBusinessSubmit() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/organisation/create/selected-organisation/" + COMPANY_ID)
                        .param("addressForm.postcodeInput", POSTCODE_LOOKUP)
                        .param("searchOrganisationId", COMPANY_ID)
                        .param("search-address", "")
                        .cookie(organisationTypeBusiness)
                        .cookie(organisationForm)
                        .header("referer", "/organisation/create/selected-organisation/")
        )
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.view().name(String.format("redirect:/organisation/create/selected-organisation/%s/%s", COMPANY_ID, POSTCODE_LOOKUP)));
    }

    @Test
    public void testSelectedBusinessSubmitSearchAddress() throws Exception {
        Cookie[] cookies = mockMvc.perform(MockMvcRequestBuilders.post("/organisation/create/selected-organisation/" + COMPANY_ID)
                .param("addressForm.postcodeInput", POSTCODE_LOOKUP)
                .param("search-address", "")
                .param("searchOrganisationId", COMPANY_ID)
                .cookie(organisationTypeBusiness)
                .cookie(organisationForm)
                .header("referer", "/organisation/create/selected-organisation/")
        )
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.view().name(String.format("redirect:/organisation/create/selected-organisation/%s/%s", COMPANY_ID, POSTCODE_LOOKUP)))
                .andReturn().getResponse().getCookies();

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(String.format("/organisation/create/selected-organisation/%s/%s", COMPANY_ID, POSTCODE_LOOKUP))
                .cookie(organisationTypeBusiness)
                .cookie(organisationForm)
        )
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andExpect(MockMvcResultMatchers.view().name("registration/organisation/confirm-selected-organisation"))
                .andExpect(MockMvcResultMatchers.model().attributeHasNoErrors("organisationForm"))
                .andReturn();


    }
    @Test
    public void testSelectedOrganisationSearchedPostcode() throws Exception {
        when(addressRestService.doLookup(anyString())).thenReturn(RestResult.restSuccess(new ArrayList<>()));
        MvcResult result2 = mockMvc.perform(MockMvcRequestBuilders.get(String.format("/organisation/create/selected-organisation/%s/%s", COMPANY_ID, POSTCODE_LOOKUP))
                .cookie(organisationTypeBusiness)
                .cookie(organisationFormWithPostcodeInput)
        )
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andExpect(MockMvcResultMatchers.view().name("registration/organisation/confirm-selected-organisation"))
                .andExpect(MockMvcResultMatchers.model().attributeHasNoErrors("organisationForm"))
                .andReturn();
    }

    @Test
    public void testSelectedOrganisationSelectedPostcode() throws Exception {
        ArrayList<AddressResource> addresses = new ArrayList<>();
        addresses.add(new AddressResource());
        addresses.add(new AddressResource());
        addresses.add(new AddressResource());
        when(addressRestService.doLookup(anyString())).thenReturn(RestResult.restSuccess(addresses));
        MvcResult result2 = mockMvc.perform(MockMvcRequestBuilders.get(String.format("/organisation/create/selected-organisation/%s/%s/0", COMPANY_ID, POSTCODE_LOOKUP))
                .cookie(organisationTypeBusiness)
                .cookie(organisationFormWithSelectedPostcode)
        )
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andExpect(MockMvcResultMatchers.view().name("registration/organisation/confirm-selected-organisation"))
                .andExpect(MockMvcResultMatchers.model().attributeHasNoErrors("organisationForm"))
                .andReturn();
    }

    @Test
    public void testSelectedBusinessSubmitSelectAddress() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/organisation/create/selected-organisation/" + COMPANY_ID)
                        .param("addressForm.postcodeInput", POSTCODE_LOOKUP)
                        .param("searchOrganisationId", COMPANY_ID)
                        .cookie(organisationTypeBusiness)
                        .cookie(organisationForm)
                        .param("addressForm.selectedPostcodeIndex", "0")
                        .param("select-address", "")
                        .header("referer", "/organisation/create/selected-organisation/")
        )
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.view().name(String.format("redirect:/organisation/create/selected-organisation/%s/%s/%s", COMPANY_ID, POSTCODE_LOOKUP, "0")));

        mockMvc.perform(MockMvcRequestBuilders.get(String.format("/organisation/create/selected-organisation/%s/%s/%s", COMPANY_ID, POSTCODE_LOOKUP, "0"))
                .cookie(organisationTypeBusiness)
                .cookie(organisationForm)

        )
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andExpect(MockMvcResultMatchers.view().name("registration/organisation/confirm-selected-organisation"))
                .andExpect(MockMvcResultMatchers.model().attributeHasNoErrors("organisationForm"))
                .andReturn();
    }

    @Test
    public void testSelectedBusinessManualAddress() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/organisation/create/selected-organisation/" + COMPANY_ID)
                        .param("searchOrganisationId", COMPANY_ID)
                        .cookie(organisationTypeBusiness)
                        .cookie(organisationForm)
                        .param("manual-address", "true")
        )
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.view().name("redirect:/organisation/create/selected-organisation/"+COMPANY_ID));
    }

    @Test
    public void testSelectedBusinessSaveBusiness() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/organisation/create/selected-organisation/" + COMPANY_ID)
                        .param("useSearchResultAddress", "true")
                        .param("_useSearchResultAddress", "on")
                        .param("save-organisation-details", "true")
                        .param("searchOrganisationId", COMPANY_ID)
                        .header("referer", "/organisation/create/selected-organisation/")
                        .cookie(organisationTypeBusiness)
                        .cookie(organisationForm)
        )
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.view().name("redirect:/organisation/create/confirm-organisation"));
    }

    /**
     * Check if request is redirected back to the form, when submit is invalid.
     */
    @Test
    public void testSelectedBusinessInvalidSubmit() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/organisation/create/selected-organisation/" + COMPANY_ID)
                .param("manualEntry", "true")
                .param("addressForm.postcodeInput", "")
                .param("search-address", "")
                .cookie(organisationTypeBusiness)
                .param("searchOrganisationId", COMPANY_ID)
                .cookie(organisationForm)
                .header("referer", "/organisation/create/selected-organisation/")

        )
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.view().name(String.format("redirect:/organisation/create/selected-organisation/%s", COMPANY_ID)))
                .andReturn();
    }

    @Test
    public void testConfirmCompany() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/organisation/create/confirm-organisation")
                        .cookie(organisationTypeBusiness)
                        .cookie(organisationForm)
        )
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andExpect(MockMvcResultMatchers.model().attributeExists("selectedOrganisation"))
                .andExpect(MockMvcResultMatchers.model().attribute("selectedOrganisation", Matchers.hasProperty("name", Matchers.equalTo(COMPANY_NAME))))
                .andExpect(MockMvcResultMatchers.view().name("registration/organisation/confirm-organisation"));
    }
}
