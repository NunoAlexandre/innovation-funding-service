*** Settings ***
Documentation     INFUND-901: As a lead applicant I want to invite application contributors to collaborate with me on the application, so that they can contribute to the application in a collaborative competition
Suite Setup       Login as User    &{lead_applicant_credentials}
Suite Teardown    TestTeardown User closes the browser
Force Tags        Create new application    collaboration
Resource          ../../../resources/GLOBAL_LIBRARIES.robot
Resource          ../../../resources/variables/GLOBAL_VARIABLES.robot
Resource          ../../../resources/variables/User_credentials.robot
Resource          ../../../resources/keywords/Login_actions.robot
Resource          ../../../resources/keywords/Applicant_actions.robot

*** Variables ***
${INVITE_COLLABORATORS_PAGE}    ${SERVER}/application/1/contributors/invite?newApplication
${INVITE_COLLABORATORS2_PAGE}    ${SERVER}/application/2/contributors/invite?newApplication

*** Test Cases ***
The lead applicant should be able to add/remove a collaborator
    [Documentation]    INFUND-901
    Given the applicant is in the invite contributors page
    And the applicant clicks the add person link
    When a new line is added to the collaborator table
    And the applicant clicks the remove link
    Then the line should be removed

The user's inputs should be autosaved
    [Documentation]    INFUND-901
    Given the applicant is in the invite contributors page
    And the applicant clicks the add person link
    When the user fills the name and email field
    And the user reloads the page
    Then the user's inputs should still be visible
    And the user goes to the second invite page
    And the inputs of the first invite should not be visible

The lead applicant shouldn't be able to remove himself
    [Documentation]    INFUND-901
    Given the applicant is in the invite contributors page
    Then the lead applicant cannot be removed

Validations for the Email field
    [Documentation]    INFUND-901
    [Tags]
    Given the applicant is in the invite contributors page
    When the applicant enters some invalid emails
    Then the applicant should not be redirected to the next page

Validation for the name field
    [Documentation]    INFUND-901
    [Tags]
    Given the applicant is in the invite contributors page
    When the applicant submits the page without entering a name
    Then the applicant should get a validation error for the name field
    And the applicant should not be redirected to the next page

Valid submit
    [Documentation]    INFUND-901
    [Tags]
    Given the applicant is in the invite contributors page
    When the applicant enters valid inputs
    Then the applicant should be redirected to the overview page

*** Keywords ***
the applicant is in the invite contributors page
    go to    ${INVITE_COLLABORATORS_PAGE}

the applicant clicks the add person link
    Click Element    jquery=li:nth-child(1) button:contains('Add person')

a new line is added to the collaborator table
    Wait Until Element Is Visible    css=li:nth-child(1) tr:nth-of-type(2) td:nth-of-type(1)

the applicant clicks the remove link
    Click Element    jquery=li:nth-child(1) button:contains('Remove')
    sleep    1s

the line should be removed
    Element Should Not Be Visible    css=li:nth-child(1) tr:nth-of-type(2) td:nth-of-type(1)

the user fills the name and email field
    Wait Until Element Is Visible    css=li:nth-child(1) tr:nth-of-type(2) td:nth-of-type(1)
    Input Text    css=li:nth-child(1) tr:nth-of-type(2) td:nth-of-type(1) input    Collaborator01
    Input Text    css=li:nth-child(1) tr:nth-of-type(2) td:nth-of-type(2) input    tester@test.com
    focus    jquery=li:nth-child(1) button:contains('Add person')
    sleep    1s

the user reloads the page
    Reload Page

the user's inputs should still be visible
    Textfield Value Should Be    css=li:nth-child(1) tr:nth-of-type(2) td:nth-of-type(1) input    Collaborator01
    ${input_value} =    Get Value    css=li:nth-child(1) tr:nth-of-type(2) td:nth-of-type(2) input
    Should Be Equal As Strings    ${input_value}    tester@test.com

the lead applicant cannot be removed
    Element Should Contain    css=li:nth-child(1) tr:nth-of-type(1) td:nth-of-type(3)    That's you!

the user goes to the second invite page
    go to    ${INVITE_COLLABORATORS2_PAGE}

the inputs of the first invite should not be visible
    Element Should Not Be Visible    css=li:nth-child(1) tr:nth-of-type(2) td:nth-of-type(1) input

the applicant enters some invalid emails
    Input Text    css=li:nth-child(1) tr:nth-of-type(2) td:nth-of-type(1) input    Collaborator01
    Input Text    css=li:nth-child(1) tr:nth-of-type(2) td:nth-of-type(2) input    @example.com
    Click Element    jquery=button:contains("Begin application")
    sleep    1s

the applicant should not be redirected to the next page
    page should contain    Inviting Contributors

the applicant submits the page without entering a name
    Input Text    css=li:nth-child(1) tr:nth-of-type(2) td:nth-of-type(1) input    ${EMPTY}
    Input Text    css=li:nth-child(1) tr:nth-of-type(2) td:nth-of-type(2) input    test@example.com
    Click Element    jquery=button:contains("Begin application")
    sleep    1s

the applicant should get a validation error for the name field
    Element Should Be Visible    css=li:nth-child(1) tr:nth-of-type(2) td:nth-of-type(1) .field-error

the applicant enters valid inputs
    # Click Button    Remove
    Input Text    css=li:nth-child(1) tr:nth-of-type(2) td:nth-of-type(1) input    tester
    Input Text    css=li:nth-child(1) tr:nth-of-type(2) td:nth-of-type(2) input    test@example.com
    Click Element    jquery=button:contains("Begin application")
    sleep    1s

the applicant should be redirected to the overview page
    Sleep    1s
    Page Should Contain    Application overview
