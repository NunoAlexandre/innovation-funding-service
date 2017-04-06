*** Settings ***
Documentation     -INFUND-408: As an applicant, and I am on the application overview I do not need to see progress updates for certain questions such as appendix questions
...
...               -INFUND-39: As an applicant and I am on the application overview, I can select a section of the application, so I can see the status of each subsection in this section.
...
...               -INFUND-37: As an applicant and I am on the application overview, I can view the status of this application, so I know what actions I need to take.
...
...               -INFUND-32: As an applicant and I am on the MyApplications page, I can view the status of all my current applications, so I know what actions I need to take
...
...               -INFUND-1072: As an Applicant I want to see the Application overview page redesigned so that they meet the agreed style
...
...               -INFUND-1162: As an applicant I want the ability to have a printable version of my application for review, so I can print and download it for offline use.
Suite Setup       log in and create new application if there is not one already
Suite Teardown    TestTeardown User closes the browser
Force Tags        Applicant
Default Tags
Resource          ../../../resources/defaultResources.robot

#This Test Suite is using the Application: Robot test application

*** Test Cases ***
Navigation to the Overview page
    [Tags]    HappyPath
    When the user navigates to the overview page of the Robot test application
    Then the user should see the text in the page  Please provide information about your project.
    And the user should see the element            jQuery=h1:contains("Application overview")

Review and submit button
    [Documentation]    -INFUND-195
    ...    -INFUND-214
    [Tags]
    When the user clicks the button/link    link=Review and submit
    Then the user should see the text in the page  You will not be able to make changes to your application or resubmit after this point.
    And the user should see the element            jQuery=h1:contains("Application summary")

List with the sections
    [Tags]    HappyPath
    When the user navigates to the overview page of the Robot test application
    Then the applicant can see the overview page divided in three sections

File uploads not visible
    [Documentation]    INFUND-428
    [Tags]
    Then the user should not see the element    css=#question-14 > div > input[type="file"]
    And the user should not see the element    css=#question-17 > div > input[type="file"]
    And the user should not see the element    css=#question-18 > div > input[type="file"]

Days left to submit are visible
    [Documentation]    -INFUND-37
    [Tags]    HappyPath
    Then the user should see the element  jQuery=.deadline:contains("Days left to submit")

The Progress bar is visible
    [Documentation]    INFUND-32
    [Tags]    HappyPath
    Then the user should see the element    css=.progress-indicator

User can print the application
    [Documentation]    INFUND-1162
    [Tags]    HappyPath
    When the user navigates to the page without the usual headers    ${SERVER}/application/9/print?noprint    #This URL its only for testing purposes
    Then the user should see the element    jQuery=.button:contains("Print your application")
    And The user navigates to the overview page of the Robot test application
    And the user clicks the button/link    link= Print your application
    Then the user should get a new print window

*** Keywords ***
the applicant can see the overview page divided in three sections
    the user should see the element  jQuery=#section-184 h2:contains("Project details")
    the user should see the element  jQuery=#section-185 h2:contains("Application questions")
    the user should see the element  jQuery=#section-186 h2:contains("Finances")

the user should get a new print window
    Select Window    Title=Print Application - Innovation Funding Service
