package com.worth.ifs.invite.transactional;

import com.google.common.collect.Sets;
import com.worth.ifs.application.domain.Application;
import com.worth.ifs.commons.error.Error;
import com.worth.ifs.commons.service.BaseEitherBackedResult;
import com.worth.ifs.commons.service.ServiceFailure;
import com.worth.ifs.commons.service.ServiceResult;
import com.worth.ifs.invite.constant.InviteStatusConstants;
import com.worth.ifs.invite.domain.Invite;
import com.worth.ifs.invite.domain.InviteOrganisation;
import com.worth.ifs.invite.mapper.InviteMapper;
import com.worth.ifs.invite.mapper.InviteOrganisationMapper;
import com.worth.ifs.invite.repository.InviteOrganisationRepository;
import com.worth.ifs.invite.repository.InviteRepository;
import com.worth.ifs.invite.resource.InviteOrganisationResource;
import com.worth.ifs.invite.resource.InviteResource;
import com.worth.ifs.invite.resource.InviteResultsResource;
import com.worth.ifs.notifications.resource.*;
import com.worth.ifs.notifications.service.NotificationService;
import com.worth.ifs.transactional.BaseTransactionalService;
import com.worth.ifs.user.domain.Organisation;
import com.worth.ifs.user.domain.ProcessRole;
import com.worth.ifs.user.domain.Role;
import com.worth.ifs.user.domain.User;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.validator.HibernateValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static com.worth.ifs.commons.error.CommonErrors.*;
import static com.worth.ifs.commons.service.ServiceResult.serviceFailure;
import static com.worth.ifs.commons.service.ServiceResult.serviceSuccess;
import static com.worth.ifs.notifications.resource.NotificationMedium.EMAIL;
import static com.worth.ifs.util.CollectionFunctions.simpleMap;
import static com.worth.ifs.util.EntityLookupCallbacks.find;
import static java.util.Collections.singletonList;

@Service
public class InviteServiceImpl extends BaseTransactionalService implements InviteService {

    private static final Log LOG = LogFactory.getLog(InviteServiceImpl.class);
    enum Notifications {
        INVITE_COLLABORATOR
    }

    @Value("${ifs.web.baseURL}")
    private String webBaseUrl;

    @Autowired
    private InviteMapper inviteMapper;
    @Autowired
    private InviteOrganisationMapper inviteOrganisationMapper;

    @Autowired
    private InviteRepository inviteRepository;

    @Autowired
    private InviteOrganisationRepository inviteOrganisationRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private SystemNotificationSource systemNotificationSource;

    LocalValidatorFactoryBean validator;

    public InviteServiceImpl() {
        validator = new LocalValidatorFactoryBean();
        validator.setProviderClass(HibernateValidator.class);
        validator.afterPropertiesSet();
    }

    @Override
    public List<ServiceResult<Notification>> inviteCollaborators(String baseUrl, List<Invite> invites) {
        List<ServiceResult<Notification>> results = new ArrayList<>();
        invites.stream().forEach(invite -> {
            Errors errors = new BeanPropertyBindingResult(invite, invite.getClass().getName());
            validator.validate(invite, errors);

            if(errors.hasErrors()){
                errors.getFieldErrors().stream().peek(e -> LOG.debug(String.format("Field error: %s ", e.getField())));
                ServiceResult<Notification> inviteResult = serviceFailure(internalServerErrorError("Validation errors"));

                results.add(inviteResult);
                inviteResult.handleSuccessOrFailure(
                        failure -> handleInviteError(invite, failure),
                        success -> handleInviteSuccess(invite)
                );
            }else{
                if(invite.getId()==null){
                    inviteRepository.save(invite);
                }
                invite.generateHash();
                inviteRepository.save(invite);

                ServiceResult<Notification> inviteResult = inviteCollaboratorToApplication(baseUrl, invite);

                results.add(inviteResult);
                inviteResult.handleSuccessOrFailure(
                        failure -> handleInviteError(invite, failure),
                        success -> handleInviteSuccess(invite)
                );
            }
        });
        return results;
    }

    private boolean handleInviteSuccess(Invite i) {
        i.setStatus(InviteStatusConstants.SEND);
        inviteRepository.save(i);
        return true;
    }

    private boolean handleInviteError(Invite i, ServiceFailure failure) {
        LOG.error(String.format("Invite failed %s , %s (error count: %s)", i.getId(), i.getEmail(), failure.getErrors().size()));
        return true;
    }

