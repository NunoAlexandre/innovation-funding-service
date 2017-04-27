package org.innovateuk.ifs.invite.transactional;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.validator.HibernateValidator;
import org.innovateuk.ifs.application.domain.Application;
import org.innovateuk.ifs.application.domain.QuestionStatus;
import org.innovateuk.ifs.application.repository.QuestionStatusRepository;
import org.innovateuk.ifs.commons.error.Error;
import org.innovateuk.ifs.commons.service.BaseEitherBackedResult;
import org.innovateuk.ifs.commons.service.ServiceFailure;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.finance.domain.ApplicationFinance;
import org.innovateuk.ifs.finance.repository.ApplicationFinanceRepository;
import org.innovateuk.ifs.form.domain.FormInputResponse;
import org.innovateuk.ifs.form.repository.FormInputResponseRepository;
import org.innovateuk.ifs.invite.constant.InviteStatus;
import org.innovateuk.ifs.invite.domain.ApplicationInvite;
import org.innovateuk.ifs.invite.domain.InviteOrganisation;
import org.innovateuk.ifs.invite.mapper.ApplicationInviteMapper;
import org.innovateuk.ifs.invite.mapper.InviteOrganisationMapper;
import org.innovateuk.ifs.invite.repository.ApplicationInviteRepository;
import org.innovateuk.ifs.invite.repository.InviteOrganisationRepository;
import org.innovateuk.ifs.invite.resource.ApplicationInviteResource;
import org.innovateuk.ifs.invite.resource.InviteOrganisationResource;
import org.innovateuk.ifs.invite.resource.InviteResultsResource;
import org.innovateuk.ifs.notifications.resource.*;
import org.innovateuk.ifs.notifications.service.NotificationService;
import org.innovateuk.ifs.security.LoggedInUserSupplier;
import org.innovateuk.ifs.transactional.BaseTransactionalService;
import org.innovateuk.ifs.user.domain.Organisation;
import org.innovateuk.ifs.user.domain.ProcessRole;
import org.innovateuk.ifs.user.domain.Role;
import org.innovateuk.ifs.user.domain.User;
import org.innovateuk.ifs.user.mapper.UserMapper;
import org.innovateuk.ifs.user.repository.UserRepository;
import org.innovateuk.ifs.user.resource.UserResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.method.P;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.innovateuk.ifs.commons.error.CommonErrors.internalServerErrorError;
import static org.innovateuk.ifs.commons.error.CommonErrors.notFoundError;
import static org.innovateuk.ifs.commons.error.CommonFailureKeys.PROJECT_INVITE_INVALID;
import static org.innovateuk.ifs.commons.error.Error.fieldError;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceFailure;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.invite.domain.Invite.generateInviteHash;
import static org.innovateuk.ifs.notifications.resource.NotificationMedium.EMAIL;
import static org.innovateuk.ifs.user.resource.UserRoleType.COLLABORATOR;
import static org.innovateuk.ifs.util.CollectionFunctions.forEachWithIndex;
import static org.innovateuk.ifs.util.CollectionFunctions.simpleMap;
import static org.innovateuk.ifs.util.EntityLookupCallbacks.find;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

@Service
public class InviteServiceImpl extends BaseTransactionalService implements InviteService {

    private static final Log LOG = LogFactory.getLog(InviteServiceImpl.class);

    enum Notifications {
        INVITE_COLLABORATOR
    }

    @Value("${ifs.web.baseURL}")
    private String webBaseUrl;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ApplicationInviteMapper applicationInviteMapper;

    @Autowired
    private InviteOrganisationMapper inviteOrganisationMapper;

    @Autowired
    private ApplicationInviteRepository applicationInviteRepository;

    @Autowired
    private QuestionStatusRepository questionStatusRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InviteOrganisationRepository inviteOrganisationRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private SystemNotificationSource systemNotificationSource;

    @Autowired
    private FormInputResponseRepository formInputResponseRepository;

    @Autowired
    private LoggedInUserSupplier loggedInUserSupplier;

    @Autowired
    private ApplicationFinanceRepository applicationFinanceRepository;

    LocalValidatorFactoryBean validator;

