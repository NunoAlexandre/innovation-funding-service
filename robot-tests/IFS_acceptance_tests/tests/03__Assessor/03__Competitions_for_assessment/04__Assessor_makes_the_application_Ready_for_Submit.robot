*** Settings ***
Documentation     INFUND-550 As an assessor I want the ‘Assessment summary’ page to show me complete and incomplete sections, so that I can easily judge how much of the application is left to do
...
...               INFUND-1485 As an Assessor I want to be able to provide my final feedback for the application so that I can tell Innovate UK whether or not I recommend the application for funding.
...
...               INFUND-4217 Assessor journey form validation
...
...               INFUND-3720 As an Assessor I can see deadlines for the assessment of applications currently in assessment on my dashboard, so that I am reminded to deliver my work on time
...
...               INFUND-5179 Introduce new resource DTO classes for recommending and rejecting assessments
...
...               INFUND-5765 As an assessor I need to be able to progress an assessment to the state of Assessed so that I am able to select it to submit
...
...               INFUND-5712 As an Assessor I can review the recommended for funding status of applications that I have assessed so that I can track my work
Suite Setup       guest user log-in    felix.wilson@gmail.com    Passw0rd
Suite Teardown    the user closes the browser
Force Tags        Assessor
Resource          ../../../resources/defaultResources.robot

*** Test Cases ***
Summary:All the sections are present
    [Documentation]    INFUND-4648
    [Tags]    HappyPath
    When The user clicks the button/link    link=${IN_ASSESSMENT_COMPETITION_NAME}
    and The user clicks the button/link    link=Juggling is very fun
    and The user clicks the button/link     jQuery=.button:contains("Review assessment")
    Then The user should see the element    jQuery=h2:contains("Overall scores")
    And The user should see the element    jQuery=h2:contains("Review assessment")
    And The user should see the element    jQuery=span:contains("Do you believe that this application is suitable for funding?")
    And The user should see the element    id=form-input-feedback
    And The user should see the element    id=form-input-comments

Summary:Number of days remaining until assessment submission
    [Documentation]    INFUND-3720
    [Tags]    HappyPath
    Then The user should see the text in the page    Days left to submit
    And the days remaining should be correct (Top of the page)    2017-01-28

Summary shows questions as incomplete
    [Documentation]    INFUND-550
    Then the collapsible button should contain    jQuery=button:contains(1. Business opportunity)    Incomplete
    And the collapsible button should contain    jQuery=button:contains(2. Potential market)    Incomplete
    And the collapsible button should contain    jQuery=button:contains(3. Project exploitation)    Incomplete
    And the collapsible button should contain    jQuery=button:contains(4. Economic benefit)    Incomplete
    And the collapsible button should contain    jQuery=button:contains(5. Technical approach)    Incomplete
    And the collapsible button should contain    jQuery=button:contains(6. Innovation)    Incomplete
    And the collapsible button should contain    jQuery=button:contains(7. Risks)    Incomplete
    And the collapsible button should contain    jQuery=button:contains(8. Project team)    Incomplete
    And the collapsible button should contain    jQuery=button:contains(9. Funding)    Incomplete
    And the collapsible button should contain    jQuery=button:contains(10. Adding value)    Incomplete
    And the collapsible button should contain    jQuery=button:contains(Scope)    Incomplete

Summary: Questions should show without score
    [Documentation]    INFUND-550
    Then the collapsible button should contain    jQuery=button:contains(1. Business opportunity)    N/A
    And the collapsible button should contain    jQuery=button:contains(2. Potential market)    N/A
    And the collapsible button should contain    jQuery=button:contains(3. Project exploitation)    N/A
    And the collapsible button should contain    jQuery=button:contains(4. Economic benefit)    N/A
    And the collapsible button should contain    jQuery=button:contains(5. Technical approach)    N/A
    And the collapsible button should contain    jQuery=button:contains(6. Innovation)    N/A
    And the collapsible button should contain    jQuery=button:contains(7. Risks)    N/A
    And the collapsible button should contain    jQuery=button:contains(8. Project team)    N/A
    And the collapsible button should contain    jQuery=button:contains(9. Funding)    N/A
    And the collapsible button should contain    jQuery=button:contains(10. Adding value)    N/A
    [Teardown]    The user clicks the button/link    link=Back to your assessment overview

