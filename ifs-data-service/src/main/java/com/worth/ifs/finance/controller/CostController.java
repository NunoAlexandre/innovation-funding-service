package com.worth.ifs.finance.controller;

import com.worth.ifs.application.domain.Question;
import com.worth.ifs.application.repository.QuestionRepository;
import com.worth.ifs.finance.domain.ApplicationFinance;
import com.worth.ifs.finance.domain.Cost;
import com.worth.ifs.finance.domain.CostField;
import com.worth.ifs.finance.domain.CostValue;
import com.worth.ifs.finance.handler.item.OrganisationFinanceHandler;
import com.worth.ifs.finance.repository.ApplicationFinanceRepository;
import com.worth.ifs.finance.repository.CostFieldRepository;
import com.worth.ifs.finance.repository.CostRepository;
import com.worth.ifs.finance.repository.CostValueRepository;
import com.worth.ifs.finance.resource.cost.CostItem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;

/**
 * This RestController exposes CRUD operations to both the
 * {@link com.worth.ifs.finance.service.CostRestServiceImpl} and other REST-API users
 * to manage {@link Cost} related data.
 */
@RestController
@RequestMapping("/cost")
public class CostController {
    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    ApplicationFinanceRepository applicationFinanceRepository;

    @Autowired
    QuestionRepository questionRepository;

    @Autowired
    CostRepository costRepository;

    @Autowired
    CostFieldRepository costFieldRepository;

    @Autowired
    CostValueRepository costValueRepository;

    @Autowired
    OrganisationFinanceHandler organisationFinanceHandler;

    @RequestMapping("/add/{applicationFinanceId}/{questionId}")
    public CostItem add(
            @PathVariable("applicationFinanceId") final Long applicationFinanceId,
            @PathVariable("questionId") final Long questionId,
            @RequestBody(required = false) final CostItem newCostItem) {
        ApplicationFinance applicationFinance = applicationFinanceRepository.findOne(applicationFinanceId);
        Question question = questionRepository.findOne(questionId);
        Cost cost;
        if(newCostItem!=null) {
            cost = addCostItem(applicationFinance, question, newCostItem);
        } else {
            cost = new Cost("", "", 0, BigDecimal.ZERO, applicationFinance, question);
            cost = costRepository.save(cost);
        }


        // RETURN 201 STATUS
        return organisationFinanceHandler.costToCostItem(cost);
    }

    @RequestMapping("/update/{id}")
    public Cost update(@PathVariable("id") final Long id,
                       @RequestBody final CostItem newCostItem) {
        if(id!=null && costRepository.exists(id)) {
            Cost newCost = organisationFinanceHandler.costItemToCost(newCostItem);
            Cost updatedCost = mapCost(id, newCost);
            Cost savedCost = costRepository.save(updatedCost);

            newCost.getCostValues()
                .stream()
                .filter(c -> c.getValue() != null)
                .filter(c -> !c.getValue().equals("null"))
                .forEach(costValue -> updateCostValue(costValue, savedCost));
            return newCost;
        } else {
            log.info("DOES NOT EXIST");
        }
        return null;
    }

    private Cost addCostItem(ApplicationFinance applicationFinance, Question question, CostItem newCostItem) {
        Cost existingCost = costRepository.findOneByApplicationFinanceIdAndQuestionId(applicationFinance.getId(), question.getId());
        if(existingCost == null) {
            Cost cost = organisationFinanceHandler.costItemToCost(newCostItem);
            cost.setQuestion(question);
            cost.setApplicationFinance(applicationFinance);
            return costRepository.save(cost);
        } else {
            return update(existingCost.getId(), newCostItem);
        }
    }

    private Cost mapCost(Long id, Cost newCost) {
        Cost currentCost = costRepository.findOne(id);
        if(newCost.getCost()!=null) {
            currentCost.setCost(newCost.getCost());
        }
        if(newCost.getDescription()!=null) {
            currentCost.setDescription(newCost.getDescription());
        }
        if(newCost.getItem()!=null) {
            currentCost.setItem(newCost.getItem());
        }
        if(newCost.getQuantity()!=null) {
            currentCost.setQuantity(newCost.getQuantity());
        }

        return currentCost;
    }

    private void updateCostValue(CostValue costValue, Cost savedCost){
        if(costValue.getCostField() == null){
            log.error("CostField is null");
            return;
        }
        CostField costField = costFieldRepository.findOne(costValue.getCostField().getId());
        costValue.setCost(savedCost);
        costValue.setCostField(costField);
        costValueRepository.save(costValue);
    }

    @RequestMapping("/get/{applicationFinanceId}")
    public List<Cost> findByApplicationId(@PathVariable("applicationFinanceId") final Long applicationFinanceId) {
        return costRepository.findByApplicationFinanceId(applicationFinanceId);
    }

    @RequestMapping("/findById/{id}")
    public CostItem findById(@PathVariable("id") final Long id) {
        Cost cost =  costRepository.findOne(id);
        if(cost!=null){
            CostItem costItem = organisationFinanceHandler.costToCostItem(cost);
            return costItem;
        }else{
            return null;
        }
    }

    @Transactional
    @RequestMapping("/delete/{costId}")
    public void delete(@PathVariable("costId") final Long costId) {
        costValueRepository.deleteByCostId(costId);
        costRepository.delete(costId);
    }
}
