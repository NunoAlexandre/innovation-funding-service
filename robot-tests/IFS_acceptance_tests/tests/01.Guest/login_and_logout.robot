*** Settings ***
Documentation     -INFUND-399: As a client, I would like to demo the system with real-like logins per user role
...
...
...               -INFUND-171: As a user, I am able to sign in providing a emailaddress and password, so I have access to my data
Suite Teardown    TestTeardown User closes the browser
Resource          ../../resources/GLOBAL_LIBRARIES.robot
Resource          ../../resources/variables/GLOBAL_VARIABLES.robot
Resource          ../../resources/variables/User_credentials.robot
Resource          ../../resources/keywords/Login_actions.robot
Resource          ../../resources/keywords/User_actions.robot

*** Test Cases ***
Log-out
    [Tags]    Guest    HappyPath
    [Setup]    Guest user log-in    &{lead_applicant_credentials}
    Given the Applicant is logged-in
    Logout as user

Invalid Login
    [Tags]    Guest
    Given the user is not logged-in
    When the guest user enters the log in credentials    steve.smith@empire.com    Passw0rd2
    And the user clicks the button/link    css=button[name="_eventId_proceed"]
    Then the guest user should get an error message

Valid login as Applicant
    [Tags]    Guest    HappyPath
    Given the user is not logged-in
    When the guest user enters the log in credentials    steve.smith@empire.com    Passw0rd
    And the user clicks the button/link    css=button[name="_eventId_proceed"]
    Then the Applicant is logged-in
    And the user should be redirected to the correct page    ${applicant_dashboard_url}
    [Teardown]    Logout as user

Valid login as Collaborator
    [Tags]    Guest    HappyPath
    Given the user is not logged-in
    When the guest user enters the log in credentials    ${collaborator1_credentials["email"]}    ${collaborator1_credentials["password"]}
    And the user clicks the button/link    css=button[name="_eventId_proceed"]
    Then the Applicant is logged-in
    And the user should be redirected to the correct page    ${applicant_dashboard_url}
    [Teardown]    Logout as user

Valid login as Assessor
    [Documentation]    INFUND-286
    [Tags]    Assessor    Guest    HappyPath
    Given the user is not logged-in
    When the guest user enters the log in credentials    ${assessor_credentials["email"]}    ${assessor_credentials["password"]}
    And the user clicks the button/link    css=button[name="_eventId_proceed"]
    Then the Applicant is logged-in
    And the user should be redirected to the correct page    ${assessor_dashboard_url}
    And the user should be logged-in as an Assessor
    [Teardown]    Logout as user

Reset password
    [Documentation]    INFUND-1889
    [Tags]    HappyPath    FailingForLocal
    [Setup]    The guest user opens the browser
    Given the user navigates to the page    ${LOGIN_URL}
    When the user clicks the button/link    link=Forgot your password?
    And the user enters text to a text field    id=id_email    worth.email.test+changepsw@gmail.com
    And the user clicks the button/link    css=input.button
    Then the user should see the text in the page    If your email address is recognised, you’ll receive an email with instructions about how to reset your password.
    And the user open the mailbox and clicks the reset link
    And the user should see the text in the page    Password reset
    And the user enters text to a text field    id=id_password    Passw0rdnew
    And the user enters text to a text field    id=id_retypedPassword    OtherPass2aa
    And the user clicks the button/link    css=input.button
    And the user should see an error    Passwords must match
    And the user enters text to a text field    id=id_password    Passw0rdnew
    And the user enters text to a text field    id=id_retypedPassword    Passw0rdnew
    And the user clicks the button/link    css=input.button
    And the user should see the text in the page    Your password is updated, you can now sign in with your new password
    And the user clicks the button/link    jQuery=.button:contains("Sign in")
    When the guest user enters the log in credentials    worth.email.test+changepsw@gmail.com    Passw0rd
    And the user clicks the button/link    css=button[name="_eventId_proceed"]
    Then the guest user should get an error message
    When the guest user enters the log in credentials    steve.smith@empire.com    Passw0rdnew
    And the user clicks the button/link    css=button[name="_eventId_proceed"]
    Then the Applicant is logged-in
    And the user should be redirected to the correct page    ${applicant_dashboard_url}

*** Keywords ***
the user is not logged-in
    Element Should Not Be Visible    link=My dashboard
    Element Should Not Be Visible    link=Logout

the guest user should get an error message
    Page Should Contain    Your login was unsuccessful because of the following issue(s)
    Page Should Contain    Your username/password combination doesn't seem to work
    Page Should Not Contain Element    link=Logout

the guest user clicks the log-in button
    Click Button    css=button[name="_eventId_proceed"]

The Applicant clicks the log-out button
    Click Element    link=Logout

the Applicant is logged-in
    Wait Until Element Is Visible    link=Logout

the user should be logged-in as an Assessor
    Title Should Be    Innovation Funding Service - Assessor Dashboard

the user open the mailbox and clicks the reset link
    Open Mailbox    server=imap.googlemail.com    user=worth.email.test@gmail.com    password=testtest1
    ${LATEST} =    wait for email    fromEmail=noresponse@innovateuk.gov.uk
    ${HTML}=    get email body    ${LATEST}
    log    ${HTML}
    ${LINK}=    Get Links From Email    ${LATEST}
    log    ${LINK}
    ${VERIFY_EMAIL}=    Get From List    ${LINK}    1
    log    ${VERIFY_EMAIL}
    go to    ${VERIFY_EMAIL}
    Capture Page Screenshot
    Delete All Emails
    close mailbox
