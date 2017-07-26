package org.innovateuk.ifs.finance.handler;

import org.innovateuk.ifs.finance.domain.ApplicationFinance;
import org.innovateuk.ifs.finance.mapper.ApplicationFinanceMapper;
import org.innovateuk.ifs.finance.repository.ApplicationFinanceRepository;
import org.innovateuk.ifs.finance.resource.ApplicationFinanceResource;
import org.innovateuk.ifs.finance.resource.ApplicationFinanceResourceId;
import org.innovateuk.ifs.finance.resource.category.FinanceRowCostCategory;
import org.innovateuk.ifs.finance.resource.cost.FinanceRowType;
import org.innovateuk.ifs.user.domain.Organisation;
import org.innovateuk.ifs.user.repository.OrganisationRepository;
import org.innovateuk.ifs.user.resource.OrganisationTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * ApplicationFinanceHandlerImpl handles the finance information on application level.
 */
@Service
public class ApplicationFinanceHandlerImpl implements ApplicationFinanceHandler {

    @Autowired
    private ApplicationFinanceRepository applicationFinanceRepository;

    @Autowired
    private OrganisationRepository organisationRepository;

    @Autowired
    private OrganisationFinanceDelegate organisationFinanceDelegate;

    @Autowired
    private ApplicationFinanceMapper applicationFinanceMapper;

    @Override
    public ApplicationFinanceResource getApplicationOrganisationFinances(ApplicationFinanceResourceId applicationFinanceResourceId) {
        ApplicationFinance applicationFinance = applicationFinanceRepository.findByApplicationIdAndOrganisationId(
                applicationFinanceResourceId.getApplicationId(), applicationFinanceResourceId.getOrganisationId());
        ApplicationFinanceResource applicationFinanceResource = null;

        //TODO: INFUND-5102 This to me seems like a very messy way of building resource object. You don't only need to map the domain object using the mapper, but then also do a bunch of things in setApplicationFinanceDetails.  We should find a better way to handle this.
        if(applicationFinance!=null) {
            applicationFinanceResource = applicationFinanceMapper.mapToResource(applicationFinance);
            setApplicationFinanceDetails(applicationFinanceResource);
        }
        return applicationFinanceResource;
    }

    @Override
    public List<ApplicationFinanceResource> getApplicationFinances(Long applicationId) {

        List<ApplicationFinance> applicationFinances = applicationFinanceRepository.findByApplicationId(applicationId);
        List<ApplicationFinanceResource> applicationFinanceResources = new ArrayList<>();

        //TODO: INFUND-5102 This to me seems like a very messy way of building resource object. You don't only need to map the domain object using the mapper, but then also do a bunch of things in setApplicationFinanceDetails.  We should find a better way to handle this.
        for(ApplicationFinance applicationFinance : applicationFinances) {
            ApplicationFinanceResource applicationFinanceResource = applicationFinanceMapper.mapToResource(applicationFinance);
            setApplicationFinanceDetails(applicationFinanceResource);
            applicationFinanceResources.add(applicationFinanceResource);
        }
        return applicationFinanceResources;
    }

    @Override
    public List<ApplicationFinanceResource> getApplicationTotals(Long applicationId) {
        List<ApplicationFinance> applicationFinances = applicationFinanceRepository.findByApplicationId(applicationId);
        List<ApplicationFinanceResource> applicationFinanceResources = new ArrayList<>();

        for(ApplicationFinance applicationFinance : applicationFinances) {
            ApplicationFinanceResource applicationFinanceResource = applicationFinanceMapper.mapToResource(applicationFinance);
            OrganisationFinanceHandler organisationFinanceHandler = organisationFinanceDelegate.getOrganisationFinanceHandler(applicationFinance.getOrganisation().getOrganisationType().getId());
            EnumMap<FinanceRowType, FinanceRowCostCategory> costs = new EnumMap<>(organisationFinanceHandler.getOrganisationFinanceTotals(applicationFinanceResource.getId(), applicationFinance.getApplication().getCompetition()));
            applicationFinanceResource.setFinanceOrganisationDetails(costs);
            applicationFinanceResources.add(applicationFinanceResource);
        }
        return applicationFinanceResources;
    }

    @Override
    public BigDecimal getResearchParticipationPercentage(Long applicationId){
        List<ApplicationFinanceResource> applicationFinanceResources = this.getApplicationTotals(applicationId);

        BigDecimal totalCosts = applicationFinanceResources.stream()
                .map(ApplicationFinanceResource::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal researchCosts = applicationFinanceResources.stream()
                .filter(f ->
                        OrganisationTypeEnum.isResearchParticipationOrganisation(organisationRepository.findOne(f.getOrganisation()).getOrganisationType().getId())
                )
                .map(ApplicationFinanceResource::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal researchParticipation = BigDecimal.ZERO;

        if(totalCosts.compareTo(BigDecimal.ZERO)!=0) {
            researchParticipation = researchCosts.divide(totalCosts, 6, BigDecimal.ROUND_HALF_UP);
        }
        researchParticipation = researchParticipation.multiply(BigDecimal.valueOf(100));
        return researchParticipation.setScale(2, BigDecimal.ROUND_CEILING);
    }

    private void setApplicationFinanceDetails(ApplicationFinanceResource applicationFinanceResource) {
        Organisation organisation = organisationRepository.findOne(applicationFinanceResource.getOrganisation());
        OrganisationFinanceHandler organisationFinanceHandler = organisationFinanceDelegate.getOrganisationFinanceHandler(organisation.getOrganisationType().getId());
        Map<FinanceRowType, FinanceRowCostCategory> costs = organisationFinanceHandler.getOrganisationFinances(applicationFinanceResource.getId());
        applicationFinanceResource.setFinanceOrganisationDetails(costs);
    }
}