Summary:Questions should show as complete
    [Documentation]    INFUND-550
    [Tags]    HappyPath
    Given the user adds score and feedback for every question
    When the user clicks the button/link    link=Review assessment
    Then the collapsible button should contain    jQuery=button:contains(1. Business opportunity)    Complete
    And the collapsible button should contain    jQuery=button:contains(2. Potential market)    Complete
    And the collapsible button should contain    jQuery=button:contains(3. Project exploitation)    Complete
    And the collapsible button should contain    jQuery=button:contains(4. Economic benefit)    Complete
    And the collapsible button should contain    jQuery=button:contains(5. Technical approach)    Complete
    And the collapsible button should contain    jQuery=button:contains(6. Innovation)    Complete
    And the collapsible button should contain    jQuery=button:contains(7. Risks)    Complete
    And the collapsible button should contain    jQuery=button:contains(8. Project team)    Complete
    And the collapsible button should contain    jQuery=button:contains(9. Funding)    Complete
    And the collapsible button should contain    jQuery=button:contains(10. Adding value)    Complete
    And the collapsible button should contain    jQuery=button:contains(Scope)    Complete

Summary:Questions should show the scores
    [Documentation]    INFUND-550
    [Tags]    HappyPath
    Then The user should see the text in the page    Total: 100/100
    And The user should see the text in the page    ${DEFAULT_ACADEMIC_GRANT_RATE_WITH_PERCENTAGE}
    And the table should show the correct scores
    And the collapsible button should contain    jQuery=button:contains(1. Business opportunity)    Score: 10/10
    And the collapsible button should contain    jQuery=button:contains(2. Potential market)    Score: 10/10
    And the collapsible button should contain    jQuery=button:contains(3. Project exploitation)    Score: 10/10
    And the collapsible button should contain    jQuery=button:contains(4. Economic benefit)    Score: 10/10
    And the collapsible button should contain    jQuery=button:contains(5. Technical approach)    Score: 10/10
    And the collapsible button should contain    jQuery=button:contains(6. Innovation)    Score: 10/10
    And the collapsible button should contain    jQuery=button:contains(7. Risks)    Score: 10/10
    And the collapsible button should contain    jQuery=button:contains(8. Project team)    Score: 10/10
    And the collapsible button should contain    jQuery=button:contains(9. Funding)    Score: 10/10
    And the collapsible button should contain    jQuery=button:contains(10. Adding value)    Score: 10/10

Summary:Overall scores section
    [Documentation]    INFUND-4648
    Then each question will contain links to respective questions
    And the scores under each question should be correct
    And the total scores should be correct

Summary:Feedback should show in each section
    [Documentation]    INFUND-550
    When The user clicks the button/link    jQuery=button:contains(1. Business opportunity)
    Then The user should see the text in the page    Testing Business opportunity feedback text
    When The user clicks the button/link    jQuery=button:contains(2. Potential market)
    Then The user should see the text in the page    Testing Potential market feedback text
    When The user clicks the button/link    jQuery=button:contains(3. Project exploitation)
    Then The user should see the text in the page    Testing Project exploitation feedback text
    When The user clicks the button/link    jQuery=button:contains(4. Economic benefit)
    Then The user should see the text in the page    Testing Economic benefit feedback text
    When The user clicks the button/link    jQuery=button:contains(5. Technical approach)
    Then The user should see the text in the page    Testing Technical approach feedback text
    When The user clicks the button/link    jQuery=button:contains(6. Innovation)
    Then The user should see the text in the page    Testing Innovation feedback text
    When The user clicks the button/link    jQuery=button:contains(7. Risks)
    Then The user should see the text in the page    Testing Risks feedback text
    When The user clicks the button/link    jQuery=button:contains(8. Project team)
    Then The user should see the text in the page    Testing Project team feedback text
    When The user clicks the button/link    jQuery=button:contains(9. Funding)
    Then The user should see the text in the page    Testing Funding feedback text
    When The user clicks the button/link    jQuery=button:contains(10. Adding value)
    Then The user should see the text in the page    Testing Adding value feedback text
    When The user clicks the button/link    jQuery=button:contains(Scope)
    Then The user should see the text in the page    Testing scope feedback text

