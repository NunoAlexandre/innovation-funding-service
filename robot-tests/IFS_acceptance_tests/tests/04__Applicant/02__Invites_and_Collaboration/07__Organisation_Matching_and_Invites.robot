*** Settings ***
Documentation     IFS-1324 IFS-1325
Suite Setup       The guest user opens the browser
Suite Teardown    Close browser and delete emails
Force Tags        Applicant  Email
Resource          ../../../resources/defaultResources.robot

# In those test cases we add Operating address as well. So we don't click the checkbox = use same address

*** Test Cases ***
Two lead applicants users signing up as a Business organisation with the same details can succesfully invite eachother
    [Tag]  Failing
    Given we create a new user  ${openCompetitionBusinessRTO}  Business  collab  businesscollab@gmail.com  ${BUSINESS_TYPE_ID}
    And the user logs out if they are logged in
    And we create a new user  ${openCompetitionBusinessRTO}  Business  lead  businesslead@gmail.com  ${BUSINESS_TYPE_ID}

    When the user invites collaborator by email address  businesslead@gmail.com  businesscollab@gmail.com
    And the user logs out if they are logged in
    And the user reads his email and clicks the link   businesscollab@gmail.com    Invitation to assess 'Sustainable living models for the future'    We are inviting you to assess applications for the competition   2

    #Then the user is able to confirm the invite
    #And sees the invite application on his dashboard


#Two lead applicants users signing up as a Research organisation with the same details can successfully invite eachother
#    Given we create a new user  ${openCompetitionRTO}  Business lead  researchlead@gmail.com  ${ACADEMIC_TYPE_ID}
#    When the user reads his email and clicks the link  academiclead@gmail.com  Please verify your email address  Once verified you can sign into your account  1
#    Given we create a new user  ${openCompetitionRTO}  Business collab  researchcollab@gmail.com  ${ACADEMIC_TYPE_ID}
#    When the user reads his email and clicks the link  academiclead@gmail.com  Please verify your email address  Once verified you can sign into your account  1

#    When Log in as a different user


# Scenario of matching Research Organisations - Organisations matching - New accounts created
New Research user applies to Competition and starts application
    [Documentation]  IFS-1325
    [Tags]
    Given the user creates new account and organisation  radio-2  ${openCompetitionRTO}
    When the user inserts the address of his research organisation  p.o. box 42  coventry  cv4 7al
    Then the user enters text to a text field    email  bob@minions.com
    And the user fills the create account form  Bob  Minion
    And the user verifies account and starts his application  bob@minions.com
    [Teardown]  logout as user

Another Research user applies to Competition and starts application
    [Documentation]  IFS-1325
    [Tags]
    Given the user creates new account and organisation  radio-2  ${openCompetitionRTO}
    When the user inserts the address of his research organisation  P.O. BOX 42  Coventry  CV4 7AL
    Then the user enters text to a text field    email  stuart@minions.com
    And the user fills the create account form  Stuart  Minion
    And the user verifies account and starts his application  stuart@minions.com

Researcher invites other researcher from same organisation in his application and the latter is able to accept
    [Documentation]  IFS-1325
    [Tags]
    Given the user navigates to the Application Team Page  stuart@minions.com
    Then the user updates his organisation inviting the user  Bob  bob@minions.com
    When log in as a different user
    Then the user reads his email and clicks the link  ${invite_email}  Invitation to collaborate in ${openCompetitionBusinessRTO_name}  You will be joining as part of the organisation  2

#CV4 7AL

*** Keywords ***
the user invites collaborator by email address
    [Arguments]    ${EXISTING_USER_EMAIL}    ${COLLAB_USER_EMAIL}
    the user clicks the button/link    link=${UNTITLED_APPLICATION_DASHBOARD_LINK}
    the user clicks the button/link           link=view and manage contributors and collaborators
    the user clicks the button/link           jQuery=a:contains("Update and add contributors from ${UNTITLED_APPLICATION_NAME}")
    the user clicks the button/link            jQuery=button:contains("Add another contributor")
    The user enters text to a text field      name=stagedInvite.name    research collab
    The user enters text to a text field       name=stagedInvite.email    ${EXISTING_USER_EMAIL}
    the user clicks the button/link            jQuery=button:contains("Invite")

the user creates new account and organisation
    [Arguments]  ${organisationType}  ${compId}
    the user navigates to the page     ${server}/competition/${compId}/overview
    the user clicks the button/link    link=Start new application
    the user clicks the button/link    link=Create account
    the user selects the radio button  organisationTypeId  ${organisationType}
    the user clicks the button/link    css=button[type="submit"]

the user inserts the address of his research organisation
    [Arguments]  ${streetOne}  ${city}  ${postcode}
    the user enters text to a text field  organisationSearchName  Warwick
    the user clicks the button/link       link=University of Warwick
    the user clicks the button/link       link=Enter address manually
    the user enters text to a text field  addressForm.selectedPostcode.addressLine1  ${streetOne}
    the user enters text to a text field  addressForm.selectedPostcode.town  ${city}
    the user enters text to a text field  addressForm.selectedPostcode.postcode  ${postcode}
    the user clicks the button/link       jQuery=button:contains("Save organisation and continue")
    the user clicks the button/link       jQuery=button:contains("Save and continue")

the user verifies account and starts his application
    [Arguments]  ${email}
    the user reads his email and clicks the link  ${email}  Please verify your email address  you can sign into your account.
    the user clicks the button/link               jQuery=.button:contains("Sign in")
    logging in and error checking                 ${email}  ${correct_password}
    the user clicks the button/link               link=${UNTITLED_APPLICATION_DASHBOARD_LINK}
    the user clicks the button/link               link=Begin application
    the user clicks the button/link               link=Application details
    the user enters text to a text field          application_details-title  ${email}'s Application
    the user clicks the button/link               jQuery=button:contains("Save and return to application overview")

the user navigates to the Application Team Page
    [Arguments]  ${email}
    the user navigates to the page   ${dashboard_url}
    the user clicks the button/link  ${email}'s Application
    the user clicks the button/link  link=view and manage contributors and collaborators

the user updates his organisation inviting the user
    [Arguments]  ${name}  ${email}
    the user clicks the button/link       link=Update and add contributors from University of Warwick
    the user clicks the button/link       link=Add another contributor
    the user enters text to a text field  stagedInvite.name  ${name}
    the user enters text to a text field  stagedInvite.email  ${email}
    the user clicks the button/link       css=button[name="executeStagedInvite"]
