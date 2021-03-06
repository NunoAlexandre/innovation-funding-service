package org.innovateuk.ifs.testdata.builders;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.commons.lang3.tuple.Pair;
import org.innovateuk.ifs.application.domain.Application;
import org.innovateuk.ifs.application.resource.ApplicationResource;
import org.innovateuk.ifs.application.resource.QuestionApplicationCompositeId;
import org.innovateuk.ifs.application.resource.QuestionResource;
import org.innovateuk.ifs.file.domain.FileEntry;
import org.innovateuk.ifs.form.domain.FormInput;
import org.innovateuk.ifs.form.domain.FormInputResponse;
import org.innovateuk.ifs.form.resource.FormInputResource;
import org.innovateuk.ifs.form.resource.FormInputResponseCommand;
import org.innovateuk.ifs.form.resource.FormInputScope;
import org.innovateuk.ifs.form.resource.FormInputType;
import org.innovateuk.ifs.testdata.builders.data.ApplicationQuestionResponseData;
import org.innovateuk.ifs.user.domain.ProcessRole;
import org.innovateuk.ifs.user.resource.ProcessRoleResource;
import org.innovateuk.ifs.user.resource.UserResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

import static java.util.Collections.emptyList;
import static org.innovateuk.ifs.util.CollectionFunctions.*;

/**
 * Handles applicant responses to Questions
 */
public class QuestionResponseDataBuilder extends BaseDataBuilder<ApplicationQuestionResponseData, QuestionResponseDataBuilder> {

    private static final Logger LOG = LoggerFactory.getLogger(QuestionResponseDataBuilder.class);

    private static Cache<Pair<Long, String>, List<FormInputResource>> formInputsByCompetitionIdAndQuestionName = CacheBuilder.newBuilder().build();

    public QuestionResponseDataBuilder withApplication(ApplicationResource application) {
        return with(data -> data.setApplication(application));
    }

    public QuestionResponseDataBuilder forQuestion(String questionName) {
        return with(data -> data.setQuestionName(questionName));
    }

    public QuestionResponseDataBuilder withAnswer(String value, String updatedBy) {
        return with(data -> {
            UserResource updateUser = retrieveUserByEmail(updatedBy);
            doAs(updateUser, () -> doAnswerQuestion(data.getQuestionName(), value, updateUser, data));
        });
    }

    public QuestionResponseDataBuilder withFileUploads(List<String> fileUploads, String uploadedBy) {
        return with(data -> {
            UserResource updateUser = retrieveUserByEmail(uploadedBy);
            doAs(updateUser, () -> {
                List<FormInputResource> formInputs = getFormInputsForQuestion(data.getQuestionName(), data);
                List<FormInputResource> fileUploadInputs = simpleFilter(formInputs, fi -> FormInputType.FILEUPLOAD == fi.getType());

                zip(fileUploadInputs, fileUploads, (input, filename) -> {

                    FileEntry fileEntry = fileEntryRepository.save(new FileEntry(null, filename, "application/pdf", 7945));

                    List<FormInputResponse> response = formInputResponseRepository.findByApplicationIdAndFormInputId(data.getApplication().getId(), input.getId());

                    Optional<FormInputResponse> formInputResponseForUser = simpleFindFirst(response, fir -> updateUser.getId().equals(fir.getUpdatedBy().getUser().getId()));

                    FormInputResponse formInputResponse = formInputResponseForUser.orElseGet(() -> {

                        ProcessRoleResource processRole = retrieveApplicantByEmail(uploadedBy, data.getApplication().getId());
                        ProcessRole processRoleEntity = processRoleRepository.findOne(processRole.getId());
                        Application application = applicationRepository.findOne(data.getApplication().getId());
                        FormInput formInputEntity = formInputRepository.findOne(input.getId());
                        return new FormInputResponse(ZonedDateTime.now(), fileEntry, processRoleEntity, formInputEntity, application);
                    });

                    formInputResponse.setFileEntry(fileEntry);
                    formInputResponse.setUpdateDate(ZonedDateTime.now());
                    formInputResponseRepository.save(formInputResponse);
                });
            });
        });
    }

    public QuestionResponseDataBuilder withAssignee(String assignee) {
        return with(data -> {

            QuestionResource question = retrieveQuestionByCompetitionAndName(data.getQuestionName(), data.getApplication().getCompetition());
            ProcessRoleResource assigneeUser = retrieveApplicantByEmail(assignee, data.getApplication().getId());
            ProcessRoleResource assignedByUser = retrieveLeadApplicant(data.getApplication().getId());
            UserResource assigningUser = retrieveUserById(assignedByUser.getUser());

            doAs(assigningUser, () ->
                questionService.assign(new QuestionApplicationCompositeId(question.getId(), data.getApplication().getId()),
                        assigneeUser.getId(), assignedByUser.getId())
            );
        });
    }

    public QuestionResponseDataBuilder markAsComplete() {
        return with(data -> {
            QuestionResource question = retrieveQuestionByCompetitionAndName(data.getQuestionName(), data.getApplication().getCompetition());
            ProcessRoleResource lead = retrieveLeadApplicant(data.getApplication().getId());
            UserResource leadUser = retrieveUserById(lead.getUser());

            doAs(leadUser, () ->
                questionService.markAsComplete(new QuestionApplicationCompositeId(question.getId(), data.getApplication().getId()), lead.getId()));
        });
    }

    private void doAnswerQuestion(String questionName, String value, UserResource user, ApplicationQuestionResponseData data) {

        List<FormInputResource> formInputs = getFormInputsForQuestion(questionName, data);
        FormInputResource applicantFormInput = simpleFindFirst(formInputs, fi -> FormInputScope.APPLICATION.equals(fi.getScope())).get();

        FormInputResponseCommand updateRequest = new FormInputResponseCommand(
                applicantFormInput.getId(), data.getApplication().getId(), user.getId(), value);

        FormInputResponse response = formInputService.saveQuestionResponse(updateRequest).getSuccessObjectOrThrowException();
        formInputResponseRepository.save(response);
    }

    private List<FormInputResource> getFormInputsForQuestion(String questionName, ApplicationQuestionResponseData data) {
        Long competitionId = data.getApplication().getCompetition();

        return fromCache(Pair.of(competitionId, questionName), formInputsByCompetitionIdAndQuestionName, () -> {
            QuestionResource question = retrieveQuestionByCompetitionAndName(questionName, competitionId);
            return formInputService.findByQuestionId(question.getId()).getSuccessObjectOrThrowException();
        });
    }

    public static QuestionResponseDataBuilder newApplicationQuestionResponseData(ServiceLocator serviceLocator) {

        return new QuestionResponseDataBuilder(emptyList(), serviceLocator);
    }

    private QuestionResponseDataBuilder(List<BiConsumer<Integer, ApplicationQuestionResponseData>> multiActions,
                                        ServiceLocator serviceLocator) {
        super(multiActions, serviceLocator);
    }

    @Override
    protected QuestionResponseDataBuilder createNewBuilderWithActions(List<BiConsumer<Integer, ApplicationQuestionResponseData>> actions) {
        return new QuestionResponseDataBuilder(actions, serviceLocator);
    }

    @Override
    protected ApplicationQuestionResponseData createInitial() {
        return new ApplicationQuestionResponseData();
    }

    @Override
    protected void postProcess(int index, ApplicationQuestionResponseData instance) {
        super.postProcess(index, instance);
        LOG.info("Created Response to Application '{}', Question '{}'", instance.getApplication().getName(), instance.getQuestionName());
    }
}
