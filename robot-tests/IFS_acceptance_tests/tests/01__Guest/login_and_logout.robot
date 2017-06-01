*** Settings ***
Documentation     INFUND-399 As a client, I would like to demo the system with real-like logins per user role
...
...
...               INFUND-171 As a user, I am able to sign in providing a emailaddress and password, so I have access to my data
...
...
...               INFUND-2130 As a competition administrator I want to be able to log into IFS so that I can access the system with appropriate permissions for my role
...
...               INFUND-1479 As an assessor with an existing Applicant AND Assessor account I want to be able to choose the correct profile when I log in, so that I don't access the wrong profile information
Suite Teardown    TestTeardown User closes the browser
Force Tags        Guest
Resource          ../../resources/defaultResources.robot

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
    [Documentation]    IFS-32
    [Tags]    HappyPath
    Given the user is not logged-in
    When the guest user enters the log in credentials    ${lead_applicant_credentials["email"]}    ${lead_applicant_credentials["password"]}
    And the user clicks the button/link    css=button[name="_eventId_proceed"]
    Then the user should see the element    link=Sign out
    And the user should not see the element    link=Sign in
    And the user should be redirected to the correct page    ${DASHBOARD_URL}
    [Teardown]    Logout as user

Valid login as Collaborator
    [Tags]    HappyPath
    Given the user is not logged-in
    When the guest user enters the log in credentials    ${collaborator1_credentials["email"]}    ${collaborator1_credentials["password"]}
    And the user clicks the button/link    css=button[name="_eventId_proceed"]
    Then the user should see the element    link=Sign out
    And the user should be redirected to the correct page    ${DASHBOARD_URL}
    [Teardown]    Logout as user

Valid login as Assessor
    [Documentation]    INFUND-286
    [Tags]    HappyPath    Pending
    #TODO INFUND-5990    Assessor bin slow in building
    Given the user is not logged-in
    When the guest user enters the log in credentials    ${assessor_credentials["email"]}    ${assessor_credentials["password"]}
    And the user clicks the button/link    css=button[name="_eventId_proceed"]
    Then the user should see the element    link=Sign out
    And the user should be redirected to the correct page    ${assessor_dashboard_url}
    [Teardown]    Logout as user

Valid login with double role as Applicant
    [Documentation]    INFUND-1479
    [Tags]    HappyPath
    Given the user is not logged-in
    When the guest user enters the log in credentials    ${Multiple_user_credentials["email"]}    ${Multiple_user_credentials["password"]}
    And the user clicks the button/link    css=button[name="_eventId_proceed"]
    Then The user should see the text in the page    Please choose the role you are signing in as today
    And The user clicks the button/link    jquery=button:contains("Continue")
    Then The user should see an error    Please select a role.
    And the user selects the radio button    selectedRole    APPLICANT
    And The user clicks the button/link    jquery=button:contains("Continue")
    Then the user should be redirected to the correct page    ${DASHBOARD_URL}
    And the user should see the element    link=Sign out
    [Teardown]    Logout as user

Valid login with Double role as Assessor
    [Documentation]    INFUND-1479
    Given the user is not logged-in
    When the guest user enters the log in credentials    ${Multiple_user_credentials["email"]}    ${Multiple_user_credentials["password"]}
    And the user clicks the button/link    css=button[name="_eventId_proceed"]
    And the user selects the radio button    selectedRole    ASSESSOR
    And The user clicks the button/link    jquery=button:contains("Continue")
    Then the user should be redirected to the correct page    ${assessor_dashboard_url}
    And the user should see the element    link=Sign out
    [Teardown]    Logout as user

Valid login as Comp Admin
    [Documentation]    INFUND-2130
    [Tags]    HappyPath
    Given the user is not logged-in
    When the guest user enters the log in credentials    john.doe@innovateuk.test    Passw0rd
    And the user clicks the button/link    css=button[name="_eventId_proceed"]
    Then the user should see the element    link=Sign out
    And the user should be redirected to the correct page    ${COMP_ADMINISTRATOR_DASHBOARD}
    [Teardown]    Logout as user

