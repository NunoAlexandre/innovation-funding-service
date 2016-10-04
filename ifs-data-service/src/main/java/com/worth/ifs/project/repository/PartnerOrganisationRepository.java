package com.worth.ifs.project.repository;

import com.worth.ifs.project.domain.PartnerOrganisation;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface PartnerOrganisationRepository extends PagingAndSortingRepository<PartnerOrganisation, Long> {

    PartnerOrganisation findOneByProjectIdAndOrganisationId(Long projectId, Long organisationId);
}
