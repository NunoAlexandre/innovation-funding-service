package org.innovateuk.ifs.competition.form;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.innovateuk.ifs.application.resource.FundingDecision;

import java.util.Optional;

/**
 * Contains the of the Funding Decision filter values.
 */

public class FundingDecisionFilterForm {
    private Optional<String>stringFilter  = Optional.empty();
    private Optional<FundingDecision> fundingFilter = Optional.empty();

    public Optional<String> getStringFilter() {
        return stringFilter;
    }

    public void setStringFilter(Optional<String> stringFilter) {
        this.stringFilter = stringFilter;
    }

    public Optional<FundingDecision> getFundingFilter() {
        return fundingFilter;
    }

    public void setFundingFilter(Optional<FundingDecision> fundingFilter) {
        this.fundingFilter = fundingFilter;
    }

    @JsonIgnore
    public String getStringFilterValue() {
        if(stringFilter.isPresent()) {
            return stringFilter.get();
        }
        else {
            return null;
        }
    }

    @JsonIgnore
    public FundingDecision getFundingFilterValue() {
        if(fundingFilter.isPresent()) {
            return fundingFilter.get();
        }
        else {
            return null;
        }
    }
}
