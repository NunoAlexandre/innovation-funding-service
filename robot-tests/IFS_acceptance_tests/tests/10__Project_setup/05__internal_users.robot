*** Settings ***
Documentation     INFUND-4821: As a project finance team member I want to have a summary overview of project details for this competition so I can refer to this in a consistent way throughout the finance checks section
...
...               INFUND-4903: As a Project Finance team member I want to view a list of the status of all partners' bank details checks so that I can navigate from the internal dashboard
...
...               INFUND-4049: As an internal user I want to have an overview of where a project is in the Project Setup process so that I can view and manage outstanding tasks
...
...               INFUND-5516:  As an internal user, I want to view the Project Setup status link
...
...               INFUND-5300: As a Project Finance team member I want to have an equivalent dashboard to the Competitions team for Project Setup so that I can view the appropriate partners'
...                            statuses and access pages appropriate to my role
...
...               INFUND-7109 Bank Details Status - Internal user
...
...               INFUND-5899 As an internal user I want to be able to use the breadcrumb navigation consistently throughout Project Setup so I can return to the previous page as appropriate
Suite Setup       the project is completed if it is not already complete
Suite Teardown    the user closes the browser
Force Tags        Project Setup
Resource          ../../resources/defaultResources.robot

*** Variables ***

*** Test Cases ***

# Project Finance can see Bank Details - testcase moved to 04__experian_feedback.robot
Other internal users cannot see Bank details or Finance checks
    [Documentation]    INFUND-4903, INFUND-5720
    [Tags]    Experian    HappyPath
    [Setup]    Log in as user    &{Comp_admin1_credentials}
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
    [Setup]    log in as a different user    &{internal_finance_credentials}
    Given the user navigates to the page    ${internal_project_summary}
    Then the user should see the text in the page    ${PROJECT_SETUP_APPLICATION_1_TITLE}
    And the user clicks the button/link    xpath=//a[contains(@href, 'project-setup-management/project/${PROJECT_SETUP_APPLICATION_1_PROJECT}/monitoring-officer')]
    And the user goes back to the previous page
    And the user should not see the element    xpath=//a[contains(@href, '/project-setup-management/project/${INFORM_APPLICATION_1_PROJECT}/spend-profile/approval')]    # since the spend profile hasn't been generated yet - see INFUND-5144


Comp Admin user cannot see the finance check summary page(duplicate)
    [Documentation]    INFUND-4821
    [Tags]
    [Setup]    Log in as a different user    &{Comp_admin1_credentials}
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
    [Setup]  Log in as a different user    &{internal_finance_credentials}
    Given the user navigates to the page  ${COMP_MANAGEMENT_PROJECT_SETUP}
    Then the user should see the element    link=${PROJECT_SETUP_COMPETITION_NAME}
    When the user clicks the button/link    link=${PROJECT_SETUP_COMPETITION_NAME}
    Then the user should see the element    jQuery=.column-third.alignright.extra-margin h2:contains("Projects in setup")
    And the user should see the element     jQuery=tr:nth-child(2) th:contains("${PROJECT_SETUP_APPLICATION_1_TITLE}")
    And the user should see the element     jQuery=tr:nth-child(2) th a:contains("${PROJECT_SETUP_APPLICATION_1_NUMBER}")
    And the user should see the element     jQuery=tr:nth-child(2) th:contains("3 partners")
    And the user should see the element     jQuery=tr:nth-child(2) th:contains("Lead: ${PROJECT_SETUP_APPLICATION_1_LEAD_ORGANISATION_NAME}")
    And the user should see the element     jQuery=tr:nth-child(3) th:contains("Office Chair for Life")
    And the user should see the element     jQuery=tr:nth-child(1) th:contains("Elbow grease")
    When the user clicks the button/link    link=${PROJECT_SETUP_APPLICATION_1_NUMBER}
    Then the user navigates to the page     ${server}/management/competition/${PROJECT_SETUP_COMPETITION}/application/${PROJECT_SETUP_APPLICATION_1}
    And the user should not see an error in the page

Project Finance can see the status of projects in PS
    [Documentation]  INFUND-5300, INFUND-7109
    [Tags]
    Given the user navigates to the page    ${internal_project_summary}
    Then the user should see the element    jQuery=#table-project-status tr:nth-of-type(2) td:nth-of-type(1).status.ok
    And the user should see the element     jQuery=#table-project-status tr:nth-of-type(2) td:nth-of-type(2).status.ok
    And the user should not see the element  jQuery=#table-project-status tr:nth-of-type(2) td:nth-of-type(3).status.waiting
    And the user should see the element     jQuery=#table-project-status tr:nth-of-type(2) td:nth-of-type(4).status.action


*** Keywords ***

