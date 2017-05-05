*** Settings ***
Documentation     INFUND-669 As an applicant I want to create a new application so that I can submit an entry into a relevant competition
...
...               INFUND-1163 As an applicant I want to create a new application so that I can submit an entry into a relevant competition
...
...               INFUND-1904 As a user registering an account and submitting the data I expect to receive a verification email so I can be sure that the provided email address is correct
...
...               INFUND-1920 As an applicant once I am accessing my dashboard and clicking on the newly created application for the first time, it will allow me to invite contributors and partners
...
...               INFUND-9243 Add marketing email option tick box to 'Your details' page in the 'Create your account' journey
Suite Setup       Delete the emails from both test mailboxes
Force Tags        Applicant
Resource          ../../../resources/defaultResources.robot

*** Variables ***
${APPLICATION_DETAILS_APPLICATION8}    ${SERVER}/application/99/form/question/428

*** Test Cases ***
Non registered users CH route: lead org Business
    [Documentation]    INFUND-669  INFUND-1904  INFUND-1920  INFUND-1785  INFUND-9280
    [Tags]    HappyPath    SmokeTest
    [Setup]    The guest user opens the browser
    Given the user follows the flow to register their organisation    radio-1  # business
    And the user enters the details and clicks the create account   Phil   Smith   ${test_mailbox_one}+business@gmail.com
    And the user should be redirected to the correct page    ${REGISTRATION_SUCCESS}
    and the user does the email verification   ${test_mailbox_one}+business@gmail.com
    [Teardown]    the user closes the browser

Non registered users CH route: lead org RTO
    [Documentation]    INFUND-669  INFUND-1904  INFUND-1785
    [Tags]    HappyPath    Email    SmokeTest
    [Setup]    The guest user opens the browser
    Given the user follows the flow to register their organisation   radio-3   # RTO
    And the user enters the details and clicks the create account   Lee    Tess    ${test_mailbox_one}+rto@gmail.com
    And the user should be redirected to the correct page    ${REGISTRATION_SUCCESS}
    and the user does the email verification   ${test_mailbox_one}+rto@gmail.com

The email address does not stay in the cookie
    [Documentation]    INFUND_2510
    [Tags]
    Given the user follows the flow to register their organisation   radio-1
    Then the user should not see the text in the page    ${test_mailbox_one}+rto@gmail.com
    [Teardown]    the user closes the browser

Non registered users non CH route
    [Documentation]    INFUND-669
    ...
    ...    INFUND-1904
    ...
    ...    INFUND-1920
    [Tags]    HappyPath
    [Setup]    the guest user opens the browser
    Given the user follows the flow to register their organisation     radio-1
    And the user enters the details and clicks the create account   Stuart    Downing   ${test_mailbox_one}+2@gmail.com
    And the user should be redirected to the correct page    ${REGISTRATION_SUCCESS}

Non registered users non CH route (email step)
    [Documentation]    INFUND-669
    ...
    ...    INFUND-1904
    ...
    ...    INFUND-1920
    [Tags]    Email    HappyPath
    Given the user reads his email and clicks the link    ${test_mailbox_one}+2@gmail.com    Please verify your email address    Once verified you can sign into your account
    When the user clicks the button/link    jQuery=.button:contains("Sign in")
    And the guest user inserts user email & password    ${test_mailbox_one}+2@gmail.com    Passw0rd123
    And the guest user clicks the log-in button
    Then the user should see the text in the page    Your dashboard
    And the user clicks the button/link    link=${UNTITLED_APPLICATION_DASHBOARD_LINK}
    And the user should see the text in the page    Application overview

Verify the name of the new application
    [Documentation]    INFUND-669
    ...
    ...    INFUND-1163
    [Tags]    HappyPath    Email    SmokeTest
    When log in as a different user    ${test_mailbox_one}+business@gmail.com    Passw0rd123
    And the user edits the competition title
    Then the user should see the text in the page    ${test_title}
    And the progress indicator should show 0
    And the user clicks the button/link    link=view team members and add collaborators
    And the user should see the text in the page    Application team
    And the user should see the text in the page    View and manage your participants
    And the new application should be visible in the dashboard page
    And the user clicks the button/link    link=${test_title}
    And the user should see the text in the page    ${test_title}

Marketing emails information should have updated on the profile
    [Documentation]    INFUND-9243
    [Tags]    HappyPath
    When the user navigates to the page    ${edit_profile_url}
    Then the user should see that the checkbox is selected    allowMarketingEmails
    [Teardown]    the user closes the browser

Special Project Finance role
    [Documentation]    INFUND-2609
    [Tags]
    [Setup]    the guest user opens the browser
    Given the user follows the flow to create an account     radio-1
    And the user enters the details and clicks the create account   Alex    Snape   ${test_mailbox_one}+project.finance1@gmail.com
    And the user should be redirected to the correct page    ${REGISTRATION_SUCCESS}

Special Project Finance role (email step)
    [Documentation]    INFUND-2609
    [Tags]    Email
    Given the user reads his email from the default mailbox and clicks the link    ${test_mailbox_one}+project.finance1@gmail.com    Please verify your email address    Once verified you can sign into your account
    When the user clicks the button/link    jQuery=.button:contains("Sign in")
    And the guest user inserts user email & password    ${test_mailbox_one}+project.finance1@gmail.com    Passw0rd123
    And the guest user clicks the log-in button
    Then the user should be redirected to the correct page without error checking    ${COMP_ADMINISTRATOR_DASHBOARD}/live

*** Keywords ***
the user does the email verification
    [Arguments]    ${email_id}
    Given the user reads his email and clicks the link    ${email_id}    Please verify your email address    Once verified you can sign into your account
    And the user should be redirected to the correct page    ${REGISTRATION_VERIFIED}
    When the user clicks the button/link    jQuery=.button:contains("Sign in")
    And the guest user inserts user email & password    ${email_id}   Passw0rd123
    And the guest user clicks the log-in button
    Then the user should see the text in the page    Your dashboard
    And the user clicks the button/link    link=${UNTITLED_APPLICATION_DASHBOARD_LINK}
    And the user clicks the button/link    jQuery=a:contains("Begin application")
    And the user should see the text in the page    Application overview
    And logout as user
    And the user reads his email and clicks the link    ${email_id}   Innovate UK applicant questionnaire    diversity survey

the new application should be visible in the dashboard page
    the user clicks the button/link    link= My dashboard
    the user should see the text in the page    ${test_title}
    the user should see the text in the page    Application number:

the user clicks the Not on company house link
    the user clicks the button/link    jQuery=summary:contains("Enter details manually")
    the user clicks the button/link    name=manual-address
    The user enters text to a text field    id=addressForm.selectedPostcode.addressLine1    street
    The user enters text to a text field    id=addressForm.selectedPostcode.town    town
    The user enters text to a text field    id=addressForm.selectedPostcode.county    country
    The user enters text to a text field    id=addressForm.selectedPostcode.postcode    post code
    The user enters text to a text field    name=organisationName    org2
    the user clicks the button/link    jQuery=.button:contains("Continue")

the user edits the competition title
    the user clicks the button/link    link=${UNTITLED_APPLICATION_DASHBOARD_LINK}
    the user should see the element    link=Application details
    the user clicks the button/link    link=Application details
    The user enters text to a text field    id=application_details-title    ${test_title}
    the user clicks the button/link    jQuery=button:contains("Save and return")

the progress indicator should show 0
    Element Should Contain    css=.progress-indicator    0
