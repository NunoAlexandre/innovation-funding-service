package com.worth.ifs.project.builder;

import com.worth.ifs.BaseBuilder;
import com.worth.ifs.address.resource.AddressResource;
import com.worth.ifs.application.resource.ApplicationResource;
import com.worth.ifs.project.resource.ProjectResource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.BiConsumer;

import static com.worth.ifs.BaseBuilderAmendFunctions.idBasedNames;
import static com.worth.ifs.BuilderAmendFunctions.setField;
import static com.worth.ifs.BuilderAmendFunctions.uniqueIds;
import static java.util.Collections.emptyList;

public class ProjectResourceBuilder extends BaseBuilder<ProjectResource, ProjectResourceBuilder> {

    private ProjectResourceBuilder(List<BiConsumer<Integer, ProjectResource>> multiActions) {
        super(multiActions);
    }

    public static ProjectResourceBuilder newProjectResource() {
        return new ProjectResourceBuilder(emptyList()).
                with(uniqueIds()).
                with(idBasedNames("Project "));
    }

    @Override
    protected ProjectResourceBuilder createNewBuilderWithActions(List<BiConsumer<Integer, ProjectResource>> actions) {
        return new ProjectResourceBuilder(actions);
    }

    @Override
    protected ProjectResource createInitial() {
        return new ProjectResource();
    }

    public ProjectResourceBuilder withId(Long... ids) {
        return withArray((id, project) -> setField("id", id, project), ids);
    }

    public ProjectResourceBuilder withName(String... name){
        return withArray((n, project) -> project.setName(n), name);
    }

    public ProjectResourceBuilder withApplication(ApplicationResource applicationResource){
        return with(project -> project.setApplication(applicationResource.getId()));
    }

    public ProjectResourceBuilder withApplication(Long... application){
        return withArray((applicationId, project) -> project.setApplication(applicationId), application);
    }

    public ProjectResourceBuilder withTargetStartDate(LocalDate... dates) {
        return withArray((date, project) -> project.setTargetStartDate(date), dates);
    }

    public ProjectResourceBuilder withAddress(AddressResource address) {
        return with(project -> project.setAddress(address));
    }

    public ProjectResourceBuilder withProjectUsers(List<Long>... projectUsers) {
        return withArray((userList, project) -> project.setProjectUsers(userList), projectUsers);
    }

    public ProjectResourceBuilder withDuration(Long... durations) {
        return withArray((duration, project) -> project.setDurationInMonths(duration), durations);
    }

    public ProjectResourceBuilder withSubmittedDate(LocalDateTime... submittedDates){
        return withArray((submittedDate, project) -> project.setSubmittedDate(submittedDate), submittedDates);
    }
}
