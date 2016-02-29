package com.worth.ifs.organisation.transactional;

import com.worth.ifs.commons.error.CommonErrors;
import com.worth.ifs.commons.service.ServiceResult;
import com.worth.ifs.organisation.domain.Academic;
import com.worth.ifs.organisation.domain.Address;
import com.worth.ifs.organisation.repository.AcademicRepository;
import com.worth.ifs.organisation.resource.OrganisationSearchResult;
import com.worth.ifs.transactional.BaseTransactionalService;
import com.worth.ifs.user.domain.AddressType;
import com.worth.ifs.user.domain.Organisation;
import com.worth.ifs.user.domain.OrganisationTypeEnum;
import com.worth.ifs.user.domain.ProcessRole;
import com.worth.ifs.user.mapper.OrganisationMapper;
import com.worth.ifs.user.repository.OrganisationRepository;
import com.worth.ifs.user.repository.OrganisationTypeRepository;
import com.worth.ifs.user.resource.OrganisationResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.worth.ifs.commons.error.CommonErrors.notFoundError;
import static com.worth.ifs.commons.service.ServiceResult.serviceFailure;
import static com.worth.ifs.commons.service.ServiceResult.serviceSuccess;
import static com.worth.ifs.util.EntityLookupCallbacks.find;
import static java.util.stream.Collectors.toCollection;

/**
 * Represents operations surrounding the use of Organisations in the system
 */
@Service
public class OrganisationServiceImpl extends BaseTransactionalService implements OrganisationService {

    @Autowired
    private OrganisationRepository organisationRepository;
    @Autowired
    private AcademicRepository academicRepository;

    @Autowired
    private OrganisationTypeRepository organisationTypeRepository;

    @Autowired
    private OrganisationMapper organisationMapper;

    @Override
    public ServiceResult<Set<Organisation>> findByApplicationId(final Long applicationId) {

        List<ProcessRole> roles = processRoleRepository.findByApplicationId(applicationId);
        Set<Organisation> organisations = roles.stream().map(role -> organisationRepository.findByProcessRoles(role)).collect(toCollection(LinkedHashSet::new));
        return serviceSuccess(organisations);
    }

    @Override
    public ServiceResult<Organisation> findById(final Long organisationId) {
        return find(organisationRepository.findOne(organisationId), notFoundError(Organisation.class, organisationId));
    }

    @Override
    public ServiceResult<OrganisationResource> create(final Organisation organisation) {

        if (organisation.getOrganisationType() == null) {
            organisation.setOrganisationType(organisationTypeRepository.findOne(OrganisationTypeEnum.BUSINESS.getOrganisationTypeId()));
        }
        Organisation savedOrganisation = organisationRepository.save(organisation);
        return serviceSuccess(organisationMapper.mapToResource(savedOrganisation));
    }

    // TODO DW - INFUND-1555 - lot of duplication between create() and saveResource()
    @Override
    public ServiceResult<OrganisationResource> saveResource(final OrganisationResource organisationResource) {
        Organisation organisation = organisationMapper.mapToDomain(organisationResource);

        if (organisation.getOrganisationType() == null) {
            organisation.setOrganisationType(organisationTypeRepository.findOne(OrganisationTypeEnum.BUSINESS.getOrganisationTypeId()));
        }
        Organisation savedOrganisation = organisationRepository.save(organisation);
        return serviceSuccess(organisationMapper.mapToResource(savedOrganisation));
    }

    @Override
    public ServiceResult<OrganisationResource> addAddress(final Long organisationId, final AddressType addressType, Address address) {
        return find(organisation(organisationId)).andOnSuccess(organisation -> {
            organisation.addAddress(address, addressType);
            Organisation updatedOrganisation = organisationRepository.save(organisation);
            return serviceSuccess(organisationMapper.mapToResource(updatedOrganisation));
        });
    }


    @Override
    public ServiceResult<List<OrganisationSearchResult>> searchAcademic(final String organisationName, int maxItems) {
        List<OrganisationSearchResult> organisations;
        organisations = academicRepository.findByNameContainingIgnoreCase(organisationName, new PageRequest(0, 10))
                .stream()
                .map(a -> new OrganisationSearchResult(a.getId().toString(), a.getName()))
                .collect(Collectors.toList());

        ServiceResult organisationResults;
        if (organisations.isEmpty()) {
            organisationResults = serviceFailure(CommonErrors.notFoundError(Academic.class, organisationName));
        } else {
            organisationResults = serviceSuccess(organisations);
        }
        return organisationResults;
    }

    @Override
    public ServiceResult<OrganisationSearchResult> getSearchOrganisation(final Long searchOrganisationId) {
        Academic academic = academicRepository.findById(searchOrganisationId);

        ServiceResult organisationResults;
        if (academic == null) {
            organisationResults = serviceFailure(CommonErrors.notFoundError(Academic.class, searchOrganisationId));
        } else {
            organisationResults = serviceSuccess(new OrganisationSearchResult(academic.getId().toString(), academic.getName()));
        }
        return organisationResults;
    }
}
