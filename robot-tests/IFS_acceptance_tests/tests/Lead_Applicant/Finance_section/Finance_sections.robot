*** Settings ***
Documentation     INFUND-45: As an applicant and I am on the application form on an open application, I expect the form to help me fill in financial details, so I can have a clear overview and less chance of making mistakes.
Suite Setup       Login as User    &{lead_applicant_credentials}
Suite Teardown    TestTeardown User closes the browser
Resource          ../../../resources/GLOBAL_LIBRARIES.robot
Resource          ../../../resources/variables/GLOBAL_VARIABLES.robot
Resource          ../../../resources/variables/User_credentials.robot
Resource          ../../../resources/keywords/Login_actions.robot
Resource          ../../../resources/keywords/Applicant_actions.robot

*** variables ***
${APPLICANT_USERNAME}    applicant@innovateuk.gov.uk
${APPLICANT_PSW}    test

*** Test Cases ***
Finance sub-sections
    [Documentation]    INFUND-192
    [Tags]    Applicant    Finance
    When Applicant goes to the Your finances section
    Then the Applicant should see all the "Your Finance" Sections

Guidance in the 'Your Finances' section
    [Documentation]    INFUND-192
    [Tags]    Applicant    Finance
    Given Applicant goes to the Your finances section
    When the Applicant is in the Labour sub-section
    And the Applicant clicks the "Labour costs guidance"
    Then the guidance text should be visible

*** Keywords ***
the Applicant should see all the "Your Finance" Sections
    Page Should Contain Element    css=.question section:nth-of-type(1) button
    Page Should Contain Element    css=.question section:nth-of-type(2) button
    Page Should Contain Element    css=.question section:nth-of-type(3) button
    Page Should Contain Element    css=.question section:nth-of-type(4) button
    Page Should Contain Element    css=.question section:nth-of-type(5) button
    Page Should Contain Element    css=.question section:nth-of-type(6) button
    Page Should Contain Element    css=.question section:nth-of-type(7) button

the Applicant is in the Labour sub-section
    Click Element    css=.question section:nth-of-type(1) button

the Applicant clicks the "Labour costs guidance"
    Click Element    css=#collapsible-1 summary

the guidance text should be visible
    Element Should Be Visible    css=#details-content-0 p
