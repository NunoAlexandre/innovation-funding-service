package org.innovateuk.ifs.form.service;

import org.innovateuk.ifs.BaseRestServiceUnitTest;
import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.commons.service.ParameterizedTypeReferences;

import org.innovateuk.ifs.form.resource.FormInputResource;
import org.innovateuk.ifs.form.resource.FormInputResponseResource;
import org.innovateuk.ifs.form.resource.FormInputScope;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.innovateuk.ifs.commons.service.ParameterizedTypeReferences.formInputResourceListType;

import static org.innovateuk.ifs.form.resource.FormInputScope.APPLICATION;
import static java.lang.String.format;
import static org.junit.Assert.*;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

public class FormInputRestServiceImplTest extends BaseRestServiceUnitTest<FormInputRestServiceImpl> {

    private static String formInputRestUrl = "/forminput";

    @Override
    protected FormInputRestServiceImpl registerRestServiceUnderTest() {
        FormInputRestServiceImpl formInputRestService = new FormInputRestServiceImpl();
        formInputRestService.setFormInputRestURL(formInputRestUrl);
        return formInputRestService;
    }

    @Test
    public void testGetByQuestionIdAndScope() throws Exception {
        List<FormInputResource> expected = Arrays.asList(1,2,3).stream().map(i -> new FormInputResource()).collect(Collectors.toList());


        Long questionId = 1L;
        FormInputScope scope = FormInputScope.APPLICATION;

        setupGetWithRestResultExpectations(String.format("%s/findByQuestionId/%s/scope/%s", formInputRestUrl, questionId, scope), formInputResourceListType(), expected, OK);
        List<FormInputResource> response = service.getByQuestionIdAndScope(questionId, scope).getSuccessObject();
        assertSame(expected, response);
    }

    @Test
    public void testGetByCompetitionIdAndScope() throws Exception {
        List<FormInputResource> expected = Arrays.asList(1,2,3).stream().map(i -> new FormInputResource()).collect(Collectors.toList());

        Long competitionId = 1L;
        FormInputScope scope = FormInputScope.APPLICATION;

        setupGetWithRestResultExpectations(String.format("%s/findByCompetitionId/%s/scope/%s", formInputRestUrl, competitionId, scope), formInputResourceListType(), expected, OK);
        List<FormInputResource> response = service.getByCompetitionIdAndScope(competitionId, scope).getSuccessObject();
        assertSame(expected, response);
    }

    @Test
    public void testSave() throws Exception {
        FormInputResource expected = new FormInputResource();

        setupPutWithRestResultExpectations(formInputRestUrl + "/", FormInputResource.class, expected, expected);
        RestResult<FormInputResource> result = service.save(expected);
        assertTrue(result.isSuccess());
        Assert.assertEquals(expected, result.getSuccessObject());
    }

    @Test
    public void testDelete() throws Exception {
        Long formInputId = 1L;

        ResponseEntity<Void> result = setupDeleteWithRestResultExpectations(formInputRestUrl + "/" + formInputId);
        assertEquals(NO_CONTENT, result.getStatusCode());
    }

}
