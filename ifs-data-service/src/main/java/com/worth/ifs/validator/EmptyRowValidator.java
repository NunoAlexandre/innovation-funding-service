package com.worth.ifs.validator;

import com.worth.ifs.application.transactional.QuestionService;
import com.worth.ifs.finance.domain.Cost;
import com.worth.ifs.finance.repository.CostRepository;
import com.worth.ifs.finance.resource.cost.CostItem;
import com.worth.ifs.finance.resource.cost.OtherFunding;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.ArrayList;
import java.util.List;

/**
 * This class validates the FormInputResponse, it checks if the maximum word count has been exceeded.
 */
@Component
public class EmptyRowValidator implements Validator {
    @Override
    public boolean supports(Class<?> clazz) {
        return ArrayList.class.equals(clazz);
    }
    private static final Log LOG = LogFactory.getLog(EmptyRowValidator.class);

    static CostRepository costRepository;
    static QuestionService questionService;

    @Autowired
    public EmptyRowValidator(CostRepository costRepository, QuestionService questionService) {
        this.costRepository = costRepository;
        this.questionService = questionService;
    }

    @Override
    public void validate(Object target, Errors errors) {
        List<CostItem> response = (List<CostItem>) target;
        Cost cost = costRepository.findOne(response.get(0).getId());

        int rowCount = 0;
        if(response.size() > 1) {
            for (final CostItem row : response) {
                if (!row.isEmpty()) {
                    rowCount++;
                }
            }
        }

        if(rowCount < response.get(0).getMinRows()){
            switch(response.get(0).getCostType()) {
                case OTHER_FUNDING:
                    if(((OtherFunding)response.get(0)).getOtherPublicFunding().equals("Yes")) {
                        errors.reject("MinimumRows", "You should provide at least " + response.get(0).getMinRows() + " source(s) of funding");
                    }
                    break;
                default:
                    errors.reject("MinimumRows", "You should provide at least" + response.get(0).getMinRows() + " row(s) of input");
                    break;
            }
        }
    }
}
