*** Settings ***
Documentation     INFUND-7363 Inflight competitions dashboards: In assessment dashboard
Suite Setup       Guest user log-in    &{Comp_admin1_credentials}
Suite Teardown    The user closes the browser
Force Tags        CompAdmin    Assessor
Resource          ../../../resources/defaultResources.robot

*** Test Cases ***
In Assessment dashboard page
    [Documentation]    INFUND-7363
    Given The user clicks the button/link    link=${IN_ASSESSMENT_COMPETITION_NAME}
    Then The user should see the text in the page    00000004: Sustainable living models for the future
    And The user should see the text in the page    In assessment
    And The user should see the text in the page    Programme
    And The user should see the text in the page    Materials and manufacturing
    And The user should see the text in the page    Earth Observation
    And the user should not see the element    link=View and update competition setup
    #The following checks test if the correct buttons are disabled
    And the user should see the element    jQuery=.disabled[aria-disabled="true"]:contains("Input and review funding decision")

Milestones for In Assessment competitions
    [Documentation]    INFUND-7561
    Then the user should see the element    jQuery=button:contains("Close assessment")
    And the user should see the element    css=li:nth-child(9).not-done    #this keyword verifies that the 8. Line Draw is not done
    And the user should see the element    css=li:nth-child(5).done    #this keyword verifies that the 5.Assessor briefing is done