    public InviteServiceImpl() {
        validator = new LocalValidatorFactoryBean();
        validator.setProviderClass(HibernateValidator.class);
        validator.afterPropertiesSet();
    }

    @Override
    public List<ServiceResult<Void>> inviteCollaborators(String baseUrl, List<ApplicationInvite> invites) {
        return invites.stream().map(invite -> processCollaboratorInvite(baseUrl, invite)).collect(toList());
    }

    private ServiceResult<Void> processCollaboratorInvite(String baseUrl, ApplicationInvite invite) {
        Errors errors = new BeanPropertyBindingResult(invite, invite.getClass().getName());
        validator.validate(invite, errors);

        if (errors.hasErrors()) {
            errors.getFieldErrors().stream().peek(e -> LOG.debug(format("Field error: %s ", e.getField())));
            return serviceFailure(internalServerErrorError()).andOnFailure(logInviteError(invite));
        } else {
            if (invite.getId() == null) {
                applicationInviteRepository.save(invite);
            }
            invite.setHash(generateInviteHash());
            applicationInviteRepository.save(invite);
            return inviteCollaboratorToApplication(baseUrl, invite).
                    andOnSuccessReturnVoid(() -> handleInviteSuccess(invite)).
                    andOnFailure(logInviteError(invite));
        }
    }

    private void handleInviteSuccess(ApplicationInvite invite) {
        applicationInviteRepository.save(invite.send(loggedInUserSupplier.get(), ZonedDateTime.now()));
    }

    private Consumer<ServiceFailure> logInviteError(ApplicationInvite i) {
        return failure -> LOG.error(format("Invite failed %s , %s (error count: %s)", i.getId(), i.getEmail(), failure.getErrors().size()));
    }

    private String getInviteUrl(String baseUrl, ApplicationInvite invite) {
        return format("%s/accept-invite/%s", baseUrl, invite.getHash());
    }

    private String getCompetitionDetailsUrl(String baseUrl, ApplicationInvite invite) {
        return baseUrl + "/competition/" + invite.getTarget().getCompetition().getId() + "/details";
    }

    @Override
    public ServiceResult<Void> inviteCollaboratorToApplication(String baseUrl, ApplicationInvite invite) {
        User loggedInUser = loggedInUserSupplier.get();
        NotificationSource from = systemNotificationSource;
        NotificationTarget to = new ExternalUserNotificationTarget(invite.getName(), invite.getEmail());

        Map<String, Object> notificationArguments = new HashMap<>();
        if (StringUtils.isNotEmpty(invite.getTarget().getName())) {
            notificationArguments.put("applicationName", invite.getTarget().getName());
        }
        notificationArguments.put("sentByName", loggedInUser.getName());
        notificationArguments.put("competitionName", invite.getTarget().getCompetition().getName());
        notificationArguments.put("competitionUrl", getCompetitionDetailsUrl(baseUrl, invite));
        notificationArguments.put("inviteUrl", getInviteUrl(baseUrl, invite));
        if (invite.getInviteOrganisation().getOrganisation() != null) {
            notificationArguments.put("inviteOrganisationName", invite.getInviteOrganisation().getOrganisation().getName());
        } else {
            notificationArguments.put("inviteOrganisationName", invite.getInviteOrganisation().getOrganisationName());
        }
        ProcessRole leadRole = invite.getTarget().getLeadApplicantProcessRole();
        Organisation organisation = organisationRepository.findOne(leadRole.getOrganisationId());
        notificationArguments.put("leadOrganisation", organisation.getName());
        notificationArguments.put("leadApplicant", invite.getTarget().getLeadApplicant().getName());

        if (invite.getTarget().getLeadApplicant().getTitle() != null) {
            notificationArguments.put("leadApplicantTitle", invite.getTarget().getLeadApplicant().getTitle());
        } else {
            notificationArguments.put("leadApplicantTitle", "");
        }
        notificationArguments.put("leadApplicantEmail", invite.getTarget().getLeadApplicant().getEmail());

        Notification notification = new Notification(from, singletonList(to), Notifications.INVITE_COLLABORATOR, notificationArguments);
        return notificationService.sendNotification(notification, EMAIL);
    }

