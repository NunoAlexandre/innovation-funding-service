*** Settings ***
Documentation     INFUND-39: As an applicant and I am on the application overview, I can select a section of the application, so I can see the status of each subsection in this section
...
...               INFUND-1072: As an Applicant I want to see the Application Overview page redesigned so that they meet the agreed style
Suite Setup       Log in create a new invite application invite academic collaborators and accept the invite
Suite Teardown    TestTeardown User closes the browser
Resource          ../../../resources/GLOBAL_LIBRARIES.robot
Resource          ../../../resources/variables/GLOBAL_VARIABLES.robot
Resource          ../../../resources/variables/User_credentials.robot
Resource          ../../../resources/keywords/Login_actions.robot
Resource          ../../../resources/keywords/User_actions.robot
Resource          ../../../resources/keywords/SUITE_SET_UP_ACTIONS.robot

*** Test Cases ***
Status changes when we assign a question
    [Documentation]    INFUND-39
    [Tags]    Applicant    Overview    HappyPath
    [Setup]    Guest user log-in    &{lead_applicant_credentials}
    Given the user navigates to the page    ${DASHBOARD_URL}
    And the user clicks the button/link    link=Academic robot test application
    And the user clicks the button/link    link=Project summary
    When the Applicant edits the Project summary
    And the applicant assigns the Project Summary    Arsene Wenger
    Then the assign assign status should be correct for the Project Summary
    And the blue flag should not be visible

Re-assign is possible from the overview page
    [Documentation]    INFUND-39
    [Tags]    Applicant    Overview
    Given the user navigates to the page    ${DASHBOARD_URL}
    And the user clicks the button/link    link=Academic robot test application
    When the applicant assigns the Project Summary question from the overview page    Steve Smith
    Then a blue flag should be visible for the Project Summary in overview page
    And the assign button should say Assigned to:You

*** Keywords ***
the Applicant edits the Project summary
    Clear Element Text    css=#form-input-11 .editor
    Input Text    css=#form-input-11 .editor    Check last updated date@#$
    Focus    css=.app-submit-btn
    Sleep    2s

the assign assign status should be correct for the Project Summary
    Given the user navigates to the page    ${DASHBOARD_URL}
    And the user clicks the button/link    link=Academic robot test application
    Page Should Contain Element    jQuery=#section-1 .section:nth-child(2) .column-third button strong
    Element Should Contain    jQuery=#section-1 .section:nth-child(2) .column-third button strong    Arsene Wenger

the applicant assigns the Project Summary question from the overview page
    [Arguments]    ${assignee_name}
    Click Element    jQuery=#section-1 .section:nth-child(2) .assign-button button
    Click Element    jQuery=button:contains("${assignee_name}")

the applicant assigns the Project Summary
    [Arguments]    ${assignee_name}
    Click Element    css=#form-input-11 .assign-button button
    Click Element    jQuery=button:contains("${assignee_name}")

a blue flag should be visible for the Project Summary in overview page
    #Reload Page
    Wait Until Page Contains Element    jQuery=#section-1 .section:nth-child(2) .assigned

the blue flag should not be visible
    Element Should Not Be Visible    jQuery=#section-1 .section:nth-child(2) .assigned

the assign button should say Assigned to:You
    Element Should Contain    jQuery=#section-1 .section:nth-child(2) .column-third button strong    You
