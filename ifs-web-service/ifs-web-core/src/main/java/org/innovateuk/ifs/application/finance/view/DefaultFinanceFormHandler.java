package org.innovateuk.ifs.application.finance.view;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.innovateuk.ifs.application.finance.model.FinanceFormField;
import org.innovateuk.ifs.application.finance.service.FinanceRowService;
import org.innovateuk.ifs.application.finance.service.FinanceService;
import org.innovateuk.ifs.application.finance.view.item.*;
import org.innovateuk.ifs.application.resource.QuestionResource;
import org.innovateuk.ifs.application.resource.SectionType;
import org.innovateuk.ifs.application.service.QuestionService;
import org.innovateuk.ifs.application.service.SectionService;
import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.commons.rest.ValidationMessages;
import org.innovateuk.ifs.finance.resource.ApplicationFinanceResource;
import org.innovateuk.ifs.finance.resource.cost.FinanceRowItem;
import org.innovateuk.ifs.finance.resource.cost.FinanceRowType;
import org.innovateuk.ifs.finance.service.ApplicationFinanceRestService;
import org.innovateuk.ifs.form.resource.FormInputType;
import org.innovateuk.ifs.user.resource.OrganisationSize;
import org.innovateuk.ifs.user.resource.ProcessRoleResource;
import org.innovateuk.ifs.user.service.ProcessRoleService;
import org.innovateuk.ifs.util.Either;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

/**
 * {@code DefaultFinanceFormHandler} retrieves the costs and handles the finance data retrieved from the request, so it can be
 * transfered to view or stored. The costs retrieved from the {@link FinanceRowService} are converted
 * to {@link FinanceRowItem}.
 */
@Component
public class DefaultFinanceFormHandler extends BaseFinanceFormHandler implements FinanceFormHandler {
    private static final Log LOG = LogFactory.getLog(DefaultFinanceFormHandler.class);

    @Autowired
    private FinanceService financeService;

    @Autowired
    private FinanceRowService financeRowService;

    @Autowired
    private SectionService sectionService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private ProcessRoleService processRoleService;

    @Autowired
    private ApplicationFinanceRestService applicationFinanceRestService;
    
    @Autowired
    private UnsavedFieldsManager unsavedFieldsManager;

    @Override
    public ValidationMessages update(HttpServletRequest request, Long userId, Long applicationId, Long competitionId) {

        ApplicationFinanceResource applicationFinanceResource = financeService.getApplicationFinanceDetails(userId, applicationId);
        if (applicationFinanceResource == null) {
            applicationFinanceResource = financeService.addApplicationFinance(userId, applicationId);
        }

        storeFinancePosition(request, applicationFinanceResource.getId(), competitionId, userId);
        ValidationMessages errors = getAndStoreCostitems(request, applicationFinanceResource.getId());
        addRemoveCostRows(request, applicationId, userId);

        return errors;
    }


    @Override
    public ValidationMessages storeCost(Long userId, Long applicationId, String fieldName, String value, Long competitionId) {

        if (fieldName == null || value == null) {
            return new ValidationMessages();
        }

        String cleanedFieldName = fieldName;
        if (fieldName.startsWith("cost-")) {
            cleanedFieldName = fieldName.replace("cost-", "");
        } else if (fieldName.startsWith("formInput[")) {
            cleanedFieldName = fieldName.replace("formInput[", "").replace("]", "");
        }

        LOG.info("store field: " + cleanedFieldName + " val: " + value);
        return storeField(cleanedFieldName, value, userId, applicationId);
    }

    @Override
    public ValidationMessages addCost(Long applicationId, Long userId, Long questionId) {
        ApplicationFinanceResource applicationFinance = financeService.getApplicationFinance(userId, applicationId);
        return financeRowService.add(applicationFinance.getId(), questionId, null);
    }
    
