*** Settings ***
Documentation     This suite depends on the Previous one!
...               INFUND-262: As a (lead) applicant, I want to see which fields in the form are being edited, so I can track progress
...
...               INFUND-265: As both lead applicant and collaborator I want to see the changes other participants have made since my last visit, so I can see progress made on the application form
...               INFUND-877: As a collaborator I want to be able to mark application questions that have been assigned to me as complete, so that my lead applicant is aware of my progress
...
...               INFUND-2219 As a collaborator I do not want to be able to submit an application so that only the lead applicant has authority to do so
...
...               INFUND-2417 As a collaborator I want to be able to review the grant Terms and Conditions so that the lead applicant can agree to them on my behalf
...
...               INFUND-3016 As a collaborator I want to mark my finances as complete so the lead can progress with submitting the application.
...
...               INFUND-3288: Assigning questions more than once leads to an internal server error
...
...               INFUND-4806 As an applicant (lead) I want to be able to remove a registered collaborator so that I can manage members no longer required to be part of the consortium
...
...               INFUND-6823 As an Applicant I want to be invited to select the primary Research area for my project
Suite Teardown    TestTeardown User closes the browser
Test Teardown
Force Tags        Applicant
Resource          ../../../resources/defaultResources.robot
Resource          ../FinanceSection_Commons.robot

*** Variables ***
# This suite uses application: Assign test

*** Test Cases ***
Lead applicant can assign a question
    [Documentation]    INFUND-275, INFUND-280
    ...
    ...    This test depends on the previous test suite to run first
    [Tags]    Email    HappyPath
    [Setup]    Guest user log-in    ${test_mailbox_one}+invite2@gmail.com  ${correct_password}
    #This test depends on the previous test suite to run first
    Given the applicant changes the name of the application
    And the user clicks the button/link    link= Public description
    When the applicant assigns the question to the collaborator    css=#form-input-12 .editor    test1233    Dennis Bergkamp
    Then the user should see the notification    Question assigned successfully
    And the user should see the element    css=#form-input-12 .readonly
    And the question should contain the correct status/name    css=#form-input-12 .assignee span+span    Dennis Bergkamp

Lead applicant can assign question multiple times
    [Documentation]    INFUND-3288
    ...
    ...    This test depends on the previous test suite to run first
    [Tags]    Email
    When the user assigns the question to the collaborator    Stuart ANDERSON
    And the question should contain the correct status/name    css=#form-input-12 .assignee span+span    you
    And the applicant assigns the question to the collaborator    css=#form-input-12 .editor    test1233    Dennis Bergkamp
    Then the user should see the element    css=#form-input-12 .readonly
    And the question should contain the correct status/name    css=#form-input-12 .assignee span+span    Dennis Bergkamp

The question is enabled for the assignee
    [Documentation]    INFUND-275
    ...
    ...    This test depends on the previous test suite to run first
    [Tags]    HappyPath    Email
    [Setup]  log in as a different user    ${test_mailbox_one}+invitedregistered@gmail.com  ${correct_password}
    Given the user navigates to the page    ${DASHBOARD_URL}
    And the user clicks the button/link    link= Assign test  #Application Title
    Then the user should see the browser notification    Stuart ANDERSON has assigned a question to you
    And the question should contain the correct status/name    jQuery=#section-1 .section:nth-child(3) .assign-container    You
    And the user clicks the button/link    link= Public description
    And the user should see the element    css=#form-input-12 .editor
    And the user should not see the element    css=#form-input-12 .readonly

Collaborator should see the terms and conditions from the overview page
    [Documentation]    INFUND-2417
    ...
    ...    This test depends on the previous test suite to run first
    [Tags]    Email
    Given the user clicks the button/link    link=Application Overview
    When The user clicks the button/link    link= view conditions of grant offer
    Then the user should see the text in the page    Terms and Conditions of an Innovate UK Grant Award
    And the user should see the text in the page    Entire Agreement

Collaborator should see the review button instead of the review and submit
    [Documentation]    INFUND-2451
    ...
    ...    This test depends on the previous test suite to run first
    [Tags]    Email    HappyPath
    Given the user navigates to the page    ${DASHBOARD_URL}
    And the user clicks the button/link    link=Assign test
    Then the user should not see the element    jQuery=.button:contains("Review and submit")
    And the user clicks the button/link    jQuery=.button:contains("Review")
    And the user should see the text in the page    All sections must be marked as complete before the application can be submitted. Only the lead applicant is able to submit the application
    And the user should not see the element    jQuery=.button:contains("Submit application")
    [Teardown]

