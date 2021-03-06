package org.innovateuk.ifs.finance.domain;

import org.innovateuk.ifs.application.domain.Application;
import org.innovateuk.ifs.user.domain.Organisation;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ApplicationFinanceTest {
    ApplicationFinance applicationFinance;

    Long id;
    Organisation organisation;
    Application application;

    @Before
    public void setUp() throws Exception {
        id=0L;
        organisation = new Organisation(1L, "Worth Internet Systems");
        application = new Application();
        applicationFinance = new ApplicationFinance(id, application, organisation);
    }

    @Test
    public void constructorsShouldCreateInstancesOnValidInput() throws Exception {
        new ApplicationFinance();
        new ApplicationFinance(application, organisation);
        new ApplicationFinance(1234132434L, application, organisation);
    }

    @Test
    public void applicationFinanceShouldReturnCorrectAttributeValues() throws Exception {
        Assert.assertEquals(applicationFinance.getId(), id);
        Assert.assertEquals(applicationFinance.getOrganisation(), organisation);
        Assert.assertEquals(applicationFinance.getApplication(), application);
    }

    @Test
    public void applicationFinanceShouldReturnCorrectAttributeValuesAfterSetId() throws Exception {
        Long newId = 2L;
        applicationFinance.setId(newId);
        Assert.assertEquals(applicationFinance.getId(), newId);
    }

}
