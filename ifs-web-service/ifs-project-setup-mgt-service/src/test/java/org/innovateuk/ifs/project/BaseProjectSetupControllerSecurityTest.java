package org.innovateuk.ifs.project;

import org.innovateuk.ifs.project.status.security.SetupSectionsPermissionRules;
import org.innovateuk.ifs.security.BaseControllerSecurityTest;
import org.junit.Before;

import java.util.function.Consumer;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public abstract class BaseProjectSetupControllerSecurityTest<ControllerType> extends BaseControllerSecurityTest<ControllerType> {

    private SetupSectionsPermissionRules permissionRules;

    @Before
    public void lookupPermissionRules() {
        permissionRules = getMockPermissionRulesBean(SetupSectionsPermissionRules.class);
    }

    protected void assertSecured(Runnable invokeControllerFn) {
        assertAccessDenied(
                invokeControllerFn::run,
                () -> getVerification().accept(verify(permissionRules, times(1)))
        );
    }

    protected abstract Consumer<SetupSectionsPermissionRules> getVerification();
}
