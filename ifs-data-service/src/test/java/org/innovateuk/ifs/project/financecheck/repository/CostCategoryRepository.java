package org.innovateuk.ifs.project.financecheck.repository;

import org.innovateuk.ifs.project.financecheck.domain.CostCategory;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Repository to help with testing
 */
public interface CostCategoryRepository extends PagingAndSortingRepository<CostCategory, Long> {
}
