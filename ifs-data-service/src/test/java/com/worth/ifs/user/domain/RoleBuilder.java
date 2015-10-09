package com.worth.ifs.user.domain;

import com.worth.ifs.BaseBuilder;
import com.worth.ifs.Builder;

import java.util.List;
import java.util.function.Consumer;

/**
 * Created by dwatson on 08/10/15.
 */
public class RoleBuilder extends BaseBuilder<Role> {

    private RoleBuilder() {
        super();
    }

    private RoleBuilder(List<Consumer<Role>> actions) {
        super(actions);
    }

    public static RoleBuilder newRole() {
        return new RoleBuilder();
    }

    @Override
    protected RoleBuilder createNewBuilderWithActions(List<Consumer<Role>> actions) {
        return new RoleBuilder(actions);
    }

    @Override
    protected Role createInitial() {
        return new Role();
    }

    public RoleBuilder withType(UserRoleType type) {
        return with(role -> role.setName(type.getName()));
    }
}