Collaborator should be able to edit the assigned question
    [Documentation]    INFUND-2302
    ...
    ...    This test depends on the previous test suite to run first
    [Tags]    Email    HappyPath
    When the user clicks the button/link    jQuery=button:contains("Public description")
    And the user should see the element    jQuery=button:contains("Assign to lead for review")

Last update message is correctly updating
    [Documentation]    INFUND-280
    ...
    ...    This test depends on the previous test suite to run first
    [Tags]    Email    HappyPath
    Given the user navigates to the page    ${DASHBOARD_URL}
    And the user clicks the button/link    link= Assign test
    And the user clicks the button/link    link= Public description
    When the collaborator edits the 'public description' question
    Then the question should contain the correct status/name    css=#form-input-12 .textarea-footer    Last updated: Today by you

Collaborators cannot assign a question
    [Documentation]    INFUND-839
    ...
    ...    This test depends on the previous test suite to run first
    [Tags]    Email    HappyPath
    Given the user navigates to the page    ${DASHBOARD_URL}
    And the user clicks the button/link    link= Assign test
    And the user clicks the button/link    link= Public description
    Then The user should see the text in the page  Assign to lead for review

Collaborators can mark as ready for review
    [Documentation]    INFUND-877
    ...
    ...    This test depends on the previous test suite to run first
    [Tags]    HappyPath    Email
    When the user clicks the button/link    jQuery=button:contains("Assign to lead for review")
    Then the user should see the notification    Question assigned successfully
    And the user should see the text in the page    You have reassigned this question to

Collaborator cannot edit after marking ready for review
    [Documentation]    INFUND-275
    ...
    ...    This test depends on the previous test suite to run first
    [Tags]    Email    HappyPath
    Then the user should see the element    css=#form-input-12 .readonly
    [Teardown]

Collaborators should not be able to edit application details
    [Documentation]    INFUND-2298
    ...
    ...    This test depends on the previous test suite to run first
    [Tags]    Email
    Given the user navigates to the page    ${DASHBOARD_URL}
    And the user clicks the button/link    link= Assign test
    And the user clicks the button/link    link=Application details
    Then the user should see the element    css=#application_details-title[readonly]
    And the user should see the element    css=#application_details-startdate_day[readonly]
    And the user should not see the element    jQuery=button:contains("Mark as complete")

The question should be reassigned to the lead applicant
    [Documentation]    INFUND-275
    ...
    ...    This test depends on the previous test suite to run first
    [Tags]    Email    HappyPath
    [Setup]  log in as a different user     ${test_mailbox_one}+invite2@gmail.com  ${correct_password}
    Given the user navigates to the page    ${DASHBOARD_URL}
    And the user clicks the button/link    link= Assign test
    Then the user should see the browser notification    Dennis Bergkamp has assigned a question to you
    And the question should contain the correct status/name    jQuery=#section-1 .section:nth-child(3) .assign-container    You
    And the user clicks the button/link    link= Public description
    And the user should see the element    css=#form-input-12 .editor
    And the user should not see the element    css=#form-input-12 .readonly

Appendices are assigned along with the question
    [Documentation]    INFUND-409
    ...
    ...    This test depends on the previous test suite to run first
    [Tags]    Email
    Given the user navigates to the page    ${DASHBOARD_URL}
    And the user clicks the button/link    link= Assign test
    And the user clicks the button/link    link=6. Innovation
    And the user should see the text in the page    Upload
    When the applicant assigns the question to the collaborator    css=#form-input-6 .editor    test1233    Dennis Bergkamp
    Then log in as a different user          ${test_mailbox_one}+invitedregistered@gmail.com  ${correct_password}
    Given the user navigates to the page    ${DASHBOARD_URL}
    And the user clicks the button/link    link= Assign test
    And the user clicks the button/link    link=6. Innovation
    And the user should see the text in the page    Upload
    And the user clicks the button/link    jQuery=button:contains("Assign to lead for review")
    And the user should not see the text in the page    Upload

Collaborator can see that Research area is not selected
    [Documentation]  INFUND-6823
    [Tags]
    Given the user navigates to his finances page    Assign test
    Then The user should see the element     jQuery=p:contains("You must give your project a research category in application details")

