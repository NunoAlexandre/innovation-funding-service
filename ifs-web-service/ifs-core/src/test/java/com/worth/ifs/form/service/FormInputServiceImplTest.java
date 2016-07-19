package com.worth.ifs.form.service;

import com.worth.ifs.BaseServiceUnitTest;
import com.worth.ifs.form.resource.FormInputResource;
import org.junit.Test;
import org.mockito.Mock;

import java.util.List;

import static com.worth.ifs.commons.rest.RestResult.restSuccess;
import static com.worth.ifs.form.builder.FormInputResourceBuilder.newFormInputResource;
import static com.worth.ifs.form.resource.FormInputScope.APPLICATION;
import static com.worth.ifs.form.resource.FormInputScope.ASSESSMENT;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.*;

public class FormInputServiceImplTest extends BaseServiceUnitTest<FormInputService> {

    @Mock
    private FormInputRestService formInputRestService;

    @Override
    protected FormInputService supplyServiceUnderTest() {
        return new FormInputServiceImpl();
    }

    @Test
    public void getOne() throws Exception {

    }

    @Test
    public void findApplicationInputsByQuestion() throws Exception {
        List<FormInputResource> expected = newFormInputResource()
                .build(2);

        Long questionId = 1L;

        when(formInputRestService.getByQuestionIdAndScope(questionId, APPLICATION)).thenReturn(restSuccess(expected));

        List<FormInputResource> response = service.findApplicationInputsByQuestion(questionId);

        assertSame(expected, response);
        verify(formInputRestService, only()).getByQuestionIdAndScope(questionId, APPLICATION);
    }

    @Test
    public void findAssessmentInputsByQuestion() throws Exception {
        List<FormInputResource> expected = newFormInputResource()
                .build(2);

        Long questionId = 1L;

        when(formInputRestService.getByQuestionIdAndScope(questionId, ASSESSMENT)).thenReturn(restSuccess(expected));

        List<FormInputResource> response = service.findApplicationInputsByQuestion(questionId);

        assertSame(expected, response);
        verify(formInputRestService, only()).getByQuestionIdAndScope(questionId, ASSESSMENT);
    }

    @Test
    public void findApplicationInputsByCompetition() throws Exception {
        List<FormInputResource> expected = newFormInputResource()
                .build(2);

        Long competitionId = 1L;

        when(formInputRestService.getByCompetitionIdAndScope(competitionId, APPLICATION)).thenReturn(restSuccess(expected));

        List<FormInputResource> response = service.findApplicationInputsByCompetition(competitionId);

        assertSame(expected, response);
        verify(formInputRestService, only()).getByCompetitionIdAndScope(competitionId, APPLICATION);
    }
}