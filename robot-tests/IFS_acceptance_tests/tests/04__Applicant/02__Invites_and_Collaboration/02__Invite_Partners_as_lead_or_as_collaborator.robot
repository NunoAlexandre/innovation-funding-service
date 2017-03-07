*** Settings ***
Documentation     INFUND-901: As a lead applicant I want to invite application contributors to collaborate with me on the application, so that they can contribute to the application in a collaborative competition
...
...               INFUND-896: As a lead applicant i want to invite partner organisations to collaborate on line in my application, so that i can create the consortium needed to complete the proposed project
...
...               INFUND-928: As a lead applicant i want a separate screen within the application form, so that i can invite/track partners/contributors throughout the application process
...
...               INFUND-929: As a lead applicant i want to be able to have a separate screen, so that i can invite contributors to the application
...
...               INFUND-1463: As a user with an invitation to collaborate on an application but not registered with IFS I want to be able to confirm my organisation so that I only have to create my account to work on the application
...
...               INFUND-3742: The overview with contributors is not matching with actual invites
Suite Setup       log in and create new application for collaboration if there is not one already
Suite Teardown    TestTeardown User closes the browser
Force Tags        Applicant
Resource          ../../../resources/defaultResources.robot

*** Variables ***
${application_name}    Invite robot test application

*** Test Cases ***
Application team page
    [Documentation]    INFUND-928
    [Tags]    HappyPath
    Given the user navigates to the page    ${DASHBOARD_URL}
    And the user clicks the button/link    link=Invite robot test application
    And the user should see the text in the page    view and add participants to your application
    When the user clicks the button/link    link=view and add participants to your application
    Then the user should see the text in the page    Application team
    And the user should see the text in the page    View and manage your contributors and partners in the application.
    And the lead applicant should have the correct status

Valid invitation submit
    [Documentation]    INFUND-901
    [Tags]    HappyPath    SmokeTest
    [Setup]    Delete the emails from both test mailboxes
    Given the user is on the invites and collaborators page
    When the user clicks the button/link    jQuery=.button:contains("Invite new contributors")
    When the applicant enters valid inputs
    Then the user should see the text in the page    Application team
    And the user should see the text in the page    Invites sent

Pending partners visible in the Application details
    [Documentation]    INFUND-2966, INFUND-2738
    [Tags]    HappyPath
    Given the user navigates to the page    ${DASHBOARD_URL}
    And the user clicks the button/link    link=Invite robot test application
    And the user clicks the button/link    link=Application details
    Then the user should see the element   jQuery=ul.list-bullet > li > span:contains("Fannie May")

Pending users visible in the assign list but not clickable
    [Documentation]    INFUND-928
    ...
    ...    INFUND-1962
    [Tags]    HappyPath
    When the user navigates to the next question
    Then the applicant cannot assign to pending invitees
    And the user should see the text in the page    Adrian Booth (pending)

Pending partners visible in Application team page
    [Documentation]    INFUND-929
    ...
    ...    INFUND-3742
    [Tags]    HappyPath
    Given the user navigates to the page    ${DASHBOARD_URL}
    And the user clicks the button/link    link=Invite robot test application
    When the user clicks the button/link    link=view and add participants to your application
    Then the status of the invited people should be correct in the application team page
    And The Lead organisation should be shown only one time

Pending partners visible in the Manage contributors page
    [Documentation]    INFUND-928
    [Tags]    HappyPath
    Given the user navigates to the page    ${DASHBOARD_URL}
    And the user clicks the button/link    link=Invite robot test application
    When the user clicks the button/link    link=view and add participants to your application
    When the user clicks the button/link    jQuery=.button:contains("Invite new contributors")
    Then the user should see the text in the page    Manage contributors
    And the status of the people should be correct in the Manage contributors page
    [Teardown]    Logout as user

Business organisation (partner accepts invitation)
    [Documentation]    INFUND-1005
    ...    INFUND-2286
    ...    INFUND-1779
    ...    INFUND-2336
    [Tags]    HappyPath    Email    SmokeTest
    [Setup]    The guest user opens the browser
    When the user reads his email and clicks the link    ${TEST_MAILBOX_ONE}+inviteorg1@gmail.com    Invitation to collaborate in ${OPEN_COMPETITION_NAME}    participate in their application
    And the user clicks the button/link    jQuery=.button:contains("Create")
    And the user selects the radio button    organisationType    1
    And the user clicks the button/link    jQuery=.button:contains("Continue")
    And the user enters text to a text field    id=organisationSearchName    Nomensa
    And the user clicks the button/link    id=org-search
    And the user clicks the button/link    link=NOMENSA LTD
    And the user selects the checkbox    address-same
    And the user clicks the button/link    jQuery=.button:contains("Save organisation and continue")
    And the user clicks the button/link    jQuery=.button:contains("Confirm and continue")
    And the user fills the create account form    Adrian    Booth
    And the user reads his email and clicks the link    ${TEST_MAILBOX_ONE}+inviteorg1@gmail.com    Please verify your email address    If you did not request an account with us
    And the user should be redirected to the correct page    ${REGISTRATION_VERIFIED}

