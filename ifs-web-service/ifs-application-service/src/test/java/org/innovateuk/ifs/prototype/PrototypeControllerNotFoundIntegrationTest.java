package org.innovateuk.ifs.prototype;

import org.innovateuk.ifs.BaseWebIntegrationTest;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import static org.junit.Assert.assertEquals;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * Integration test of the Prototype Controller but without the "prototypes" profile to check that requests are not found.
 */
@Ignore("Ignored since fetching these pages invokes org.innovateuk.ifs.interceptors.AlertMessageHandlerInterceptor.addAlertMessages. Needs running ifs-data-service which is not deployed to the embedded Tomcat")
@ActiveProfiles("example")
public class PrototypeControllerNotFoundIntegrationTest extends BaseWebIntegrationTest {

    @Value("http://localhost:${local.server.port}")
    private String baseWebUrl;

    private RestTemplate template = new TestRestTemplate();

    private static final String KNOWN_PROTOTYPE = "/prototypes/631-finances-assigned-to-you";

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void test_getPrototypeIndex() throws Exception {
        ResponseEntity<String> entity = template.getForEntity(baseWebUrl + "/prototypes", String.class);
        assertEquals(NOT_FOUND, entity.getStatusCode());
    }

    @Test
    public void test_getPrototypePage() throws Exception {
        ResponseEntity<String> entity = template.getForEntity(baseWebUrl + KNOWN_PROTOTYPE, String.class);
        assertEquals(NOT_FOUND, entity.getStatusCode());
    }

}
