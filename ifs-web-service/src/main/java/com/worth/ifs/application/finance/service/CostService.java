package com.worth.ifs.application.finance.service;

import com.worth.ifs.finance.domain.Cost;
import com.worth.ifs.finance.domain.CostField;

import java.util.List;

public interface CostService {
    Cost getById(Long costId);
    List<CostField> getCostFields();
    public void update(Cost cost);
}