    @Override
    public ServiceResult<ApplicationInvite> findOne(Long id) {
        return find(applicationInviteRepository.findOne(id), notFoundError(ApplicationInvite.class, id));
    }

    @Override
    public ServiceResult<InviteResultsResource> createApplicationInvites(InviteOrganisationResource inviteOrganisationResource) {
        return validateInviteOrganisationResource(inviteOrganisationResource).andOnSuccess(() ->
                validateUniqueEmails(inviteOrganisationResource.getInviteResources())).andOnSuccess(() ->
                assembleInviteOrganisationFromResource(inviteOrganisationResource).andOnSuccessReturn(inviteOrganisation -> {
                            List<ApplicationInvite> invites = saveInviteOrganisationWithInvites(inviteOrganisation, inviteOrganisationResource.getInviteResources());
                            return sendInvites(invites);
                        }
                ));
    }

    @Override
    public ServiceResult<InviteOrganisationResource> getInviteOrganisationByHash(String hash) {
        return getByHash(hash).andOnSuccessReturn(invite -> inviteOrganisationMapper.mapToResource(inviteOrganisationRepository.findOne(invite.getInviteOrganisation().getId())));
    }

    @Override
    public ServiceResult<List<InviteOrganisationResource>> getInvitesByApplication(Long applicationId) {
        return serviceSuccess(
                simpleMap(
                        inviteOrganisationRepository.findDistinctByInvitesApplicationId(applicationId),
                        inviteOrganisationMapper::mapToResource
                )
        );
    }

    @Override
    public ServiceResult<InviteResultsResource> saveInvites(List<ApplicationInviteResource> inviteResources) {
        return validateUniqueEmails(inviteResources).andOnSuccess(() -> {
            List<ApplicationInvite> invites = simpleMap(inviteResources, invite -> mapInviteResourceToInvite(invite, null));
            applicationInviteRepository.save(invites);
            return serviceSuccess(sendInvites(invites));
        });
    }

    @Override
    public ServiceResult<Void> acceptInvite(String inviteHash, Long userId) {
        return find(invite(inviteHash), user(userId)).andOnSuccess((invite, user) -> {
            if (invite.getEmail().equalsIgnoreCase(user.getEmail())) {
                invite.open();
                List<Organisation> usersOrganisations = organisationRepository.findByUsers(user);
                if (invite.getInviteOrganisation().getOrganisation() == null && !usersOrganisations.isEmpty()) {
                    invite.getInviteOrganisation().setOrganisation(usersOrganisations.get(0));
                }
                invite = applicationInviteRepository.save(invite);
                initializeInvitee(invite, user);

                return serviceSuccess();
            }
            LOG.error(format("Invited emailaddress not the same as the users emailaddress %s => %s ", user.getEmail(), invite.getEmail()));
            Error e = new Error("Invited emailaddress not the same as the users emailaddress", NOT_ACCEPTABLE);
            return serviceFailure(e);
        });
    }

    private void initializeInvitee(ApplicationInvite invite, User user) {
        Application application = invite.getTarget();
        Role role = roleRepository.findOneByName(COLLABORATOR.getName());
        Organisation organisation = invite.getInviteOrganisation().getOrganisation();
        ProcessRole processRole = new ProcessRole(user, application.getId(), role, organisation.getId());
        processRoleRepository.save(processRole);
    }

    private ApplicationInviteResource mapInviteToInviteResource(ApplicationInvite invite) {
        ApplicationInviteResource inviteResource = applicationInviteMapper.mapToResource(invite);
        Organisation organisation = organisationRepository.findOne(inviteResource.getLeadOrganisationId());
        inviteResource.setLeadOrganisation(organisation.getName());
        return inviteResource;
    }

    @Override
    public ServiceResult<ApplicationInviteResource> getInviteByHash(String hash) {
        return getByHash(hash).andOnSuccessReturn(this::mapInviteToInviteResource);
    }

    @Override
    public ServiceResult<Boolean> checkUserExistingByInviteHash(@P("hash") String hash) {
        return getByHash(hash)
                .andOnSuccessReturn(i -> userRepository.findByEmail(i.getEmail()))
                .andOnSuccess(u -> serviceSuccess(u.isPresent()));
    }

