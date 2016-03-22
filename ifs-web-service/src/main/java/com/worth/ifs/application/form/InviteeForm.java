package com.worth.ifs.application.form;

import com.worth.ifs.invite.constant.InviteStatusConstants;
import com.worth.ifs.util.FormUtil;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

import java.io.Serializable;

public class InviteeForm implements Serializable {
    private static final long serialVersionUID = 8494848676778443648L;

    private Long userId;
    @NotEmpty
    private String personName;
    @NotEmpty
    @Email(regexp = FormUtil.EMAIL_DISALLOW_INVALID_CHARACTERS_REGEX)
    private String email;
    private InviteStatusConstants inviteStatus;

    public InviteeForm(Long userId, String personName, String email) {
        this.userId = userId;
        this.personName = personName;
        this.email = email;
    }

    public InviteeForm() {
        this.personName = "";
        this.email = "";
    }

    public String getPersonName() {
        return personName;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setInviteStatus(InviteStatusConstants inviteStatus) {
        this.inviteStatus = inviteStatus;
    }

    public InviteStatusConstants getInviteStatus() {
        return inviteStatus;
    }
}
