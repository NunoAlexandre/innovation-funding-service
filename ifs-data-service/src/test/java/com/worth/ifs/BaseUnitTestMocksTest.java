package com.worth.ifs;

import com.worth.ifs.address.mapper.AddressMapper;
import com.worth.ifs.address.repository.AddressRepository;
import com.worth.ifs.address.repository.AddressTypeRepository;
import com.worth.ifs.address.transactional.AddressLookupService;
import com.worth.ifs.address.transactional.AddressService;
import com.worth.ifs.alert.mapper.AlertMapper;
import com.worth.ifs.alert.repository.AlertRepository;
import com.worth.ifs.alert.transactional.AlertService;
import com.worth.ifs.application.mapper.ApplicationMapper;
import com.worth.ifs.application.mapper.QuestionMapper;
import com.worth.ifs.application.repository.*;
import com.worth.ifs.application.transactional.ApplicationFundingService;
import com.worth.ifs.application.transactional.ApplicationService;
import com.worth.ifs.application.transactional.AssessorFeedbackService;
import com.worth.ifs.application.transactional.QuestionService;
import com.worth.ifs.assessment.mapper.AssessmentMapper;
import com.worth.ifs.assessment.mapper.AssessorFormInputResponseMapper;
import com.worth.ifs.assessment.repository.AssessmentRepository;
import com.worth.ifs.assessment.repository.AssessorFormInputResponseRepository;
import com.worth.ifs.assessment.transactional.AssessmentService;
import com.worth.ifs.assessment.transactional.AssessorFormInputResponseService;
import com.worth.ifs.authentication.service.IdentityProviderService;
import com.worth.ifs.bankdetails.mapper.BankDetailsMapper;
import com.worth.ifs.bankdetails.repository.BankDetailsRepository;
import com.worth.ifs.bankdetails.transactional.BankDetailsService;
import com.worth.ifs.category.mapper.CategoryLinkMapper;
import com.worth.ifs.category.mapper.CategoryMapper;
import com.worth.ifs.category.repository.CategoryLinkRepository;
import com.worth.ifs.category.repository.CategoryRepository;
import com.worth.ifs.category.transactional.CategoryLinkService;
import com.worth.ifs.category.transactional.CategoryService;
import com.worth.ifs.competition.repository.CompetitionCoFunderRepository;
import com.worth.ifs.competition.repository.CompetitionRepository;
import com.worth.ifs.email.service.EmailService;
import com.worth.ifs.file.mapper.FileEntryMapper;
import com.worth.ifs.file.transactional.FileHttpHeadersValidator;
import com.worth.ifs.file.transactional.FileService;
import com.worth.ifs.finance.mapper.ApplicationFinanceMapper;
import com.worth.ifs.finance.repository.ApplicationFinanceRepository;
import com.worth.ifs.finance.repository.CostRepository;
import com.worth.ifs.form.repository.FormInputRepository;
import com.worth.ifs.form.repository.FormInputResponseRepository;
import com.worth.ifs.form.transactional.FormInputService;
import com.worth.ifs.invite.repository.InviteOrganisationRepository;
import com.worth.ifs.invite.repository.InviteProjectRepository;
import com.worth.ifs.invite.repository.InviteRepository;
import com.worth.ifs.invite.transactional.InviteProjectService;
import com.worth.ifs.notifications.resource.SystemNotificationSource;
import com.worth.ifs.notifications.service.NotificationService;
import com.worth.ifs.organisation.repository.OrganisationAddressRepository;
import com.worth.ifs.organisation.transactional.OrganisationService;
import com.worth.ifs.project.mapper.MonitoringOfficerMapper;
import com.worth.ifs.project.mapper.ProjectMapper;
import com.worth.ifs.project.mapper.ProjectUserMapper;
import com.worth.ifs.project.repository.MonitoringOfficerRepository;
import com.worth.ifs.project.repository.ProjectRepository;
import com.worth.ifs.project.repository.ProjectUserRepository;
import com.worth.ifs.project.transactional.ProjectService;
import com.worth.ifs.sil.experian.service.SilExperianEndpoint;
import com.worth.ifs.token.repository.TokenRepository;
import com.worth.ifs.token.transactional.TokenService;
import com.worth.ifs.user.mapper.UserMapper;
import com.worth.ifs.user.repository.*;
import com.worth.ifs.user.transactional.PasswordPolicyValidator;
import com.worth.ifs.user.transactional.RegistrationService;
import com.worth.ifs.user.transactional.UserService;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * This is a convenience subclass for all tests that require Mockito support.  At its simplest this class is simply a
 * place to store and initialise Mockito mocks.  Mocks can then be injected into particular attributes using the @InjectMocks
 * annotation.
 */
public abstract class BaseUnitTestMocksTest extends BaseTest {

    @Mock
    protected AlertService alertServiceMock;

    @Mock
    protected AlertRepository alertRepositoryMock;

    @Mock
    protected AlertMapper alertMapperMock;

    @Mock
    protected AddressRepository addressRepositoryMock;

    @Mock
    protected ApplicationRepository applicationRepositoryMock;

    @Mock
    protected ApplicationMapper applicationMapperMock;