    @Override
    public ServiceResult<UserResource> getUserByInviteHash(@P("hash") String hash) {
        return getByHash(hash)
                .andOnSuccessReturn(i -> userRepository.findByEmail(i.getEmail()).map(userMapper::mapToResource))
                .andOnSuccess(u -> u.isPresent() ?
                        serviceSuccess(u.get()) :
                        serviceFailure(notFoundError(UserResource.class)));
    }

    @Override
    public ServiceResult<Void> removeApplicationInvite(long applicationInviteId) {
        return find(applicationInviteMapper.mapIdToDomain(applicationInviteId), notFoundError(ApplicationInvite.class))
                .andOnSuccessReturnVoid(applicationInvite -> {
                    ProcessRole leadApplicantProcessRole = applicationInvite.getTarget().getLeadApplicantProcessRole();
                    Long applicationId = applicationInvite.getTarget().getId();

                    List<ProcessRole> collaboratorProcessRoles = processRoleRepository.findByUserAndApplicationId(
                            applicationInvite.getUser(),
                            applicationInvite.getTarget().getId()
                    );

                    reassignCollaboratorResponsesAndQuestionStatuses(applicationId, leadApplicantProcessRole, collaboratorProcessRoles);

                    processRoleRepository.delete(collaboratorProcessRoles);

                    InviteOrganisation inviteOrganisation = applicationInvite.getInviteOrganisation();

                    if (inviteOrganisation.getInvites().size() < 2) {
                        inviteOrganisationRepository.delete(inviteOrganisation);
                        deleteOrganisationsApplicationData(inviteOrganisation.getOrganisation(), applicationInvite.getTarget());
                    } else {
                        inviteOrganisation.getInvites().remove(applicationInvite);
                        inviteOrganisationRepository.save(inviteOrganisation);
                    }
                });
    }

    private void deleteOrganisationsApplicationData(Organisation organisation, Application application) {
        ApplicationFinance finance = applicationFinanceRepository.findByApplicationIdAndOrganisationId(application.getId(), organisation.getId());
        if (finance != null) {
            applicationFinanceRepository.delete(finance);
        }
    }

    protected Supplier<ServiceResult<ApplicationInvite>> invite(final String hash) {
        return () -> getByHash(hash);
    }

    private ServiceResult<ApplicationInvite> getByHash(String hash) {
        return find(applicationInviteRepository.getByHash(hash), notFoundError(ApplicationInvite.class, hash));
    }

    private InviteResultsResource sendInvites(List<ApplicationInvite> invites) {
        List<ServiceResult<Void>> results = inviteCollaborators(webBaseUrl, invites);

        long failures = results.stream().filter(BaseEitherBackedResult::isFailure).count();
        long successes = results.stream().filter(BaseEitherBackedResult::isSuccess).count();
        LOG.info(format("Invite sending requests %s Success: %s Failures: %s", invites.size(), successes, failures));

        InviteResultsResource resource = new InviteResultsResource();
        resource.setInvitesSendFailure((int) failures);
        resource.setInvitesSendSuccess((int) successes);
        return resource;
    }

    private ServiceResult<InviteOrganisation> assembleInviteOrganisationFromResource(InviteOrganisationResource inviteOrganisationResource) {

        if (inviteOrganisationResource.getOrganisation() != null) {
            return find(organisation(inviteOrganisationResource.getOrganisation())).andOnSuccess(organisation -> {
                InviteOrganisation newInviteOrganisation = new InviteOrganisation(
                        inviteOrganisationResource.getOrganisationName(),
                        organisation,
                        null);
                return serviceSuccess(newInviteOrganisation);
            });
        } else {
            InviteOrganisation newInviteOrganisation = new InviteOrganisation(
                    inviteOrganisationResource.getOrganisationName(),
                    null,
                    null);
            return serviceSuccess(newInviteOrganisation);
        }
    }

