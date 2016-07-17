*** Settings ***
Documentation     INFUND-901: As a lead applicant I want to invite application contributors to collaborate with me on the application, so that they can contribute to the application in a collaborative competition
...
...
...               INFUND-896: As a lead applicant i want to invite partner organisations to collaborate on line in my application, so that i can create the consortium needed to complete the proposed project
...
...
...               INFUND-2375: Error message needed on contributors invite if user tries to add duplicate email address
Suite Setup       Login and create a new application
Suite Teardown    TestTeardown User closes the browser
Force Tags        Collaboration    Applicant
Resource          ../../../resources/GLOBAL_LIBRARIES.robot
Resource          ../../../resources/variables/GLOBAL_VARIABLES.robot
Resource          ../../../resources/variables/User_credentials.robot
Resource          ../../../resources/keywords/Login_actions.robot
Resource          ../../../resources/keywords/User_actions.robot

*** Variables ***
${INVITE_COLLABORATORS2_PAGE}    ${SERVER}/application/3/contributors/invite?newApplication
${APPLICATION_3_TEAM_PAGE}    ${SERVER}/application/3/contributors

*** Test Cases ***
lead applicant can add/remove partners
    [Documentation]    INFUND-901
    [Tags]    HappyPath
    Given The user navigates to the invitation page of the test application
    When The user clicks the button/link    jquery=li:nth-child(1) button:contains('Add person')
    Then The user should see the element    css=li:nth-child(1) tr:nth-of-type(2) td:nth-of-type(1)
    And The user clicks the button/link    jquery=li:nth-child(1) button:contains('Remove')
    Then The user should not see the element    css=li:nth-child(1) tr:nth-of-type(2) td:nth-of-type(1)

lead applicant cannot remove himself
    [Documentation]    INFUND-901
    [Tags]
    Then the lead applicant cannot be removed

Validations for the Email field
    [Documentation]    INFUND-901
    [Tags]    HappyPath
    When The user clicks the button/link    jquery=li:nth-child(1) button:contains('Add person')
    And the applicant fills the lead organisation fields    Collaborator01    @hiveit.co.uk
    Then the user should see an error    not a well-formed email address

Validations for the name field
    [Documentation]    INFUND-901
    [Tags]
    When the applicant fills the lead organisation fields    ${EMPTY}    ewan+5@hiveit.co.uk
    Then the user should see an error    ${empty_field_warning_message}

Link to remove partner organisation
    [Documentation]    INFUND-1039
    [Tags]    HappyPath
    # on the user interface.    All we can test is that the state is saved in cookie, so not lost on page reload.
    When The user clicks the button/link    jquery=li:nth-last-child(1) button:contains('Add additional partner organisation')
    And the applicant inputs details    1
    Then The user should see the element    jquery=li:nth-child(2) button:contains('Remove')
    When The user clicks the button/link    jquery=li:nth-child(2) button:contains('Remove')
    Then The user should not see the text in the page    Organisation name

Autosaved works (in cookie)
    [Documentation]    INFUND-1039
    [Tags]    HappyPath
    When The user clicks the button/link    jquery=li:nth-last-child(1) button:contains('Add additional partner organisation')
    And the applicant can enter Organisation name, Name and E-mail
    Then the applicant's inputs should be visible

Blank organisation name is not allowed
    [Documentation]    INFUND-896
    [Tags]
    Given the user enters text to a text field    css=li:nth-child(1) tr:nth-of-type(2) td:nth-of-type(1) input    MR Tester
    When the applicant fills the Partner organisation fields    1    ${EMPTY}    Collaborator 7    ewan+6@hiveit.co.uk
    Then the user should see an error    ${empty_field_warning_message}

Blank person name is not allowed
    [Documentation]    INFUND-896
    [Tags]
    When the applicant fills the Partner organisation fields    1    Fannie May    ${EMPTY}    ewan+7@hiveit.co.uk
    Then the user should see an error    ${empty_field_warning_message}

Blank email is not allowed
    [Documentation]    INFUND-896
    [Tags]
    When the applicant fills the Partner organisation fields    1    Fannie May    Collaborator 10    ${EMPTY}
    And browser validations have been disabled
    And The user clicks the button/link    jquery=button:contains("Save Changes")
    Then the user should see an error    ${empty_field_warning_message}