Summary:Assessor should be able to re-edit before submit
    [Documentation]    INFUND-3400
    When The user clicks the button/link    jQuery=#collapsible-1 a:contains(Return to this question)
    and The user should see the text in the page    What is the business opportunity that your project addresses?
    When the user selects the option from the drop-down menu    8    id=assessor-question-score
    And the user enters text to a text field    css=.editor    This is a new feedback entry.
    And the user clicks the button/link    jQuery=a:contains(Back to your assessment overview)
    And the user clicks the button/link    jQuery=a:contains(Review assessment)
    When The user clicks the button/link    jQuery=button:contains(1. Business opportunity)
    Then the user should see the text in the page    This is a new feedback entry.
    And the user should see the text in the page    8

Summary:Feedback validations
    [Documentation]    INFUND-1485
    ...
    ...    INFUND-4217
    ...
    ...    INFUND-5228
    When the user enters text to a text field    id=feedback    Testing the feedback word count. Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum. Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco ullamcoullamco ullamco
    And the user enters text to a text field    id=comment    Testing the comments word count. Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum. Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco ullamcoullamco ullamco
    And The user clicks the button/link    jQuery=.button:contains(Save assessment)
    Then The user should see an error    Please indicate your decision
    And the word count should be correct    Words remaining: -4
    And The user enters text to a text field    id=feedback    ${EMPTY}
    And The user enters text to a text field    id=comment    ${EMPTY}
    Then the user selects the radio button    fundingConfirmation    false
    And The user clicks the button/link    jQuery=.button:contains(Save assessment)
    Then The user should see an error    Please enter your feedback
    And The user enters text to a text field    id=feedback    Testing the required feedback textarea when the decision is "No".
    And The user clicks the button/link    jQuery=.button:contains(Save assessment)
    Then The user should not see the text in the page    Please enter your feedback

Summary:Word count check(Your feedback)
    [Documentation]    INFUND-1485
    ...
    ...    INFUND-4217
    ...
    ...    INFUND-5178
    ...
    ...    INFUND-5179
    [Tags]    HappyPath
    Given The user clicks the button/link    link=Juggling is very fun
    and The user clicks the button/link     jQuery=.button:contains("Review assessment")
    When the user enters text to a text field    id=feedback    Testing the feedback word count. Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum. Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco ullamcoullamco ullamco
    Then the word count should be correct    Words remaining: -4
    And the user clicks the button/link    jQuery=.button:contains(Save assessment)
    Then the user should see an error    Maximum word count exceeded. Please reduce your word count to 100.
    And the user enters multiple strings into a text field    id=feedback    test    5001
    And the user clicks the button/link    jQuery=.button:contains(Save assessment)
    Then the user should see an error    This field cannot contain more than 5,000 characters
    And the user enters text to a text field    id=feedback    Testing the feedback word count.
    Then The user should not see the text in the page    Maximum word count exceeded. Please reduce your word count to 100.
    And the word count should be correct    Words remaining: 95

Summary:Word count check(Comments for InnovateUK)
    [Documentation]    INFUND-1485
    ...
    ...    INFUND-4217
    ...
    ...    INFUND-5178
    ...
    ...    INFUND-5179
    When the user enters text to a text field    id=comment    Testing the comments word count. Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum. Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco ullamcoullamco ullamco
    Then the word count should be correct    Words remaining: -4
    And the user clicks the button/link    jQuery=.button:contains(Save assessment)
    Then the user should see an error    Maximum word count exceeded. Please reduce your word count to 100.
    And the user enters multiple strings into a text field    id=feedback    test    5001
    And the user clicks the button/link    jQuery=.button:contains(Save assessment)
    Then the user should see an error    This field cannot contain more than 5,000 characters
    And the user enters text to a text field    id=comment    Testing the comments word count.
    Then The user should not see the text in the page    Maximum word count exceeded. Please reduce your word count to 100.
    And the word count should be correct    Words remaining: 95

