package org.innovateuk.ifs.project.queries.form;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.validator.constraints.NotBlank;
import org.innovateuk.ifs.commons.validation.constraints.EnumValidator;
import org.innovateuk.ifs.commons.validation.constraints.WordCount;
import org.innovateuk.ifs.controller.BaseBindingResultTarget;
import org.innovateuk.ifs.notesandqueries.resource.thread.FinanceChecksSectionType;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.Size;

public class FinanceChecksQueriesAddQueryForm extends BaseBindingResultTarget {

    @NotBlank(message = "{validation.notesandqueries.query.required}")
    @Size(max = FinanceChecksQueriesFormConstraints.MAX_QUERY_CHARACTERS, message = "{validation.notesandqueries.query.character.length.max}")
    @WordCount(max = FinanceChecksQueriesFormConstraints.MAX_QUERY_WORDS, message = "{validation.notesandqueries.query.word.length.max}")
    private String query;

    @NotBlank(message = "{validation.notesandqueries.thread.title.required}")
    @Size(max = FinanceChecksQueriesFormConstraints.MAX_TITLE_CHARACTERS, message = "{validation.notesandqueries.thread.title.length.max}")
    private String queryTitle;

    @Size(max = FinanceChecksQueriesFormConstraints.MAX_SECTION_CHARACTERS, message = "{validation.notesandqueries.thread.section.length.max}")
    @EnumValidator( enumClazz=FinanceChecksSectionType.class, message="{validation.notesandqueries.thread.section.enum}")
    private String section;

    private MultipartFile attachment;

    public MultipartFile getAttachment() {
        return attachment;
    }

    public void setAttachment(MultipartFile attachment) {
        this.attachment = attachment;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getQueryTitle() {
        return queryTitle;
    }

    public void setQueryTitle(String title) {
        this.queryTitle = title;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FinanceChecksQueriesAddQueryForm that = (FinanceChecksQueriesAddQueryForm) o;

        if (query != null ? !query.equals(that.query) : that.query != null) return false;
        if (queryTitle != null ? !queryTitle.equals(that.queryTitle) : that.queryTitle != null) return false;
        if (section != null ? !section.equals(that.section) : that.section != null) return false;
        return attachment != null ? attachment.equals(that.attachment) : that.attachment == null;

    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(query)
                .append(queryTitle)
                .append(section)
                .append(attachment)
                .toHashCode();
    }
}
