*** Settings ***
Documentation     INFUND-4821: As a project finance team member I want to have a summary overview of project details for this competition so I can refer to this in a consistent way throughout the finance checks section
...
...               INFUND-4903: As a Project Finance team member I want to view a list of the status of all partners' bank details checks so that I can navigate from the internal dashboard
...
...               INFUND-4049: As an internal user I want to have an overview of where a project is in the Project Setup process so that I can view and manage outstanding tasks
...
...               INFUND-5516:  As a internal user, I want to view the Project Setup status link
...
...               INFUND-5300: As a Project Finance team member I want to have an equivalent dashboard to the Competitions team for Project Setup so that I can view the appropriate partners'
...                            statuses and access pages appropriate to my role
...
...               INFUND-7109 Bank Details Status - Internal user
...
...               INFUND-5899 As an internal user I want to be able to use the breadcrumb navigation consistently throughout Project Setup so I can return to the previous page as appropriate
Suite Setup
Suite Teardown    the user closes the browser
Force Tags        Project Setup
Resource          ../../resources/defaultResources.robot
Resource          PS_Variables.robot

*** Variables ***

*** Test Cases ***

# Project Finance can see Bank Details - testcase moved to 04__experian_feedback.robot
Other internal users cannot see Bank details or Finance checks
    [Documentation]    INFUND-4903, INFUND-5720
    [Tags]    Experian    HappyPath
    [Setup]    Log in as user    john.doe@innovateuk.test    Passw0rd
    # This is added to HappyPath because CompAdmin should NOT have access to Bank details
    Given the user navigates to the page          ${COMP_MANAGEMENT_PROJECT_SETUP}
    And the user clicks the button/link           link=${PROJECT_SETUP_COMPETITION_NAME}
    Then the user should see the element          jQuery=h2:contains("Projects in setup")
    And the user should not see the element       jQuery=#table-project-status tr:nth-of-type(1) td.status.waiting:nth-of-type(3) a
    And the user should not see the element       jQuery=#table-project-status tr:nth-of-type(1) td.status.action:nth-of-type(4) a
    And the user navigates to the page and gets a custom error message    ${server}/project-setup-management/project/${PROJECT_SETUP_APPLICATION_1_PROJECT}/review-all-bank-details    You do not have the necessary permissions for your request
    And the user navigates to the page and gets a custom error message    ${server}/project-setup-management/project/${PROJECT_SETUP_APPLICATION_1_PROJECT}/finance-check    You do not have the necessary permissions for your request


Project Finance user can see the internal project summary page
    [Documentation]    INFUND-4049, INFUND-5144
    [Tags]
    [Setup]    log in as a different user    lee.bowman@innovateuk.test    Passw0rd
    Given the user navigates to the page    ${internal_project_summary}
    Then the user should see the text in the page    ${PROJECT_SETUP_APPLICATION_1_TITLE}
    And the user clicks the button/link    xpath=//a[contains(@href, 'project-setup-management/project/${PROJECT_SETUP_APPLICATION_1_PROJECT}/monitoring-officer')]
    And the user goes back to the previous page
    And the user should not see the element    xpath=//a[contains(@href, '/project-setup-management/project/${INFORM_APPLICATION_1_PROJECT}/spend-profile/approval')]    # since the spend profile hasn't been generated yet - see INFUND-5144


Comp Admin user cannot see the finance check summary page(duplicate)
    [Documentation]    INFUND-4821
    [Tags]
    [Setup]    Log in as a different user    john.doe@innovateuk.test    Passw0rd
    Given the user navigates to the page and gets a custom error message    ${server}/project-setup-management/project/${PROJECT_SETUP_APPLICATION_1_PROJECT}/finance-check    You do not have the necessary permissions for your request

Comp Admin user can see the internal project summary page
    [Documentation]    INFUND-4049, INFUND-5899
    [Tags]
    Given the user navigates to the page    ${internal_project_summary}
    Then the user should see the text in the page    ${PROJECT_SETUP_APPLICATION_1_TITLE}
    And the user clicks the button/link    xpath=//a[contains(@href, '/project-setup-management/project/${PROJECT_SETUP_APPLICATION_1_PROJECT}/monitoring-officer')]
    And the user should not see an error in the page
    And the user goes back to the previous page
    When the user clicks the button/link    link=Competition dashboard
    Then the user should see the text in the page    All competitions
    [Teardown]    the user goes back to the previous page


Project Finance has a dashboard and can see projects in PS
    [Documentation]    INFUND-5300
    [Tags]
    [Setup]  Log in as a different user    lee.bowman@innovateuk.test    Passw0rd
    Given the user navigates to the page  ${COMP_MANAGEMENT_PROJECT_SETUP}
    Then the user should see the element    link=${PROJECT_SETUP_COMPETITION_NAME}
    When the user clicks the button/link    link=${PROJECT_SETUP_COMPETITION_NAME}
    Then the user should see the element    jQuery=.column-third.alignright.extra-margin h2:contains("Projects in setup")
    And the user should see the element     jQuery=tr:nth-child(1) th:contains("${PROJECT_SETUP_APPLICATION_1_TITLE}")
    And the user should see the element     jQuery=tr:nth-child(1) th a:contains("${PROJECT_SETUP_APPLICATION_1_NUMBER}")
    And the user should see the element     jQuery=tr:nth-child(1) th:contains("3 partners")
    And the user should see the element     jQuery=tr:nth-child(1) th:contains("Lead: ${PROJECT_SETUP_APPLICATION_1_LEAD_ORGANISATION_NAME}")
    And the user should see the element     jQuery=tr:nth-child(2) th:contains("Office Chair for Life")
    And the user should see the element     jQuery=tr:nth-child(3) th:contains("Elbow grease")
    When the user clicks the button/link    link=${PROJECT_SETUP_APPLICATION_1_NUMBER}
    Then the user navigates to the page     ${server}/management/competition/${PROJECT_SETUP_COMPETITION}/application/${PROJECT_SETUP_APPLICATION_1}
    And the user should not see an error in the page

Project Finance can see the status of projects in PS
    [Documentation]  INFUND-5300, INFUND-7109
    [Tags]
    Given the user navigates to the page    ${internal_project_summary}
    Then the user should see the element    jQuery=#table-project-status tr:nth-of-type(1) td:nth-of-type(1).status.ok
    And the user should see the element     jQuery=#table-project-status tr:nth-of-type(1) td:nth-of-type(2).status.ok
    And the user should not see the element  jQuery=#table-project-status tr:nth-of-type(1) td:nth-of-type(3).status.waiting
    And the user should see the element     jQuery=#table-project-status tr:nth-of-type(1) td:nth-of-type(4).status.action


