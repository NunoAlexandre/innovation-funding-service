package org.innovateuk.ifs.application.finance.service;

import org.innovateuk.ifs.application.resource.ApplicationResource;
import org.innovateuk.ifs.application.service.ApplicationService;
import org.innovateuk.ifs.application.service.CompetitionService;
import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.commons.rest.ValidationMessages;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.file.resource.FileEntryResource;
import org.innovateuk.ifs.file.service.FileEntryRestService;
import org.innovateuk.ifs.finance.resource.ApplicationFinanceResource;
import org.innovateuk.ifs.finance.resource.ProjectFinanceResource;
import org.innovateuk.ifs.finance.service.ApplicationFinanceRestService;
import org.innovateuk.ifs.finance.service.FinanceRowRestService;
import org.innovateuk.ifs.project.ProjectService;
import org.innovateuk.ifs.project.finance.service.ProjectFinanceRestService;
import org.innovateuk.ifs.user.resource.ProcessRoleResource;
import org.innovateuk.ifs.user.service.UserRestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * {@code FinanceServiceImpl} implements {@link FinanceService} handles the finances for each of the organisations.
 */
@Service
public class FinanceServiceImpl implements FinanceService {

    @Autowired
    private UserRestService userRestService;

    @Autowired
    private FinanceRowRestService financeRowRestService;

    @Autowired
    private ApplicationFinanceRestService applicationFinanceRestService;

    @Autowired
    private ProjectFinanceRestService projectFinanceRestService;

    @Autowired
    private FileEntryRestService fileEntryRestService;

    @Autowired
    private CompetitionService competitionService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private ProjectService projectService;

    @Override
    public ApplicationFinanceResource addApplicationFinance(Long userId, Long applicationId) {
        ProcessRoleResource processRole = userRestService.findProcessRole(userId, applicationId).getSuccessObjectOrThrowException();

        ApplicationResource applicationResource = applicationService.getById(applicationId);
        CompetitionResource competitionResource = competitionService.getById(applicationResource.getCompetition());

        if(processRole.getOrganisationId()!=null && competitionResource.isOpen()) {
            return applicationFinanceRestService.addApplicationFinanceForOrganisation(applicationId, processRole.getOrganisationId()).getSuccessObjectOrThrowException();
        }
        return null;
    }

    @Override
    public ApplicationFinanceResource getApplicationFinance(Long userId, Long applicationId) {
        ProcessRoleResource userApplicationRole = userRestService.findProcessRole(userId, applicationId).getSuccessObjectOrThrowException();
        return applicationFinanceRestService.getApplicationFinance(applicationId, userApplicationRole.getOrganisationId()).getSuccessObjectOrThrowException();
    }

    @Override
    public ApplicationFinanceResource getApplicationFinanceByApplicationIdAndOrganisationId(Long applicationId, Long organisationId) {
        return applicationFinanceRestService.getApplicationFinance(applicationId, organisationId).getSuccessObjectOrThrowException();
    }

    @Override
    public ApplicationFinanceResource getApplicationFinanceDetails(Long userId, Long applicationId) {
        ProcessRoleResource userApplicationRole = userRestService.findProcessRole(userId, applicationId).getSuccessObjectOrThrowException();
        return getApplicationFinanceDetails(userId, applicationId, userApplicationRole.getOrganisationId());
    }

    @Override
    public ApplicationFinanceResource getApplicationFinanceDetails(Long userId, Long applicationId, Long organisationId) {
        return applicationFinanceRestService.getFinanceDetails(applicationId, organisationId).getSuccessObjectOrThrowException();
    }

    @Override
    public List<ApplicationFinanceResource> getApplicationFinanceDetails(Long applicationId) {
        return applicationFinanceRestService.getApplicationFinances(applicationId).getSuccessObjectOrThrowException();
    }

    @Override
    public List<ApplicationFinanceResource> getApplicationFinanceTotals(Long applicationId) {
        return applicationFinanceRestService.getFinanceTotals(applicationId).handleSuccessOrFailure(
                failure -> Collections.<ApplicationFinanceResource> emptyList(),
                success -> success
        );
    }

    @Override
    public List<ProjectFinanceResource> getProjectFinanceTotals(Long projectId){
        return projectFinanceRestService.getFinanceTotals(projectId).handleSuccessOrFailure(
                failure -> Collections.<ProjectFinanceResource> emptyList(),
                success -> success
        );
    }

    @Override
    public ProjectFinanceResource addProjectFinance(Long projectId, Long organisationId) {
        return projectFinanceRestService.addProjectFinanceForOrganisation(projectId, organisationId).getSuccessObjectOrThrowException();
    }

    @Override
    public ProjectFinanceResource getProjectFinance(Long projectId, Long organisationId){
        return projectFinanceRestService.getProjectFinance(projectId, organisationId).getSuccessObjectOrThrowException();
    }

    @Override
    public ValidationMessages addCost(Long applicationFinanceId, Long questionId) {
        return financeRowRestService.add(applicationFinanceId, questionId, null).getSuccessObjectOrThrowException();
    }

    @Override
    public RestResult<FileEntryResource> addFinanceDocument(Long applicationFinanceId, String contentType, long contentLength, String originalFilename, byte[] file) {
        return applicationFinanceRestService.addFinanceDocument(applicationFinanceId, contentType, contentLength, originalFilename, file);
    }

    @Override
    public RestResult<Void> removeFinanceDocument(Long applicationFinanceId) {
        return applicationFinanceRestService.removeFinanceDocument(applicationFinanceId);
    }

    @Override
    public RestResult<FileEntryResource> getFinanceEntry(Long applicationFinanceFileEntryId) {
        return fileEntryRestService.findOne(applicationFinanceFileEntryId);
    }

    @Override
    public RestResult<FileEntryResource> getFinanceEntryByApplicationFinanceId(Long applicationFinanceId) {
        return applicationFinanceRestService.getFileDetails(applicationFinanceId);
    }

    @Override
    public RestResult<ByteArrayResource> getFinanceDocumentByApplicationFinance(Long applicationFinanceId) {
        return applicationFinanceRestService.getFile(applicationFinanceId);
    }
}
