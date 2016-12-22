*** Settings ***
Documentation     INFUND-228: As an Assessor I can see competitions that I have been invited to assess so that I can accept or reject them.
...
...               INFUND-4631: As an assessor I want to be able to reject the invitation for a competition, so that the competition team is aware that I am available for assessment
...
...               INFUND-304: As an assessor I want to be able to accept the invitation for a competition, so that the competition team is aware that I am available for assessment
...
...               INFUND-3716: As an Assessor when I have accepted to assess within a competition and the assessment period is current, I can see the number of competitions and their titles on my dashboard, so that I can plan my work. \ INFUND-3720 As an Assessor I can see deadlines for the assessment of applications currently in assessment on my dashboard, so that I am reminded to deliver my work on time
...
...               INFUND-5157 Add missing word count validation when rejecting an application for assessment
...
...               INFUND-3718 As an Assessor I can see all the upcoming competitions that I have accepted to assess so that I can make informed decisions about other invitations
...
...               INFUND-5165 As an assessor attempting to accept/reject an invalid invitation to assess in a competition, I will receive a notification that I cannot reject the competition as soon as I attempt to reject it.
...
...               INFUND-5001 As an assessor I want to see information about competitions that I have accepted to assess so that I can remind myself of the subject matter.
...
...               INFUND-5509 As an Assessor I can see details relating to work and payment, so that I can decide whether to accept it.
Suite Setup       log in as user    &{existing_assessor1_credentials}
Suite Teardown    TestTeardown User closes the browser
Force Tags        Assessor
Resource          ../../../resources/defaultResources.robot

*** Variables ***
${Invitation_existing_assessor1}    ${server}/assessment/invite/competition/dcc0d48a-ceae-40e8-be2a-6fd1708bd9b7
${Invitation_for_upcoming_comp_assessor1}    ${server}/assessment/invite/competition/1ec7d388-3639-44a9-ae62-16ad991dc92c
${Invitation_nonexisting_assessor2}    ${server}/assessment/invite/competition/396d0782-01d9-48d0-97ce-ff729eb555b0 #invitation for assessor:david.peters@innovateuk.test
${Upcoming_comp_assessor1_dashboard}    ${server}/assessment/assessor/dashboard
${Correct_date}    12 January to 29 January

*** Test Cases ***
Assessor dashboard should be empty
    [Documentation]    INFUND-3716
    ...
    ...    INFUND-4950
    [Tags]    HappyPath
    [Setup]
    Given the user should see the text in the page    Assessor dashboard
    Then The user should not see the element    css=.my-applications h2
    And The user should not see the text in the page    Competitions for assessment
    And The user should see the text in the page    Upcoming competitions to assess
    And The user should see the text in the page    ${UPCOMING_COMPETITION_TO_ASSESS_NAME}

Existing assessor: Reject invitation
    [Documentation]    INFUND-4631
    ...
    ...    INFUND-5157
    [Tags]    HappyPath
    Given the user navigates to the page    ${Invitation_existing_assessor1}
    And the user should see the text in the page    Invitation to assess '${READY_TO_OPEN_COMPETITION_NAME}'
    And the user should see the text in the page    You are invited to assess the competition '${READY_TO_OPEN_COMPETITION_NAME}'
    And the user clicks the button/link    css=form a
    And The user enters text to a text field    id=rejectComment    a a a a a a a a \ a a a a \ a a a a a a \ a a a a a \ a a a a \ a a a a \ a a a a a a a a a a a \ a a \ a a a a a a a a a a \ a a a a a a a a a a a a a a a a a a a \ a a a a a a a \ a a a \ a a \ aa \ a a a a a a a a a a a a a a \ a
    And the user clicks the button/link    jQuery=button:contains("Reject")
    Then the user should see an error    The reason cannot be blank
    And the user should see an error    Maximum word count exceeded. Please reduce your word count to 100.
    And the assessor fills all fields with valid inputs
    And the user clicks the button/link    jQuery=button:contains("Reject")
    And the user should see the text in the page    Thank you for letting us know you are unable to assess applications within this competition.

