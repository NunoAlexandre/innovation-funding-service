package com.worth.ifs;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.persistence.EntityManager;
import java.util.Arrays;
import java.util.List;

/**
 * This is the base class for all integration tests against a configured Spring application.  Subclasses of this base can be
 * of the form of either integration tests with a running server ({@link BaseWebIntegrationTest}) or without
 * (e.g. {@link BaseRepositoryIntegrationTest}).
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@TestPropertySource(locations="classpath:application-web-integration-test.properties")
public abstract class BaseIntegrationTest extends BaseTest {

    public static final int USER_COUNT  = 11;
    public static final List<String> ALL_USERS_EMAIL = Arrays.asList("steve.smith@empire.com", "jessica.doe@ludlow.co.uk", "paul.plum@gmail.com", "competitions@innovateuk.gov.uk", "finance@innovateuk.gov.uk", "pete.tom@egg.com", "felix.wilson@gmail.com", "ewan+1@hiveit.co.uk", "ewan+2@hiveit.co.uk", "ewan+12@hiveit.co.uk");

    @Autowired
    private EntityManager em;

    protected void flushAndClearSession() {
        em.flush();
        em.clear();
    }

}