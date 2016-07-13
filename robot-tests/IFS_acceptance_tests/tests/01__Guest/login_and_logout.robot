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
    Given the user should see the element    link=Sign out
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
    Then the user should see the element    link=Sign out
    And the user should be redirected to the correct page    ${applicant_dashboard_url}
    [Teardown]    Logout as user

Valid login as Collaborator
    [Tags]    HappyPath
    Given the user is not logged-in
    When the guest user enters the log in credentials    ${collaborator1_credentials["email"]}    ${collaborator1_credentials["password"]}
    And the user clicks the button/link    css=button[name="_eventId_proceed"]
    Then the user should see the element    link=Sign out
    And the user should be redirected to the correct page    ${applicant_dashboard_url}
    [Teardown]    Logout as user

Valid login as Assessor
    [Documentation]    INFUND-286
    [Tags]    Assessor    HappyPath    Pending
    # Pending until Assessor Journey is completed
    Given the user is not logged-in
    When the guest user enters the log in credentials    ${assessor_credentials["email"]}    ${assessor_credentials["password"]}
    And the user clicks the button/link    css=button[name="_eventId_proceed"]
    Then the user should see the element    link=Sign out
    And the user should be redirected to the correct page    ${assessor_dashboard_url}
    And the user should be logged-in as an Assessor
    [Teardown]    Logout as user

Valid login as Comp Admin
    [Documentation]    INFUND-2130
    [Tags]
    Given the user is not logged-in
    When the guest user enters the log in credentials    john.doe@innovateuk.test    Passw0rd
    And the user clicks the button/link    css=button[name="_eventId_proceed"]
    Then the user should see the element    link=Sign out
    And the user should be redirected to the correct page    ${COMP_ADMINISTRATOR_DASHBOARD}
    [Teardown]    Logout as user

Valid login as Project Finance role
    [Documentation]    INFUND-2609
    [Tags]
    Given the user is not logged-in
    When the guest user enters the log in credentials    project.finance1@innovateuk.test    Passw0rd
    And the user clicks the button/link    css=button[name="_eventId_proceed"]
    Then the user should be redirected to the correct page without error checking    ${PROJECT_FINANCE_DASHBOARD_URL}
    # note that I haven't used error checking on this redirect as this page will currently produce an error
    # in sprint 9 PS we have created the role, which redirects to a page which will be created in sprint 10 PS
    # at that point this can be changed to include error checking

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

Reset password validations
    [Documentation]    INFUND-1889
    [Tags]
    When the user enters text to a text field    id=id_password    Passw0rdnew
    And the user enters text to a text field    id=id_retypedPassword    OtherPass2aa
    And the user clicks the button/link    jQuery=input[value*="Save password"]
    Then the user should see an error    Passwords must match

Reset password user enters new psw
    [Documentation]    INFUND-1889
    [Tags]    Email    HappyPath
    [Setup]    Clear the login fields
    When the user enters text to a text field    id=id_password    Passw0rdnew
    And the user enters text to a text field    id=id_retypedPassword    Passw0rdnew
    And the user clicks the button/link    jQuery=input[value*="Save password"]
    Then the user should see the text in the page    Your password is updated, you can now sign in with your new password
    And the user clicks the button/link    jQuery=.button:contains("Sign in")
    And the guest user enters the log in credentials    worth.email.test+changepsw@gmail.com    Passw0rd
    And the user clicks the button/link    css=button[name="_eventId_proceed"]
    Then the guest user should get an error message
    When the guest user enters the log in credentials    worth.email.test+changepsw@gmail.com    Passw0rdnew
    And the user clicks the button/link    css=button[name="_eventId_proceed"]
    Then the user should see the element    link=Sign out
    And the user should be redirected to the correct page    ${applicant_dashboard_url}

*** Keywords ***
the user is not logged-in
    the user should not see the element    link=My dashboard
    the user should not see the element    link=Sign out

the guest user should get an error message
    the user should see the text in the page    Your username/password combination doesn't seem to work
    the user should not see the element    link=Sign out

the user should be logged-in as an Assessor
    Title Should Be    Assessor Dashboard - Innovation Funding Service

the user opens the mailbox and clicks the reset link
    Open Mailbox    server=imap.googlemail.com    user=worth.email.test@gmail.com    password=testtest1
    ${LATEST} =    wait for email
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

Clear the login fields
    Reload Page
    When the user enters text to a text field    id=id_password    ${EMPTY}
    And the user enters text to a text field    id=id_retypedPassword    ${EMPTY}
    Mouse Out    id=id_retypedPassword
    sleep    200ms