Invalid email address is not allowed
    [Documentation]    INFUND-896
    [Tags]
    When the applicant fills the Partner organisation fields    1    Fannie May    Collaborator 10    collaborator10_invalid_email
    Then the user should see an error    not a well-formed email address

Already invite email should is not allowed
    [Tags]
    When the applicant fills the Partner organisation fields    1    Fannie May    Collaborator 10    ewan+5@hiveit.co.uk
    Then the user should see an error    You have already added this email address

Link to add multiple partner organisation
    [Tags]    HappyPath
    When The user clicks the button/link    jquery=li:nth-last-child(1) button:contains('Add additional partner organisation')
    And The user should see the element    css=li:nth-child(3)
    And The user clicks the button/link    jQuery=li:nth-child(3) button:contains("Remove")
    Then The user should not see the element    jQuery=li:nth-child(3) button:contains("Remove")

The user's inputs should be autosaved
    [Documentation]    INFUND-901
    [Tags]    HappyPath
    When the user fills the name and email field and reloads the page    1
    Then the user's inputs should still be visible    1
    And the user navigates to the page    ${INVITE_COLLABORATORS2_PAGE}
    And the user should not see the element    css=li:nth-child(1) tr:nth-of-type(2) td:nth-of-type(1) input
    And the user should not see the text in the page    Collaborator01
    And the user should not see the text in the page    ewan+8@hiveit.co.uk
    When the user navigates to the page    ${APPLICATION_3_TEAM_PAGE}
    And the user clicks the button/link    jQuery=.button:contains("Invite new contributors")
    Then the user should not see the element    css=li:nth-child(1) tr:nth-of-type(2) td:nth-of-type(1) input
    And the user should not see the text in the page    Collaborator01
    And the user should not see the text in the page    ewan+8@hiveit.co.uk

*** Keywords ***
the user fills the name and email field and reloads the page
    [Arguments]    ${group_number}
    Wait Until Element Is Visible    css=li:nth-child(1) tr:nth-of-type(2) td:nth-of-type(1)
    Input Text    css=li:nth-child(1) tr:nth-of-type(2) td:nth-of-type(1) input    Collaborator01
    Input Text    css=li:nth-child(1) tr:nth-of-type(2) td:nth-of-type(2) input    ewan+8@hiveit.co.uk
    Input Text    name=organisations[${group_number}].organisationName    Test name
    Input Text    css=li:nth-last-child(2) tr:nth-of-type(1) td:nth-of-type(1) input    Collaborator test
    Input Text    css=li:nth-last-child(2) tr:nth-of-type(1) td:nth-of-type(2) input    ewan+9@hiveit.co.uk
    sleep    500ms
    focus    jquery=li:nth-child(1) button:contains('Add person')
    the user reloads the page

the user's inputs should still be visible
    [Arguments]    ${group_number}
    Textfield Value Should Be    css=li:nth-child(1) tr:nth-of-type(2) td:nth-of-type(1) input    Collaborator01
    ${input_value} =    Get Value    css=li:nth-child(1) tr:nth-of-type(2) td:nth-of-type(2) input
    Should Be Equal As Strings    ${input_value}    ewan+8@hiveit.co.uk
    Textfield Value Should Be    name=organisations[${group_number}].organisationName    Test name
    Textfield Value Should Be    css=li:nth-last-child(2) tr:nth-of-type(1) td:nth-of-type(1) input    Collaborator test
    ${input_value} =    Get Value    css=li:nth-last-child(2) tr:nth-of-type(1) td:nth-of-type(2) input
    Should Be Equal As Strings    ${input_value} =    ${input_value} =

the lead applicant cannot be removed
    Element Should Contain    css=li:nth-child(1) tr:nth-of-type(1) td:nth-of-type(3)    Lead applicant

the applicant fills the lead organisation fields
    [Arguments]    ${LEAD_NAME}    ${LEAD_EMAIL}
    Input Text    css=li:nth-child(1) tr:nth-of-type(2) td:nth-of-type(1) input    ${LEAD_NAME}
    Input Text    css=li:nth-child(1) tr:nth-of-type(2) td:nth-of-type(2) input    ${LEAD_EMAIL}
    # the following keyword disables the browser's validation
    Execute Javascript    jQuery('form').attr('novalidate','novalidate');
    Focus    jquery=button:contains("Save Changes")
    browser validations have been disabled
    Click Element    jquery=button:contains("Save Changes")
    sleep    500ms