    @Mock
    protected ApplicationFinanceMapper applicationFinanceMapperMock;

    @Mock
    protected ApplicationFinanceRepository applicationFinanceRepositoryMock;

    @Mock
    protected AssessmentMapper assessmentMapperMock;

    @Mock
    protected AssessmentService assessmentServiceMock;

    @Mock
    protected AssessorFormInputResponseMapper assessorFormInputResponseMapperMock;

    @Mock
    protected AssessorFormInputResponseService assessorFormInputResponseServiceMock;

    @Mock
    protected FormInputResponseRepository formInputResponseRepositoryMock;

    @Mock
    protected UserRepository userRepositoryMock;

    @Mock
    protected CompAdminEmailRepository compAdminEmailRepositoryMock;

    @Mock
    protected ProjectFinanceEmailRepository projectFinanceEmailRepositoryMock;

    @Mock
    protected RoleRepository roleRepositoryMock;

    @Mock
    protected ProcessRoleRepository processRoleRepositoryMock;

    @Mock
    protected CompetitionRepository competitionRepositoryMock;

    @Mock
    protected OrganisationRepository organisationRepositoryMock;

    @Mock
    protected ApplicationStatusRepository applicationStatusRepositoryMock;

    @Mock
    protected FormInputRepository formInputRepositoryMock;

    @Mock
    protected SectionRepository sectionRepositoryMock;

    @Mock
    protected MonitoringOfficerRepository monitoringOfficerRepository;

    @Mock
    protected MonitoringOfficerMapper monitoringOfficerMapper;

    @Mock
    protected ApplicationService applicationServiceMock;

    @Mock
    protected QuestionService questionServiceMock;

    @Mock
    protected QuestionRepository questionRepositoryMock;

    @Mock
    protected QuestionStatusRepository questionStatusRepositoryMock;

    @Mock
    protected QuestionMapper questionMapperMock;

    @Mock
    protected FileService fileServiceMock;

    @Mock
    protected EmailService emailServiceMock;

    @Mock
    protected NotificationService notificationServiceMock;

    @Mock
    protected InviteOrganisationRepository inviteOrganisationRepositoryMock;

    @Mock
    protected InviteRepository inviteRepositoryMock;

    @Mock
    protected InviteProjectRepository inviteProjectRepositoryMock;

    @Mock
    protected InviteProjectService inviteProjectServiceMock;

    @Mock
    protected AddressLookupService addressLookupServiceMock;

    @Mock
    protected AddressService addressService;

    @Mock
    protected OrganisationService organisationServiceMock;

    @Mock
    protected UserService userServiceMock;

    @Mock
    protected CostRepository costRepositoryMock;

    @Mock
    protected AssessmentRepository assessmentRepositoryMock;

    @Mock
    protected AssessorFormInputResponseRepository assessorFormInputResponseRepositoryMock;

    @Mock
    protected RegistrationService registrationServiceMock;

    @Mock
    protected IdentityProviderService idpServiceMock;

    @Mock
    protected TokenService tokenServiceMock;

    @Mock
    protected TokenRepository tokenRepositoryMock;

    @Mock
    protected UserMapper userMapperMock;

    @Mock
    protected PasswordPolicyValidator passwordPolicyValidatorMock;

    @Mock
    protected FormInputService formInputServiceMock;

    @Mock
    protected ApplicationFundingService applicationFundingServiceMock;

    @Mock
    protected SystemNotificationSource systemNotificationSourceMock;

    @Mock
    protected FileHttpHeadersValidator fileValidatorMock;

    @Mock
    protected FileEntryMapper fileEntryMapperMock;

    @Mock
    protected AddressMapper addressMapperMock;

    @Mock
    protected AddressTypeRepository addressTypeRepositoryMock;

    @Mock
    protected OrganisationAddressRepository organisationAddressRepositoryMock;

    @Mock
    protected AssessorFeedbackService assessorFeedbackServiceMock;

    @Mock
    protected ProjectService projectServiceMock;

    @Mock
    protected ProjectMapper projectMapperMock;

    @Mock
    protected ProjectUserMapper projectUserMapperMock;

    @Mock
    protected ProjectRepository projectRepositoryMock;

    @Mock
    protected ProjectUserRepository projectUserRepositoryMock;

    @Mock
    protected CategoryService categoryServiceMock;

    @Mock
    protected CategoryRepository categoryRepositoryMock;

    @Mock
    protected CategoryMapper categoryMapperMock;

    @Mock
    protected CategoryLinkService categoryLinkServiceMock;

    @Mock
    protected CategoryLinkRepository categoryLinkRepositoryMock;

    @Mock
    protected CategoryLinkMapper categoryLinkMapperMock;

    @Mock
    protected BankDetailsMapper bankDetailsMapperMock;

    @Mock
    protected BankDetailsRepository bankDetailsRepositoryMock;

    @Mock
    protected BankDetailsService bankDetailsServiceMock;

    @Mock
    protected CompetitionCoFunderRepository competitionCoFunderRepositoryMock;

    @Mock
    protected SilExperianEndpoint silExperianEndpointMock;

    @Before
    public void setupMockInjection() {
        // Process mock annotations
        MockitoAnnotations.initMocks(this);
    }
}