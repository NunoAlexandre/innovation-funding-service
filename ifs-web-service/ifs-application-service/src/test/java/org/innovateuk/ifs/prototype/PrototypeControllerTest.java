package org.innovateuk.ifs.prototype;

import org.innovateuk.ifs.BaseControllerMockMVCTest;
import org.junit.Before;
import org.junit.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Controller test class using Spring MVC test support to test expected routes and their responses.
 */
public class PrototypeControllerTest extends BaseControllerMockMVCTest<PrototypeController> {

    @Override
    protected PrototypeController supplyControllerUnderTest() {
        return new PrototypeController();
    }

    @Before
    public void setUp() {
        super.setUp();
    }

    @Test
    public void test_getPrototypeIndex() throws Exception {
        mockMvc.perform(get("/prototypes"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("/prototypes/index"));
    }

    @Test
    public void test_getPrototypePage() throws Exception {
        mockMvc.perform(get("/prototypes/sample-template"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("/prototypes/sample-template"));
    }

}
