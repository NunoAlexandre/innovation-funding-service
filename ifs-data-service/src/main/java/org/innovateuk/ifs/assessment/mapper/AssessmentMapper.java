package org.innovateuk.ifs.assessment.mapper;

import org.innovateuk.ifs.application.mapper.ApplicationMapper;
import org.innovateuk.ifs.assessment.domain.Assessment;
import org.innovateuk.ifs.assessment.resource.AssessmentResource;
import org.innovateuk.ifs.commons.mapper.BaseMapper;
import org.innovateuk.ifs.commons.mapper.GlobalMapperConfig;
import org.innovateuk.ifs.competition.mapper.CompetitionMapper;
import org.innovateuk.ifs.user.mapper.ProcessRoleMapper;
import org.innovateuk.ifs.user.mapper.UserMapper;
import org.innovateuk.ifs.workflow.mapper.ProcessOutcomeMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

/**
 * Maps between domain and resource DTO for {@link org.innovateuk.ifs.assessment.domain.Assessment}.
 */
@Mapper(
        config = GlobalMapperConfig.class,
        uses = {
                ProcessOutcomeMapper.class,
                ProcessRoleMapper.class,
                ApplicationMapper.class,
                UserMapper.class,
                CompetitionMapper.class
        }
)
public abstract class AssessmentMapper extends BaseMapper<Assessment, AssessmentResource, Long> {

    @Mappings({
            @Mapping(source = "processEvent", target = "event"),
            @Mapping(source = "version", target = "lastModified"),
            @Mapping(source = "participant", target = "processRole"),
            @Mapping(source = "target", target = "application"),
            @Mapping(source = "target.name", target = "applicationName"),
            @Mapping(source = "target.competition", target = "competition"),
            @Mapping(source = "activityState", target = "assessmentState"),
    })
    @Override
    public abstract AssessmentResource mapToResource(Assessment domain);

    @Mappings({
            @Mapping(target = "processEvent", source = "event"),
            @Mapping(target = "participant", source = "processRole"),
            @Mapping(target = "target", source = "application"),
            @Mapping(target = "activityState", source = "assessmentState", ignore = true),
            @Mapping(target = "responses", ignore = true)
    })
    @Override
    public abstract Assessment mapToDomain(AssessmentResource resource);

    public Long mapAssessmentToId(Assessment object) {
        if (object == null) {
            return null;
        }
        return object.getId();
    }
}
