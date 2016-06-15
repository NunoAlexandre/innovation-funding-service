*** Settings ***
Documentation     INFUND-901: As a lead applicant I want to invite application contributors to collaborate with me on the application, so that they can contribute to the application in a collaborative competition
...
...
...               INFUND-896: As a lead applicant i want to invite partner organisations to collaborate on line in my application, so that i can create the consortium needed to complete the proposed project
...
...
...               INFUND-928: As a lead applicant i want a separate screen within the application form, so that i can invite/track partners/contributors throughout the application process
...
...
...               INFUND-929: As a lead applicant i want to be able to have a separate screen, so that i can invite contributors to the application
...
...
...               INFUND-1463: As a user with an invitation to collaborate on an application but not registered with IFS I want to be able to confirm my organisation so that I only have to create my account to work on the application
Suite Setup       log in and create new application for collaboration if there is not one already
Suite Teardown    TestTeardown User closes the browser
Force Tags        Collaboration
Resource          ../../../resources/GLOBAL_LIBRARIES.robot
Resource          ../../../resources/variables/GLOBAL_VARIABLES.robot
Resource          ../../../resources/variables/User_credentials.robot
Resource          ../../../resources/keywords/Login_actions.robot
Resource          ../../../resources/keywords/User_actions.robot
Resource          ../../../resources/keywords/SUITE_SET_UP_ACTIONS.robot

*** Test Cases ***
Application team page
    [Documentation]    INFUND-928
    [Tags]    HappyPath
    Given the user navigates to the page    ${DASHBOARD_URL}
    And the user clicks the button/link    link=Invite robot test application
    And the user should see the text in the page    View team members and add collaborators
    When the user clicks the button/link    link=View team members and add collaborators
    Then the user should see the text in the page    Application team
    And the user should see the text in the page    View and manage your contributors and partners in the application.
    And the lead applicant should have the correct status

Valid invitation submit
    [Documentation]    INFUND-901
    [Tags]    HappyPath
    [Setup]    Delete the emails from both test mailboxes
    Given the user clicks the button/link    jQuery=.button:contains("Invite new contributors")
    When the applicant enters valid inputs
    Then the user should see the text in the page    Application team
    And the user should see the text in the page    Invites sent

Pending partners visible in the Application details
    [Documentation]    INFUND-2966
    ...
    ...    INFUND-2738
    [Tags]
    Given the user navigates to the page    ${DASHBOARD_URL}
    And the user clicks the button/link    link=Invite robot test application
    And the user clicks the button/link    link=Application details
    Then pending partners should be visible in the page

Pending users visible in the assign list but not clickable
    [Documentation]    INFUND-928
    ...
    ...    INFUND-1962
    [Tags]
    When the user navigates to the next question
    Then the applicant cannot assign to pending invitees
    And the user should see the text in the page    Adrian Booth (pending)

Pending partners visible in Application team page
    [Documentation]    INFUND-929
    [Tags]    HappyPath
    Given the user navigates to the page    ${DASHBOARD_URL}
    And the user clicks the button/link    link=Invite robot test application
    When the user clicks the button/link    link=View team members and add collaborators
    Then the status of the invited people should be correct in the application team page

Pending partners visible in the Manage contributors page
    [Documentation]    INFUND-928
    [Tags]    HappyPath
    Given the user navigates to the page    ${DASHBOARD_URL}
    And the user clicks the button/link    link=Invite robot test application
    When the user clicks the button/link    link=View team members and add collaborators
    When the user clicks the button/link    jQuery=.button:contains("Invite new contributors")
    Then the user should see the text in the page    Manage Contributors
    And the status of the people should be correct in the Manage contributors page
    [Teardown]    Logout as user