Existing assessor: Accept invitation
    [Documentation]    INFUND-228
    ...
    ...    INFUND-304
    ...
    ...    INFUND-3716
    ...
    ...    INFUND-5509
    [Tags]    HappyPath
    Given the user navigates to the page    ${Invitation_for_upcoming_comp_assessor1}
    And the user should see the text in the page    You are invited to assess the competition '${IN_ASSESSMENT_COMPETITION_NAME}'.
    And the user should see the text in the page    Invitation to assess '${IN_ASSESSMENT_COMPETITION_NAME}'
    And the user should see the text in the page    12 January 2016 to 28 January 2017: Assessment period
    # TODO And the user should see the text in the page    taking place at 15 January 2018.
    And the user should see the text in the page    100.00 per application.
    When the user clicks the button/link    jQuery=.button:contains("Yes")
    Then The user should see the text in the page    Assessor dashboard
    And the user should see the element    link=${IN_ASSESSMENT_COMPETITION_NAME}

Upcoming competition should be visible
    [Documentation]    INFUND-3718
    ...
    ...    INFUND-5001
    [Tags]    HappyPath
    Given the user navigates to the page    ${Upcoming_comp_assessor1_dashboard}
    And the user should see the text in the page    Competitions for assessment
    And the assessor should see the correct date
    When The user clicks the button/link    link=Sustainable living models for the future
    # TODO And the user should see the text in the page    You have agreed to be an assessor for the upcoming competition 'Photonics for health'
    # TODO And The user clicks the button/link    link=Back to assessor dashboard
    # TODO Then The user should see the text in the page    Upcoming competitions to assess

When the assessment period starts the comp moves to the comp for assessment
    [Tags]    MySQL    HappyPath
    [Setup]    Connect to Database    @{database}
    Given the assessment start period changes in the db in the past
    Then The user should not see the text in the page    Upcoming competitions to assess
    [Teardown]    execute sql string    UPDATE `${database_name}`.`milestone` SET `DATE`='2018-02-24 00:00:00' WHERE `competition_id`='${READY_TO_OPEN_COMPETITION}' and type IN ('OPEN_DATE', 'SUBMISSION_DATE', 'ASSESSORS_NOTIFIED');

Milestone date for assessment submission is visible
    [Documentation]    INFUND-3720
    [Tags]    MySQL
    Then the assessor should see the date for submission of assessment

Number of days remaining until assessment submission
    [Documentation]    INFUND-3720
    [Tags]    MySQL
    Then the assessor should see the number of days remaining
    And the calculation of the remaining days should be correct    2019-01-28

Calculation of the Competitions for assessment should be correct
    [Documentation]    INFUND-3716
    [Tags]    MySQL    HappyPath
    Then the total calculation in dashboard should be correct    Competitions for assessment    //div[3]/ul/li

Registered user should not allowed to accept other assessor invite
    [Documentation]    INFUND-4895
    [Tags]
    Given the user navigates to the page    ${Invitation_nonexisting_assessor2}
    When the user clicks the button/link    jQuery=.button:contains("Yes")
    Then The user should see permissions error message

The user should not be able to accept or reject the same applications
    [Documentation]    NFUND-5165
    Then the assessor shouldn't be able to accept the rejected competition
    And the assessor shouldn't be able to reject the rejected competition
    Then the assessor shouldn't be able to accept the accepted competition
    And the assessor shouldn't be able to reject the accepted competition

*** Keywords ***
the assessor fills all fields with valid inputs
    Select From List By Index    id=rejectReason    2
    The user should not see the text in the page    This field cannot be left blank
    The user enters text to a text field    id=rejectComment    Unable to assess this application.

the assessor should see the date for submission of assessment
    the user should see the element    css=.my-applications div:nth-child(2) .competition-deadline .day
    the user should see the element    css=.my-applications div:nth-child(2) .competition-deadline .month

the assessor should see the number of days remaining
    the user should see the element    css=.my-applications div:nth-child(2) .pie-container .pie-overlay .day

the assessor shouldn't be able to accept the rejected competition
    When the user navigates to the page    ${Invitation_existing_assessor1}
    the assessor is unable to see the invitation

the assessor shouldn't be able to reject the rejected competition
    When the user navigates to the page    ${Invitation_existing_assessor1}
    the assessor is unable to see the invitation

the assessor shouldn't be able to accept the accepted competition
    When the user navigates to the page    ${Invitation_for_upcoming_comp_assessor1}
    the assessor is unable to see the invitation

the assessor shouldn't be able to reject the accepted competition
    When the user navigates to the page    ${Invitation_for_upcoming_comp_assessor1}
    the assessor is unable to see the invitation

The assessor is unable to see the invitation
    The user should see the text in the page    This invitation is now closed
    The user should see the text in the page    You have already accepted or rejected this invitation.

the assessor should see the correct date
    ${Assessment_period}=    Get Text    css=.invite-to-assess .column-assessment-status.navigation-right .heading-small.no-margin
    Should Be Equal    ${Assessment_period}    ${Correct_date}
