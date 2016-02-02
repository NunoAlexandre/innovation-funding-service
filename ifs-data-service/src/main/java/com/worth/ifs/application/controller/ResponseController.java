package com.worth.ifs.application.controller;

import com.worth.ifs.application.domain.Application;
import com.worth.ifs.application.domain.Response;
import com.worth.ifs.application.repository.ApplicationRepository;
import com.worth.ifs.application.repository.QuestionRepository;
import com.worth.ifs.application.repository.ResponseRepository;
import com.worth.ifs.application.transactional.ResponseService;
import com.worth.ifs.assessment.dto.Feedback;
import com.worth.ifs.assessment.transactional.AssessorService;
import com.worth.ifs.commons.rest.RestResult;
import com.worth.ifs.commons.service.ServiceResult;
import com.worth.ifs.security.CustomPermissionEvaluator;
import com.worth.ifs.commons.error.Error;
import com.worth.ifs.user.domain.ProcessRole;
import com.worth.ifs.user.domain.Role;
import com.worth.ifs.user.repository.ProcessRoleRepository;
import com.worth.ifs.user.repository.RoleRepository;
import com.worth.ifs.user.repository.UserRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

import static com.worth.ifs.commons.controller.RestResultBuilder.newRestResult;
import static com.worth.ifs.transactional.BaseTransactionalService.Failures.NOT_FOUND_ENTITY;
import static com.worth.ifs.commons.rest.RestResults.internalServerError2;
import static com.worth.ifs.commons.rest.RestResults.ok2;
import static com.worth.ifs.user.domain.UserRoleType.ASSESSOR;
import static com.worth.ifs.util.EntityLookupCallbacks.getOrFail;

/**
 * ApplicationController exposes Application data and operations through a REST API.
 */
@RestController
@RequestMapping("/response")
public class ResponseController {

    private static final Error processRoleNotFoundError = new Error(NOT_FOUND_ENTITY, ProcessRole.class, ASSESSOR);
    private static final Error assessorRoleNotFoundError = new Error(NOT_FOUND_ENTITY, Role.class, ASSESSOR);


    @Autowired
    ApplicationRepository applicationRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    ProcessRoleRepository processRoleRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ResponseRepository responseRepository;

    @Autowired
    QuestionRepository questionRepository;

    @Autowired
    AssessorService assessorService;

    @Autowired
    CustomPermissionEvaluator permissionEvaluator;

    @Autowired
    ResponseService responseService;

    QuestionController questionController = new QuestionController();

    private final Log log = LogFactory.getLog(getClass());

    @RequestMapping("/findResponsesByApplication/{applicationId}")
    public List<Response> findResponsesByApplication(@PathVariable("applicationId") final Long applicationId){
        return responseService.findResponsesByApplication(applicationId);
    }

    @RequestMapping(value = "/saveQuestionResponse/{responseId}/assessorFeedback", params="assessorUserId", method = RequestMethod.PUT, produces = "application/json")
    public RestResult<Void> saveQuestionResponseAssessorScore(@PathVariable("responseId") Long responseId,
                                                              @RequestParam("assessorUserId") Long assessorUserId,
                                                              @RequestParam("feedbackValue") Optional<String> feedbackValue,
                                                              @RequestParam("feedbackText") Optional<String> feedbackText) {

        return newRestResult(Feedback.class, Void.class).
               andOnSuccess(ok2()).
               andWithDefaultFailure(internalServerError2()).
               perform(() -> {

            // TODO DW - INFUND-854 - get rid of get(0) occurrances in code below
            Response response = responseRepository.findOne(responseId);
            Application application = response.getApplication();
            return getOrFail(() -> roleRepository.findByName(ASSESSOR.name()), assessorRoleNotFoundError).map(assessorRole ->
                   getOrFail(() -> processRoleRepository.findByUserIdAndRoleAndApplicationId(assessorUserId, assessorRole.get(0), application.getId()), processRoleNotFoundError).map(assessorProcessRole ->
                   assessorService.updateAssessorFeedback(new Feedback().setResponseId(response.getId()).setAssessorProcessRoleId(assessorProcessRole.get(0).getId()).setValue(feedbackValue).setText(feedbackText))
            ));
        });
    }

    @RequestMapping(value= "/assessorFeedback/{responseId}/{assessorProcessRoleId}", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody Feedback getFeedback(@PathVariable("responseId") Long responseId,
                                               @PathVariable("assessorProcessRoleId") Long assessorProcessRoleId){
        ServiceResult<Feedback> feedback = assessorService.getFeedback(new Feedback.Id().setAssessorProcessRoleId(assessorProcessRoleId).setResponseId(responseId));
        // TODO DW - INFUND-854 - how do we return a generic envelope to be consumed? failure is currently simply returning null.
        return feedback.mapLeftOrRight(l -> null, r -> r);
    }


    @RequestMapping(value= "assessorFeedback/permissions/{responseId}/{assessorProcessRoleId}", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody List<String> permissions(@PathVariable("responseId") Long responseId,
                                                  @PathVariable("assessorProcessRoleId") Long assessorUserId){
        return permissionEvaluator.getPermissions(SecurityContextHolder.getContext().getAuthentication(),
                new Feedback.Id().setAssessorProcessRoleId(assessorUserId).setResponseId(responseId));
    }

    @RequestMapping(value= "assessorFeedback/permissions", method = RequestMethod.POST, produces = "application/json")
    public @ResponseBody List<String> permissions(@RequestBody Feedback feedback){
        return permissionEvaluator.getPermissions(SecurityContextHolder.getContext().getAuthentication(), feedback);
    }
}