Business organisation (partner accepts invitation)
    [Documentation]    INFUND-1005
    ...    INFUND-2286
    ...    INFUND-1779
    ...    INFUND-2336
    [Tags]    HappyPath    Email
    [Setup]    The guest user opens the browser
    When the user opens the mailbox and accepts the invitation to collaborate
    And the user clicks the button/link    jQuery=.button:contains("Create")
    And the user selects the radio button    organisationType    1
    And the user clicks the button/link    jQuery=.button:contains("Continue")
    And the user enters text to a text field    id=organisationSearchName    Nomensa
    And the user clicks the button/link    id=org-search
    And the user clicks the button/link    link=NOMENSA LTD
    And the user selects the checkbox    id=address-same
    And the user clicks the button/link    jQuery=.button:contains("Save organisation and continue")
    And the user clicks the button/link    jQuery=.button:contains("Save")
    And the user fills the create account form    Adrian    Booth
    And the user opens the mailbox and verifies the email from
    And the user should be redirected to the correct page    ${REGISTRATION_VERIFIED}

Partner should be able to log-in and see the new company name throughout the application
    [Documentation]    INFUND-2083
    [Tags]    Email
    Given the user clicks the button/link    jQuery=.button:contains("Sign in")
    When guest user log-in    worth.email.test+inviteorg1@gmail.com    Passw0rd123
    Then the user should be redirected to the correct page    ${DASHBOARD_URL}
    And the user can see the updated company name throughout the application
    [Teardown]    Logout as user

Partner can invite others to his own organisation
    [Documentation]    INFUND-2335
    [Tags]    Email
    Given guest user log-in    worth.email.test+inviteorg1@gmail.com    Passw0rd123
    When the user navigates to the page    ${DASHBOARD_URL}
    And the user clicks the button/link    link=Invite robot test application
    And the user clicks the button/link    link=View team members and add collaborators
    And the user clicks the button/link    jQuery=.button:contains("Invite new contributors")
    Then the user can invite another person to their own organisation

Partner cannot invite others to other organisations
    [Documentation]    INFUND-2335
    [Tags]    Email
    Then the user cannot invite another person to a different organisation
    [Teardown]    the user closes the browser

Partner who accepted the invite should be visible in the assign list
    [Documentation]    INFUND-1779
    [Tags]    HappyPath    Email    Pending
    [Setup]    Log in as user    &{lead_applicant_credentials}
    # Pending due to INFUND-3266
    Given the user navigates to the page    ${DASHBOARD_URL}
    And the user clicks the button/link    link=Invite robot test application
    And the user clicks the button/link    link=Project summary
    When the user clicks the button/link    css=.assign-button
    Then the user should see the element    jQuery=button:contains("Adrian Booth")
    [Teardown]    Logout as user

Partners are not editable
    [Documentation]    INFUND-929
    [Tags]
    Given guest user log-in    &{lead_applicant_credentials}
    And the user navigates to the page    ${DASHBOARD_URL}
    And the user clicks the button/link    link=Invite robot test application
    When the user clicks the button/link    link=View team members and add collaborators
    When the user clicks the button/link    jQuery=.button:contains("Invite new contributors")
    Then the user should see the text in the page    Manage Contributors
    And the invited collaborators are not editable

The Lead applicant invites a non registered user in the same organisation
    [Documentation]    INFUND-928
    ...
    ...    INFUND-1463
    ...
    ...    This test checks if the invited partner who are in the same organisation they can go directly to the create account and they don't have to create an organisation first.
    [Tags]    HappyPath
    [Setup]    Delete the emails from both test mailboxes
    Given the user navigates to the page    ${DASHBOARD_URL}
    And the user clicks the button/link    link=Invite robot test application
    When the user clicks the button/link    link=View team members and add collaborators
    When the user clicks the button/link    jQuery=.button:contains("Invite new contributors")
    Then the user should see the text in the page    Manage Contributors
    And the user clicks the button/link    jQuery=li:nth-child(1) button:contains("Add person")
    When the user adds new collaborator
    And the user clicks the button/link    jquery=button:contains("Save Changes")
    Then the user should see the text in the page    Application team
    And the user should see the text in the page    View and manage your contributors and partners in the application
    [Teardown]    the user closes the browser

