*** Settings ***
Documentation     INFUND-2601 As a competition administrator I want a view of all applications at the 'Funders Panel' stage
Suite Setup       Log in as user    email=lee.bowman@innovateuk.test    password=Passw0rd
Suite Teardown    the user closes the browser
Force Tags        CompAdmin
Resource          ../../../resources/defaultResources.robot

*** Variables ***
${funders_panel_competition_url}    ${server}/management/competition/${FUNDERS_PANEL_COMPETITION}/applications
${dialogue_warning_message}    Are you sure you wish to inform applicants if they have been successful in gaining funding?
${email_success_message}    We are pleased to inform you that your application
${email_failure_message}    Unfortunately Innovate UK is unable to fund

*** Test Cases ***
Notify applicants should be disabled
    [Documentation]    INFUND-2601
    [Tags]    HappyPath
    When the user navigates to the page    ${funders_panel_competition_url}
    Then the user should see the text in the page    Funders Panel
    And the option to notify applicants is disabled

User should be able to Notify applicants when the fund project have chosen
    [Documentation]    INFUND-2601
    [Tags]    HappyPath
    When the user selects the option from the drop-down menu    Yes    id=fund25
    And the user selects the option from the drop-down menu    No    id=fund26
    Then the option to notify applicants is enabled

Autosave of the page should work
    [Documentation]    INFUND-2885
    [Tags]
    When the user reloads the page
    Then the user should see the dropdown option selected    Yes    id=fund25
    And the user should see the dropdown option selected    No    id=fund26
    And the option to notify applicants is enabled

When a Fund Project option is unselected the Notify button become disabled
    [Documentation]    INFUND-2601
    [Tags]
    When the user selects the option from the drop-down menu    -    id=fund25
    Then the option to notify applicants is disabled

Pushing the notify applicants button brings up a warning dialogue
    [Documentation]    INFUND-2646
    [Tags]    HappyPath
    [Setup]    The user selects the option from the drop-down menu    Yes    id=fund25
    When the user clicks the button/link    jQuery=.button:contains("Notify applicants")
    Then the user should see the text in the page    ${dialogue_warning_message}
    And the user should see the element    jQuery=button:contains("Cancel")
    And the user should see the element    jQuery=.button:contains("Notify applicants")

Choosing cancel on the dialogue goes back to the Funder's Panel page
    [Documentation]    INFUND-2646
    [Tags]
    When the user clicks the button/link    jQuery=button:contains("Cancel")
    Then the user should be redirected to the correct page    ${funders_panel_competition_url}
    And the user should see the text in the page    Funders Panel
    [Teardown]    The user clicks the button/link    jQuery=.button:contains("Notify applicants")

Choosing Notify applicants on the dialogue redirects to the Assessor feedback page
    [Documentation]    INFUND-2646
    [Tags]    HappyPath
    When the user clicks the button/link    name=publish
    Then the user should be redirected to the correct page    ${funders_panel_competition_url}

Once applicants are notified, the whole state of the competition changes to Assessor feedback
    [Documentation]    INFUND-2646
    [Tags]    HappyPath
    Then the user should see the text in the page    Assessor Feedback

Successful applicants are notified of the funding decision
    [Documentation]    INFUND-2603
    [Tags]    Email
    Then the user reads his email from the default mailbox    worth.email.test+fundsuccess@gmail.com    Your application into the competition ${FUNDERS_PANEL_COMPETITION_NAME}    pleased to inform you

Unsuccessful applicants are notified of the funding decision
    [Documentation]    INFUND-2603
    [Tags]    Email
    Then the user reads his email from the second default mailbox    worth.email.test.two+fundfailure@gmail.com    Your application into the competition ${FUNDERS_PANEL_COMPETITION_NAME}    unable to fund your application

Successful applicants can see the assessment outcome on the dashboard page
    [Documentation]    INFUND-2604
    [Tags]    HappyPath
    [Setup]    Log in as a different user    &{successful_applicant_credentials}
    When the user navigates to the page    ${server}
    Then the user should see the text in the page    Projects in setup
    And the successful application shows in the project setup section
    And the successful application shows in the previous applications section

Successful applicants can see the assessment outcome on the overview page
    [Documentation]    INFUND-2605, INFUND-2611
    [Tags]    HappyPath
    When the user clicks the button/link    link=${FUNDERS_PANEL_APPLICATION_1_HEADER}
    Then the user should see the text in the page    Project setup status
    And the user should be redirected to the correct page    ${SUCCESSFUL_FUNDERS_PANEL_PROJECT_PAGE}

Unsuccessful applicants can see the assessment outcome on the dashboard page
    [Documentation]    INFUND-2605
    [Tags]
    [Setup]    Log in as a different user    &{unsuccessful_applicant_credentials}
    When the user navigates to the page    ${server}
    Then the user should not see the text in the page    Projects in setup
    And the unsuccessful application shows in the previous applications section

Unsuccessful applicants can see the assessment outcome on the overview page
    [Documentation]    INFUND-2604
    [Tags]
    When the user clicks the button/link    link=${FUNDERS_PANEL_APPLICATION_2_HEADER}
    Then the user should not see the text in the page    Project setup status
    And the user should see the text in the page    Your application has not been successful in this competition

*** Keywords ***
The option to notify applicants is disabled
    the user should see the element    css=#publish-funding-decision.button.disabled

The option to notify applicants is enabled
    the user should see the element    id=publish-funding-decision
    the user should not see the element    css=#publish-funding-decision.button.disabled

the successful application shows in the project setup section
    Element Should Contain    css=section.projects-in-setup    Sensing & Control network using the lighting infrastructure

the successful application shows in the previous applications section
    Element Should Contain    css=section.previous-applications    Sensing & Control network using the lighting infrastructure

the unsuccessful application shows in the previous applications section
    Element Should Contain    css=section.previous-applications    Matter - Planning for Web
