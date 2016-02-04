package com.worth.ifs.finance.controller;

import com.worth.ifs.finance.domain.CostField;
import com.worth.ifs.finance.mapper.CostFieldMapper;
import com.worth.ifs.finance.transactional.CostFieldService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CostFieldControllerTest {
    @Mock
    CostFieldService costFieldService;

    @Mock
    CostFieldMapper costFieldMapper;

    private MockMvc mockMvc;

    @InjectMocks
    private CostFieldController costFieldController;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(costFieldController)
                .build();
    }

    @Test
    public void findAllShouldReturnListOfCostFields() throws Exception{
        when(costFieldService.findAll()).thenReturn(Arrays.asList(new CostField(), new CostField()));

        mockMvc.perform(get("/costfield/findAll/"))
                .andExpect(status().isOk());

        verify(costFieldService, times(1)).findAll();
        verifyNoMoreInteractions(costFieldService);
    }
}