User Saves the Assessment as Recomended
    [Documentation]    INFUND-4996
    ...
    ...    INFUND-5765
    [Tags]
    Given the user enters text to a text field    id=feedback    ${EMPTY}
    And the user selects the radio button    fundingConfirmation    true
    When The user clicks the button/link    jQuery=.button:contains(Save assessment)
    Then The user should not see the text in the page    Please enter your feedback
    And The user should see the text in the page    Assessed
    And the user should see the element    css=.recommend.yes
    And the user should see the element    css=.assessment-submit-checkbox

User Saves the Assessment as Not Recomended
    [Documentation]    INFUND-5712
    Given The user clicks the button/link    link=Juggling is not fun
    And the user adds score and feedback for every question
    And the user clicks the button/link     jQuery=.button:contains("Review assessment")
    When the user selects the radio button    fundingConfirmation    false
    And the user enters text to a text field    id=feedback    Negative feedback
    And The user clicks the button/link    jQuery=.button:contains(Save assessment)
    And The user should see the element    css=.recommend.no

*** Keywords ***
the collapsible button should contain
    [Arguments]    ${BUTTON}    ${TEXT}
    Element Should Contain    ${BUTTON}    ${TEXT}

the user adds score and feedback for every question
    The user clicks the button/link    link=Scope
    The user selects the option from the drop-down menu    Technical feasibility studies    id=research-category
    The user clicks the button/link    jQuery=label:contains(Yes)
    The user enters text to a text field    css=.editor    Testing scope feedback text
    Focus    jQuery=a:contains("Sign out")
    wait until page contains    Saving
    The user clicks the button/link    css=.next
    The user selects the option from the drop-down menu    10    id=assessor-question-score
    The user enters text to a text field    css=.editor    Testing Business opportunity feedback text
    Focus    jQuery=a:contains("Sign out")
    wait until page contains    Saving
    The user clicks the button/link    css=.next
    The user selects the option from the drop-down menu    10    id=assessor-question-score
    The user enters text to a text field    css=.editor    Testing Potential market feedback text
    Focus    jQuery=a:contains("Sign out")
    wait until page contains    Saving
    The user clicks the button/link    css=.next
    The user selects the option from the drop-down menu    10    id=assessor-question-score
    The user enters text to a text field    css=.editor    Testing Project exploitation feedback text
    Focus    jQuery=a:contains("Sign out")
    wait until page contains    Saving
    The user clicks the button/link    css=.next
    The user selects the option from the drop-down menu    10    id=assessor-question-score
    The user enters text to a text field    css=.editor    Testing Economic benefit feedback text
    Focus    jQuery=a:contains("Sign out")
    wait until page contains    Saving
    The user clicks the button/link    css=.next
    The user selects the option from the drop-down menu    10    id=assessor-question-score
    The user enters text to a text field    css=.editor    Testing Technical approach feedback text
    Focus    jQuery=a:contains("Sign out")
    wait until page contains    Saving
    The user clicks the button/link    css=.next
    The user selects the option from the drop-down menu    10    id=assessor-question-score
    The user enters text to a text field    css=.editor    Testing Innovation feedback text
    Focus    jQuery=a:contains("Sign out")
    wait until page contains    Saving
    The user clicks the button/link    css=.next
    The user selects the option from the drop-down menu    10    id=assessor-question-score
    The user enters text to a text field    css=.editor    Testing Risks feedback text
    Focus    jQuery=a:contains("Sign out")
    wait until page contains    Saving
    The user clicks the button/link    css=.next
    The user selects the option from the drop-down menu    10    id=assessor-question-score
    The user enters text to a text field    css=.editor    Testing Project team feedback text
    Focus    jQuery=a:contains("Sign out")
    wait until page contains    Saving
    The user clicks the button/link    css=.next
    The user selects the option from the drop-down menu    10    id=assessor-question-score
    The user enters text to a text field    css=.editor    Testing Funding feedback text
    Focus    jQuery=a:contains("Sign out")
    wait until page contains    Saving
    The user clicks the button/link    css=.next
    The user selects the option from the drop-down menu    10    id=assessor-question-score
    The user enters text to a text field    css=.editor    Testing Adding value feedback text
    Focus    jQuery=a:contains("Sign out")
    wait until page contains    Saving
    The user clicks the button/link    jquery=button:contains("Save and return to assessment overview")