Should not see the Sign in link when on the login page
    Given the user navigates to the page    ${LOGIN_URL}
    Then the user should not see the element    link=Sign in

Should see the Sign in link when not logged in
    Given the user is not logged-in
    And the user navigates to the page    ${frontDoor}
    Then the user should see the element    link=Sign in

Valid login as Project Finance role
    [Documentation]    INFUND-2609
    [Tags]
    Given the user is not logged-in
    When the guest user enters the log in credentials    lee.bowman@innovateuk.test    Passw0rd
    And the user clicks the button/link    css=button[name="_eventId_proceed"]
    Then the user should be redirected to the correct page    ${COMP_ADMINISTRATOR_DASHBOARD}
    # note that this has been updated as per the most recent requirements.
    # project finance users now use the same dashboard as other internal users

Page not found
    [Documentation]    INFUND-8712
    When the user navigates to the page and gets a custom error message    ${SERVER}/ibble/dibble    ${404_error_message}
    [Teardown]    the user closes the browser

Reset password
    [Documentation]    INFUND-1889
    [Tags]    Email    HappyPath
    [Setup]    Run Keywords    delete the emails from the default test mailbox
    ...    AND    the guest user opens the browser
    Given the user navigates to the page    ${LOGIN_URL}
    When the user clicks the forgot psw link
    Then the user should see the element    link=Sign in
    And the user enters text to a text field    id=id_email    ${test_mailbox_one}+changepsw@gmail.com
    And the user clicks the button/link    css=input.button
    Then the user should see the text in the page    If your email address is recognised and valid, you’ll receive a notification with instructions about how to reset your password. If you do not receive a notification, please check your junk folder or try again.
    And the user reads his email from the default mailbox and clicks the link    ${test_mailbox_one}+changepsw@gmail.com    Reset your password    If you didn't request this
    And the user should see the text in the page    Password reset
    # TODO INFUND-5582

Reset password user enters new psw
    [Documentation]    INFUND-1889
    [Tags]    Email    HappyPath
    [Setup]    Clear the login fields
    When the user enters text to a text field    id=id_password    Passw0rdnew
    And the user moves focus to the element    jQuery=input[value*="Save password"]
    And the user clicks the button/link    jQuery=input[value*="Save password"]
    Then the user should see the text in the page    Your password is updated, you can now sign in with your new password
    And the user clicks the button/link    jQuery=.button:contains("Sign in")
    And the guest user enters the log in credentials    ${test_mailbox_one}+changepsw@gmail.com    Passw0rd
    And the user clicks the button/link    css=button[name="_eventId_proceed"]
    Then the guest user should get an error message
    When the guest user enters the log in credentials    ${test_mailbox_one}+changepsw@gmail.com    Passw0rdnew
    And the user clicks the button/link    css=button[name="_eventId_proceed"]
    Then the user should see the element    link=Sign out
    And the user should be redirected to the correct page    ${DASHBOARD_URL}
    # TODO INFUND-5582

*** Keywords ***
the user is not logged-in
    the user should not see the element    link=My dashboard
    the user should not see the element    link=Sign out

the guest user should get an error message
    the user should see the text in the page    Your email/password combination doesn't seem to work
    the user should not see the element    link=Sign out

the user should be logged-in as an Assessor
    Title Should Be    Assessor Dashboard - Innovation Funding Service

Clear the login fields
    the user reloads the page
    When the user enters text to a text field    id=id_password    ${EMPTY}
    Mouse Out    id=id_password
    wait for autosave

the user clicks the forgot psw link
    ${STATUS}    ${VALUE}=    Run Keyword And Ignore Error Without Screenshots    click element    link=forgot your password?
    Run Keyword If    '${status}' == 'FAIL'    click element    jQuery=summary:contains("Need help signing in or creating an account?")
    Run Keyword If    '${status}' == 'FAIL'    click element    link=Forgotten your password?
