*** Settings ***
Documentation     INFUND-2607 As an applicant I want to have a link to the feedback for my application from the Application Overview page when it becomes available so I can review the assessor feedback for my application
Suite Setup       Log in as user    email=john.doe@innovateuk.test    password=Passw0rd
Suite Teardown    User closes the browser
Force Tags        Comp admin    Upload
Resource          ../../../resources/GLOBAL_LIBRARIES.robot
Resource          ../../../resources/variables/GLOBAL_VARIABLES.robot
Resource          ../../../resources/variables/User_credentials.robot
Resource          ../../../resources/keywords/Login_actions.robot
Resource          ../../../resources/keywords/User_actions.robot

*** Variables ***
${successful_application_overview}    ${server}/application/16
${unsuccessful_application_overview}    ${server}/application/17

*** Test Cases ***
Partner can view the file
    [Documentation]    INFUND-2607
    [Tags]
    [Setup]    Run Keywords    Logout as user
    Given guest user log-in    worth.email.test+fundsuccess@gmail.com    Passw0rd
    And the user navigates to the page    ${successful_application_overview}
    When the user should see the text in the page    ${valid_pdf}
    And the user clicks the button/link    link=testing.pdf (7.94 KB)
    Then the user should see the text in the page    ${valid_pdf_excerpt}
    [Teardown]    the user navigates to the page    ${successful_application_overview}

Partner cannot remove the file
    [Documentation]    INFUND-2607
    [Tags]
    When the user should see the text in the page    ${valid_pdf}
    Then the user should not see the text in the page    Remove
    And the user should not see the element    link=Remove

Partner can download the file
    [Documentation]    INFUND-2607
    [Tags]    Pending
    # Pending until download functionality has been plugged in
    Given the user should see the text in the page    ${valid_pdf}
    When the user downloads the file from the link    ${valid_pdf}    ${download_link}
    Then the file should be downloaded    ${valid_pdf}
    [Teardown]    Remove File    ${valid_pdf}

Comp admin can view partner's feedback
    [Documentation]    INFUND-2607
    [Tags]
    [Setup]    Run Keywords    Logout as user
    Given guest user log-in    john.doe@innovateuk.test    Passw0rd
    When the user navigates to the page    ${unsuccessful_application_overview}
    And the user should see the text in the page    ${valid_pdf}
    And the user clicks the button/link    link=testing.pdf (7.94 KB)
    Then the user should see the text in the page    ${valid_pdf_excerpt}

Comp admin can view unsuccessful applicant's feedback
    [Documentation]    INFUND-2607
    [Tags]
    Given the user navigates to the page    ${unsuccessful_application_overview}
    When the user should see the text in the page    ${valid_pdf}
    And the user clicks the button/link    link=testing.pdf (7.94 KB)
    Then the user should see the text in the page    ${valid_pdf_excerpt}
    And the user navigates to the page    ${unsuccessful_application_overview}
    [Teardown]    Logout as user

Unsuccessful applicant can view the file
    [Documentation]    INFUND-2607
    [Tags]
    [Setup]    guest user log-in    worth.email.test.two+fundfailure@gmail.com    Passw0rd
    Given the user navigates to the page    ${unsuccessful_application_overview}
    When the user should see the text in the page    ${valid_pdf}
    And the user clicks the button/link    link=testing.pdf (7.94 KB)
    Then the user should see the text in the page    ${valid_pdf_excerpt}
    [Teardown]    the user navigates to the page    ${unsuccessful_application_overview}

Unsuccessful applicant cannot remove the file
    [Documentation]    INFUND-2607
    [Tags]
    When the user should see the text in the page    ${valid_pdf}
    Then the user should not see the text in the page    Remove
    And the user should not see the element    link=Remove

Unsuccessful applicant can download the file
    [Documentation]    INFUND-2607
    [Tags]    Pending
    # Pending until download functionality has been plugged in
    Given the user should see the text in the page    ${valid_pdf}
    When the user downloads the file from the link    ${valid_pdf}    ${download_link}
    Then the file should be downloaded    ${valid_pdf}
    [Teardown]    Remove File    ${valid_pdf}
