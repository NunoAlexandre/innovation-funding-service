*** Settings ***
Documentation     INFUND-399: As a client, I would like to demo the system with real-like logins per user role
...
...
...               INFUND-171: As a user, I am able to sign in providing a emailaddress and password, so I have access to my data
...
...
...               INFUND-2130: As a competition administrator I want to be able to log into IFS so that I can access the system with appropriate permissions for my role
Suite Teardown    TestTeardown User closes the browser
Force Tags        Guest
Resource          ../../resources/GLOBAL_LIBRARIES.robot
Resource          ../../resources/variables/GLOBAL_VARIABLES.robot
Resource          ../../resources/variables/User_credentials.robot
Resource          ../../resources/keywords/Login_actions.robot
Resource          ../../resources/keywords/User_actions.robot

*** Test Cases ***
Log-out
    [Tags]    HappyPath
    [Setup]    Guest user log-in    &{lead_applicant_credentials}
    Given the user should see the element        link=Logout
    Logout as user

Invalid Login
    [Tags]
    Given the user is not logged-in
    When the guest user enters the log in credentials    steve.smith@empire.com    Passw0rd2
    And the user clicks the button/link    css=button[name="_eventId_proceed"]
    Then the guest user should get an error message

Valid login as Applicant
    [Tags]    HappyPath
    Given the user is not logged-in
    When the guest user enters the log in credentials    steve.smith@empire.com    Passw0rd
    And the user clicks the button/link    css=button[name="_eventId_proceed"]
    Then the user should see the element        link=Logout
    And the user should be redirected to the correct page    ${applicant_dashboard_url}
    [Teardown]    Logout as user

Valid login as Collaborator
    [Tags]    HappyPath
    Given the user is not logged-in
    When the guest user enters the log in credentials    ${collaborator1_credentials["email"]}    ${collaborator1_credentials["password"]}
    And the user clicks the button/link    css=button[name="_eventId_proceed"]
    Then the user should see the element        link=Logout
    And the user should be redirected to the correct page    ${applicant_dashboard_url}
    [Teardown]    Logout as user

Valid login as Assessor
    [Documentation]    INFUND-286
    [Tags]    Assessor    HappyPath
    Given the user is not logged-in
    When the guest user enters the log in credentials    ${assessor_credentials["email"]}    ${assessor_credentials["password"]}
    And the user clicks the button/link    css=button[name="_eventId_proceed"]
    Then the user should see the element        link=Logout
    And the user should be redirected to the correct page    ${assessor_dashboard_url}
    And the user should be logged-in as an Assessor
    [Teardown]    Logout as user

Valid login as Comp Admin
    [Documentation]    INFUND-2130
    Given the user is not logged-in
    When the guest user enters the log in credentials    john.doe@innovateuk.test    Passw0rd
    And the user clicks the button/link    css=button[name="_eventId_proceed"]
    Then the user should see the element        link=Logout
    And the user should be redirected to the correct page    ${COMP_ADMINISTRATOR_OPEN}
    [Teardown]    Logout as user

Reset password (psw does not match)
    [Documentation]    INFUND-1889
    [Tags]    Email
    [Setup]    The guest user opens the browser
    Given the user navigates to the page    ${LOGIN_URL}
    When the user clicks the button/link    link=Forgot your password?
    And the user enters text to a text field    id=id_email    worth.email.test+changepsw@gmail.com
    And the user clicks the button/link    css=input.button
    Then the user should see the text in the page    If your email address is recognised, you’ll receive an email with instructions about how to reset your password.
    And the user opens the mailbox and clicks the reset link
    And the user should see the text in the page    Password reset
    And the user enters text to a text field    id=id_password    Passw0rdnew
    And the user enters text to a text field    id=id_retypedPassword    OtherPass2aa
    And browser validations have been disabled
    And the user clicks the button/link    jQuery=input[value*="Save password"]
    And the user should see an error    Passwords must match
    [Teardown]    TestTeardown User closes the browser

Reset password
    [Documentation]    INFUND-1889
    [Tags]    Email    HappyPath
    [Setup]    The guest user opens the browser
    Given the user navigates to the page    ${LOGIN_URL}
    When the user clicks the button/link    link=Forgot your password?
    And the user enters text to a text field    id=id_email    worth.email.test+changepsw@gmail.com
    And the user clicks the button/link    css=input.button
    Then the user should see the text in the page    If your email address is recognised, you’ll receive an email with instructions about how to reset your password.
    And the user opens the mailbox and clicks the reset link
    And the user should see the text in the page    Password reset
    And the user enters text to a text field    id=id_password    Passw0rdnew
    And the user enters text to a text field    id=id_retypedPassword    Passw0rdnew
    And the user clicks the button/link    jQuery=input[value*="Save password"]
    And the user should see the text in the page    Your password is updated, you can now sign in with your new password
    And the user clicks the button/link    jQuery=.button:contains("Sign in")
    When the guest user enters the log in credentials    worth.email.test+changepsw@gmail.com    Passw0rd
    And the user clicks the button/link    css=button[name="_eventId_proceed"]
    Then the guest user should get an error message
    When the guest user enters the log in credentials    worth.email.test+changepsw@gmail.com    Passw0rdnew
    And the user clicks the button/link    css=button[name="_eventId_proceed"]
    Then the user should see the element        link=Logout
    And the user should be redirected to the correct page    ${applicant_dashboard_url}

*** Keywords ***
the user is not logged-in
    the user should not see the element    link=My dashboard
    the user should not see the element    link=Logout

the guest user should get an error message
    the user should see the text in the page    Your login was unsuccessful because of the following issue(s)
    the user should see the text in the page    Your username/password combination doesn't seem to work
    the user should not see the element    link=Logout


the user should be logged-in as an Assessor
    Title Should Be    Assessor Dashboard - Innovation Funding Service

the user opens the mailbox and clicks the reset link
    Open Mailbox    server=imap.googlemail.com    user=worth.email.test@gmail.com    password=testtest1
    And the user opens the mailbox and verifies the email from