the table should show the correct scores
    Element should contain    css=.table-overflow tr:nth-of-type(2) td:nth-of-type(1)    10
    Element should contain    css=.table-overflow tr:nth-of-type(2) td:nth-of-type(2)    10
    Element should contain    css=.table-overflow tr:nth-of-type(2) td:nth-of-type(3)    10
    Element should contain    css=.table-overflow tr:nth-of-type(2) td:nth-of-type(4)    10
    Element should contain    css=.table-overflow tr:nth-of-type(2) td:nth-of-type(5)    10
    Element should contain    css=.table-overflow tr:nth-of-type(2) td:nth-of-type(6)    10
    Element should contain    css=.table-overflow tr:nth-of-type(2) td:nth-of-type(7)    10
    Element should contain    css=.table-overflow tr:nth-of-type(2) td:nth-of-type(8)    10
    Element should contain    css=.table-overflow tr:nth-of-type(2) td:nth-of-type(9)    10
    Element should contain    css=.table-overflow tr:nth-of-type(2) td:nth-of-type(10)    10

each question will contain links to respective questions
    The user should see the element    link=Q1
    The user should see the element    link=Q2
    The user should see the element    link=Q3
    The user should see the element    link=Q4
    The user should see the element    link=Q5
    The user should see the element    link=Q6
    The user should see the element    link=Q7
    The user should see the element    link=Q8
    The user should see the element    link=Q9
    The user should see the element    link=Q10
    the user clicks the button/link    link=Q1
    Then The user should see the text in the page     What is the business opportunity that your project addresses?
    The user goes back to the previous page
    the user clicks the button/link    link=Q10
    Then The user should see the text in the page    How does financial support from Innovate UK and its funding partners add value?
    The user goes back to the previous page

the scores under each question should be correct
    Element should contain    css=.table-overflow tr:nth-of-type(2) td:nth-of-type(1)    10
    Element should contain    css=.table-overflow tr:nth-of-type(2) td:nth-of-type(2)    10
    Element should contain    css=.table-overflow tr:nth-of-type(2) td:nth-of-type(3)    10
    Element should contain    css=.table-overflow tr:nth-of-type(2) td:nth-of-type(4)    10
    Element should contain    css=.table-overflow tr:nth-of-type(2) td:nth-of-type(5)    10
    Element should contain    css=.table-overflow tr:nth-of-type(2) td:nth-of-type(6)    10
    Element should contain    css=.table-overflow tr:nth-of-type(2) td:nth-of-type(7)    10
    Element should contain    css=.table-overflow tr:nth-of-type(2) td:nth-of-type(8)    10
    Element should contain    css=.table-overflow tr:nth-of-type(2) td:nth-of-type(9)    10
    Element should contain    css=.table-overflow tr:nth-of-type(2) td:nth-of-type(10)    10

the word count should be correct
    [Arguments]    ${wordCount}
    the user should see the text in the page    ${wordCount}

the total scores should be correct
    Element should contain    css=div:nth-child(5) p.no-margin strong    Total: 100/100
    Element should contain    css=div:nth-child(5) p:nth-child(2) strong    ${DEFAULT_ACADEMIC_GRANT_RATE_WITH_PERCENTAGE}

The user accepts the juggling is word that sound funny application
    The user clicks the button/link    link=${IN_ASSESSMENT_COMPETITION_NAME}
    The user clicks the button/link    jQuery=a:contains("accept / reject assessment")
    The user should see the text in the page    Accept application
    The user clicks the button/link    jQuery=button:contains("Accept")
    The user should be redirected to the correct page    ${Assessor_application_dashboard}

the status of the status of the application should be correct
    [Arguments]    ${ELEMENT}    ${STATUS}
    Element should contain    ${ELEMENT}    ${STATUS}
