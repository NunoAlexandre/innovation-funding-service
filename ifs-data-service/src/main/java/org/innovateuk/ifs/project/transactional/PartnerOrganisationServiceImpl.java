package org.innovateuk.ifs.project.transactional;

import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.project.domain.PartnerOrganisation;
import org.innovateuk.ifs.project.mapper.PartnerOrganisationMapper;
import org.innovateuk.ifs.project.repository.PartnerOrganisationRepository;
import org.innovateuk.ifs.project.resource.PartnerOrganisationResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.innovateuk.ifs.commons.error.CommonErrors.notFoundError;
import static org.innovateuk.ifs.util.CollectionFunctions.simpleMap;
import static org.innovateuk.ifs.util.EntityLookupCallbacks.find;
import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

@Service
public class PartnerOrganisationServiceImpl implements PartnerOrganisationService {
    @Autowired
    protected PartnerOrganisationRepository partnerOrganisationRepository;

    @Autowired
    protected PartnerOrganisationMapper partnerOrganisationMapper;

    @Override
    public ServiceResult<List<PartnerOrganisationResource>> getProjectPartnerOrganisations(Long projectId) {
        return find(partnerOrganisationRepository.findByProjectId(projectId),
                notFoundError(PartnerOrganisation.class, id)).
                andOnSuccessReturn(lst -> simpleMap(lst, partnerOrganisationMapper::mapToResource));
    }
}
