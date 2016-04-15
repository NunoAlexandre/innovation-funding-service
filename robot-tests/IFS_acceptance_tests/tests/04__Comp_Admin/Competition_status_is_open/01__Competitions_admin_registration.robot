*** Settings ***
Documentation     INFUND-2129 As an internal Innovate UK user I want to be able to register with IFS as a competition administrator so that I can access the system with appropriate permissions for my role
...
...
...               INFUND-1987 As a Competition Administrator I want to be able to export specified data from all successfully submitted applications so that the competitions team can work with this data in the existing competitions database
Suite Setup       The guest user opens the browser
Suite Teardown    User closes the browser
Resource          ../../../resources/GLOBAL_LIBRARIES.robot
Resource          ../../../resources/variables/GLOBAL_VARIABLES.robot
Resource          ../../../resources/variables/User_credentials.robot
Resource          ../../../resources/keywords/Login_actions.robot
Resource          ../../../resources/keywords/User_actions.robot

*** Test Cases ***
If user from the list is not registered shouldn't be able to login
    [Documentation]    INFUND-2129
    Given the user navigates to the page    ${LOGIN_URL}
    When the guest user enters the log in credentials    worth.email.test+admin2@gmail.com    Passw0rd
    And the user clicks the button/link    css=button[name="_eventId_proceed"]
    Then the user should see the text in the page    Your username/password combination doesn't seem to work

Registration for a user who is in the list
    [Documentation]    INFUND-2129
    [Tags]    HappyPath    Email
    [Setup]    Delete the emails from the test mailbox
    Given the user navigates to the page    ${COMPETITION_DETAILS_URL}
    And User creates new account verifies email and login    worth.email.test+admin1@gmail.com
    Then the user should be redirected to the correct page    ${COMP_ADMINISTRATOR}

*** Keywords ***
User creates new account verifies email and login
    [Arguments]    ${CREATE_ACCOUNT_EMAIL}
    When the user clicks the button/link    jQuery=.column-third .button:contains("Apply now")
    And the user clicks the button/link    jQuery=.button:contains("Sign in to apply")
    And the user clicks the button/link    jQuery=.button:contains("Create")
    And the user enters text to a text field    id=organisationSearchName    Innovate
    And the user clicks the button/link    id=org-search
    And the user clicks the button/link    LINK=INNOVATE LTD
    Select Checkbox    id=address-same
    And the user clicks the button/link    jQuery=.button:contains("Save organisation and continue")
    And the user clicks the button/link    jQuery=.button:contains("Save")
    And the user enters the details and clicks the create account    ${CREATE_ACCOUNT_EMAIL}
    And the user should be redirected to the correct page    ${REGISTRATION_SUCCESS}
    And the user opens the mailbox and verifies the email from
    And the user should be redirected to the correct page    ${REGISTRATION_VERIFIED}
    And the user clicks the button/link    jQuery=.button:contains("Log in")
    And the guest user inserts user email & password    ${CREATE_ACCOUNT_EMAIL}    Passw0rd
    And the guest user clicks the log-in button

the user enters the details and clicks the create account
    [Arguments]    ${CREATE_ACCOUNT_EMAIL}
    Input Text    id=firstName    Eric
    Input Text    id=lastName    Cantona
    Input Text    id=phoneNumber    0505050508
    Input Text    id=email    ${CREATE_ACCOUNT_EMAIL}
    Input Password    id=password    Passw0rd
    Input Password    id=retypedPassword    Passw0rd
    Select Checkbox    termsAndConditions
    Submit Form