the project is completed if it is not already complete
    log in as user    &{lead_applicant_credentials}
    the user navigates to the page    ${server}/project-setup/project/${PROJECT_SETUP_APPLICATION_1_PROJECT}/details
    ${project_manager_not_set}    ${value}=    run keyword and ignore error without screenshots    The user should not see the element    jQuery=#project-manager-status.yes
    run keyword if    '${project_manager_not_set}' == 'PASS'     all previous sections of the project are completed

all previous sections of the project are completed
    lead partner selects project manager and address
    partners submit their finance contacts
    partners submit bank details
    project finance approves bank details
    project finance fills up monitoring officer

lead partner selects project manager and address
    log in as a different user           &{lead_applicant_credentials}
    the user navigates to the page       ${project_in_setup_details_page}
    the user clicks the button/link      link=Project Manager
    the user selects the radio button    projectManager    projectManager2
    the user clicks the button/link      jQuery=.button:contains("Save")
    the user clicks the button/link      link=Project address
    the user selects the radio button    addressType    REGISTERED
    the user clicks the button/link    jQuery=.button:contains("Save project address")
    the user clicks the button/link      jQuery=.button:contains("Mark as complete")
    the user clicks the button/link      jQuery=button:contains("Submit")

partners submit their finance contacts
    the user navigates to the page     ${server}/project-setup/project/${PROJECT_SETUP_APPLICATION_1_PROJECT}/details/finance-contact?organisation=${PROJECT_SETUP_APPLICATION_1_LEAD_ORGANISATION_ID}
    the user selects the radio button  financeContact    financeContact1
    the user clicks the button/link    jQuery=.button:contains("Save")
    log in as a different user         &{collaborator1_credentials}
    the user navigates to the page     ${server}/project-setup/project/${PROJECT_SETUP_APPLICATION_1_PROJECT}/details/finance-contact?organisation=${PROJECT_SETUP_APPLICATION_1_PARTNER_ID}
    the user selects the radio button  financeContact    financeContact1
    the user clicks the button/link    jQuery=.button:contains("Save")
    log in as a different user         &{collaborator2_credentials}
    the user navigates to the page     ${server}/project-setup/project/${PROJECT_SETUP_APPLICATION_1_PROJECT}/details/finance-contact?organisation=${PROJECT_SETUP_APPLICATION_1_ACADEMIC_PARTNER_ID}
    the user selects the radio button  financeContact    financeContact1
    the user clicks the button/link    jQuery=.button:contains("Save")

partners submit bank details
    partner submits his bank details  ${PROJECT_SETUP_APPLICATION_1_LEAD_PARTNER_EMAIL}
    partner submits his bank details  ${PROJECT_SETUP_APPLICATION_1_ACADEMIC_PARTNER_EMAIL}

partner submits his bank details
    [Arguments]  ${email}
    log in as a different user            ${email}    ${short_password}
    the user navigates to the page        ${server}/project-setup/project/${PROJECT_SETUP_APPLICATION_1_PROJECT}/bank-details
    the user enters text to a text field  id=bank-acc-number  51406795
    the user enters text to a text field  id=bank-sort-code  404745
    the user selects the radio button     addressType    REGISTERED
    the user clicks the button/link       jQuery=.button:contains("Submit bank account details")
    the user clicks the button/link       jQuery=.button:contains("Submit")

project finance approves bank details
    log in as a different user                   &{internal_finance_credentials}
    proj finance approves partners bank details  ${PROJECT_SETUP_APPLICATION_1_LEAD_ORGANISATION_ID}
    proj finance approves partners bank details  ${PROJECT_SETUP_APPLICATION_1_ACADEMIC_PARTNER_ID}

proj finance approves partners bank details
    [Arguments]  ${id}
    the user navigates to the page     ${server}/project-setup-management/project/${PROJECT_SETUP_APPLICATION_1_PROJECT}/organisation/${id}/review-bank-details
    the user clicks the button/link    jQuery=.button:contains("Approve bank account details")
    the user clicks the button/link    jQuery=.button:contains("Approve account")

project finance fills up monitoring officer
    the user navigates to the page          ${server}/project-setup-management/project/${PROJECT_SETUP_APPLICATION_1_PROJECT}/monitoring-officer
    the user enters text to a text field    id=firstName    Grace
    the user enters text to a text field    id=lastName    Harper
    The user enters text to a text field    id=emailAddress    ${test_mailbox_two}+monitoringofficer@gmail.com
    The user enters text to a text field    id=phoneNumber    08549731414
    the user clicks the button/link         jQuery=.button[type="submit"]:contains("Assign Monitoring Officer")
    the user clicks the button/link         jQuery=.modal-assign-mo button:contains("Assign Monitoring Officer")







