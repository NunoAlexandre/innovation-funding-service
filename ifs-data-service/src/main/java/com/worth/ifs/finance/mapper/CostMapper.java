package com.worth.ifs.finance.mapper;

import com.worth.ifs.application.mapper.QuestionMapper;
import com.worth.ifs.commons.mapper.BaseMapper;
import com.worth.ifs.commons.mapper.GlobalMapperConfig;
import com.worth.ifs.finance.domain.Cost;
import com.worth.ifs.finance.resource.CostResource;
import org.mapstruct.Mapper;

@Mapper(
    config = GlobalMapperConfig.class,
    uses = {
        CostValueMapper.class,
        ApplicationFinanceMapper.class,
        QuestionMapper.class
    }
)
public abstract class CostMapper extends BaseMapper<Cost, CostResource, Long> {

    public Long mapCostToId(Cost object) {
        if (object == null) {
            return null;
        }
        return object.getId();
    }
}