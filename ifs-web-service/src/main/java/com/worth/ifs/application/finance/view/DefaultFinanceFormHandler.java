package com.worth.ifs.application.finance.view;

import com.worth.ifs.application.finance.model.FinanceFormField;
import com.worth.ifs.application.finance.service.CostService;
import com.worth.ifs.application.finance.service.FinanceService;
import com.worth.ifs.application.finance.view.item.*;
import com.worth.ifs.finance.resource.ApplicationFinanceResource;
import com.worth.ifs.finance.resource.CostFieldResource;
import com.worth.ifs.finance.resource.cost.CostItem;
import com.worth.ifs.finance.resource.cost.CostType;
import com.worth.ifs.finance.service.ApplicationFinanceRestService;
import com.worth.ifs.user.domain.OrganisationSize;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

/**
 * {@code DefaultFinanceFormHandler} retrieves the costs and handles the finance data retrieved from the request, so it can be
 * transfered to view or stored. The costs retrieved from the {@link CostService} are converted
 * to {@link CostItem}.
 */
@Component
public class DefaultFinanceFormHandler implements FinanceFormHandler {
    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    private CostService costService;

    @Autowired
    private FinanceService financeService;

    @Autowired
    private ApplicationFinanceRestService applicationFinanceRestService;

    @Override
    public Map<String, List<String>> update(HttpServletRequest request, Long userId, Long applicationId) {
        ApplicationFinanceResource applicationFinanceResource = financeService.getApplicationFinanceDetails(userId, applicationId);
        if (applicationFinanceResource == null){
            applicationFinanceResource = financeService.addApplicationFinance(userId, applicationId);
        }

        storeFinancePosition(request, applicationFinanceResource.getId());
        List<CostItem> costItems = getCostItems(request);
        storeCostItems(costItems);

        addRemoveCostRows(request, applicationId, userId);
        return new HashMap<>();
    }

    @Override
    public void storeCost(Long userId, Long applicationId, String fieldName, String value) {
        if (fieldName != null && value != null) {
            String cleanedFieldName = fieldName;
            if (fieldName.startsWith("cost-")) {
                cleanedFieldName = fieldName.replace("cost-", "");
            } else if (fieldName.startsWith("formInput[")) {
                cleanedFieldName = fieldName.replace("formInput[", "").replace("]", "");
            }
            log.info("store field: " + cleanedFieldName + " val: " + value);
            storeField(cleanedFieldName, value, userId, applicationId);
        }
    }

    @Override
    public CostItem addCost(Long applicationId, Long userId, Long questionId) {
        ApplicationFinanceResource applicationFinance = financeService.getApplicationFinance(userId, applicationId);
        return costService.add(applicationFinance.getId(), questionId, null);
    }

    private void addRemoveCostRows(HttpServletRequest request, Long applicationId, Long userId) {
        Map<String, String[]> requestParams = request.getParameterMap();
        if (requestParams.containsKey("add_cost")) {
            String addCostParam = request.getParameter("add_cost");
            ApplicationFinanceResource applicationFinance = financeService.getApplicationFinance(userId, applicationId);
            financeService.addCost(applicationFinance.getId(), Long.valueOf(addCostParam));
        }
        if (requestParams.containsKey("remove_cost")) {
            String removeCostParam = request.getParameter("remove_cost");
            costService.delete(Long.valueOf(removeCostParam));
        }
    }

    // TODO DW - INFUND-1555 - handle rest results
    private void storeFinancePosition(HttpServletRequest request, @NotNull Long applicationFinanceId) {
        List<String> financePositionKeys = request.getParameterMap().keySet().stream().filter(k -> k.contains("financePosition-")).collect(Collectors.toList());
        if (!financePositionKeys.isEmpty()) {
            ApplicationFinanceResource applicationFinance = applicationFinanceRestService.getById(applicationFinanceId).getSuccessObjectOrThrowException();

            financePositionKeys.stream().forEach(k -> {
                String values = request.getParameterValues(k)[0];
                log.debug(String.format("finance position k : %s value: %s ", k, values));
                updateFinancePosition(applicationFinance, k, values);
            });
            applicationFinanceRestService.update(applicationFinance.getId(), applicationFinance);
        }
    }

    @Override
    public void updateFinancePosition(Long userId, Long applicationId, String fieldName, String value) {
        ApplicationFinanceResource applicationFinanceResource = financeService.getApplicationFinanceDetails(userId, applicationId);
        updateFinancePosition(applicationFinanceResource, fieldName, value);
        applicationFinanceRestService.update(applicationFinanceResource.getId(), applicationFinanceResource);
    }


    private void updateFinancePosition(ApplicationFinanceResource applicationFinance, String fieldName, String value) {
        fieldName = fieldName.replace("financePosition-", "");
        switch (fieldName) {
            case "organisationSize":
                applicationFinance.setOrganisationSize(OrganisationSize.valueOf(value));
                break;
            default:
                log.error(String.format("value not saved: %s / %s", fieldName, value));
        }
    }

    private List<CostItem> getCostItems(HttpServletRequest request) {
        List<CostFieldResource> costFields = costService.getCostFields();
        return mapRequestParametersToCostItems(request, costFields);
    }