    private List<ApplicationInvite> saveInviteOrganisationWithInvites(InviteOrganisation inviteOrganisation, List<ApplicationInviteResource> applicationInviteResources) {
        inviteOrganisationRepository.save(inviteOrganisation);
        return applicationInviteResources.stream().map(inviteResource ->
                applicationInviteRepository.save(mapInviteResourceToInvite(inviteResource, inviteOrganisation))).collect(toList());
    }

    private ApplicationInvite mapInviteResourceToInvite(ApplicationInviteResource inviteResource, InviteOrganisation newInviteOrganisation) {
        Application application = applicationRepository.findOne(inviteResource.getApplication());
        if (newInviteOrganisation == null && inviteResource.getInviteOrganisation() != null) {
            newInviteOrganisation = inviteOrganisationRepository.findOne(inviteResource.getInviteOrganisation());
        }
        return new ApplicationInvite(inviteResource.getName(), inviteResource.getEmail(), application, newInviteOrganisation, null, InviteStatus.CREATED);
    }

    private ServiceResult<Void> validateInviteOrganisationResource(InviteOrganisationResource inviteOrganisationResource) {
        if (inviteOrganisationResource.getOrganisation() != null || StringUtils.isNotBlank(inviteOrganisationResource.getOrganisationName())
                && inviteOrganisationResource.getInviteResources().stream().allMatch(this::applicationInviteResourceIsValid)) {
            return serviceSuccess();
        }
        return serviceFailure(PROJECT_INVITE_INVALID);
    }

    private boolean applicationInviteResourceIsValid(ApplicationInviteResource inviteResource) {
        return inviteResource.getApplication() != null && StringUtils.isNotBlank(inviteResource.getEmail()) && StringUtils.isNotBlank(inviteResource.getName());
    }

    private ServiceResult<Void> validateUniqueEmails(List<ApplicationInviteResource> inviteResources) {
        List<Error> failures = new ArrayList<>();
        long applicationId = inviteResources.get(0).getApplication();
        Set<String> uniqueEmails = getUniqueEmailAddressesForApplication(applicationId);
        forEachWithIndex(inviteResources, (index, invite) -> {
            if (!uniqueEmails.add(invite.getEmail())) {
                failures.add(fieldError(format("applicants[%s].email", index), invite.getEmail(), "email.already.in.invite"));
            }
        });
        return failures.isEmpty() ? serviceSuccess() : serviceFailure(failures);
    }

    private Set<String> getUniqueEmailAddressesForApplication(long applicationId) {
        Set<String> result = getUniqueEmailAddressesForApplicationInvites(applicationId);
        String leadApplicantEmail = getLeadApplicantEmail(applicationId);
        if (leadApplicantEmail != null) {
            result.add(leadApplicantEmail);
        }
        return result;
    }

    private Set<String> getUniqueEmailAddressesForApplicationInvites(long applicationId) {
        List<InviteOrganisationResource> inviteOrganisationResources = getInvitesByApplication(applicationId).getSuccessObject();
        return inviteOrganisationResources.stream().flatMap(inviteOrganisationResource ->
                inviteOrganisationResource.getInviteResources().stream().map(ApplicationInviteResource::getEmail)).collect(Collectors.toSet());
    }

    private String getLeadApplicantEmail(long applicationId) {
        Application application = applicationRepository.findOne(applicationId);
        return application.getLeadApplicant() != null ? application.getLeadApplicant().getEmail() : null;
    }

    private void reassignCollaboratorResponsesAndQuestionStatuses(long applicationId,
                                                                  ProcessRole leadApplicantProcessRole,
                                                                  List<ProcessRole> collaboratorProcessRoles) {
        collaboratorProcessRoles.forEach(collaboratorProcessRole -> {
            List<ProcessRole> organisationRoles = getOrganisationProcessRolesExcludingCollaborator(applicationId, collaboratorProcessRole);

            reassignCollaboratorFormResponses(leadApplicantProcessRole, collaboratorProcessRole, organisationRoles);
            reassignCollaboratorQuestionStatuses(applicationId, leadApplicantProcessRole, collaboratorProcessRole, organisationRoles);
        });
    }

