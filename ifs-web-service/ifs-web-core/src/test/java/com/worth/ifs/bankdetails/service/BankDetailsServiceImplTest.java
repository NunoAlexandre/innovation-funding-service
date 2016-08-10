package com.worth.ifs.bankdetails.service;

import com.worth.ifs.bankdetails.BankDetailsService;
import com.worth.ifs.bankdetails.BankDetailsServiceImpl;
import com.worth.ifs.bankdetails.resource.BankDetailsResource;
import com.worth.ifs.commons.service.ServiceResult;
import com.worth.ifs.project.resource.ProjectResource;
import com.worth.ifs.user.resource.OrganisationResource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.worth.ifs.bankdetails.builder.BankDetailsResourceBuilder.newBankDetailsResource;
import static com.worth.ifs.commons.rest.RestResult.restSuccess;
import static com.worth.ifs.project.builder.ProjectResourceBuilder.newProjectResource;
import static com.worth.ifs.user.builder.OrganisationResourceBuilder.newOrganisationResource;
import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BankDetailsServiceImplTest {

    private BankDetailsService service;

    @Mock
    private BankDetailsRestService bankDetailsRestService;

    private ProjectResource projectResource;
    private BankDetailsResource bankDetailsResource;
    private OrganisationResource organisationResource;

    @Before
    public void setUp(){
        service = new BankDetailsServiceImpl(bankDetailsRestService);
        projectResource = newProjectResource().build();
        bankDetailsResource = newBankDetailsResource().build();
        organisationResource = newOrganisationResource().build();
    }

    @Test
    public void testGetById() {
        when(bankDetailsRestService.getByProjectIdAndBankDetailsId(projectResource.getId(), bankDetailsResource.getId())).thenReturn(restSuccess(bankDetailsResource));

        BankDetailsResource returnedBankDetailsResource = service.getByProjectIdAndBankDetailsId(projectResource.getId(), bankDetailsResource.getId());

        assertEquals(bankDetailsResource, returnedBankDetailsResource);

        verify(bankDetailsRestService).getByProjectIdAndBankDetailsId(projectResource.getId(), bankDetailsResource.getId());
    }

    @Test
    public void updateBankDetails() {
        when(bankDetailsRestService.updateBankDetails(projectResource.getId(), bankDetailsResource)).thenReturn(restSuccess());

        ServiceResult<Void> result = service.updateBankDetails(projectResource.getId(), bankDetailsResource);

        assertTrue(result.isSuccess());

        verify(bankDetailsRestService).updateBankDetails(projectResource.getId(), bankDetailsResource);
    }

    @Test
    public void getBankDetailsByProjectAndOrganisation() {
        when(bankDetailsRestService.getBankDetailsByProjectAndOrganisation(projectResource.getId(), organisationResource.getId())).thenReturn(restSuccess(bankDetailsResource));

        BankDetailsResource returnedBankDetailsResource = service.getBankDetailsByProjectAndOrganisation(projectResource.getId(), organisationResource.getId());

        assertEquals(bankDetailsResource, returnedBankDetailsResource);

        verify(bankDetailsRestService).getBankDetailsByProjectAndOrganisation(projectResource.getId(), organisationResource.getId());
    }
}
