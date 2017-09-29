*** Settings ***
Documentation   IFS-984 Innovation Leads user journey navigation
...
...             IFS-191 Innovation Lead Stakeholder view filtered dashboard
Suite Setup     The user logs-in in new browser  &{innovation_lead_one}
Suite Teardown  the user closes the browser
Force Tags      InnovationLead
Resource        ../../resources/defaultResources.robot
Resource        ../02__Competition_Setup/CompAdmin_Commons.robot

*** Test Cases ***
Innovation Lead should see Submitted and Ineligible Applications
    [Documentation]  IFS-984
    [Tags]  HappyPath
    Given the user navigates to the page      ${CA_Live}
    Then the user should see all live competitions
    When the user navigates to the page       ${server}/management/competition/${competition_ids['${CLOSED_COMPETITION_NAME}']}
    Then the user should not see the element  jQuery=a:contains("View and update competition setup")
    When the user clicks the button/link      link=Applications: Submitted, ineligible
    And the user clicks the button/link       link=Submitted applications
    Then the user should see the element      jQuery=td:contains("${CLOSED_COMPETITION_APPLICATION_TITLE}") ~ td:contains("57,803")
    When the user navigates to the page       ${server}/management/competition/${competition_ids['${CLOSED_COMPETITION_NAME}']}/applications/ineligible
    Then the user should see the element      css=#application-list
    When the user navigates to the page       ${server}/management/competition/${IN_ASSESSMENT_COMPETITION}/applications/ineligible
    And the user clicks the button/link       jQuery=a:contains(${application_ids["Ineligible Virtualisation"]})
    And the user should not see the element   jQuery=.button:contains("Reinstate application")
    When the user clicks the button/link      jQuery=a:contains("Back")
    Then the user should not see the element  jQuery=.button:contains("Inform applicant")

Innovation lead cannot access CompSetup, Invite Assessors, Manage assessments, Funding decision, All Applictions
    [Documentation]  IFS-984, IFS-1414
    [Tags]
    The user should see permission error on page  ${server}/management/competition/setup/${openCompetitionRTO}
    The user should see permission error on page  ${server}/management/competition/${IN_ASSESSMENT_COMPETITION}/assessors/find
    The user should see permission error on page  ${server}/management/assessment/competition/${competition_ids['${CLOSED_COMPETITION_NAME}']}
    The user should see permission error on page  ${server}/management/competition/${FUNDERS_PANEL_COMPETITION_NUMBER}/funding
    The user should see permission error on page  ${server}/management/competition/${IN_ASSESSMENT_COMPETITION}/applications/all

Innnovation lead can see competitions assigned to him only
    [Documentation]    IFS-191
    [Setup]  Inn lead is added to a competition
    When Log in as a different user                   &{innovation_lead_two}
    Then the user should see the element              jQuery=a:contains("Generic innovation")
    And the user should see the element               jQuery=a:contains(${openCompetitionRTO_name})
    And the user should not see the text in the page  ${openCompetitionBusinessRTO_name}


*** Keywords ***
The user should see permission error on page
    [Arguments]  ${page}
    The user navigates to the page and gets a custom error message  ${page}  ${403_error_message}

Inn lead is added to a competition
    Log in as a different user           &{Comp_admin1_credentials}
    the user navigates to the page       ${COMP_MANAGEMENT_UPDATE_COMP}/manage-innovation-leads/find
    the user clicks the button/link      jQuery=td:contains("Peter Freeman") button:contains("Add")
    the user should not see the element  jQuery=td:contains("Peter Freeman")