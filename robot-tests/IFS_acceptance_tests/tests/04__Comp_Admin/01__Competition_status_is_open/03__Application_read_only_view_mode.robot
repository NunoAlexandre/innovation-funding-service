*** Settings ***
Documentation     INFUND-2443 Acceptance test: Check that the comp manager cannot edit an application's finances
...
...               INFUND-2304 Read only view mode of applications from the application list page
Suite Setup       Run Keywords    Log in as user    &{Comp_admin1_credentials}
...               AND    Given the user navigates to the page    ${COMP_MANAGEMENT_APPLICATIONS_LIST}
Suite Teardown    the user closes the browser
Force Tags
Resource          ../../../resources/GLOBAL_LIBRARIES.robot
Resource          ../../../resources/variables/GLOBAL_VARIABLES.robot
Resource          ../../../resources/variables/User_credentials.robot
Resource          ../../../resources/keywords/Login_actions.robot
Resource          ../../../resources/keywords/User_actions.robot

*** Variables ***
${valid_pdf}      testing.pdf
${valid_pdf_excerpt}    Adobe PDF is an ideal format for electronic document distribution
${quarantine_warning}   This file has been quarantined by the virus scanner

*** Test Cases ***
Comp admin can open the view mode of the application
    [Documentation]    INFUND-2300
    ...
    ...    INFUND-2304
    ...
    ...    INFUND-2435
    [Tags]    Competition management
    [Setup]    Run keywords    Log in as user    &{lead_applicant_credentials}
    ...    AND    the user can see the option to upload a file on the page    ${technical_approach_url}
    ...    AND    the user uploads the file to the 'technical approach' question    ${valid_pdf}

    Given log in as user    &{Comp_admin1_credentials}
    And the user navigates to the page    ${COMP_MANAGEMENT_APPLICATIONS_LIST}
    When the user clicks the button/link    link=00000001
    Then the user should be redirected to the correct page    ${COMP_MANAGEMENT_APPLICATION_1_OVERVIEW}
    And the user should see the element    link=Print application
    And the user should see the text in the page    A novel solution to an old problem
    And the user should see the text in the page   ${valid_pdf}
    And the user can view this file without any errors
    # And the user should see the text in the page         ${quarantine_pdf}
    # nad the user cannot see this file but gets a quarantined message

Comp admin should not be able to edit the finances
    [Documentation]    INFUND-2443
    Given the user navigates to the page    ${COMP_MANAGEMENT_APPLICATION_1_OVERVIEW}
    When the user clicks the button/link    jQuery=button:contains("Finances Summary")
    Then the user should not see the element    link=your finances

Comp admin should view the detailed finance section for every partner
    [Documentation]    INFUND-2483
    [Tags]     Pending
    # Pending due to ongoing work
    Given the user navigates to the page    ${Providing_Sustainable_Childcare_Application_Overview}
    When the user clicks the button/link    jQuery=button:contains("Finances Summary")
    Then the user should see the text in the page    Funding breakdown
    And the finance summary calculations should be correct
    And the finance Project cost breakdown calculations should be correct

*** Keywords ***
the user uploads the file to the 'technical approach' question
    [Arguments]    ${file_name}
    Choose File    name=formInput[14]    ${UPLOAD_FOLDER}/${file_name}
    Sleep    500ms

the user can see the option to upload a file on the page
    [Arguments]    ${url}
    The user navigates to the page    ${url}
    the user should see the text in the page        Upload

the user can view this file without any errors
    the user clicks the button/link         link=testing.pdf(7 KB)
    the user should see the text in the page    ${valid_pdf_excerpt}
    the user goes back to the previous page

the user cannot see this file but gets a quarantined message
    the user clicks the button/link      link=test_quarantine.pdf(7 KB)
    the user should not see the text in the page    ${valid_pdf_excerpt}
    the user should see the text in the page        ${quarantine_warning}

the finance summary calculations should be correct
    Element Should Contain    css=.finance-summary tr:nth-of-type(4) td:nth-of-type(1)    £129,000
    Element Should Contain    css=.finance-summary tr:nth-of-type(1) td:nth-of-type(2)    50%
    Element Should Contain    css=.finance-summary tr:nth-of-type(2) td:nth-of-type(2)    70%
    Element Should Contain    css=.finance-summary tr:nth-of-type(3) td:nth-of-type(2)    100%
    Element Should Contain    css=.finance-summary tr:nth-of-type(4) td:nth-of-type(3)    £61,000
    Element Should Contain    css=.finance-summary tr:nth-of-type(4) td:nth-of-type(4)    £20,000
    Element Should Contain    css=.finance-summary tr:nth-of-type(4) td:nth-of-type(5)    £48,000

the finance Project cost breakdown calculations should be correct
    Element Should Contain    css=.project-cost-breakdown tr:nth-of-type(1) td:nth-of-type(3)    £0
    Element Should Contain    css=.project-cost-breakdown tr:nth-of-type(4) td:nth-of-type(1)    £129,000
    Element Should Contain    css=.project-cost-breakdown tr:nth-of-type(1) td:nth-of-type(1)    £60,000
    Element Should Contain    css=.project-cost-breakdown tr:nth-of-type(2) td:nth-of-type(1)    £60,000
    Element Should Contain    css=.project-cost-breakdown tr:nth-of-type(3) td:nth-of-type(1)    £9,000