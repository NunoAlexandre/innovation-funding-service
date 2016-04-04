package com.worth.ifs.application.mapper;

import com.worth.ifs.application.domain.Application;
import com.worth.ifs.application.resource.ApplicationResource;
import com.worth.ifs.commons.mapper.BaseMapper;
import com.worth.ifs.commons.mapper.GlobalMapperConfig;
import com.worth.ifs.competition.mapper.CompetitionMapper;
import com.worth.ifs.finance.mapper.ApplicationFinanceMapper;
import com.worth.ifs.invite.mapper.InviteMapper;
import com.worth.ifs.user.mapper.ProcessRoleMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(
    config = GlobalMapperConfig.class,
    uses = {
        ProcessRoleMapper.class,
        ApplicationFinanceMapper.class,
        ApplicationStatusMapper.class,
        CompetitionMapper.class,
        InviteMapper.class
    }
)
public abstract class ApplicationMapper extends BaseMapper<Application, ApplicationResource, Long> {

    public Long mapApplicationToId(Application object) {
        if (object == null) {
            return null;
        }
        return object.getId();
    }

    @Mappings({
            @Mapping(source = "competition.name", target = "competitionName"),
            @Mapping(source = "applicationStatus.name", target = "applicationStatusName"),
            @Mapping(target = "applicationStatusConstant", ignore = true)
    })
    @Override
    public abstract ApplicationResource mapToResource(Application domain);
}