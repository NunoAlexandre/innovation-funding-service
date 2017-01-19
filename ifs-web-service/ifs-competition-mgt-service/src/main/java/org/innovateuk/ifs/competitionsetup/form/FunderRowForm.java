package org.innovateuk.ifs.competitionsetup.form;

import org.hibernate.validator.constraints.NotEmpty;
import org.innovateuk.ifs.competition.resource.CompetitionFunderResource;

import javax.validation.constraints.Digits;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigInteger;

/**
 * Form representing a row in the funders table.
 */
public class FunderRowForm {

    @NotEmpty(message = "{validation.additionalinfoform.fundername.required}")
    private String funder;

    @Min(value=0, message = "{validation.additionalinfoform.funderbudget.min}")
    @NotNull(message = "{validation.additionalinfoform.funderbudget.required}")
    @Digits(integer = 8, fraction = 2, message = "{validation.additionalinfoform.funderbudget.invalid}")
    private BigInteger funderBudget;

    @NotNull
    private Boolean coFunder;

    public FunderRowForm() {

    }

    public FunderRowForm(CompetitionFunderResource funderResource) {
        this.setFunder(funderResource.getFunder());
        this.setFunderBudget(funderResource.getFunderBudget());
        this.setCoFunder(funderResource.getCoFunder());
    }

    public String getFunder() {
        return funder;
    }

    public void setFunder(String funder) {
        this.funder = funder;
    }

    public BigInteger getFunderBudget() {
        return funderBudget;
    }

    public void setFunderBudget(BigInteger funderBudget) {
        this.funderBudget = funderBudget;
    }

    public Boolean getCoFunder() {
        return coFunder;
    }

    public void setCoFunder(Boolean coFunder) {
        this.coFunder = coFunder;
    }
}
