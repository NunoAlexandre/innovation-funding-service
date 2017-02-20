package org.innovateuk.ifs.project.financechecks.form;

import org.hibernate.validator.constraints.NotBlank;
import org.innovateuk.ifs.commons.validation.constraints.WordCount;
import org.innovateuk.ifs.controller.BaseBindingResultTarget;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.Size;

public class FinanceChecksQueryResponseForm extends BaseBindingResultTarget {
    @NotBlank(message = "{validation.notesandqueries.response.required}")
    @Size(max = FinanceChecksQueryConstraints.MAX_QUERY_CHARACTERS, message = "{validation.notesandqueries.response.character.length.max}")
    @WordCount(max = FinanceChecksQueryConstraints.MAX_QUERY_WORDS, message = "{validation.notesandqueries.response.word.length.max}")
    private String response;

    private MultipartFile attachment;

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public MultipartFile getAttachment() {
        return attachment;
    }

    public void setAttachment(MultipartFile attachment) {
        this.attachment = attachment;
    }
}
