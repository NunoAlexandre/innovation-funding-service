*** Settings ***
Documentation     -INFUND-885: As an applicant I want to be able to submit a username (email address) and password combination to create a new profile so I can log into the system
Suite Setup       The guest user opens the browser
Suite Teardown    TestTeardown User closes the browser
Test Template     Invalid Email Check
Resource          ../../../resources/GLOBAL_LIBRARIES.robot
Resource          ../../../resources/variables/GLOBAL_VARIABLES.robot
Resource          ../../../resources/variables/User_credentials.robot
Resource          ../../../resources/keywords/Login_actions.robot
Resource          ../../../resources/keywords/Applicant_actions.robot

*** Variables ***
${valid_email}    _ewan__@worth.systems
${invalid_email_plain}    notavalidemail
${invalid_email_symbols}    @%^%#$@#$@#.com
${invalid_email_no_username}    @example.com
${invalid_email_format}    Joe Smith <email@example.com>
${invalid_email_no_at}    email.example.com
${invalid_email_no_domain}    joesmith@example

*** Test Cases ***    email
Invalid email plaintext
                      [Documentation]                 INFUND-885
                      [Tags]                          Account
                      ${invalid_email_plain}

Invalid email disallowed symbols
                      [Documentation]                 INFUND-885
                      [Tags]                          Account
                      ${invalid_email_symbols}

Invalid email no username
                      [Documentation]                 INFUND-885
                      [Tags]                          Account
                      ${invalid_email_no_username}

Invalid email format
                      [Documentation]                 INFUND-885
                      [Tags]                          Account
                      ${invalid_email_format}

Invalid email no @ symbol
                      [Documentation]                 INFUND-885
                      [Tags]                          Account
                      ${invalid_email_no_at}

*** Keywords ***
Invalid Email Check
    [Arguments]    ${invalid_email}
    Given the user is on the account creation page
    When the user inputs a first name
    And the user inputs a last name
    And the user inputs a phone number
    And the user inputs the invalid email address    ${invalid_email}
    And the user inputs a valid password
    And the user retypes the password correctly
    And the user submits their information
    Then the user should see an error
    And the user cannot login with the invalid email    ${invalid_email}

the user is on the account creation page
    go to    ${ACCOUNT_CREATION_FORM_URL}

the user inputs a first name
    Input Text    id=firstName    John

the user inputs a last name
    Input Text    id=lastName    Smith

the user inputs a phone number
    Input Text    id=phoneNumber    01141234567

the user inputs the invalid email address
    [Arguments]    ${invalid_email_addy}
    Input Text    id=email    ${invalid_email_addy}

the user inputs a valid password
    Input Password    id=password    password

the user retypes the password correctly
    Input Password    id=retypedPassword    password

the user submits their information
    Execute Javascript    jQuery('form').attr('novalidate','novalidate');
    Select Checkbox    termsAndConditions
    Submit Form

the user should see an error
    Page Should Contain    We were unable to create your account

the user cannot login with the invalid email
    [Arguments]    ${invalid_email_addy}
    go to    ${LOGIN_URL}
    Input Text    id=username    ${invalid_email_addy}
    Input Password    id=password    password
    Execute Javascript    jQuery('form').attr('novalidate','novalidate');
    Click Button    css=button[name="_eventId_proceed"]
    # Page Should Contain    Your login was unsuccessful
    # Page Should Contain    Please enter a valid e-mail address
    Page Should Contain    The username you entered cannot be identified
