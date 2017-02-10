package org.innovateuk.ifs.category.transactional;

import org.innovateuk.ifs.category.resource.InnovationAreaResource;
import org.innovateuk.ifs.category.resource.InnovationSectorResource;
import org.innovateuk.ifs.category.resource.ResearchCategoryResource;
import org.innovateuk.ifs.commons.security.SecuredBySpring;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

public interface CategoryService {
    @SecuredBySpring(value = "TODO", description = "TODO")
    @PreAuthorize("hasAnyAuthority('comp_admin', 'project_finance', 'assessor', 'system_registrar')")
    ServiceResult<List<InnovationAreaResource>> getInnovationAreas();

    @SecuredBySpring(value = "TODO", description = "TODO")
    @PreAuthorize("hasAnyAuthority('comp_admin', 'project_finance', 'assessor')")
    ServiceResult<List<InnovationSectorResource>> getInnovationSectors();

    @SecuredBySpring(value = "TODO", description = "TODO")
    @PreAuthorize("hasAnyAuthority('comp_admin', 'project_finance', 'assessor', 'applicant')")
    ServiceResult<List<ResearchCategoryResource>> getResearchCategories();

    @SecuredBySpring(value = "TODO", description = "TODO")
    @PreAuthorize("hasAnyAuthority('comp_admin', 'project_finance')")
    ServiceResult<List<InnovationAreaResource>> getInnovationAreasBySector(long sectorId);
}