The user should not create new org but should follow the create account flow
    [Documentation]    INFUND-1463
    ...
    ...    This test checks if the invited partner who are in the same organisation they can go directly to the create account and they don't have to create an organisation first.
    [Tags]    Email
    [Setup]    The guest user opens the browser
    When the user opens the mailbox and accepts the invitation to collaborate
    And the user should see the text in the page    Join an application
    And the user clicks the button/link    jQuery=.button:contains("Create")
    And the user should see the text in the page    Your organisation
    And the user should see the text in the page    Business Organisation
    And the user should see the element    link=email the application lead
    And the user clicks the button/link    jQuery=.button:contains("Continue")
    And the user fills the create account form    Roger    Axe
    And the user opens the mailbox and verifies the email from
    And the user should be redirected to the correct page    ${REGISTRATION_VERIFIED}

*** Keywords ***
the applicant enters valid inputs
    Click Element    jquery=li:nth-last-child(1) button:contains('Add additional partner organisation')
    Input Text    name=organisations[1].organisationName    Fannie May
    Input Text    name=organisations[1].invites[0].personName    Adrian Booth
    Input Text    name=organisations[1].invites[0].email    worth.email.test+inviteorg1@gmail.com
    focus    jquery=button:contains("Save Changes")
    Click Element    jquery=button:contains("Save Changes")

The lead applicant should have the correct status
    the user should see the element    css=#content h2.heading-medium
    ${input_value} =    get text    css=#content h2.heading-medium
    Should Be Equal As Strings    ${input_value}    Empire Ltd (Lead organisation)
    the user should see the element    link=Steve Smith
    ${input_value} =    get text    css=.list-bullet li small
    Should Be Equal As Strings    ${input_value}    (Lead Applicant)

the user adds new collaborator
    the user should see the element    css=li:nth-child(1) tr:nth-of-type(2) td:nth-of-type(1) input
    Input Text    css=li:nth-child(1) tr:nth-of-type(2) td:nth-of-type(1) input    Roger Axe
    Input Text    css=li:nth-child(1) tr:nth-of-type(2) td:nth-of-type(2) input    worth.email.test+inviteorg2@gmail.com
    focus    jquery=li:nth-child(1) button:contains('Add person')
    sleep    300ms

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

the lead applicant logs out
    Logout as user

the lead applicant logs back in
    guest user log-in    &{lead_applicant_credentials}

the user can see the updated company name throughout the application
    Given the user navigates to the page    ${DASHBOARD_URL}
    And the user clicks the button/link    link=Invite robot test application
    And the user clicks the button/link    link=Your finances
    the user should see the text in the page    NOMENSA LTD
    Given the user navigates to the page    ${DASHBOARD_URL}
    And the user clicks the button/link    link=Invite robot test application
    When the user clicks the button/link    link=View team members and add collaborators
    the user should see the text in the page    NOMENSA LTD

the user can invite another person to their own organisation
    ${OWN_ORG}=    Get WebElement    jQuery=li:has(input[value='NOMENSA LTD'])
    the user clicks the button/link    jQuery=button:contains('Add person')
    the user should see the element    jQuery=li[data-invite-org=${OWN_ORG.get_attribute('data-invite-org')}] tr:nth-of-type(2) td:nth-child(2) input:not([readonly])
    the user should not see the element    jQuery=li[data-invite-org=${OWN_ORG.get_attribute('data-invite-org')}] tr:nth-of-type(2) td:nth-child(2) [readonly]

the user cannot invite another person to a different organisation
    the user should not see the element    jQuery=li:nth-child(1) button:contains("Add person")
    #This comments should be removed after the review
    #${OTHER_ORG}=    Get WebElement    jQuery=li:has(input[value='HIVE IT LIMITED'])
    #the user should see the element    jQuery=li[data-invite-org=${OTHER_ORG.get_attribute('data-invite-org')}] tr:nth-of-type(1) td:nth-child(2) [readonly]

pending partners should be visible in the page
    the user should see the element    xpath=//span[contains(text(),"Fannie May")]//following::small
    Element Should Contain    xpath=//span[contains(text(),"Fannie May")]//following::small    (pending)

the user navigates to the next question
    The user clicks the button/link    css=.next .pagination-label
    Run Keyword And Ignore Error    confirm action
