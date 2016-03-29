*** Settings ***
Documentation     INNFUND-669 As an applicant I want to create a new application so that I can submit an entry into a relevant competition
...
...
...               INFUND-1163 As an applicant I want to create a new application so that I can submit an entry into a relevant competition
...
...
...               INFUND-1904 As a user registering an account and submitting the data I expect to receive a verification email so I can be sure that the provided email address is correct
...
...
...               INFUND-1920 As an applicant once I am accessing my dashboard and clicking on the newly created application for the first time, it will allow me to invite contributors and partners
Test Setup        The guest user opens the browser
Test Teardown     User closes the browser
Resource          ../../../resources/GLOBAL_LIBRARIES.robot
Resource          ../../../resources/variables/GLOBAL_VARIABLES.robot
Resource          ../../../resources/variables/User_credentials.robot
Resource          ../../../resources/keywords/Login_actions.robot
Resource          ../../../resources/keywords/User_actions.robot

*** Variables ***
${APPLICATION_DETAILS_APPLICATION8}    ${SERVER}/application/8/form/question/9

*** Test Cases ***
Non registered users CH route
    [Documentation]    INFUND-669
    ...
    ...    INFUND-1904
    ...
    ...    INFUND-1920
    [Tags]    Create application    HappyPath    FailingForLocal
    Given the user navigates to the page    ${COMPETITION_DETAILS_URL}
    When the user clicks the button/link    jQuery=.column-third .button:contains("Apply now")
    And the user clicks the button/link    jQuery=.button:contains("Sign in to apply")
    And the user clicks the button/link    jQuery=.button:contains("Create")
    And the user enters text to a text field    id=organisationSearchName    Innovate
    And the user clicks the button/link    id=org-search
    And the user clicks the button/link    LINK=INNOVATE LTD
    And the user enters text to a text field    id=addressForm.postcodeInput    2234
    And the user clicks the button/link    id=postcode-lookup
    And the user clicks the button/link    css=#select-address-block > button
    And the user clicks the button/link    jQuery=.button:contains("Save organisation and continue")
    And the user clicks the button/link    jQuery=.button:contains("Save")
    And the user enters the details and clicks the create account    worth.email.test+1@gmail.com
    And the user should be redirected to the correct page    ${REGISTRATION_SUCCESS}
    And the user opens the mailbox and verifies the email
    And the user should be redirected to the correct page    ${REGISTRATION_VERIFIED}
    And the user clicks the button/link    jQuery=.button:contains("Log in")
    And the guest user inserts user email & password    worth.email.test+1@gmail.com    Passw0rd2
    And the guest user clicks the log-in button
    Then the user should see the text in the page    Your dashboard
    And the user clicks the button/link    link=Technology Inspired
    And the user clicks the button/link    jQuery=.button:contains("Begin application")
    And the user should see the text in the page    Application overview

Non registered users non CH route
    [Documentation]    INFUND-669
    ...
    ...    INFUND-1904
    ...
    ...    INFUND-1920
    [Tags]    Create application    HappyPath    FailingForLocal
    Given the user navigates to the page    ${COMPETITION_DETAILS_URL}
    When the user clicks the button/link    jQuery=.column-third .button:contains("Apply now")
    And the user clicks the button/link    jQuery=.button:contains("Sign in to apply")
    And the user clicks the button/link    jQuery=.button:contains("Create")
    And the user clicks the Not on company house link
    And the user clicks the button/link    jQuery=.button:contains("Save")
    And the user enters the details and clicks the create account    worth.email.test+2@gmail.com
    And the user should be redirected to the correct page    ${REGISTRATION_SUCCESS}
    And the user opens the mailbox and verifies the email
    And the user should be redirected to the correct page    ${REGISTRATION_VERIFIED}
    And the user clicks the button/link    jQuery=.button:contains("Log in")
    The guest user inserts user email & password    worth.email.test+2@gmail.com    Passw0rd2
    And the guest user clicks the log-in button
    Then the user should see the text in the page    Your dashboard
    And the user clicks the button/link    link=Technology Inspired
    And the user clicks the button/link    jQuery=.button:contains("Begin application")
    And the user should see the text in the page    Application overview

Verify the name of the new application
    [Documentation]    INFUND-669
    ...
    ...    INFUND-1163
    [Tags]    Applicant    New application    HappyPath    FailingForLocal
    # failing because of a user not being created by failing verification email test
    When guest user log-in    worth.email.test+1@gmail.com    Passw0rd2
    And the user edits the competition title
    Then the user should see the text in the page    test title
    And the progress indicator should show 0
    And the user clicks the button/link    link=View team members and add collaborators
    And the user should see the text in the page    Application team
    And the user should see the text in the page    View and manage your contributors and partners
    And the new application should be visible in the dashboard page
    And the user clicks the button/link    link=test title
    And the user should see the text in the page    test title

*** Keywords ***
the new application should be visible in the dashboard page
    Click Link    link= My dashboard
    sleep    1s
    Wait Until Page Contains    test title
    Page Should Contain    Application number: 0000

the user enters the details and clicks the create account
    [Arguments]    ${REG_EMAIL}
    Input Text    id=firstName    Stuart
    Input Text    id=lastName    ANDERSON
    Input Text    id=phoneNumber    23232323
    Input Text    id=email    ${REG_EMAIL}
    Input Password    id=password    Passw0rd2
    Input Password    id=retypedPassword    Passw0rd2
    Select Checkbox    termsAndConditions
    Submit Form

the user clicks the Not on company house link
    Click Element    name=not-in-company-house
    Click Element    name=manual-address
    Input Text    id=addressForm.selectedPostcode.addressLine1    street
    Input Text    id=addressForm.selectedPostcode.town    town
    Input Text    id=addressForm.selectedPostcode.county    country
    Input Text    id=addressForm.selectedPostcode.postcode    post code
    #Input Text    id=org-name    org1
    Input Text    name=organisationName    org2
    Click Element    jQuery=.button:contains("Continue")

the user edits the competition title
    click link    Technology Inspired
    sleep    2s
    click link    Application details
    Input Text    id=application_details-title    test title
    Click Element    jQuery=button:contains("Save and return")

the progress indicator should show 0
    Element Should Contain    css=.progress-indicator    0

the user opens the mailbox and verifies the email
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
