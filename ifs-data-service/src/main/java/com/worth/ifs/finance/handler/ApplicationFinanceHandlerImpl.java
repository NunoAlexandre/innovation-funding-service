package com.worth.ifs.finance.handler;

import com.worth.ifs.finance.domain.ApplicationFinance;
import com.worth.ifs.finance.domain.ProjectFinance;
import com.worth.ifs.finance.mapper.ApplicationFinanceMapper;
import com.worth.ifs.finance.mapper.ProjectFinanceMapper;
import com.worth.ifs.finance.repository.ApplicationFinanceRepository;
import com.worth.ifs.finance.repository.ProjectFinanceRepository;
import com.worth.ifs.finance.resource.*;
import com.worth.ifs.finance.resource.category.FinanceRowCostCategory;
import com.worth.ifs.finance.resource.cost.FinanceRowType;
import com.worth.ifs.user.domain.Organisation;
import com.worth.ifs.user.repository.OrganisationRepository;
import com.worth.ifs.user.resource.OrganisationTypeEnum;
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
    private ProjectFinanceRepository projectFinanceRepository;

    @Autowired
    private OrganisationRepository organisationRepository;

    @Autowired
    private OrganisationFinanceDelegate organisationFinanceDelegate;

    @Autowired
    private ApplicationFinanceMapper applicationFinanceMapper;

    @Autowired
    private ProjectFinanceMapper projectFinanceMapper;

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
    public ProjectFinanceResource getProjectOrganisationFinances(ProjectFinanceResourceId projectFinanceResourceId) {
        ProjectFinance projectFinance = projectFinanceRepository.findByProjectIdAndOrganisationId(
                projectFinanceResourceId.getProjectId(), projectFinanceResourceId.getOrganisationId());
        ProjectFinanceResource projectFinanceResource = null;

        //TODO: INFUND-5102 This to me seems like a very messy way of building resource object. You don't only need to map the domain object using the mapper, but then also do a bunch of things in setApplicationFinanceDetails.  We should find a better way to handle this.
        if(projectFinance!=null) {
            projectFinanceResource = projectFinanceMapper.mapToResource(projectFinance);
            setProjectFinanceDetails(projectFinanceResource);
        }
        return projectFinanceResource;
    }

    @Override
    public List<ApplicationFinanceResource> getApplicationTotals(Long applicationId) {
        List<ApplicationFinance> applicationFinances = applicationFinanceRepository.findByApplicationId(applicationId);
        List<ApplicationFinanceResource> applicationFinanceResources = new ArrayList<>();

        for(ApplicationFinance applicationFinance : applicationFinances) {
            ApplicationFinanceResource applicationFinanceResource = applicationFinanceMapper.mapToResource(applicationFinance);
            OrganisationFinanceHandler organisationFinanceHandler = organisationFinanceDelegate.getOrganisationFinanceHandler(applicationFinance.getOrganisation().getOrganisationType().getName());
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
                        OrganisationTypeEnum.isResearch(organisationRepository.findOne(f.getOrganisation()).getOrganisationType().getId())
                )
                .map(ApplicationFinanceResource::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal researchParticipation = BigDecimal.ZERO;

        if(totalCosts.compareTo(BigDecimal.ZERO)!=0) {
            researchParticipation = researchCosts.divide(totalCosts, 6, BigDecimal.ROUND_HALF_UP);
        }
        researchParticipation = researchParticipation.multiply(BigDecimal.valueOf(100));
        return researchParticipation.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    @Override
    public List<ProjectFinanceResource> getFinanceChecksTotals(Long projectId) {
        List<ProjectFinance> finances = projectFinanceRepository.findByProjectId(projectId);
        List<ProjectFinanceResource> financeResources = new ArrayList<>();

        for(ProjectFinance finance : finances) {
            ProjectFinanceResource financeResource = projectFinanceMapper.mapToResource(finance);
            OrganisationFinanceHandler organisationFinanceHandler = organisationFinanceDelegate.getOrganisationFinanceHandler(finance.getOrganisation().getOrganisationType().getName());
            EnumMap<FinanceRowType, FinanceRowCostCategory> costs = new EnumMap<>(organisationFinanceHandler.getProjectOrganisationFinanceTotals(financeResource.getId(), finance.getProject().getApplication().getCompetition()));
            financeResource.setFinanceOrganisationDetails(costs);
            financeResources.add(financeResource);
        }
        return financeResources;
    }

    private void setApplicationFinanceDetails(ApplicationFinanceResource applicationFinanceResource) {
        Organisation organisation = organisationRepository.findOne(applicationFinanceResource.getOrganisation());
        OrganisationFinanceHandler organisationFinanceHandler = organisationFinanceDelegate.getOrganisationFinanceHandler(organisation.getOrganisationType().getName());
        Map<FinanceRowType, FinanceRowCostCategory> costs = organisationFinanceHandler.getOrganisationFinances(applicationFinanceResource.getId());
        applicationFinanceResource.setFinanceOrganisationDetails(costs);
    }

    private void setProjectFinanceDetails(ProjectFinanceResource projectFinanceResource) {
        Organisation organisation = organisationRepository.findOne(projectFinanceResource.getOrganisation());
        OrganisationFinanceHandler organisationFinanceHandler = organisationFinanceDelegate.getOrganisationFinanceHandler(organisation.getOrganisationType().getName());
        Map<FinanceRowType, FinanceRowCostCategory> costs = organisationFinanceHandler.getProjectOrganisationFinances(projectFinanceResource.getId());
        projectFinanceResource.setFinanceOrganisationDetails(costs);
    }
}