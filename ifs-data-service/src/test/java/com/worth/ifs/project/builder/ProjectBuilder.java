package com.worth.ifs.project.builder;

import com.worth.ifs.BaseBuilder;
import com.worth.ifs.address.domain.Address;
import com.worth.ifs.application.domain.Application;
import com.worth.ifs.project.domain.Project;
import com.worth.ifs.user.domain.ProcessRole;

import java.time.LocalDate;
import java.util.List;
import java.util.function.BiConsumer;

import static com.worth.ifs.BuilderAmendFunctions.setField;
import static com.worth.ifs.BuilderAmendFunctions.uniqueIds;
import static java.util.Collections.emptyList;

public class ProjectBuilder extends BaseBuilder<Project, ProjectBuilder> {

    private ProjectBuilder(List<BiConsumer<Integer, Project>> multiActions) {
        super(multiActions);
    }

    public static ProjectBuilder newProject() {
        return new ProjectBuilder(emptyList()).with(uniqueIds());
    }

    @Override
    protected ProjectBuilder createNewBuilderWithActions(List<BiConsumer<Integer, Project>> actions) {
        return new ProjectBuilder(actions);
    }

    @Override
    protected Project createInitial() {
        return new Project();
    }

    public ProjectBuilder withId(Long... ids) {
        return withArray((id, project) -> setField("id", id, project), ids);
    }

    public ProjectBuilder withTargetStartDate(LocalDate... dates) {
        return withArray((date, project) -> project.setTargetStartDate(date), dates);
    }

    public ProjectBuilder withAddress(Address... address) {
        return withArray((add, project) -> project.setAddress(add), address);
    }

    public ProjectBuilder withProjectManager(ProcessRole... projectManager) {
        return withArray((processRole, project) -> project.setProjectManager(processRole), projectManager);
    }

    public ProjectBuilder withDuration(Long... durations) {
        return withArray((duration, project) -> project.setDurationInMonths(duration), durations);
    }

    public ProjectBuilder withApplication(Application... application){
        return withArray((app, project) -> project.setApplication(app), application);
    }
}