    private String getInviteUrl(String baseUrl, Invite invite) {
        return String.format("%s/accept-invite/%s", baseUrl, invite.getHash());
    }

    @Override
    public ServiceResult<Notification> inviteCollaboratorToApplication(String baseUrl, Invite invite) {
        NotificationSource from = systemNotificationSource;
        NotificationTarget to = new ExternalUserNotificationTarget(invite.getName(), invite.getEmail());

        Map<String, Object> notificationArguments = new HashMap<>();
        if(StringUtils.isNotEmpty(invite.getApplication().getName())){
            notificationArguments.put("applicationName", invite.getApplication().getName());
        }
        notificationArguments.put("competitionName", invite.getApplication().getCompetition().getName());
        notificationArguments.put("inviteUrl", getInviteUrl(baseUrl, invite));
        if(invite.getInviteOrganisation().getOrganisation() != null){
            notificationArguments.put("inviteOrganisationName", invite.getInviteOrganisation().getOrganisation().getName());
        }else{
            notificationArguments.put("inviteOrganisationName", invite.getInviteOrganisation().getOrganisationName());
        }
        notificationArguments.put("leadOrganisation", invite.getApplication().getLeadOrganisation().getName());
        notificationArguments.put("leadApplicant", invite.getApplication().getLeadApplicant().getName());
        notificationArguments.put("leadApplicantEmail", invite.getApplication().getLeadApplicant().getEmail());

        Notification notification = new Notification(from, singletonList(to), Notifications.INVITE_COLLABORATOR, notificationArguments);
        return notificationService.sendNotification(notification, EMAIL);
    }

    @Override
    public ServiceResult<Invite> findOne(Long id) {
        return find(inviteRepository.findOne(id), notFoundError(Invite.class, id));
    }

    @Override
    public ServiceResult<InviteResultsResource> createApplicationInvites(InviteOrganisationResource inviteOrganisationResource) {

        if (!inviteOrganisationResourceIsValid(inviteOrganisationResource)) {
            return serviceFailure(badRequestError("The Invite is not valid"));
        }

        return assembleInviteOrganisationFromResource(inviteOrganisationResource).andOnSuccessReturn(newInviteOrganisation -> {
            List<Invite> newInvites = assembleInvitesFromInviteOrganisationResource(inviteOrganisationResource, newInviteOrganisation);
            inviteOrganisationRepository.save(newInviteOrganisation);
            Iterable<Invite> savedInvites = inviteRepository.save(newInvites);
            InviteResultsResource sentInvites = sendInvites(newArrayList(savedInvites));
            return sentInvites;
        });
    }

    @Override
    public ServiceResult<InviteOrganisationResource> getInviteOrganisationByHash(String hash) {
        return getByHash(hash).andOnSuccessReturn(invite -> inviteOrganisationMapper.mapToResource(inviteOrganisationRepository.findOne(invite.getInviteOrganisation().getId())));
    }

    @Override
    public ServiceResult<Set<InviteOrganisationResource>> getInvitesByApplication(Long applicationId) {
        return findByApplicationId(applicationId).andOnSuccessReturn(invites -> {

            List<Long> inviteOrganisationIds = invites.stream().map(i -> i.getInviteOrganisation().getId()).collect(Collectors.toList());
            Iterable<InviteOrganisation> inviteOrganisations = inviteOrganisationRepository.findAll(inviteOrganisationIds);
            return Sets.newHashSet(inviteOrganisationMapper.mapToResource(inviteOrganisations));
        });
    }

    @Override
    public ServiceResult<InviteResultsResource> saveInvites(List<InviteResource> inviteResources) {
        List<Invite> invites = simpleMap(inviteResources, invite -> mapInviteResourceToInvite(invite, null));
        inviteRepository.save(invites);
        return serviceSuccess(sendInvites(invites));
    }

    @Override
    public ServiceResult<Void> acceptInvite(String inviteHash, Long userId) {
        LOG.error(String.format("acceptInvite %s => %s ", inviteHash, userId));
        return find(invite(inviteHash), user(userId)).andOnSuccess((invite, user) -> {

            if(invite.getEmail().equals(user.getEmail())){
                invite.setStatus(InviteStatusConstants.ACCEPTED);
                invite = inviteRepository.save(invite);
                initializeInvitee(invite, user);
                return serviceSuccess();
            }
            LOG.error(String.format("Invited emailaddress not the same as the users emailaddress %s => %s ", user.getEmail(), invite.getEmail()));
            Error e = new Error("Invited emailaddress not the same as the users emailaddress", HttpStatus.NOT_ACCEPTABLE);
            return serviceFailure(e);
        });
    }