    private List<CostItem> mapRequestParametersToCostItems(HttpServletRequest request, List<CostFieldResource> costFields) {
        List<CostItem> costItems = new ArrayList<>();
        for (CostType costType : CostType.values()) {
            List<String> costTypeKeys = request.getParameterMap().keySet().stream().
                    filter(k -> k.startsWith(costType.getType() + "-")).collect(Collectors.toList());
            Map<Long, List<FinanceFormField>> costFieldMap = getCostDataRows(request, costTypeKeys);
            List<CostItem> costItemsForType = getCostItems(costFieldMap, costType, costTypeKeys);
            costItems.addAll(costItemsForType);
        }

        return costItems;
    }

    /**
     * Retrieve the complete cost item data row, so everything is together
     */
    private Map<Long, List<FinanceFormField>> getCostDataRows(HttpServletRequest request, List<String> costTypeKeys) {
        // make sure that we have the fields together acting on one cost
        Map<Long, List<FinanceFormField>> costKeyMap = new HashMap<>();
        for (String costTypeKey : costTypeKeys) {
            String value = request.getParameter(costTypeKey);
            FinanceFormField financeFormField = getCostFormField(costTypeKey, value);
            if (financeFormField == null)
                continue;

            if (financeFormField.getId() != null && !financeFormField.getId().equals("null")) {
                Long id = Long.valueOf(financeFormField.getId());
                if (costKeyMap.containsKey(id)) {
                    costKeyMap.get(id).add(financeFormField);
                } else {
                    List<FinanceFormField> costKeyValues = new ArrayList<>();
                    costKeyValues.add(financeFormField);
                    costKeyMap.put(id, costKeyValues);
                }
            }
        }
        return costKeyMap;
    }

    /**
     * Retrieve the cost items from the request based on their type
     */
    private List<CostItem> getCostItems(Map<Long, List<FinanceFormField>> costFieldMap, CostType costType, List<String> costTypeKeys) {
        List<CostItem> costItems = new ArrayList<>();
        CostHandler costHandler = getCostItemHandler(costType);

        // create new cost items
        for (Map.Entry<Long, List<FinanceFormField>> entry : costFieldMap.entrySet()) {
            CostItem costItem = costHandler.toCostItem(entry.getKey(), entry.getValue());
            if (costItem != null) {
                costItems.add(costItem);
            }
        }
        return costItems;
    }

    private void storeField(String fieldName, String value, Long userId, Long applicationId) {
        FinanceFormField financeFormField = getCostFormField(fieldName, value);
        CostType costType = CostType.fromString(financeFormField.getKeyType());
        CostHandler costHandler = getCostItemHandler(costType);
        Long costFormFieldId = 0L;
        if (financeFormField.getId() != null && !financeFormField.getId().equals("null")) {
            costFormFieldId = Long.parseLong(financeFormField.getId());
        }
        CostItem costItem = costHandler.toCostItem(costFormFieldId, Arrays.asList(financeFormField));
        storeCostItem(costItem, userId, applicationId, financeFormField.getQuestionId());
    }

    private FinanceFormField getCostFormField(String costTypeKey, String value) {
        String[] keyParts = costTypeKey.split("-");
        if (keyParts.length > 3) {
            return new FinanceFormField(costTypeKey, value, keyParts[3], keyParts[2], keyParts[1], keyParts[0]);
        } else if (keyParts.length == 3) {
            return new FinanceFormField(costTypeKey, value, null, keyParts[2], keyParts[1], keyParts[0]);
        }
        return null;
    }

    private CostHandler getCostItemHandler(CostType costType) {
        switch (costType) {
            case LABOUR:
                return new LabourCostHandler();
            case MATERIALS:
                return new MaterialsHandler();
            case SUBCONTRACTING_COSTS:
                return new SubContractingCostHandler();
            case FINANCE:
                return new GrantClaimHandler();
            case OVERHEADS:
                return new OverheadsHandler();
            case CAPITAL_USAGE:
                return new CapitalUsageHandler();
            case TRAVEL:
                return new TravelCostHandler();
            case OTHER_COSTS:
                return new OtherCostHandler();
            case OTHER_FUNDING:
                return new OtherFundingHandler();
            case YOUR_FINANCE:
                return new YourFinanceHandler();
            default:
                log.error("getCostItem, unsupported type: " + costType);
                return null;
        }
    }

    private void storeCostItem(CostItem costItem, Long userId, Long applicationId, String question) {
        if (costItem.getId().equals(0L)) {
            addCostItem(costItem, userId, applicationId, question);
        } else {
            costService.update(costItem);
        }
    }

    private void addCostItem(CostItem costItem, Long userId, Long applicationId, String question) {
        ApplicationFinanceResource applicationFinanceResource = financeService.getApplicationFinanceDetails(userId, applicationId);

        if (question != null && !question.isEmpty()) {
            Long questionId = Long.parseLong(question);
            costService.add(applicationFinanceResource.getId(), questionId, costItem);
        }
    }

    private void storeCostItems(List<CostItem> costItems) {
        costItems.stream().forEach(c -> costService.update(c));
    }
}
