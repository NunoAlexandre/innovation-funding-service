package org.innovateuk.ifs.application.viewmodel.finance;

import org.innovateuk.ifs.application.resource.QuestionResource;
import org.innovateuk.ifs.finance.resource.cost.FinanceRowType;
import org.innovateuk.ifs.form.resource.FormInputType;

/**
 * Created by luke.harper on 08/05/2017.
 */
public class OverheadCostViewModel extends AbstractCostViewModel {

    private QuestionResource labourQuestion;

    @Override
    protected FormInputType formInputType() {
        return FormInputType.OVERHEADS;
    }

    @Override
    public FinanceRowType getFinanceRowType() {
        return FinanceRowType.OVERHEADS;
    }

    public QuestionResource getLabourQuestion() {
        return labourQuestion;
    }

    public void setLabourQuestion(QuestionResource labourQuestion) {
        this.labourQuestion = labourQuestion;
    }
}
