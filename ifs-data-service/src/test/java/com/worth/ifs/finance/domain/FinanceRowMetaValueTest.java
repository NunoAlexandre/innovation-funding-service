package com.worth.ifs.finance.domain;

import com.worth.ifs.application.domain.Question;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;

public class FinanceRowMetaValueTest {
    FinanceRowMetaValue costValue;
    Cost cost;
    FinanceRowMetaField financeRowMetaField;
    String value;
    ApplicationFinance applicationFinance;
    Question question;
    BigDecimal price;

    @Before
    public void setUp() throws Exception {
        price  = new BigDecimal(1000);
        applicationFinance = new ApplicationFinance();
        question = new Question();
        cost = new Cost(1L, "cost key", "cost item", "cost description", 10, price, applicationFinance, question);
        financeRowMetaField = new FinanceRowMetaField(1L, "NVP", "String");
        value = "19000";
        costValue = new FinanceRowMetaValue(cost, financeRowMetaField, value);
    }

    @Test
    public void constructorsShouldCreateInstancesOnValidInput() throws Exception {
        new FinanceRowMetaValue();
        new FinanceRowMetaValue(financeRowMetaField, value);
        new FinanceRowMetaValue(cost, financeRowMetaField, value);
    }

    @Test
    public void costValueShouldReturnCorrectAttributeValues() throws Exception {
        Assert.assertEquals(costValue.getCost(), cost);
        Assert.assertEquals(costValue.getFinanceRowMetaField(), financeRowMetaField);
        Assert.assertEquals(costValue.getValue(), value);
    }

    @Test
    public void costValueShouldReturnCorrectAttributeValuesAfterSetters() throws Exception {
        Cost newCost = new Cost(2L, "cost key", "cost item", "cost description", 10, price, applicationFinance, question);
        FinanceRowMetaField newFinanceRowMetaField = new FinanceRowMetaField(2L,"title","type");

        costValue.setCost(newCost);
        costValue.setFinanceRowMetaField(newFinanceRowMetaField);

        Assert.assertEquals(costValue.getCost(), newCost);
        Assert.assertEquals(costValue.getFinanceRowMetaField(), newFinanceRowMetaField);
    }
}