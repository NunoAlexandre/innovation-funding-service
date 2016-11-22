package com.worth.ifs.application.domain;

import com.worth.ifs.form.domain.FormInput;

import javax.persistence.*;

/**
 * GuidanceRow defines database relations and columns for a row of guidance displayed next to a form input.
 */
@Entity
public class GuidanceRow {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_input_id", referencedColumnName = "id")
    private FormInput formInput;

    private String subject;

    private String justification;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public FormInput getFormInput() {
        return formInput;
    }

    public void setFormInput(FormInput formInput) {
        this.formInput = formInput;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getJustification() {
        return justification;
    }

    public void setJustification(String justification) {
        this.justification = justification;
    }
}
