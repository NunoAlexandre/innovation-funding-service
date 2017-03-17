package org.innovateuk.ifs.project.projectdetails.form;

import org.innovateuk.ifs.commons.validation.ValidationConstants;
import org.innovateuk.ifs.commons.validation.constraints.EmailRequiredIfOptionIs;
import org.innovateuk.ifs.commons.validation.constraints.FieldRequiredIfOptionIs;
import org.innovateuk.ifs.controller.BaseBindingResultTarget;

import javax.validation.constraints.NotNull;

@FieldRequiredIfOptionIs(required = "name", argument = "projectManager", predicate = -1L, message = "{validation.project.invite.name.required}")
@EmailRequiredIfOptionIs(required = "email", argument = "projectManager", predicate = -1L, regexp = ValidationConstants.EMAIL_DISALLOW_INVALID_CHARACTERS_REGEX, message = "{validation.project.invite.email.required}", invalidMessage= "{validation.project.invite.email.invalid}")
public class ProjectManagerForm extends BaseBindingResultTarget {
    @NotNull(message = "{validation.projectmanagerform.projectmanager.required}")
	private Long projectManager;

	public Long getProjectManager() {
		return projectManager;
	}

	public void setProjectManager(Long projectManager) {
		this.projectManager = projectManager;
	}

	private String name;

	private String email;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

}
