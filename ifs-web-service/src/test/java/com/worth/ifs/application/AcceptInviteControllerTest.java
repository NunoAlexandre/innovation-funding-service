package com.worth.ifs.application;

import com.worth.ifs.BaseUnitTest;
import com.worth.ifs.commons.error.exception.InvalidURLException;
import com.worth.ifs.exception.ErrorController;
import com.worth.ifs.security.CookieFlashMessageFilter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.Validator;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(MockitoJUnitRunner.class)
@TestPropertySource(locations = "classpath:application.properties")
public class AcceptInviteControllerTest extends BaseUnitTest {

    @InjectMocks
    private AcceptInviteController acceptInviteController;

    @Mock
    private Validator validator;
    @Mock
    CookieFlashMessageFilter cookieFlashMessageFilter;
    private Long applicationId;

    @Before
    public void setUp() throws Exception {

        super.setup();
        // Process mock annotations
        MockitoAnnotations.initMocks(this);

        CookieLocaleResolver localeResolver = new CookieLocaleResolver();
        localeResolver.setCookieDomain("domain");

        final StaticApplicationContext applicationContext = new StaticApplicationContext();
        applicationContext.registerSingleton("exceptionHandler", ErrorController.class);

        final WebMvcConfigurationSupport webMvcConfigurationSupport = new WebMvcConfigurationSupport();
        webMvcConfigurationSupport.setApplicationContext(applicationContext);

        mockMvc = MockMvcBuilders.standaloneSetup(acceptInviteController, new ErrorController())
                .setHandlerExceptionResolvers(webMvcConfigurationSupport.handlerExceptionResolver())
                .setViewResolvers(viewResolver())
                .setLocaleResolver(localeResolver)
                .addFilters(new CookieFlashMessageFilter())
                .build();


        this.setupCompetition();
        this.setupApplicationWithRoles();
        this.setupApplicationResponses();
        this.loginDefaultUser();
        this.setupFinances();
        this.setupInvites();
        this.setupOrganisationTypes();

        applicationId = applications.get(0).getId();
    }

    @Test
    public void testInviteEntryPage() throws Exception {
        mockMvc.perform(
                get(String.format("/accept-invite/%s", INVITE_HASH))
        )
                .andExpect(status().is2xxSuccessful())
                .andExpect(cookie().exists(AcceptInviteController.INVITE_HASH))
                .andExpect(cookie().value(AcceptInviteController.INVITE_HASH, INVITE_HASH))
                .andExpect(view().name("registration/accept-invite"));
    }

    @Test
    public void testInviteEntryPageExistingUser() throws Exception {
        mockMvc.perform(
                get(String.format("/accept-invite/%s", INVITE_HASH_EXISTING_USER))
        )
                .andExpect(status().is2xxSuccessful())
                .andExpect(cookie().exists(AcceptInviteController.INVITE_HASH))
                .andExpect(cookie().value(AcceptInviteController.INVITE_HASH, INVITE_HASH_EXISTING_USER))
                .andExpect(model().attribute("emailAddressRegistered", "true"))
                .andExpect(view().name("registration/accept-invite"));
    }

    @Test
    public void testInviteEntryPageInvalid() throws Exception {
        mockMvc.perform(
                get(String.format("/accept-invite/%s", INVALID_INVITE_HASH))
        )
                .andExpect(status().is2xxSuccessful())
                .andExpect(cookie().value(AcceptInviteController.INVITE_HASH, ""))
                .andExpect(view().name("url-hash-invalid"));
    }
    @Test
    public void testInviteEntryPageAccepted() throws Exception {
        mockMvc.perform(
                get(String.format("/accept-invite/%s", ACCEPTED_INVITE_HASH))
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(cookie().value(AcceptInviteController.INVITE_HASH, ""))
                .andExpect(view().name("redirect:/login"));
    }
}