Lead selects Research Area
    [Documentation]  INFUND-6823
    [Tags]  Email
    [Setup]  log in as a different user       ${test_mailbox_one}+invite2@gmail.com  ${correct_password}
    # this test is tagged as Email since it relies on an earlier invitation being accepted via email
    Given the user navigates to his finances page    Assign test
    Then the user should see the element      jQuery=p:contains("You must give your project a research category in application details")
    When the user navigates to the page       ${DASHBOARD_URL}
    Then the user clicks the button/link      link=Assign test
    When the user clicks the button/link      link=Application details
    # The following line has been commented  as 'alert message' is commented in application details section html due to upcoming functionality
    #Then the user should see the element      jQuery=h2:contains("Research category determines funding")
    Then the user should see the element       jQuery=legend:contains("Research category")
    And the user selects the radio button     application.researchCategoryId   financePosition-cat-35

Lead marks finances as complete
    [Documentation]    INFUND-3016
    ...
    ...    This test depends on the previous test suite to run first
    [Tags]
    Given the user navigates to the page  ${DASHBOARD_URL}
    And the user clicks the button/link  link=Assign test
    And the applicant completes the application details
    Given the user navigates to his finances page  Assign test
    Then the user should see the element   link=Your project costs
    And the user should see the element    link=Your organisation
    And the user should see the element    jQuery=h3:contains("Your funding")
    When the user fills in the project costs
    And the user navigates to his finances page  Assign test
    Then the user fills in the organisation information
    And the user fills in the funding information  Assign test
    When the user navigates to his finances page  Assign test
    Then the user should see all finance subsections complete

Collaborator from another organisation should be able to mark Finances as complete
    [Documentation]  INFUND-3016
    ...              This test depends on the previous test suite to run first
    [Tags]
    [Setup]  log in as a different user     ${test_mailbox_one}+invitedregistered@gmail.com  ${correct_password}
    Given the user navigates to his finances page  Assign test
    Then the user should see all finance subsections incomplete
    And the collaborator is able to edit the finances

The question is disabled for other collaborators
    [Documentation]    INFUND-275
    ...
    ...    This test case is still using the old application
    [Tags]
    [Setup]  log in as a different user    &{lead_applicant_credentials}
    Given Steve smith assigns a question to the collaborator
    Given log in as a different user       &{collaborator2_credentials}
    When the user navigates to the page    ${PUBLIC_DESCRIPTION_URL}
    Then The user should see the element    css=#form-input-12 .readonly

The question is disabled on the summary page for other collaborators
    [Documentation]    INFUND-2302
    ...
    ...    This test case is still using the old application
    [Tags]
    Given the user navigates to the page    ${SUMMARY_URL}
    When the user clicks the button/link    jQuery=button:contains("Public description")
    Then the user should see the element    css=#form-input-12 .readonly
    And the user should not see the element    jQuery=button:contains("Assign to lead for review")

Lead applicant should be able to remove the registered partner
    [Documentation]    INFUND-4806
    [Tags]
    [Setup]    log in as a different user    ${test_mailbox_one}+invite2@gmail.com  ${correct_password}
    Given the user clicks the button/link    link= Assign test
    And the user clicks the button/link    link=view team members and add collaborators
    When the user clicks the button/link    jQuery=div:nth-child(6) a:contains("Remove")
    And the user clicks the button/link    jQuery=button:contains("Remove")
    Then the user should not see the element    link=Dennis Bergkamp
    #The following steps check if the collaborator should not see the application in the dashboard page
    And log in as a different user  ${test_mailbox_one}+invitedregistered@gmail.com  ${correct_password}
    And the user should not see the element    link= Assign test

*** Keywords ***
the collaborator edits the 'public description' question
    Clear Element Text    css=#form-input-12 .editor
    Focus    css=#form-input-12 .editor
    The user enters text to a text field    css=#form-input-12 .editor    collaborator's text
    Focus    css=.app-submit-btn
    wait for autosave
    the user reloads the page

the question should contain the correct status/name
    [Arguments]    ${ELEMENT}    ${STATUS}
    Element Should Contain    ${ELEMENT}    ${STATUS}

the collaborator is able to edit the finances
    the user fills in the project costs
    the user navigates to his finances page  Assign test
    the user fills in the organisation information
    the user fills in the funding information  Assign test

the applicant changes the name of the application
    Given the user clicks the button/link     link= ${OPEN_COMPETITION_NAME}
    And the user clicks the button/link       link= Application details
    And the user enters text to a text field  id=application_details-title  Assign test
    And The user clicks the button/link       jQuery=button:contains("Save and return")

Steve smith assigns a question to the collaborator
    the user navigates to the page    ${PUBLIC_DESCRIPTION_URL}
    When the applicant assigns the question to the collaborator  css=#form-input-12 .editor  test1233  Jessica Doe