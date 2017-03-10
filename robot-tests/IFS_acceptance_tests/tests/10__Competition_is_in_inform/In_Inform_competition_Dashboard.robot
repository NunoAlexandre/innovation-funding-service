*** Settings ***
Documentation     INFUND-7365 Inflight competition dashboards: Inform dashboard
...
...               INFUND-7561 Inflight competition dashboards- View milestones
...
...               INFUND-8050 Release feedback and send notification email
Suite Setup       Guest user log-in    &{Comp_admin1_credentials}
Suite Teardown    the user closes the browser
Force Tags        CompAdmin
Resource          ../../resources/defaultResources.robot

*** Test Cases ***
Competition Dashboard
    [Documentation]    INFUND-7365
    When The user clicks the button/link    link=${INFORM_COMPETITION_NAME}
    Then The user should see the text in the page    7: Integrated delivery programme - low carbon vehicles
    And The user should see the text in the page    Inform
    And The user should see the text in the page    Programme
    And The user should see the text in the page    Materials and manufacturing
    And The user should see the text in the page    Satellite Applications
    And The user should see the element    jQuery=a:contains("Invite assessors to assess the competition")
    And the user should not see the element    link=View and update competition setup

Milestones for the In inform competition
    [Documentation]    INFUND-7561
    [Tags]
    Then the user should see the element    jQuery=.button:contains("Manage funding notifications")
    And The user should see the element    jQuery=button:contains("Release feedback")
    And the user should see the element    css=li:nth-child(13).done    #Verify that 12. Notifications
    And the user should see the element    css=li:nth-child(14).not-done    #Verify that 13. Release feedback is not done

Release feedback
    [Documentation]    INFUND-8050
    When The user clicks the button/link    jQuery=button:contains("Release feedback")
    Then The user should not see the text in the page    Inform
    When The user clicks the button/link    jQuery=a:contains(Live)
    Then The user should not see the text in the page    ${INFORM_COMPETITION_NAME}

Unsuccessful applicant sees unsuccessful alert
    [Documentation]    INFUND-7861
    [Setup]    log in as a different user    &{unsuccessful_released_credentials}
    Given the user should see the element    jQuery=.status:contains("Unsuccessful")
    When the user clicks the button/link     jQuery=a:contains("Electric Drive")
    And the user should see the element      jQuery=.warning-alert:contains("Your application has not been successful in this competition")

Successful applicant see successful alert
    [Documentation]    INFUND-7861
    [Setup]    log in as a different user    &{successful_released_credentials}
    Given the user should see the element    jQuery=.status:contains("Successful")
    When the user clicks the button/link     jQuery=.previous-applications a:contains("High Performance Gasoline Stratified")
    Then the user should see the element      jQuery=.success-alert:contains("Congratulations, your application has been successful")

View feedback from each assessor
    [Documentation]    INFUND-8172
    [Tags]
    Then the user should see the element    jQuery=h3:contains("Assessor 1") ~ p:contains("I have no problem recommending this application")
    And the user should see the element     jQuery=h3:contains("Assessor 2") ~ p:contains("Very good, but could have been better in areas")
    And the user should see the element     jQuery=h3:contains("Assessor 3") ~ p:contains("I enjoyed reading this application, well done")

Overall scores and application details are correct
    [Documentation]    INFUND-8169   INFUND-7861
    [Tags]
    Then the overall scores are correct
    And the application question scores are correct
    And the application details are correct

*** Keywords ***
the overall scores are correct
    the user should see the element    jQuery=.table-overflow td:nth-child(2):contains("6")
    the user should see the element    jQuery=.table-overflow td:nth-child(3):contains("5")
    the user should see the element    jQuery=.table-overflow td:nth-child(4):contains("6")
    the user should see the element    jQuery=.table-overflow td:nth-child(5):contains("4")
    the user should see the element    jQuery=.table-overflow td:nth-child(6):contains("4")
    the user should see the element    jQuery=.table-overflow td:nth-child(7):contains("5")
    the user should see the element    jQuery=.table-overflow td:nth-child(8):contains("7")
    the user should see the element    jQuery=.table-overflow td:nth-child(9):contains("7")
    the user should see the element    jQuery=.table-overflow td:nth-child(10):contains("3")
    the user should see the element    jQuery=.table-overflow td:nth-child(11):contains("7")

the application question scores are correct
    the user should see the element    jQuery=.column-two-thirds:contains("Business opportunity") ~ div div:contains("Average score 6 / 10")
    the user should see the element    jQuery=.column-two-thirds:contains("Potential market") ~ div div:contains("Average score 5 / 10")
    the user should see the element    jQuery=.column-two-thirds:contains("Project exploitation") ~ div div:contains("Average score 6 / 10")
    the user should see the element    jQuery=.column-two-thirds:contains("Economic benefit") ~ div div:contains("Average score 4 / 10")
    the user should see the element    jQuery=.column-two-thirds:contains("Technical approach") ~ div div:contains("Average score 4 / 10")
    the user should see the element    jQuery=.column-two-thirds:contains("Innovation") ~ div div:contains("Average score 5 / 10")
    the user should see the element    jQuery=.column-two-thirds:contains("Risks") ~ div div:contains("Average score 7 / 10")
    the user should see the element    jQuery=.column-two-thirds:contains("Project team") ~ div div:contains("Average score 7 / 10")
    the user should see the element    jQuery=.column-two-thirds:contains("Funding") ~ div div:contains("Average score 3 / 10")
    the user should see the element    jQuery=.column-two-thirds:contains("Adding value") ~ div div:contains("Average score 7 / 10")
    the user should see the element    jQuery=p:contains("Average overall: 53%")