    @Override
    public FinanceRowItem addCostWithoutPersisting(Long applicationId, Long userId, Long questionId) {
        ApplicationFinanceResource applicationFinance = financeService.getApplicationFinance(userId, applicationId);
        return financeRowService.addWithoutPersisting(applicationFinance.getId(), questionId);
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
            financeRowService.delete(Long.valueOf(removeCostParam));
        }
    }

    // TODO DW - INFUND-1555 - handle rest results
    private void storeFinancePosition(HttpServletRequest request, @NotNull Long applicationFinanceId, Long competitionId, Long userId) {
        List<String> financePositionKeys = request.getParameterMap().keySet().stream().filter(k -> k.contains("financePosition-")).collect(Collectors.toList());
        if (!financePositionKeys.isEmpty()) {
            ApplicationFinanceResource applicationFinance = applicationFinanceRestService.getById(applicationFinanceId).getSuccessObjectOrThrowException();

            financePositionKeys.stream().forEach(k -> {
                String values = request.getParameterValues(k)[0];
                LOG.debug(String.format("finance position k : %s value: %s ", k, values));
                updateFinancePosition(applicationFinance, k, values, competitionId, userId);
            });
            applicationFinanceRestService.update(applicationFinance.getId(), applicationFinance);
        }
    }

    @Override
    public void updateFinancePosition(Long userId, Long applicationId, String fieldName, String value, Long competitionId) {
        ApplicationFinanceResource applicationFinanceResource = financeService.getApplicationFinanceDetails(userId, applicationId);

        if (applicationFinanceResource != null || !fieldName.equals("application.researchCategoryId")) {
            updateFinancePosition(applicationFinanceResource, fieldName, value, competitionId, userId);

            if (!fieldName.equals("application.researchCategoryId")) {
                applicationFinanceRestService.update(applicationFinanceResource.getId(), applicationFinanceResource);
            }
        }
    }

    private void updateFinancePosition(ApplicationFinanceResource applicationFinance, String fieldName, String value, Long competitionId, Long userId) {
        String fieldNameReplaced = fieldName.replace("financePosition-", "");
        switch (fieldNameReplaced) {
            case "organisationSize":
                OrganisationSize newValue = OrganisationSize.valueOf(value);
                handleOrganisationSizeChange(applicationFinance, competitionId, userId, applicationFinance.getOrganisationSize(), newValue);
                applicationFinance.setOrganisationSize(newValue);
                break;
            case "application.researchCategoryId":
                handleResearchCategoryChange(applicationFinance, competitionId);
                break;
            default:
                LOG.error(String.format("value not saved: %s / %s", fieldNameReplaced, value));
        }
    }

    private void handleOrganisationSizeChange(ApplicationFinanceResource applicationFinance, Long competitionId, Long userId, OrganisationSize oldValue, OrganisationSize newValue) {
        if(null != oldValue && !oldValue.equals(newValue)) {
            resetFundingAndMarkAsIncomplete(applicationFinance, competitionId, userId);
        }
    }

    private void handleResearchCategoryChange(ApplicationFinanceResource applicationFinance, Long competitionId) {
        resetFundingLevelAndMarkAsIncompleteForAllCollaborators(competitionId, applicationFinance.getApplication());
    }

    private void resetFundingAndMarkAsIncomplete(ApplicationFinanceResource applicationFinance, Long competitionId, Long userId) {

        Optional<ProcessRoleResource> processRole = processRoleService.getByApplicationId(applicationFinance.getApplication()).stream()
                .filter(processRoleResource -> userId.equals(processRoleResource.getUser()))
                .findFirst();

        //set your funding section to marked as incomplete
        sectionService.getSectionsForCompetitionByType(competitionId, SectionType.FUNDING_FINANCES)
                .forEach(fundingSection ->
                        sectionService.markAsInComplete(fundingSection.getId(),
                                applicationFinance.getApplication(),
                                (processRole.isPresent() ? processRole.get().getId() : null))
                );

        QuestionResource financeQuestion = questionService.getQuestionByCompetitionIdAndFormInputType(competitionId, FormInputType.FINANCE).getSuccessObjectOrThrowException();

        resetFundingLevel(applicationFinance, financeQuestion.getId());
    }

    private void resetFundingLevelAndMarkAsIncompleteForAllCollaborators(Long competitionId, Long applicationId) {

        QuestionResource financeQuestion = questionService.getQuestionByCompetitionIdAndFormInputType(competitionId, FormInputType.FINANCE).getSuccessObjectOrThrowException();

        Set<Long> userIds = processRoleService.getByApplicationId(applicationId).stream().filter(processRole -> processRole.getUser() != null).map(
                processRole -> processRole.getUser()).collect(Collectors.toSet());

        userIds.stream().forEach(userId ->
                //set user's funding section to marked as incomplete
                sectionService.getSectionsForCompetitionByType(competitionId, SectionType.FUNDING_FINANCES)
                        .forEach(fundingSection ->
                                sectionService.markAsInComplete(fundingSection.getId(),
                                        applicationId, userId))
        );

        financeService.getApplicationFinanceDetails(applicationId).stream().forEach(applicationFinance -> {
            resetFundingLevel(applicationFinance, financeQuestion.getId());
            applicationFinanceRestService.update(applicationFinance.getId(), applicationFinance);
        });
    }

    private void resetFundingLevel(ApplicationFinanceResource applicationFinance, Long financeQuestionId) {
        if (applicationFinance.getGrantClaim() != null) {
            applicationFinance.getGrantClaim().setGrantClaimPercentage(0);
            financeRowService.add(applicationFinance.getId(), financeQuestionId, applicationFinance.getGrantClaim());
        }
    }

    /**
     * Retrieve the cost items from the request based on their type
     */
    protected List<Either<FinanceRowItem, ValidationMessages>> getFinanceRowItems(Map<Long, List<FinanceFormField>> costFieldMap, FinanceRowType costType, Long applicationFinanceId) {
        List<Either<FinanceRowItem, ValidationMessages>> costItems = new ArrayList<>();

        if(costFieldMap.size() == 0) {
            return costItems;
        }
        FinanceRowHandler financeRowHandler = getFinanceRowItemHandler(costType);

        // create new cost items
        for (Map.Entry<Long, List<FinanceFormField>> entry : costFieldMap.entrySet()) {
            try{
            	Long id = entry.getKey();
            	List<FinanceFormField> fields = entry.getValue();
            	
            	if(id == -1L) {
            		List<List<FinanceFormField>> fieldsSeparated = unsavedFieldsManager.separateFields(fields);
            		for(List<FinanceFormField> fieldGroup: fieldsSeparated) {
            			FinanceRowItem costItem = financeRowHandler.toFinanceRowItem(null, fieldGroup);
                        if (costItem != null && fieldGroup.size() > 0) {
                    		Long questionId = Long.valueOf(fieldGroup.get(0).getQuestionId());
                    		ValidationMessages addResult = financeRowService.add(applicationFinanceId, questionId, costItem);
                    		Either<FinanceRowItem, ValidationMessages> either;
                    		if(addResult.hasErrors()) {
                    			either = Either.right(addResult);
                    		} else {
                    			FinanceRowItem added = financeRowService.findById(addResult.getObjectId());
                    			either = Either.left(added);
                    		}
                            
                            costItems.add(either);
                        }
            		}
            	} else {
            		FinanceRowItem costItem = financeRowHandler.toFinanceRowItem(id, fields);
                    if (costItem != null) {
                        Either<FinanceRowItem, ValidationMessages> either = Either.left(costItem);
                        costItems.add(either);
                    }
            	}
                
            }catch(NumberFormatException e){
                ValidationMessages validationMessages = getValidationMessageFromException(entry, e);
                Either<FinanceRowItem, ValidationMessages> either = Either.right(validationMessages);
                costItems.add(either);
            }
        }
        return costItems;
    }

	private ValidationMessages storeField(String fieldName, String value, Long userId, Long applicationId) {
        FinanceFormField financeFormField = getCostFormField(fieldName, value);
        FinanceRowType costType = FinanceRowType.fromType(FormInputType.findByName(financeFormField.getKeyType()));
        FinanceRowHandler financeRowHandler = getFinanceRowItemHandler(costType);
        Long costFormFieldId = 0L;
        if (financeFormField.getId() != null && !"null".equals(financeFormField.getId()) && !financeFormField.getId().startsWith("unsaved")) {
            costFormFieldId = Long.parseLong(financeFormField.getId());
        }
        FinanceRowItem costItem = financeRowHandler.toFinanceRowItem(costFormFieldId, Arrays.asList(financeFormField));
        if(costItem != null) {
        	return storeFinanceRowItem(costItem, userId, applicationId, financeFormField.getQuestionId());
        } else {
        	return new ValidationMessages();
        }
    }

    private ValidationMessages storeFinanceRowItem(FinanceRowItem costItem, Long userId, Long applicationId, String question) {

        if (costItem.getId().equals(0L)) {
            return addFinanceRowItem(costItem, userId, applicationId, question);
        } else {
            RestResult<ValidationMessages> messages = financeRowService.update(costItem);
            ValidationMessages validationMessages = messages.getSuccessObject();

            if (validationMessages == null || validationMessages.getErrors() == null || validationMessages.getErrors().isEmpty()) {
                LOG.debug("no validation errors on cost items");
                return messages.getSuccessObject();
            } else {
                messages.getSuccessObject().getErrors().stream()
                        .peek(e -> LOG.debug(String.format("Got cost item Field error: %s", e.getErrorKey())));
                return messages.getSuccessObject();
            }
        }
    }

    private ValidationMessages addFinanceRowItem(FinanceRowItem costItem, Long userId, Long applicationId, String question) {
        ApplicationFinanceResource applicationFinanceResource = financeService.getApplicationFinanceDetails(userId, applicationId);

        if (question != null && !question.isEmpty()) {
            Long questionId = Long.parseLong(question);
            return financeRowService.add(applicationFinanceResource.getId(), questionId, costItem);
        }
        return null;
    }

    @Override
    public RestResult<ByteArrayResource> getFile(Long applicationFinanceId) {
        throw new NotImplementedException("Finance upload is not available for the default finances");

    }
}
