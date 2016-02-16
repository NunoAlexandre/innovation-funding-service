*** Settings ***
Suite Setup       Login as User    &{lead_applicant_credentials}
Suite Teardown    User closes the browser
Resource          ../../../resources/GLOBAL_LIBRARIES.robot
Resource          ../../../resources/variables/GLOBAL_VARIABLES.robot
Resource          ../../../resources/variables/User_credentials.robot
Resource          ../../../resources/keywords/Login_actions.robot
Resource          ../../../resources/keywords/Applicant_actions.robot

*** Variables ***
${DAYS_LEFT}      ${EMPTY}

*** Test Cases ***
Verify the "days left to submit" in the dashboard page
    [Documentation]    INFUND-37 As an applicant and I am on the application overview, I can view the status of this application, so I know what actions I need to take
    [Tags]    Applicant     HappyPath
    When the applicant is in the dashboard page
    Then the Applicant should see the "days left to submit" in the dashboard page
    And the "days left to submit" should be correct in the dashboard page

*** Keywords ***
the applicant is in the dashboard page
    go to    ${DASHBOARD_URL}

the Applicant should see the "days left to submit" in the dashboard page
    Element Should Be Visible    css=#content > div > section.in-progress > ul > li:nth-child(1) > div > div:nth-child(2) > div.pie-container > div.pie-overlay

the "days left to submit" should be correct in the dashboard page
    ${DAYS_LEFT}=    Get Text    css=.pie-overlay div
    Should Be True    ${DAYS_LEFT}>=0
