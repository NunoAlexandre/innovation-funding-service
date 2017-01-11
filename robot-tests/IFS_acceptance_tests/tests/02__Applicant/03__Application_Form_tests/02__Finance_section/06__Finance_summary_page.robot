*** Settings ***
Documentation     INFUND-524 As an applicant I want to see the finance summary updated and recalculated as each partner adds their finances.
...
...               INFUND-435 As an applicant and I am on the finance summary, I want to see the partner details listed horizontally so I can see all partner details in the finance summary table
...
...               INFUND-927 As a lead partner i want the system to show me when all questions and sections (partner finances) are complete on the finance summary, so that i know i can submit the application
...
...               INFUND-894 As a lead partner I want to easily see whether or not my partner's finances are marked as complete, so that i can have the right level of confidence in the figures
...
...               INFUND-438: As an applicant and I am filling in the finance details I want a fully working Other funding section
...
...               INFUND-1436 As a lead applicant I want to be able to view the ratio of research participation costs in my consortium so I know my application is within the required range
Suite Setup       log in and create new application if there is not one already
Suite Teardown    the user closes the browser
Force Tags        Applicant
Default Tags
Resource          ../../../../resources/defaultResources.robot

*** Variables ***
${OVERVIEW_PAGE_PROVIDING_SUSTAINABLE_CHILDCARE_APPLICATION}    ${SERVER}/application/${OPEN_COMPETITION_APPLICATION_2}
${PROVIDING_SUSTAINABLE_CHILDCARE_FINANCE_SECTION}    ${SERVER}/application/${OPEN_COMPETITION_APPLICATION_2}/form/section/7
${PROVIDING_SUSTAINABLE_CHILDCARE_FINANCE_SUMMARY}    ${SERVER}/application/${OPEN_COMPETITION_APPLICATION_2}/form/section/8

*** Test Cases ***
Calculations for Lead applicant
    [Documentation]    INFUND-524
    ...
    ...    This test case still use the old application after the refactoring. We need to add an extra collaborator in the newly created application for this.
    [Tags]
    When the user navigates to the page    ${PROVIDING_SUSTAINABLE_CHILDCARE_FINANCE_SUMMARY}
    Then the finance summary calculations should be correct
    And the finance Project cost breakdown calculations should be correct
    [Teardown]    The user closes the browser

Calculations for the first collaborator
    [Documentation]    INFUND-524
    ...
    ...
    ...    This test case still use the old application after the refactoring
    [Tags]
    [Setup]    Guest user log-in    &{collaborator1_credentials}
    When the user navigates to the page    ${PROVIDING_SUSTAINABLE_CHILDCARE_FINANCE_SUMMARY}
    Then the finance summary calculations should be correct
    And the finance Project cost breakdown calculations should be correct
    And the applicant enters a bigger funding amount
    Then the contribution to project and funding sought should be 0 and not a negative number
    [Teardown]    The user closes the browser

Red warning should show when the finances are incomplete
    [Documentation]    INFUND-927
    ...
    ...    INFUND-894
    ...
    ...    INFUND-446
    [Tags]    HappyPath
    [Setup]    Guest user log-in    &{lead_applicant_credentials}
    When the user navigates to the page    ${DASHBOARD_URL}
    And the user clicks the button/link    link=Robot test application
    And the user clicks the button/link    link=Finances overview
    Then the red warning should be visible
    And the user should see the element    css=.warning-alert
    And the user should see the text in the page    The following organisations have not marked their finances as complete:

Green check should show when the finances are complete
    [Documentation]    INFUND-927, INFUND-894, INFUND-446
    [Tags]  Pending
    #TODO Pending due to INFUND-6390 will update ticket onces finances update is merged.
    [Setup]    Make the finances ready for mark as complete
    When the user marks the finances as complete
    Then the user redirects to the page    Please provide Innovate UK with information about your project.    Application overview
    And the user clicks the button/link    link=Finances overview
    Then Green check should be visible
    [Teardown]    The user closes the browser

Alert shows If the academic research participation is too high
    [Documentation]    INFUND-1436
    [Tags]    Email
    [Setup]    Login new application invite academic    ${test_mailbox_one}+academictest@gmail.com    Invitation to collaborate in ${OPEN_COMPETITION_NAME}    participate in their project
    Given guest user log-in    ${test_mailbox_one}+academictest@gmail.com    Passw0rd123
    And The user navigates to the academic application finances
    When the user enters text to a text field    id=incurred-staff    1000000000
    And Guest user log-in    &{lead_applicant_credentials}
    And the user navigates to the finance overview of the academic
    Then the user should see the text in the page    The participation levels of this project are not within the required range
    And the user navigates to the page    ${DASHBOARD_URL}
    And the user clicks the button/link    link=Academic robot test application
    And the user clicks the button/link    link=Review and submit
    And the user clicks the button/link    jquery=button:contains("Finances Summary")
    Then the user should see the text in the page    The participation levels of this project are not within the required range
    [Teardown]

