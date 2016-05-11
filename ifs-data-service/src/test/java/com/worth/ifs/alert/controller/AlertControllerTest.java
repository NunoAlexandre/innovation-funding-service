package com.worth.ifs.alert.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.worth.ifs.BaseControllerMockMVCTest;
import com.worth.ifs.alert.resource.AlertResource;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.worth.ifs.alert.builder.AlertResourceBuilder.newAlertResource;
import static com.worth.ifs.alert.domain.AlertType.MAINTENANCE;
import static com.worth.ifs.commons.service.ServiceResult.serviceSuccess;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyString;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AlertControllerTest extends BaseControllerMockMVCTest<AlertController> {

    private ObjectMapper objectMapper = setupObjectMapper();

    @Before
    public void setUp() throws Exception {
    }

    @Override
    protected AlertController supplyControllerUnderTest() {
        return new AlertController();
    }

    @Test
    public void test_findAllVisible() throws Exception {
        final AlertResource expected1 = newAlertResource()
                .withId(8888L)
                .build();

        final AlertResource expected2 = newAlertResource()
                .withId(9999L)
                .build();

        final List<AlertResource> expected = new ArrayList<>(asList(expected1, expected2));

        when(alertServiceMock.findAllVisible()).thenReturn(serviceSuccess(expected));

        mockMvc.perform(get("/alert/findAllVisible"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("[0]id", is(8888)))
                .andExpect(jsonPath("[1]id", is(9999)));
    }

    @Test
    public void test_findAllVisibleByType() throws Exception {
        final AlertResource expected1 = newAlertResource()
                .withId(8888L)
                .build();

        final AlertResource expected2 = newAlertResource()
                .withId(9999L)
                .build();

        final List<AlertResource> expected = new ArrayList<>(asList(expected1, expected2));

        when(alertServiceMock.findAllVisibleByType(MAINTENANCE)).thenReturn(serviceSuccess(expected));

        mockMvc.perform(get("/alert/findAllVisible/MAINTENANCE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("[0]id", is(8888)))
                .andExpect(jsonPath("[1]id", is(9999)));
    }

    @Test
    public void test_getById() throws Exception {
        final AlertResource expected = newAlertResource()
                .withId(9999L)
                .build();

        when(alertServiceMock.findById(9999L)).thenReturn(serviceSuccess(expected));

        mockMvc.perform(get("/alert/{id}", 9999L))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(expected)));
    }

    @Test
    public void test_create() throws Exception {
        final AlertResource alertResource = newAlertResource()
                .build();

        final AlertResource expected = newAlertResource()
                .withId(9999L)
                .build();

        when(alertServiceMock.create(alertResource)).thenReturn(serviceSuccess(expected));

        mockMvc.perform(post("/alert/")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(alertResource)))
                .andExpect(status().isCreated())
                .andExpect(content().string(objectMapper.writeValueAsString(expected)));
    }

    @Test
    public void test_delete() throws Exception {
        when(alertServiceMock.delete(9999L)).thenReturn(serviceSuccess());

        mockMvc.perform(delete("/alert/{id}", 9999L))
                .andExpect(status().isNoContent())
                .andExpect(content().string(isEmptyString()));
    }


    @Test
    public void test_deleteAllByType() throws Exception {
        when(alertServiceMock.deleteAllByType(MAINTENANCE)).thenReturn(serviceSuccess());

        mockMvc.perform(delete("/alert/delete/MAINTENANCE"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(isEmptyString()));
    }

    private ObjectMapper setupObjectMapper() {
        final ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.registerModule(new JavaTimeModule());
    }
}