package com.worth.ifs.application.finance.view;

import com.worth.ifs.finance.resource.cost.CostItem;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

public interface FinanceFormHandler {
    Map<String, List<String>> update(HttpServletRequest request, Long userId, Long applicationId);
    void storeCost(Long userId, Long applicationId, String fieldName, String value);
    void updateFinancePosition(Long userId, Long applicationId, String fieldName, String value);
    CostItem addCost(Long applicationId, Long userId, Long questionId);
}
