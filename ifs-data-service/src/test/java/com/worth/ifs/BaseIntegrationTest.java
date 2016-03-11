package com.worth.ifs;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static com.worth.ifs.BuilderAmendFunctions.clearUniqueIds;

/**
 * This is the base class for all integration tests against a configured Spring application.  Subclasses of this base can be
 * of the form of either integration tests with a running server ({@link BaseWebIntegrationTest}) or without
 * (e.g. {@link BaseRepositoryIntegrationTest}).
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@TestPropertySource(locations="classpath:application-web-integration-test.properties")
public abstract class BaseIntegrationTest {

    @Before
    public void resetBuilderIds() {
        clearUniqueIds();
    }
}