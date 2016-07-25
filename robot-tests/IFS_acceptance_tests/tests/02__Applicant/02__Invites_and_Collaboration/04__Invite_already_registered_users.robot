*** Settings ***
Documentation     INFUND-1458 As a existing user with an invitation to collaborate on an application and I am already registered with IFS I want to be able to use my existing credentials and confirm my details so that I don't have to follow the registration process again.
...
...
...               INFUND-2716: Error in where the name of an invited partner doesn't update in 'View team members and add collaborators'
...
...
...               INFUND-3759: Existing Applicant should be able to accept invitations for other applications in the same organisation
Suite Setup       The guest user opens the browser
Suite Teardown    TestTeardown User closes the browser
Force Tags        Email    Applicant    Collaboration
Resource          ../../../resources/GLOBAL_LIBRARIES.robot
Resource          ../../../resources/variables/GLOBAL_VARIABLES.robot
Resource          ../../../resources/variables/User_credentials.robot
Resource          ../../../resources/keywords/Login_actions.robot
Resource          ../../../resources/keywords/User_actions.robot

*** Variables ***

*** Test Cases ***
The invited user should not follow the registration flow again
    [Documentation]    INFUND-1458
    [Tags]    HappyPath
    [Setup]    Delete the emails from both test mailboxes
    Given we create a new user    ${test_mailbox_one}+invitedregistered@gmail.com
    Given the lead applicant invites a registered user    ${test_mailbox_one}+invite2@gmail.com    ${test_mailbox_one}+invitedregistered@gmail.com
    When the user opens the mailbox and accepts the invitation to collaborate
    Then the user should see the text in the page    We've found an existing user account with the invited email address

The user clicks the login link
    [Documentation]    INFUND-1458
    [Tags]    HappyPath
    When the user clicks the button/link    link=Click here to sign in
    And the guest user inserts user email & password    ${test_mailbox_one}+invitedregistered@gmail.com    Passw0rd123
    And the guest user clicks the log-in button
    Then the user should see the text in the page    Confirm your organisation

The user should see the correct content in the confirm page
    [Documentation]    INFUND-1458
    [Tags]    HappyPath
    Then the user should see the text in the page    INNOVATE LTD
    And the user should see the text in the page    BH12 4NZ
    And the user should see the element    link=email the application lead

The continue button should redirect to the overview page
    [Documentation]    INFUND-1458
    [Tags]    HappyPath
    When the user clicks the button/link    jQuery=.button:contains("Continue to application")
    Then the user should see the text in the page    Application overview

When this user edits the name this should be changed in the View team page
    [Documentation]    INFUND-2716: Error in where the name of an invited partner doesn't update in 'View team members and add collaborators'.
    [Tags]    HappyPath
    Given the user navigates to the page    ${DASHBOARD_URL}
    When the user clicks the button/link    link=View and edit your profile details
    And the user clicks the button/link    link=Edit your details
    And the user enters profile details
    Then the user should see the change in the view team members page
    [Teardown]    TestTeardown User closes the browser

Invite a user with the same organisation under the same organisation
    [Documentation]    INFUND-3759
    [Setup]    Guest user log-in    ${test_mailbox_one}+invitedregistered@gmail.com    Passw0rd123
    When Existing user creates a new application and invites a user from the same organisation
    Then the invited user should get a message to contact the helpdesk

*** Keywords ***
the user enters profile details
    Wait Until Element Is Visible    id=title
    Input Text    id=firstName    Dennis
    Input Text    id=lastName    Bergkamp
    focus         css=[name="create-account"]
    Click Element    css=[name="create-account"]

the user should see the change in the view team members page
    click element    link=My dashboard
    click element    xpath=//*[@id="content"]/div[2]/section[1]/ul/li[2]/div/div[1]/h3/a
    click element    link=View team members and add collaborators
    Page Should Contain Element    link= Dennis Bergkamp
    Capture Page Screenshot

Existing user creates a new application and invites a user from the same organisation
    When the user navigates to the page    ${COMPETITION_DETAILS_URL}
    And the user clicks the button/link    jQuery=.button:contains("Apply now")
    And the user clicks the button/link    jQuery=.button:contains("Apply now")
    And the user clicks the button/link    jQuery=Label:contains("Yes I want to create a new application")
    And the user clicks the button/link    jQuery=.button:contains("Continue")
    And The user clicks the button/link    jquery=li:nth-child(1) button:contains('Add person')
    And The user enters text to a text field    css=li:nth-child(1) tr:nth-of-type(2) td:nth-of-type(1) input    Olivier Giroud
    And The user enters text to a text field    css=li:nth-child(1) tr:nth-of-type(2) td:nth-of-type(2) input    ${test_mailbox_one}+invite2@gmail.com
    Capture Page Screenshot
    And the user clicks the button/link    jQuery=.button:contains("Begin application")
    And the user clicks the button/link    link=Application details
    And the user enters text to a text field    id=application_details-title    Invite a user with the same org@
    And the user clicks the button/link    jQuery=button:contains("Save and return")
    And the user closes the browser

Then the invited user should get a message to contact the helpdesk
    And the guest user opens the browser
    When the user opens the mailbox and accepts the invitation to collaborate
    When the user clicks the button/link    link=Click here to sign in
    And the guest user inserts user email & password    ${test_mailbox_one}+invite2@gmail.com    Passw0rd123
    And the guest user clicks the log-in button
    Then the user should see the text in the page    Sorry, you are unable to accept this invitation
    And the user should see the text in the page    If you want to remain in the same organisation but join a different application, please contact the helpdesk on 0300 321 4357