Partner should be able to log-in and see the new company name
    [Documentation]    INFUND-2083
    [Tags]    Email    HappyPath    SmokeTest
    Given the user clicks the button/link  jQuery=.button:contains("Sign in")
    When guest user log-in    ${test_mailbox_one}+inviteorg${unique_email_number}@gmail.com  ${correct_password}
    Then the user should be redirected to the correct page    ${DASHBOARD_URL}
    And the user can see the updated company name throughout the application

Partner can invite others to his own organisation
    [Documentation]    INFUND-2335
    [Tags]    Email
    When the user navigates to the page    ${DASHBOARD_URL}
    And the user clicks the button/link    link=Invite robot test application
    And the user clicks the button/link    link=view and add participants to your application
    And the user clicks the button/link    jQuery=.button:contains("Invite new contributors")
    Then the user can invite another person to their own organisation

Partner cannot invite others to other organisations
    [Documentation]    INFUND-2335
    [Tags]    Email
    Then the user cannot invite another person to a different organisation
    [Teardown]    the user closes the browser

Lead should see the accepted partner in the assign list
    [Documentation]    INFUND-1779
    [Tags]    HappyPath    Email
    [Setup]    Log in as user    &{lead_applicant_credentials}
    Given the user navigates to the page    ${DASHBOARD_URL}
    And the user clicks the button/link    link=Invite robot test application
    And the user clicks the button/link    link=Project summary
    When the user clicks the button/link    css=.assign-button
    Then the user should see the element    jQuery=button:contains("Adrian Booth")
    [Teardown]    Logout as user

Lead should not be able to edit Partners
    [Documentation]    INFUND-929
    [Tags]
    Given guest user log-in    &{lead_applicant_credentials}
    And the user navigates to the page    ${DASHBOARD_URL}
    And the user clicks the button/link    link=Invite robot test application
    When the user clicks the button/link    link=view and add participants to your application
    When the user clicks the button/link    jQuery=.button:contains("Invite new contributors")
    Then the user should see the text in the page    Manage contributors
    And the invited collaborators are not editable

Lead applicant invites a non registered user in the same organisation
    [Documentation]    INFUND-928, INFUND-1463
    ...    This test checks if the invited partner who are in the same organisation they can go directly to the create account and they don't have to create an organisation first.
    [Tags]
    [Setup]    Delete the emails from both test mailboxes
    Given the user navigates to the page    ${DASHBOARD_URL}
    And the user clicks the button/link    link=Invite robot test application
    When the user clicks the button/link    link=view and add participants to your application
    When the user clicks the button/link    jQuery=.button:contains("Invite new contributors")
    Then the user should see the text in the page    Manage contributors
    And the user clicks the button/link    jQuery=li:nth-child(1) button:contains("Add another person")
    When the user adds new collaborator
    And the user clicks the button/link    jquery=button:contains("Save changes")
    Then the user should see the text in the page    Application team
    And the user should see the text in the page    View and manage your contributors and partners in the application
    [Teardown]    the user closes the browser

Registered partner should not create new org but should follow the create account flow
    [Documentation]    INFUND-1463
    ...    This test checks if the invited partner who are in the same organisation they can go directly to the create account and they don't have to create an organisation first.
    [Tags]    Email
    [Setup]    The guest user opens the browser
    When the user reads his email and clicks the link    ${TEST_MAILBOX_ONE}+inviteorg2@gmail.com    Invitation to collaborate in ${OPEN_COMPETITION_NAME}    participate in their application
    And the user should see the text in the page    Join an application
    And the user clicks the button/link    jQuery=.button:contains("Create")
    And the user should see the text in the page    Your organisation
    And the user should see the text in the page    Business organisation
    And the user should see the element    link=email the lead applicant
    And the user clicks the button/link    jQuery=.button:contains("Continue")
    And the user fills the create account form    Roger    Axe
    And the user reads his email and clicks the link    ${TEST_MAILBOX_ONE}+inviteorg2@gmail.com    Please verify your email address    If you did not request an account with us
    And the user should be redirected to the correct page    ${REGISTRATION_VERIFIED}

