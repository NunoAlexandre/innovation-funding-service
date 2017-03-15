package org.innovateuk.ifs.user.repository;

import org.innovateuk.ifs.BaseRepositoryIntegrationTest;
import org.innovateuk.ifs.user.domain.Agreement;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.innovateuk.ifs.user.builder.AgreementBuilder.newAgreement;
import static org.junit.Assert.*;

public class AgreementRepositoryIntegrationTest extends BaseRepositoryIntegrationTest<AgreementRepository> {

    @Override
    @Autowired
    protected void setRepository(AgreementRepository repository) {
        this.repository = repository;
    }

    @Test
    public void findByCurrentTrue() throws Exception {
        loginPaulPlum();

        repository.deleteAll();
        List<Agreement> agreements = newAgreement()
                .withId(null, null)
                .withText("foo", "bar")
                .withAnnexA("annexA1", "annexA2")
                .withAnnexB("annexB1", "annexB2")
                .withAnnexC("annexC1", "annexC2")
                .withCurrent(true, false)
                .build(2);
        repository.save(agreements);

        Agreement expectedAgreement = agreements.get(0);
        Agreement agreement = repository.findByCurrentTrue();

        assertEquals(expectedAgreement.getText(), agreement.getText());
        assertEquals(expectedAgreement.getAnnexA(), agreement.getAnnexA());
        assertEquals(expectedAgreement.getAnnexB(), agreement.getAnnexB());
        assertEquals(expectedAgreement.getAnnexC(), agreement.getAnnexC());
        assertTrue(agreement.isCurrent());

        assertEquals(getPaulPlum().getId(), agreement.getCreatedBy().getId());
        assertEquals(getPaulPlum().getId(), agreement.getModifiedBy().getId());
        assertNotNull(agreement.getCreatedOn());
        assertNotNull(agreement.getModifiedOn());
    }
}
