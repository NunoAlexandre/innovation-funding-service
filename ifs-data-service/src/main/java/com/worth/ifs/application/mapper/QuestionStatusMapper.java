package com.worth.ifs.application.mapper;

import com.worth.ifs.application.domain.QuestionStatus;
import com.worth.ifs.application.repository.QuestionStatusRepository;
import com.worth.ifs.application.resource.QuestionStatusResource;
import com.worth.ifs.commons.mapper.GlobalMapperConfig;
import com.worth.ifs.user.mapper.ProcessRoleMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(
    config = GlobalMapperConfig.class,
    uses = {
        ProcessRoleMapper.class,
        ApplicationMapper.class,
        QuestionMapper.class
    }
)
public abstract class QuestionStatusMapper {

    @Autowired
    QuestionStatusRepository repository;

    @Mappings({
            @Mapping(target = "assigneeName", ignore = true),
            @Mapping(target = "assigneeUserId", ignore = true),
            @Mapping(target = "assignedByName", ignore = true),
            @Mapping(target = "assignedByUserId", ignore = true)
    })
    public abstract QuestionStatusResource mapQuestionStatusToResource(QuestionStatus object);

    public abstract QuestionStatus resourceToQuestionStatus(QuestionStatusResource resource);

    public Long mapQuestionStatusToId(QuestionStatus object) {
        if (object == null) {
            return null;
        }
        return object.getId();
    }

    public QuestionStatus mapIdToQuestionStatus(Long id) {
        return repository.findOne(id);
    }

    public QuestionStatusResource mapQuestionStatusToPopulatedResource(final QuestionStatus object){
        final QuestionStatusResource resource = this.mapQuestionStatusToResource(object);
        resource.setAssigneeName(object.getAssignee().getUser().getName());
        resource.setAssigneeUserId(object.getAssignee().getId());
        resource.setAssignedByName(object.getAssignedBy().getUser().getName());
        resource.setAssignedByUserId(object.getAssignedBy().getUser().getId());
        return resource;
    }
}