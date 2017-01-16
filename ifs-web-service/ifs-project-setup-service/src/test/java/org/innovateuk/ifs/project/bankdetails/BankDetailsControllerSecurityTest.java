package org.innovateuk.ifs.project.bankdetails;

import org.innovateuk.ifs.project.BankDetailsController;
import org.innovateuk.ifs.project.BaseProjectSetupControllerSecurityTest;
import org.innovateuk.ifs.project.ProjectSetupSectionsPermissionRules;
import org.innovateuk.ifs.user.resource.UserResource;
import org.junit.Ignore;
import org.junit.Test;

import java.util.function.Consumer;

import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;

@Ignore("Ignoring for now, as these will be fixed when dev is merged in prior to PR")
public class BankDetailsControllerSecurityTest extends BaseProjectSetupControllerSecurityTest<BankDetailsController> {

    @Override
    protected Class<? extends BankDetailsController> getClassUnderTest() {
        return BankDetailsController.class;
    }

    @Test
    public void testBankDetails() {
        assertSecured(() -> classUnderTest.bankDetails(null, 123L, null, null));
    }

    @Test
    public void testConfirmBankDetails() {
        assertSecured(() -> classUnderTest.confirmBankDetails(null, null, null, null, 123L, null));
    }

    @Test
    public void testManualAddress() {
        assertSecured(() -> classUnderTest.manualAddress(null, null, 123L, null));
    }

    @Test
    public void testSearchAddress() {
        assertSecured(() -> classUnderTest.searchAddress(null, 123L, null, null, null));
    }

    @Test
    public void testSelectAddress() {
        assertSecured(() -> classUnderTest.selectAddress(null, 123L, null, null));
    }

    @Test
    public void testSubmitBankDetails() {
        assertSecured(() -> classUnderTest.submitBankDetails(null, null, null, null, 123L, null));
    }

    @Override
    protected Consumer<ProjectSetupSectionsPermissionRules> getVerification() {
        return permissionRules -> permissionRules.partnerCanAccessBankDetailsSection(eq(123L), isA(UserResource.class));
    }
}