    private void initializeInvitee(Invite invite, User user) {
        LOG.error("initializeInvitee");
        Application application = invite.getApplication();
        Role role = roleRepository.findByName("collaborator").get(0);
        Organisation organisation = invite.getInviteOrganisation().getOrganisation();
        ProcessRole processRole = new ProcessRole(user, application, role, organisation);
        processRoleRepository.save(processRole);
        LOG.error("initializeInvitee saved");
    }


    @Override
    public ServiceResult<InviteResource> getInviteByHash(String hash) {
        return getByHash(hash).andOnSuccessReturn(inviteMapper::mapToResource);
    }

    protected Supplier<ServiceResult<Invite>> invite(final String hash) {
        return () -> getByHash(hash);
    }

    private ServiceResult<Invite> getByHash(String hash) {
        return find(inviteRepository.getByHash(hash), notFoundError(Invite.class, hash));
    }

    private ServiceResult<List<Invite>> findByApplicationId(Long applicationId) {
        return serviceSuccess(inviteRepository.findByApplicationId(applicationId));
    }

    private InviteResultsResource sendInvites(List<Invite> invites) {
        List<ServiceResult<Notification>> results = inviteCollaborators(webBaseUrl, invites);

        long failures = results.stream().filter(BaseEitherBackedResult::isFailure).count();
        long successes = results.stream().filter(BaseEitherBackedResult::isSuccess).count();
        LOG.info(String.format("Invite sending requests %s Success: %s Failures: %s", invites.size(), successes, failures));

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

    private List<Invite> assembleInvitesFromInviteOrganisationResource(InviteOrganisationResource inviteOrganisationResource, InviteOrganisation newInviteOrganisation) {
        List<Invite> invites = new ArrayList<>();
        inviteOrganisationResource.getInviteResources().forEach(inviteResource ->
                invites.add(mapInviteResourceToInvite(inviteResource, newInviteOrganisation))
        );

        return invites;
    }

    private Invite mapInviteResourceToInvite(InviteResource inviteResource, InviteOrganisation newInviteOrganisation) {
        Application application = applicationRepository.findOne(inviteResource.getApplication());
        if (newInviteOrganisation == null && inviteResource.getInviteOrganisation() != null) {
            newInviteOrganisation = inviteOrganisationRepository.findOne(inviteResource.getInviteOrganisation());
        }
        Invite invite = new Invite(inviteResource.getName(), inviteResource.getEmail(), application, newInviteOrganisation, null, InviteStatusConstants.CREATED);
        if(newInviteOrganisation.getOrganisation()!= null){
            List<InviteOrganisation> existingOrgInvite = inviteOrganisationRepository.findByOrganisationId(newInviteOrganisation.getOrganisation().getId());
            if(!existingOrgInvite.isEmpty()){
                invite.setInviteOrganisation(existingOrgInvite.get(0));
            }
        }

        return invite;
    }

    private boolean inviteOrganisationResourceIsValid(InviteOrganisationResource inviteOrganisationResource) {
        if (!inviteOrganisationResourceNameAndIdAreValid(inviteOrganisationResource)) {
            return false;
        }

        if (!allInviteResourcesAreValid(inviteOrganisationResource)) {
            return false;
        }

        return true;
    }

    private boolean inviteOrganisationResourceNameAndIdAreValid(InviteOrganisationResource inviteOrganisationResource) {
        if ((inviteOrganisationResource.getOrganisationName() == null ||
                inviteOrganisationResource.getOrganisationName().isEmpty())
                &&
                inviteOrganisationResource.getOrganisation() == null) {
            return false;
        } else {
            return true;
        }
    }

    private boolean allInviteResourcesAreValid(InviteOrganisationResource inviteOrganisationResource) {
        if (inviteOrganisationResource.getInviteResources()
                .stream()
                .filter(inviteResource -> !inviteResourceIsValid(inviteResource))
                .count() > 0) {
            return false;
        } else {
            return true;
        }
    }

    private boolean inviteResourceIsValid(InviteResource inviteResource) {

        if (StringUtils.isEmpty(inviteResource.getEmail()) || StringUtils.isEmpty(inviteResource.getName()) || inviteResource.getApplication() == null) {
            return false;
        }

        return true;
    }
}
