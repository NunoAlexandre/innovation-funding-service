package com.worth.ifs.project.finance.transactional;

import com.worth.ifs.commons.error.CommonFailureKeys;
import com.worth.ifs.commons.service.ServiceResult;
import com.worth.ifs.project.finance.domain.Cost;
import com.worth.ifs.project.finance.domain.CostGroup;
import com.worth.ifs.project.finance.domain.FinanceCheck;
import com.worth.ifs.project.finance.repository.FinanceCheckProcessRepository;
import com.worth.ifs.project.finance.repository.FinanceCheckRepository;
import com.worth.ifs.project.finance.resource.CostGroupResource;
import com.worth.ifs.project.finance.resource.CostResource;
import com.worth.ifs.project.finance.resource.FinanceCheckResource;
import com.worth.ifs.project.finance.workflow.financechecks.configuration.FinanceCheckWorkflowHandler;
import com.worth.ifs.project.finance.workflow.financechecks.resource.FinanceCheckProcessResource;
import com.worth.ifs.project.resource.ProjectOrganisationCompositeId;
import com.worth.ifs.project.transactional.AbstractProjectServiceImpl;
import com.worth.ifs.user.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static com.worth.ifs.commons.error.CommonErrors.notFoundError;
import static com.worth.ifs.commons.error.CommonFailureKeys.FINANCE_CHECKS_CANNOT_PROGRESS_WORKFLOW;
import static com.worth.ifs.commons.service.ServiceResult.serviceFailure;
import static com.worth.ifs.commons.service.ServiceResult.serviceSuccess;
import static com.worth.ifs.util.CollectionFunctions.simpleMap;
import static com.worth.ifs.util.EntityLookupCallbacks.find;
import static java.util.Collections.emptyList;
import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

/**
 * A transactional service for finance check functionality
 */
@Service
public class FinanceCheckServiceImpl extends AbstractProjectServiceImpl implements FinanceCheckService {

    @Autowired
    private FinanceCheckRepository financeCheckRepository;

    @Autowired
    private FinanceCheckWorkflowHandler financeCheckWorkflowHandler;

    @Autowired
    private FinanceCheckProcessRepository financeCheckProcessRepository;

    @Autowired
    private UserMapper userMapper;

    @Override
    public ServiceResult<FinanceCheckResource> getByProjectAndOrganisation(ProjectOrganisationCompositeId key) {
        return find(financeCheckRepository.findByProjectIdAndOrganisationId(key.getProjectId(), key.getOrganisationId()),
                notFoundError(FinanceCheck.class, id)).
                andOnSuccessReturn(this::mapToResource);
    }

    @Override
    public ServiceResult<Void> save(FinanceCheckResource financeCheckResource) {

        FinanceCheck toSave = mapToDomain(financeCheckResource);
        financeCheckRepository.save(toSave);

        return getCurrentlyLoggedInUser().andOnSuccess(user ->
               getPartnerOrganisation(toSave.getProject().getId(), toSave.getOrganisation().getId()).andOnSuccessReturn(partnerOrganisation ->
               financeCheckWorkflowHandler.financeCheckFiguresEdited(partnerOrganisation, user))).andOnSuccess(workflowResult ->
               workflowResult ? serviceSuccess() : serviceFailure(CommonFailureKeys.FINANCE_CHECKS_CANNOT_PROGRESS_WORKFLOW));
    }

    @Override
    public ServiceResult<FinanceCheckResource> generate(ProjectOrganisationCompositeId key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ServiceResult<Void> approve(Long projectId, Long organisationId) {

        return getCurrentlyLoggedInUser().andOnSuccess(currentUser ->
               getPartnerOrganisation(projectId, organisationId).andOnSuccessReturn(partnerOrg ->
               financeCheckWorkflowHandler.approveFinanceCheckFigures(partnerOrg, currentUser)).
               andOnSuccess(workflowResult -> workflowResult ? serviceSuccess() : serviceFailure(FINANCE_CHECKS_CANNOT_PROGRESS_WORKFLOW)));
    }

    @Override
    public ServiceResult<FinanceCheckProcessResource> getFinanceCheckApprovalStatus(Long projectId, Long organisationId) {

        return getPartnerOrganisation(projectId, organisationId).andOnSuccessReturn(partnerOrganisation ->
               financeCheckProcessRepository.findOneByTargetId(partnerOrganisation.getId())).andOnSuccessReturn(process ->
               new FinanceCheckProcessResource(
                       process.getActivityState(),
                       projectUserMapper.mapToResource(process.getParticipant()),
                       userMapper.mapToResource(process.getInternalParticipant()),
                       LocalDateTime.ofInstant(process.getLastModified().toInstant(), ZoneId.systemDefault()),
                       false));
    }

    private FinanceCheck mapToDomain(FinanceCheckResource financeCheckResource){
        FinanceCheck fc = financeCheckRepository.findByProjectIdAndOrganisationId(financeCheckResource.getProject(), financeCheckResource.getOrganisation());
        for(CostResource cr : financeCheckResource.getCostGroup().getCosts()){
            Optional<Cost> oc = fc.getCostGroup().getCostById(cr.getId());
            if(oc.isPresent()){
                Cost c = oc.get();
                c.setValue(cr.getValue());
            }
        }
        return fc;
    }

    private FinanceCheckResource mapToResource(FinanceCheck fc) {
        FinanceCheckResource financeCheckResource = new FinanceCheckResource();
        financeCheckResource.setId(fc.getId());
        financeCheckResource.setOrganisation(fc.getOrganisation().getId());
        financeCheckResource.setProject(fc.getProject().getId());
        financeCheckResource.setCostGroup(mapCostGroupToResource(fc.getCostGroup()));
        return financeCheckResource;
    }

    private CostGroupResource mapCostGroupToResource(CostGroup costGroup){
        CostGroupResource costGroupResource = new CostGroupResource();
        if(costGroup != null) {
            costGroupResource.setId(costGroup.getId());
            costGroupResource.setDescription(costGroup.getDescription());
            costGroupResource.setCosts(mapCostsToCostResource(costGroup.getCosts()));
        }
        return costGroupResource;
    }

    private List<CostResource> mapCostsToCostResource(List<Cost> costs){
        if(costs == null){
            return emptyList();
        }
        return simpleMap(costs, c -> {
            CostResource cr = new CostResource();
            cr.setId(c.getId());
            cr.setValue(c.getValue());
            return cr;
        });
    }
}
