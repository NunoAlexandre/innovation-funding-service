package com.worth.ifs.application.service;

import com.worth.ifs.BaseRestServiceUnitTest;
import com.worth.ifs.application.domain.Question;
import com.worth.ifs.application.resource.QuestionResource;
import com.worth.ifs.commons.rest.RestResult;
import com.worth.ifs.commons.rest.ValidationMessages;
import com.worth.ifs.commons.service.ParameterizedTypeReferences;
import org.junit.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;
import java.util.concurrent.Future;

import static com.worth.ifs.application.builder.QuestionBuilder.newQuestion;
import static com.worth.ifs.application.builder.QuestionResourceBuilder.newQuestionResource;
import static com.worth.ifs.application.service.Futures.settable;
import static com.worth.ifs.commons.service.ParameterizedTypeReferences.longsListType;
import static com.worth.ifs.commons.service.ParameterizedTypeReferences.questionResourceListType;
import static com.worth.ifs.commons.service.ParameterizedTypeReferences.validationMessagesListType;
import static java.util.Arrays.asList;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.GET;

public class QuestionRestServiceMocksTest extends BaseRestServiceUnitTest<QuestionRestServiceImpl> {

    private static final String questionRestURL = "/question";


    @Override
    protected QuestionRestServiceImpl registerRestServiceUnderTest() {
        QuestionRestServiceImpl questionRestService = new QuestionRestServiceImpl();
        questionRestService.questionRestURL = questionRestURL;
        return questionRestService;
    }

    @Test
    public void assignTest() {

        setupPutWithRestResultExpectations(questionRestURL + "/assign/1/2/3/4", Void.class, null, null);

        // now run the method under test
        assertTrue(service.assign(1L, 2L, 3L, 4L).isSuccess());
    }

    @Test
    public void findByCompetitionTest() {

        List<QuestionResource> questions = newQuestionResource().build(3);
        setupGetWithRestResultExpectations(questionRestURL + "/findByCompetition/1", questionResourceListType(), questions);

        // now run the method under test
        List<QuestionResource> returnedQuestions = service.findByCompetition(1L).getSuccessObject();

        // verify
        assertEquals(questions, returnedQuestions);
    }

    @Test
    public void findByIdTest() {

        QuestionResource question = newQuestionResource().build();
        setupGetWithRestResultExpectations(questionRestURL + "/id/1", QuestionResource.class, question);

        // now run the method under test
        QuestionResource returnedQuestion = service.findById(1L).getSuccessObject();

        // verify
        assertNotNull(returnedQuestion);
        assertEquals(question, returnedQuestion);
    }

    @Test
    public void getMarkedAsCompleteTest() throws Exception {
        String expectedUrl = dataServicesUrl + questionRestURL + "/getMarkedAsComplete/1/2";

        Long[] questionIds = new Long[]{3L, 4L, 5L};
        when(mockAsyncRestTemplate.exchange(expectedUrl, GET, httpEntityForRestCall(""), Long[].class)).thenReturn(settable(new ResponseEntity<>(questionIds, HttpStatus.OK)));

        // now run the method under test
        Set<Long> returnedQuestionIds = service.getMarkedAsComplete(1L, 2L).get();

        // verify
        assertNotNull(questionIds);
        assertEquals(3, returnedQuestionIds.size());
        assertEquals(new HashSet<>(Arrays.asList(questionIds)), returnedQuestionIds);
    }

    @Test
    public void markAsCompleteTest() {
        setupPutWithRestResultExpectations(questionRestURL + "/markAsComplete/1/2/3", validationMessagesListType(), null, null, HttpStatus.OK);
        assertTrue(service.markAsComplete(1L, 2L, 3L).isSuccess());
    }

    @Test
    public void markAsInCompleteTest() {
        setupPutWithRestResultExpectations(questionRestURL + "/markAsInComplete/1/2/3", Void.class, null, null);
        assertTrue(service.markAsInComplete(1L, 2L, 3L).isSuccess());
    }

    @Test
    public void updateNotificationTest() {
        setupPutWithRestResultExpectations(questionRestURL + "/updateNotification/1/true", Void.class, null, null);
        assertTrue(service.updateNotification(1L, true).isSuccess());
    }

    @Test
    public void getNextQuestionTest() {
        QuestionResource question = newQuestionResource().build();
        setupGetWithRestResultExpectations(questionRestURL + "/getNextQuestion/1", QuestionResource.class, question);

        QuestionResource nextQuestion = service.getNextQuestion(1L).getSuccessObject();
        assertEquals(question, nextQuestion);
    }

    @Test
    public void getPreviousQuestionTest() {
        QuestionResource question = newQuestionResource().build();
        setupGetWithRestResultExpectations(questionRestURL + "/getPreviousQuestion/2", QuestionResource.class, question);

        QuestionResource nextQuestion = service.getPreviousQuestion(2L).getSuccessObject();
        assertEquals(question, nextQuestion);
    }
}