    private List<ProcessRole> getOrganisationProcessRolesExcludingCollaborator(long applicationId, ProcessRole collaboratorProcessRole) {
        List<ProcessRole> organisationRoles = processRoleRepository.findByApplicationIdAndOrganisationId(applicationId, collaboratorProcessRole.getOrganisationId());
        organisationRoles.remove(collaboratorProcessRole);
        return organisationRoles;
    }

    private void reassignCollaboratorFormResponses(ProcessRole leadApplicantProcessRole,
                                                   ProcessRole collaboratorProcessRole,
                                                   List<ProcessRole> organisationRoles) {
        List<FormInputResponse> formInputResponses = formInputResponseRepository.findByUpdatedById(collaboratorProcessRole.getId());

        List<FormInputResponse> unassignableFormInputResponses = new ArrayList<>();

        formInputResponses.forEach(collaboratorResponse -> {
            if (collaboratorResponse.getFormInput().getQuestion().hasMultipleStatuses()) {
                if (organisationRoles.isEmpty()) {
                    unassignableFormInputResponses.add(collaboratorResponse);
                } else {
                    collaboratorResponse.setUpdatedBy(organisationRoles.get(0));
                }
            } else {
                collaboratorResponse.setUpdatedBy(leadApplicantProcessRole);
            }
        });

        formInputResponseRepository.save(formInputResponses);
        formInputResponseRepository.delete(unassignableFormInputResponses);
    }

    private void reassignCollaboratorQuestionStatuses(long applicationId,
                                                      ProcessRole leadApplicantProcessRole,
                                                      ProcessRole collaboratorProcessRole,
                                                      List<ProcessRole> organisationRoles) {
        List<QuestionStatus> questionStatuses = questionStatusRepository.findByApplicationIdAndMarkedAsCompleteByIdOrAssigneeIdOrAssignedById(
                applicationId,
                collaboratorProcessRole.getId(),
                collaboratorProcessRole.getId(),
                collaboratorProcessRole.getId()
        );

        List<QuestionStatus> unassignableQuestionStatuses = new ArrayList<>();

        questionStatuses.forEach(questionStatus -> {
            if (questionStatus.getQuestion().hasMultipleStatuses()) {
                if (organisationRoles.isEmpty()) {
                    unassignableQuestionStatuses.add(questionStatus);
                } else {
                    reassignQuestionStatusRoles(questionStatus, organisationRoles.get(0), leadApplicantProcessRole);
                }
            } else {
                reassignQuestionStatusRoles(questionStatus, leadApplicantProcessRole, leadApplicantProcessRole);
            }
        });

        questionStatusRepository.save(questionStatuses);
        questionStatusRepository.delete(unassignableQuestionStatuses);
    }

    private QuestionStatus reassignQuestionStatusRoles(QuestionStatus questionStatus, ProcessRole reassignTo, ProcessRole leadApplicantRole) {
        if (questionStatus.getAssignee() != null && questionStatus.getAssignedBy() != null) {
            ProcessRole assignee =
                    convertToProcessRoleIfOriginalRoleNotForLeadApplicant(questionStatus.getAssignee(), reassignTo, leadApplicantRole);
            ProcessRole assignedBy =
                    convertToProcessRoleIfOriginalRoleNotForLeadApplicant(questionStatus.getAssignedBy(), reassignTo, leadApplicantRole);

            questionStatus.setAssignee(assignee, assignedBy, ZonedDateTime.now());
        }

        if (questionStatus.getMarkedAsCompleteBy() != null) {
            ProcessRole markedAsCompleteBy =
                    convertToProcessRoleIfOriginalRoleNotForLeadApplicant(questionStatus.getMarkedAsCompleteBy(), reassignTo, leadApplicantRole);

            questionStatus.setMarkedAsCompleteBy(markedAsCompleteBy);
        }

        return questionStatus;
    }

    private ProcessRole convertToProcessRoleIfOriginalRoleNotForLeadApplicant(ProcessRole processRoleFrom,
                                                                              ProcessRole processRoleTo,
                                                                              ProcessRole leadApplicantRole) {
        return Optional.of(processRoleFrom)
                .filter(originalRole -> !originalRole.getId().equals(leadApplicantRole.getId()))
                .map(originalRole -> processRoleTo)
                .orElse(leadApplicantRole);
    }
}
