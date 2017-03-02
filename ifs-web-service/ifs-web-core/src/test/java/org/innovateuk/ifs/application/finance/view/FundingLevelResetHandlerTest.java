package org.innovateuk.ifs.application.finance.view;

import org.innovateuk.ifs.application.builder.QuestionResourceBuilder;
import org.innovateuk.ifs.application.builder.SectionResourceBuilder;
import org.innovateuk.ifs.application.finance.service.FinanceRowService;
import org.innovateuk.ifs.application.finance.service.FinanceService;
import org.innovateuk.ifs.application.resource.QuestionResource;
import org.innovateuk.ifs.application.resource.SectionResource;
import org.innovateuk.ifs.application.resource.SectionType;
import org.innovateuk.ifs.application.service.QuestionService;
import org.innovateuk.ifs.application.service.SectionService;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.finance.builder.ApplicationFinanceResourceBuilder;
import org.innovateuk.ifs.finance.resource.ApplicationFinanceResource;
import org.innovateuk.ifs.finance.resource.cost.FinanceRowItem;
import org.innovateuk.ifs.form.resource.FormInputType;
import org.innovateuk.ifs.user.builder.ProcessRoleResourceBuilder;
import org.innovateuk.ifs.user.builder.UserResourceBuilder;
import org.innovateuk.ifs.user.resource.ProcessRoleResource;
import org.innovateuk.ifs.user.resource.UserResource;
import org.innovateuk.ifs.user.service.ProcessRoleService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.*;

/**
 * Tests for FundingLevelResetHandler
 */
@RunWith(MockitoJUnitRunner.class)
public class FundingLevelResetHandlerTest {

    @InjectMocks
    private FundingLevelResetHandler target;

    @Mock
    private ProcessRoleService processRoleService;

    @Mock
    private SectionService sectionService;

    @Mock
    private QuestionService questionService;

    @Mock
    private FinanceRowService financeRowService;

    @Mock
    private FinanceService financeService;

    @Test
    public void tetsResetFundingAndMarkAsIncomplete() throws ExecutionException, InterruptedException {

        Long applicationId = 1L;
        Long competitionId = 3L;
        Long userId = 2L;

        Future<List<ProcessRoleResource>> future = mock(Future.class);
        ApplicationFinanceResource applicationFinanceResource = ApplicationFinanceResourceBuilder.newApplicationFinanceResource()
                .withApplication(applicationId).withGrantClaimPercentage(20).build();
        UserResource user = UserResourceBuilder.newUserResource().withId(userId).build();
        List<ProcessRoleResource> processRoles = ProcessRoleResourceBuilder.newProcessRoleResource().withApplication(applicationId).withUser(user).build(1);
        List<SectionResource> sectionResources = SectionResourceBuilder.newSectionResource().build(1);
        QuestionResource questionResource = QuestionResourceBuilder.newQuestionResource().build();

        when(future.get()).thenReturn(processRoles);
        when(processRoleService.findAssignableProcessRoles(1L)).thenReturn(future);
        when(sectionService.getSectionsForCompetitionByType(competitionId, SectionType.FUNDING_FINANCES)).thenReturn(sectionResources);
        when(questionService.getQuestionByCompetitionIdAndFormInputType(competitionId, FormInputType.FINANCE)).thenReturn(ServiceResult.serviceSuccess(questionResource));

        target.resetFundingAndMarkAsIncomplete(applicationFinanceResource, competitionId, userId);

        InOrder inOrder = Mockito.inOrder(sectionService, financeRowService);

        inOrder.verify(sectionService).markAsInComplete(sectionResources.get(0).getId(),
                applicationId, processRoles.get(0).getId());
        inOrder.verify(financeRowService).add(eq(applicationFinanceResource.getId()),
                eq(questionResource.getId()), isA(FinanceRowItem.class));
    }

    @Test
    public void resetFundingLevelAndMarkAsIncompleteForAllCollaborators() throws ExecutionException, InterruptedException {

        Long applicationId = 1L;
        Long competitionId = 3L;
        Long userId = 2L;

        Future<List<ProcessRoleResource>> future = mock(Future.class);
        UserResource user = UserResourceBuilder.newUserResource().withId(userId).build();
        List<ProcessRoleResource> processRoles = ProcessRoleResourceBuilder.newProcessRoleResource().withApplication(applicationId).withUser(user).build(2);
        List<SectionResource> sectionResources = SectionResourceBuilder.newSectionResource().build(1);
        QuestionResource questionResource = QuestionResourceBuilder.newQuestionResource().build();
        List<ApplicationFinanceResource> applicationFinanceResources = ApplicationFinanceResourceBuilder.newApplicationFinanceResource()
        .withApplication(applicationId).withGrantClaimPercentage(20).build(3);

        when(future.get()).thenReturn(processRoles);
        when(processRoleService.findAssignableProcessRoles(1L)).thenReturn(future);
        when(sectionService.getSectionsForCompetitionByType(competitionId, SectionType.FUNDING_FINANCES)).thenReturn(sectionResources);
        when(questionService.getQuestionByCompetitionIdAndFormInputType(competitionId, FormInputType.FINANCE)).thenReturn(ServiceResult.serviceSuccess(questionResource));
        when(financeService.getApplicationFinanceDetails(applicationId)).thenReturn(applicationFinanceResources);

        target.resetFundingLevelAndMarkAsIncompleteForAllCollaborators(competitionId, applicationId);

        InOrder inOrder = Mockito.inOrder(sectionService, financeService, financeRowService);

        inOrder.verify(sectionService).markAsInComplete(sectionResources.get(0).getId(),applicationId, processRoles.get(0).getId());
        inOrder.verify(sectionService).markAsInComplete(sectionResources.get(0).getId(),applicationId, processRoles.get(1).getId());

        inOrder.verify(financeService, calls(1)).getApplicationFinanceDetails(applicationId);
        inOrder.verify(financeRowService, calls(3)).add(isA(Long.class),
                eq(questionResource.getId()), isA(FinanceRowItem.class));
    }
}