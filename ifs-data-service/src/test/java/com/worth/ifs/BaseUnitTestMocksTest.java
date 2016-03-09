package com.worth.ifs;

import com.worth.ifs.address.transactional.AddressLookupService;
import com.worth.ifs.address.transactional.AddressService;
import com.worth.ifs.application.mapper.ApplicationMapper;
import com.worth.ifs.application.repository.*;
import com.worth.ifs.application.transactional.ApplicationService;
import com.worth.ifs.application.transactional.ResponseService;
import com.worth.ifs.assessment.repository.AssessmentRepository;
import com.worth.ifs.authentication.service.IdentityProviderService;
import com.worth.ifs.competition.repository.CompetitionRepository;
import com.worth.ifs.email.service.EmailService;
import com.worth.ifs.file.transactional.FileService;
import com.worth.ifs.finance.repository.ApplicationFinanceRepository;
import com.worth.ifs.finance.repository.CostRepository;
import com.worth.ifs.form.repository.FormInputRepository;
import com.worth.ifs.form.repository.FormInputResponseRepository;
import com.worth.ifs.invite.repository.InviteOrganisationRepository;
import com.worth.ifs.invite.repository.InviteRepository;
import com.worth.ifs.notifications.service.NotificationService;
import com.worth.ifs.organisation.repository.AddressRepository;
import com.worth.ifs.organisation.transactional.OrganisationService;
import com.worth.ifs.token.repository.TokenRepository;
import com.worth.ifs.token.transactional.TokenService;
import com.worth.ifs.user.repository.OrganisationRepository;
import com.worth.ifs.user.repository.ProcessRoleRepository;
import com.worth.ifs.user.repository.RoleRepository;
import com.worth.ifs.user.repository.UserRepository;
import com.worth.ifs.user.transactional.RegistrationService;
import com.worth.ifs.user.transactional.UserService;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * This is a convenience subclass for all tests that require Mockito support.  At its simplest this class is simply a
 * place to store and initialise Mockito mocks.  Mocks can then be injected into particular attributes using the @InjectMocks
 * annotation.
 *
 */
public abstract class BaseUnitTestMocksTest extends BaseTest {

    @Mock
    protected ResponseService responseService;

    @Mock
    protected AddressRepository addressRepositoryMock;

    @Mock
    protected ApplicationRepository applicationRepositoryMock;

    @Mock
    protected ApplicationMapper applicationMapperMock;

    @Mock
    protected ApplicationFinanceRepository applicationFinanceRepository;

    @Mock
    protected FormInputResponseRepository formInputResponseRepositoryMock;

    @Mock
    protected UserRepository userRepositoryMock;

    @Mock
    protected RoleRepository roleRepositoryMock;

    @Mock
    protected ProcessRoleRepository processRoleRepositoryMock;

    @Mock
    protected ResponseRepository responseRepositoryMock;

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
    protected ApplicationService applicationService;

    @Mock
    protected QuestionRepository questionRepository;

    @Mock
    protected QuestionStatusRepository questionStatusRepository;

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
    protected AddressLookupService addressLookupServiceMock;

    @Mock
    protected AddressService addressServiceMock;

    @Mock
    protected OrganisationService organisationServiceMock;

    @Mock
    protected UserService userServiceMock;

    @Mock
    protected CostRepository costRepositoryMock;

    @Mock
    protected AssessmentRepository assessmentRepositoryMock;

    @Mock
    protected RegistrationService registrationServiceMock;

    @Mock
    protected IdentityProviderService idpServiceMock;

    @Mock
    protected TokenService tokenServiceMock;

    @Mock
    protected TokenRepository tokenRepositoryMock;

    @Before
    public void setupMockInjection() {
        // Process mock annotations
        MockitoAnnotations.initMocks(this);
    }
}