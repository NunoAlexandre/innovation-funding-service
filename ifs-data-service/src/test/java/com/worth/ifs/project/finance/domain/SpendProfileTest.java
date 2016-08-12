package com.worth.ifs.project.finance.domain;

import com.worth.ifs.project.domain.Project;
import com.worth.ifs.user.domain.Organisation;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;

import static com.worth.ifs.BaseBuilderAmendFunctions.name;
import static com.worth.ifs.project.builder.ProjectBuilder.newProject;
import static com.worth.ifs.user.builder.OrganisationBuilder.newOrganisation;
import static com.worth.ifs.util.CollectionFunctions.simpleMap;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class SpendProfileTest {

    @Test
    public void testCreateSpendProfile() {

        CostCategory cat1 = new CostCategory("cat1");
        CostCategory cat2 = new CostCategory("cat2");
        CostCategoryGroup costCategoryGroup = new CostCategoryGroup("My group", asList(cat1, cat2));
        CostCategoryType costCategoryType = new CostCategoryType("My type", costCategoryGroup);

        List<Cost> eligibleCosts = asList(new Cost("1.23"), new Cost("4.56"));
        List<Cost> spendProfileCosts = asList(new Cost("7.89"), new Cost("10.11"));

        Organisation organisation = newOrganisation().withName("My Org").build();
        Project project = newProject().with(name("My Proj")).build();

        SpendProfile spendProfile = new SpendProfile(organisation, project, costCategoryType, eligibleCosts, spendProfileCosts);

        assertEquals(organisation, spendProfile.getOrganisation());
        assertEquals(project, spendProfile.getProject());
        assertEquals(costCategoryType, spendProfile.getCostCategoryType());

        assertEquals("Eligible costs for Partner Organisation", spendProfile.getEligibleCosts().getDescription());
        assertEquals(asList(new BigDecimal("1.23"), new BigDecimal("4.56")),
                simpleMap(spendProfile.getEligibleCosts().getCosts(), Cost::getValue));
        spendProfile.getEligibleCosts().getCosts().forEach(c -> assertEquals(spendProfile.getEligibleCosts(), c.getCostGroup().get()));

        assertEquals("Spend Profile figures for Partner Organisation", spendProfile.getSpendProfileFigures().getDescription());
        assertEquals(asList(new BigDecimal("7.89"), new BigDecimal("10.11")),
                simpleMap(spendProfile.getSpendProfileFigures().getCosts(), Cost::getValue));
        spendProfile.getSpendProfileFigures().getCosts().forEach(c -> assertEquals(spendProfile.getSpendProfileFigures(), c.getCostGroup().get()));
    }
}
