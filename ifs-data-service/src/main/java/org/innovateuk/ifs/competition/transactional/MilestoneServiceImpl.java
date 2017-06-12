package org.innovateuk.ifs.competition.transactional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.innovateuk.ifs.commons.error.Error;
import org.innovateuk.ifs.commons.rest.ValidationMessages;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.competition.domain.Competition;
import org.innovateuk.ifs.competition.domain.Milestone;
import org.innovateuk.ifs.competition.mapper.MilestoneMapper;
import org.innovateuk.ifs.competition.repository.CompetitionRepository;
import org.innovateuk.ifs.competition.repository.MilestoneRepository;
import org.innovateuk.ifs.competition.resource.MilestoneResource;
import org.innovateuk.ifs.competition.resource.MilestoneType;
import org.innovateuk.ifs.transactional.BaseTransactionalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Comparator.comparing;
import static org.innovateuk.ifs.commons.error.CommonErrors.notFoundError;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceFailure;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.util.CollectionFunctions.simpleFilter;
import static org.innovateuk.ifs.util.EntityLookupCallbacks.find;

/**
 * Service for operations around the usage and processing of Milestones
 */
@Service
public class MilestoneServiceImpl extends BaseTransactionalService implements MilestoneService {
    private static final Log LOG = LogFactory.getLog(MilestoneServiceImpl.class);
    private static final List<MilestoneType> PUBLIC_MILESTONES = asList(MilestoneType.OPEN_DATE, MilestoneType.NOTIFICATIONS, MilestoneType.SUBMISSION_DATE);
    @Autowired
    private MilestoneRepository milestoneRepository;

    @Autowired
    private CompetitionRepository competitionRepository;

    @Autowired
    private MilestoneMapper milestoneMapper;

    @Override
    public ServiceResult<List<MilestoneResource>> getAllPublicMilestonesByCompetitionId(Long id) {
        return serviceSuccess((List<MilestoneResource>)
                milestoneMapper.mapToResource(milestoneRepository
                        .findByCompetitionIdAndTypeIn(id, PUBLIC_MILESTONES)));
    }

    @Override
    public ServiceResult<Boolean> allPublicDatesComplete(Long competitionId) {
        List<Milestone> milestones = milestoneRepository
                .findByCompetitionIdAndTypeIn(competitionId, PUBLIC_MILESTONES);

        return serviceSuccess(milestones.size() == PUBLIC_MILESTONES.size() && milestones.stream().noneMatch(milestone -> milestone.getDate() == null));
    }

    @Override
    public ServiceResult<List<MilestoneResource>> getAllMilestonesByCompetitionId(Long id) {
        return serviceSuccess((List<MilestoneResource>) milestoneMapper.mapToResource(milestoneRepository.findAllByCompetitionId(id)));
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceResult<MilestoneResource> getMilestoneByTypeAndCompetitionId(MilestoneType type, Long id) {
        return find(milestoneRepository.findByTypeAndCompetitionId(type, id), notFoundError(MilestoneResource.class, type, id))
                .andOnSuccess(milestone -> serviceSuccess(milestoneMapper.mapToResource(milestone)));
    }

    @Override
    @Transactional
    public ServiceResult<Void> updateMilestones(List<MilestoneResource> milestones) {
        ValidationMessages messages = validate(milestones);

        if (messages.hasErrors()) {
            return serviceFailure(messages.getErrors());
        }

        milestones.forEach(milestone -> milestoneRepository.save(milestoneMapper.mapToDomain(milestone)));
        return serviceSuccess();

    }

    @Override
    @Transactional
    public ServiceResult<Void> updateMilestone(MilestoneResource milestoneResource) {
        milestoneRepository.save(milestoneMapper.mapToDomain(milestoneResource));
        return serviceSuccess();
    }

    @Override
    @Transactional
    public ServiceResult<MilestoneResource> create(MilestoneType type, Long id) {
        Competition competition = competitionRepository.findById(id);

        // TODO INFUND-6256 remove public default constructor for Milestone
        Milestone milestone = new Milestone();
        milestone.setType(type);
        milestone.setCompetition(competition);
        return serviceSuccess(milestoneMapper.mapToResource(milestoneRepository.save(milestone)));
    }

    private ValidationMessages validate(List<MilestoneResource> milestones) {
        ValidationMessages vm = new ValidationMessages();

        vm.addAll(validateCompetitionIdConsistency(milestones));
        vm.addAll(validateDates(milestones));
        vm.addAll(validateDateOrder(milestones));

        return vm;
    }

    private ValidationMessages validateDates(List<MilestoneResource> milestones) {
        ValidationMessages vm = new ValidationMessages();
        Competition competition = competitionRepository.findById(milestones.get(0).getCompetitionId());

        vm.addAll(validateDateNotNull(milestones));

        if (!competition.isNonIfs()) {
            vm.addAll(validateDateInFuture(milestones));
        }

        return vm;
    }

    private ValidationMessages validateDateInFuture(List<MilestoneResource> milestones) {
        ValidationMessages vm = new ValidationMessages();

        milestones.forEach(m -> {
            if (m.getDate() != null && m.getDate().isBefore(ZonedDateTime.now())) {
                Error error = new Error("error.milestone.pastdate", HttpStatus.BAD_REQUEST);
                vm.addError(error);
            }
        });

        return vm;
    }

    private ValidationMessages validateDateNotNull(List<MilestoneResource> milestones) {
        ValidationMessages vm = new ValidationMessages();

        milestones.forEach(m -> {
            if (m.getDate() == null) {
                Error error = new Error("error.milestone.nulldate", HttpStatus.BAD_REQUEST);
                vm.addError(error);
            }
        });

        return vm;
    }

    private ValidationMessages validateDateOrder(List<MilestoneResource> milestones) {
        ValidationMessages vm = new ValidationMessages();

        milestones.sort(comparing(MilestoneResource::getType));
        // preset milestones must be in the correct order
        List<MilestoneResource> presetMilestones = simpleFilter(milestones, milestoneResource -> milestoneResource.getType().isPresetDate());

        for (int i = 1; i < presetMilestones.size(); i++) {
            MilestoneResource previous = presetMilestones.get(i - 1);
            MilestoneResource current = presetMilestones.get(i);

            if (current.getDate() != null && previous.getDate() != null) {
                if (previous.getDate().isAfter(current.getDate())) {
                    Error error = new Error("error.milestone.nonsequential", HttpStatus.BAD_REQUEST);
                    vm.addError(error);
                }
            }
        }

        return vm;
    }

    private ValidationMessages validateCompetitionIdConsistency(List<MilestoneResource> milestones) {
        ValidationMessages vm = new ValidationMessages();
        Long firstMilestoneCompetitionId = milestones.get(0).getCompetitionId();

        boolean allCompetitionIdsMatch = milestones.stream().allMatch(milestone -> milestone.getCompetitionId().equals(firstMilestoneCompetitionId));

        if (!allCompetitionIdsMatch) {
            Error error = new Error("error.title.status.400", HttpStatus.BAD_REQUEST);
            vm.addError(error);
        }

        return vm;
    }
}
