package com.worth.ifs.finance.service;

import com.worth.ifs.finance.resource.cost.CostItem;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * Interface for CRUD operations on {@link Cost} related data.
 */
public interface CostRestService{
    public CostItem add(Long applicationFinanceId, Long questionId, CostItem costItem);
    public List<CostItem> getCosts(Long applicationFinanceId);
    public void update(CostItem costItem);
    public CostItem findById(Long id);
    public void delete(Long costId);
}
