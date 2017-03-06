package org.innovateuk.ifs.application.domain;

import org.innovateuk.ifs.competition.domain.Competition;
import org.innovateuk.ifs.finance.domain.ApplicationFinance;
import org.innovateuk.ifs.form.domain.FormInput;
import org.innovateuk.ifs.form.domain.FormInputResponse;
import org.innovateuk.ifs.user.domain.ProcessRole;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.innovateuk.ifs.category.builder.InnovationAreaBuilder.newInnovationArea;
import static org.innovateuk.ifs.form.builder.FormInputBuilder.newFormInput;
import static org.innovateuk.ifs.form.builder.FormInputResponseBuilder.newFormInputResponse;
import static org.junit.Assert.assertEquals;

public class ApplicationTest {
    private Application application;

    private Competition competition;
    private String name;
    private List<ProcessRole> processRoles;
    private ApplicationStatus applicationStatus;
    private Long id;
    private List<ApplicationFinance> applicationFinances;

    @Before
    public void setUp() throws Exception {
        id = 0L;
        name = "testApplicationName";
        applicationStatus = new ApplicationStatus();
        competition = new Competition();
        applicationFinances = new ArrayList<>();

        processRoles = new ArrayList<>();
        processRoles.add(new ProcessRole());
        processRoles.add(new ProcessRole());
        processRoles.add(new ProcessRole());

        application = new Application(competition, name, processRoles, applicationStatus, id);
    }

    @Test
    public void applicationShouldReturnCorrectAttributeValues() throws Exception {
        Assert.assertEquals(application.getId(), id);
        Assert.assertEquals(application.getName(), name);
        Assert.assertEquals(application.getApplicationStatus(), applicationStatus);
        Assert.assertEquals(application.getProcessRoles(), processRoles);
        Assert.assertEquals(application.getCompetition(), competition);
        Assert.assertEquals(application.getApplicationFinances(), applicationFinances);
    }
    @Test
    public void addFormInputResponse() {
    	FormInput fi = newFormInput().build();
    	FormInputResponse fir = newFormInputResponse().withFormInputs(fi).build();
    	
    	application.addFormInputResponse(fir);
    	
    	assertEquals(1, application.getFormInputResponses().size());
    	assertEquals(fir, application.getFormInputResponses().get(0));
    }
    
    @Test
    public void addFormInputResponsesForDifferentInputs() {
    	FormInput fi1 = newFormInput().build();
    	FormInputResponse fir1 = newFormInputResponse().withFormInputs(fi1).build();
    	FormInput fi2 = newFormInput().build();
    	FormInputResponse fir2 = newFormInputResponse().withFormInputs(fi2).build();
    	
    	application.addFormInputResponse(fir1);
    	application.addFormInputResponse(fir2);
    	
    	assertEquals(2, application.getFormInputResponses().size());
    	assertEquals(fir1, application.getFormInputResponses().get(0));
    	assertEquals(fir2, application.getFormInputResponses().get(1));
    }
    
    @Test
    public void addFormInputResponsesForSameInputs() {
    	FormInput fi = newFormInput().build();
    	FormInputResponse fir1 = newFormInputResponse().withFormInputs(fi).withValue("1").build();
    	FormInputResponse fir2 = newFormInputResponse().withFormInputs(fi).withValue("2").build();
    	
    	application.addFormInputResponse(fir1);
    	application.addFormInputResponse(fir2);
    	
    	assertEquals(1, application.getFormInputResponses().size());
    	assertEquals(fir1, application.getFormInputResponses().get(0));
    	assertEquals("2", application.getFormInputResponses().get(0).getValue());
    }

    @Test(expected=IllegalStateException.class)
    public void addingInnovationAreaAndThenNotApplicableShouldResultInIllegalStateException() {
        Application application = new Application();
        application.setInnovationArea(newInnovationArea().build());
        application.setNoInnovationAreaApplicable(true);
    }

    @Test(expected=IllegalStateException.class)
    public void addingNotApplicableAndThenInnovationAreaShouldResultInIllegalStateException() {
        Application application = new Application();
        application.setNoInnovationAreaApplicable(true);
        application.setInnovationArea(newInnovationArea().build());
    }
}