the applicant can enter Organisation name, Name and E-mail
    Input Text    name=organisations[1].organisationName    Fannie May
    Input Text    css=li:nth-child(2) tr:nth-of-type(1) td:nth-of-type(1) input    Collaborator 2
    Input Text    css=li:nth-child(2) tr:nth-of-type(1) td:nth-of-type(2) input    ewan+10@hiveit.co.uk
    Click Element    jquery=li:nth-child(2) button:contains('Add person')
    Input Text    css=li:nth-child(2) tr:nth-of-type(2) td:nth-of-type(1) input    Collaborator 3
    Input Text    css=li:nth-child(2) tr:nth-of-type(2) td:nth-of-type(2) input    ewan+11@hiveit.co.uk
    focus    jquery=li:nth-child(2) button:contains('Add person')
    Sleep    500ms
    the user reloads the page

the applicant's inputs should be visible
    Textfield Value Should Be    name=organisations[1].organisationName    Fannie May
    ${input_value} =    Get Value    name=organisations[1].organisationName
    Should Be Equal As Strings    ${input_value}    Fannie May
    Textfield Value Should Be    css=li:nth-child(2) tr:nth-of-type(1) td:nth-of-type(1) input    Collaborator 2
    ${input_value} =    Get Value    css=li:nth-child(2) tr:nth-of-type(1) td:nth-of-type(1) input
    Should Be Equal As Strings    ${input_value}    Collaborator 2
    Textfield Value Should Be    css=li:nth-child(2) tr:nth-of-type(2) td:nth-of-type(1) input    Collaborator 3
    ${input_value} =    Get Value    css=li:nth-child(2) tr:nth-of-type(2) td:nth-of-type(1) input
    Should Be Equal As Strings    ${input_value}    Collaborator 3

the applicant inputs details
    [Arguments]    ${group_number}
    Input Text    name=organisations[${group_number}].organisationName    Fannie May
    Input Text    css=li:nth-last-child(2) tr:nth-of-type(1) td:nth-of-type(1) input    Collaborator 2
    Input Text    css=li:nth-last-child(2) tr:nth-of-type(1) td:nth-of-type(2) input    ewan+10@hiveit.co.uk

the applicant fills the Partner organisation fields
    [Arguments]    ${group_number}    ${PARTNER_ORG_NAME}    ${ORG_NAME}    ${EMAIL_NAME}
    browser validations have been disabled
    Input Text    name=organisations[${group_number}].organisationName    ${PARTNER_ORG_NAME}
    Input Text    css=li:nth-last-child(2) tr:nth-of-type(1) td:nth-of-type(1) input    ${ORG_NAME}
    Input Text    css=li:nth-last-child(2) tr:nth-of-type(1) td:nth-of-type(2) input    ${EMAIL_NAME}
    # the following keyword disables the browser's validation
    Focus    jquery=button:contains("Save Changes")
    Click Element    jquery=button:contains("Save Changes")
    sleep    500ms

a validation error is shown on organisation name
    [Arguments]    ${group_number}
    Wait Until Element Is Visible    css=input[name='organisations[${group_number}].organisationName'].field-error

Login and create a new application
    Given Guest user log-in    &{lead_applicant_credentials}
    When the user navigates to the page    ${COMPETITION_DETAILS_URL}
    And the user clicks the button/link    jQuery=.button:contains("Apply now")
    And the user clicks the button/link    jQuery=.button:contains("Apply now")
    And the user selects the radio button    create-application    true
    And the user clicks the button/link    jQuery=.button:contains("Continue")
    And the user clicks the button/link    jquery=button:contains("Begin application")
    And the user clicks the button/link    link=Application details
    And the user enters text to a text field    id=application_details-title    Invitation page test
    And the user clicks the button/link    jQuery=button:contains("Save and return")

The user navigates to the invitation page of the test application
    Given the user navigates to the page    ${DASHBOARD_URL}
    And the user clicks the button/link    link=Invitation page test
    And the user should see the text in the page    View team members and add collaborators
    And the user clicks the button/link    link=View team members and add collaborators
    And The user clicks the button/link    jQuery=.button:contains("Invite new contributors")