Alert should not show If research participation is below the maximum level
    [Documentation]    INFUND-1436
    [Tags]  Pending
    #TODO Pending due to INFUND-6390 will update ticket onces finances update is merged.
    [Setup]    Log in as a different user   &{lead_applicant_credentials}
    When Lead enters a valid research participation value
    And the user navigates to the finance overview of the academic
    Then the user should see the text in the page    The participation levels of this project are within the required range
    And the user navigates to the page    ${DASHBOARD_URL}
    And the user clicks the button/link    link=Academic robot test application
    And the user clicks the button/link    link=Review and submit
    And the user clicks the button/link    jquery=button:contains("Finances Summary")
    Then the user should see the text in the page    The participation levels of this project are within the required range
    [Teardown]

*** Keywords ***
the finance Project cost breakdown calculations should be correct
    the user sees the text in the element    css=.project-cost-breakdown tr:nth-of-type(1) td:nth-of-type(3)    £385
    the user sees the text in the element    css=.project-cost-breakdown tr:nth-of-type(4) td:nth-of-type(1)    £202,169
    the user sees the text in the element    css=.project-cost-breakdown tr:nth-of-type(2) td:nth-of-type(1)    £100,837
    the user sees the text in the element    css=.project-cost-breakdown tr:nth-of-type(3) td:nth-of-type(1)    £495

the finance summary calculations should be correct
    the user sees the text in the element    css=.finance-summary tr:nth-of-type(4) td:nth-of-type(1)    £202,169
    the user sees the text in the element    css=.finance-summary tr:nth-of-type(1) td:nth-of-type(2)    30%
    the user sees the text in the element    css=.finance-summary tr:nth-of-type(2) td:nth-of-type(2)    30%
    the user sees the text in the element    css=.finance-summary tr:nth-of-type(3) td:nth-of-type(2)    100%
    the user sees the text in the element    css=.finance-summary tr:nth-of-type(4) td:nth-of-type(3)    £58,529
    the user sees the text in the element    css=.finance-summary tr:nth-of-type(4) td:nth-of-type(4)    £2,468
    the user sees the text in the element    css=.finance-summary tr:nth-of-type(4) td:nth-of-type(5)    £141,172

the applicant enters a bigger funding amount
    [Documentation]    Check if the Contribution to project and the Funding sought remain £0 and not minus
    the user navigates to the page    ${PROVIDING_SUSTAINABLE_CHILDCARE_FINANCE_SECTION}
    the user enters text to a text field    css=#other-funding-table tbody tr:nth-of-type(1) td:nth-of-type(3) input    8000000
    the user enters text to a text field    css=#other-funding-table tbody tr:nth-of-type(1) td:nth-of-type(1) input    test2
    Execute Javascript    jQuery('form').attr('data-test','true');

the contribution to project and funding sought should be 0 and not a negative number
    the user navigates to the page    ${PROVIDING_SUSTAINABLE_CHILDCARE_FINANCE_SUMMARY}
    the user sees the text in the element    css=.finance-summary tr:nth-of-type(2) td:nth-of-type(3)    £0
    the user sees the text in the element    css=.finance-summary tr:nth-of-type(2) td:nth-of-type(5)    £0

Green check should be visible
    Page Should Contain Image    css=.finance-summary tr:nth-of-type(1) img[src*="/images/field/tick-icon"]

the red warning should be visible
    When the user navigates to the page    ${DASHBOARD_URL}
    And the user clicks the button/link    link=Robot test application
    And the user clicks the button/link    link=Finances overview
    Page Should Contain Image    css=.finance-summary tr:nth-of-type(1) img[src*="/images/warning-icon"]

Lead enters a valid research participation value
    When The user navigates to the academic application finances
    the user clicks the button/link    jQuery=button:contains("Labour")
    the user should see the element    name=add_cost
    the user clicks the button/link    jQuery=button:contains('Add another role')
    the user should see the element    css=.labour-costs-table tr:nth-of-type(1) td:nth-of-type(2) input
    the user enters text to a text field    css=.labour-costs-table tr:nth-of-type(1) td:nth-of-type(2) input    1200000000
    the user enters text to a text field    css=.labour-costs-table tr:nth-of-type(1) td:nth-of-type(4) input    1000
    the user enters text to a text field    css=.labour-costs-table tr:nth-of-type(1) td:nth-of-type(1) input    Test
    Focus    jQuery= button:contains('Save and return')
    wait for autosave