*** Keywords ***
the applicant enters valid inputs
    The user clicks the button/link    jquery=li:nth-last-child(1) button:contains('Add additional partner organisation')
    The user enters text to a text field    name=organisations[1].organisationName    Fannie May
    The user enters text to a text field    name=organisations[1].invites[0].personName    Adrian Booth
    The user enters text to a text field    name=organisations[1].invites[0].email    ${test_mailbox_one}+inviteorg${unique_email_number}@gmail.com
    focus    jquery=button:contains("Save changes")
    The user clicks the button/link    jquery=button:contains("Save changes")

The lead applicant should have the correct status
    the user should see the element    css=#content h2.heading-medium
    ${input_value} =    get text    css=#content h2.heading-medium
    Should Be Equal As Strings    ${input_value}    Empire Ltd (Lead organisation)
    the user should see the element    link=Steve Smith
    ${input_value} =    get text    css=.list-bullet li small
    Should Be Equal As Strings    ${input_value}    (Lead Applicant)

the user adds new collaborator
    the user should see the element    css=li:nth-child(1) tr:nth-of-type(2) td:nth-of-type(1) input
    The user enters text to a text field    css=li:nth-child(1) tr:nth-of-type(2) td:nth-of-type(1) input    Roger Axe
    The user enters text to a text field    css=li:nth-child(1) tr:nth-of-type(2) td:nth-of-type(2) input    ${test_mailbox_one}+inviteorg2@gmail.com
    focus    jquery=li:nth-child(1) button:contains('Add another person')
    wait for autosave

The status of the invited people should be correct in the application team page
    the user should see the text in the page    Adrian Booth
    Element Should Contain    xpath=//a[contains(text(),"Adrian Booth")]//following::small    (pending)

the invited collaborators are not editable
    the user should see the element    jQuery=li:nth-child(1) tr:nth-of-type(1) td:nth-child(1) [readonly]
    the user should see the element    jQuery=li:nth-child(1) tr:nth-of-type(1) td:nth-child(2) [readonly]
    the user should see the element    jQuery=li:nth-child(2) tr:nth-of-type(1) td:nth-child(1) [readonly]
    the user should see the element    jQuery=li:nth-child(2) tr:nth-of-type(1) td:nth-child(2) [readonly]

the applicant cannot assign to pending invitees
    the user clicks the button/link    jQuery=button:contains("Assigned to")
    the user should not see the element    jQuery=button:contains("Adrian Booth")

the status of the people should be correct in the Manage contributors page
    Element Should Contain    css=li:nth-child(1) tr:nth-of-type(1) td:nth-child(3)    Lead applicant
    Element Should Contain    css=li:nth-child(2) tr:nth-of-type(1) td:nth-child(3)    (pending)

the user can see the updated company name throughout the application
    Given the user navigates to the page    ${DASHBOARD_URL}
    And the user clicks the button/link    link=${application_name}
    And the user clicks the button/link    link=Your finances
    And the user should see the element    link=Your project costs
    And the user should see the element    link=Your organisation
    And the user should see the element    jQuery=h3:contains("Your funding")
    Given the user navigates to the page    ${DASHBOARD_URL}
    And the user clicks the button/link    link=${application_name}
    When the user clicks the button/link    link=view and add participants to your application
    Then the user should see the element    jQuery=h2:contains("NOMENSA LTD")

the user can invite another person to their own organisation
    ${OWN_ORG}=    Get WebElement    jQuery=li:has(input[value='NOMENSA LTD'])
    the user clicks the button/link    jQuery=button:contains('Add another person')
    the user should see the element    jQuery=li[data-invite-org=${OWN_ORG.get_attribute('data-invite-org')}] tr:nth-of-type(2) td:nth-child(2) input:not([readonly])
    the user should not see the element    jQuery=li[data-invite-org=${OWN_ORG.get_attribute('data-invite-org')}] tr:nth-of-type(2) td:nth-child(2) [readonly]

the user cannot invite another person to a different organisation
    the user should not see the element    jQuery=li:nth-child(1) button:contains("Add another person")

the user navigates to the next question
    The user clicks the button/link    css=.next .pagination-label
    Run Keyword And Ignore Error Without Screenshots    confirm action

the user is on the invites and collaborators page
    ${status}=    Run Keyword And Ignore Error Without Screenshots    the user should see the element    jQuery=.button:contains("Invite new contributors")
    run keyword if    ${status}!=('PASS', None)    log into smoke test application

log into smoke test application
    logout as user
    guest user log-in    ${test_mailbox_one}+${unique_email_number}@gmail.com    Passw0rd123
    the user clicks the button/link    link=IFS smoke test
    the user clicks the button/link    link=view and add participants to your application

The Lead organisation should be shown only one time
    Element Should not Contain    css=div:nth-child(7)    Steve Smith
