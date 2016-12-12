package org.innovateuk.ifs.user.builder;

import org.innovateuk.ifs.BaseBuilder;
import org.innovateuk.ifs.user.resource.UserRoleType;
import org.innovateuk.ifs.user.resource.RoleResource;

import java.util.List;
import java.util.function.BiConsumer;

import static org.innovateuk.ifs.base.amend.BaseBuilderAmendFunctions.setField;
import static org.innovateuk.ifs.base.amend.BaseBuilderAmendFunctions.uniqueIds;
import static java.util.Collections.emptyList;

public class RoleResourceBuilder extends BaseBuilder<RoleResource, RoleResourceBuilder> {
    private RoleResourceBuilder(List<BiConsumer<Integer, RoleResource>> multiActions) {
        super(multiActions);
    }

    public static RoleResourceBuilder newRoleResource() {
        return new RoleResourceBuilder(emptyList()).with(uniqueIds());
    }

    @Override
    protected RoleResourceBuilder createNewBuilderWithActions(List<BiConsumer<Integer, RoleResource>> actions) {
        return new RoleResourceBuilder(actions);
    }

    @Override
    protected RoleResource createInitial() {
        return new RoleResource();
    }

    public RoleResourceBuilder withId(Long... ids) {
        return withArray((id, role) -> role.setId(id), ids);
    }

    public RoleResourceBuilder withType(UserRoleType... types) {
        return withArray((type, role) -> role.setName(type.getName()), types);
    }

    public RoleResourceBuilder withName(String... names) {
        return withArray((name, role) -> role.setName(name), names);
    }

    public RoleResourceBuilder withUrl(String... urls) {
        return withArray((name, role) -> role.setUrl(name), urls);
    }
}
