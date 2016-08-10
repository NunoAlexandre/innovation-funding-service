*** Settings ***
Documentation     INFUND-3780: As an Assessor I want the system to autosave my work so that I can be sure that my assessment is always in its most current state.
...
...               INFUND-3303: As an Assessor I want the ability to reject the application after I have been given access to the full details so I can make Innovate UK aware.
...
...               INFUND-4203: Prevent navigation options appearing for questions that are not part of an assessment
...
...               INFUND-1483: As an Assessor I want to be asked to confirm whether the application is in the correct research category and scope so that Innovate UK know that the application aligns with the competition
...
...               INFUND-3394 Acceptance Test: Assessor should be able to view the full application and finance summaries for assessment
Suite Setup       guest user log-in    paul.plum@gmail.com    Passw0rd
Suite Teardown    the user closes the browser
Force Tags        Assessor
Resource          ../../../resources/GLOBAL_LIBRARIES.robot
Resource          ../../../resources/variables/GLOBAL_VARIABLES.robot
Resource          ../../../resources/variables/User_credentials.robot
Resource          ../../../resources/keywords/Login_actions.robot
Resource          ../../../resources/keywords/User_actions.robot

*** Test Cases ***
Scope section If NO selected then feedback should be added
    [Documentation]    INFUND-1483
    [Tags]
    Given the user navigates to the page    ${Assessment_overview_9}
    And the user clicks the button/link    link=Scope
    When Assessor selects research category
    And the user clicks the button/link    jQuery=label:contains(No)
    And the user clicks the button/link    link=Back to assessment overview
    And the user should see the text in the page    In scope? No
    Then The user should not see the element    css=.column-third > img    #green flag

Scope section: Autosave
    [Documentation]    INFUND-1483
    Given the user navigates to the page    ${Assessment_overview_9}
    And the user clicks the button/link    link=Scope
    When Assessor selects research category
    And the user clicks the button/link    jQuery=label:contains(No)
    And The user enters text to a text field    css=#form-input-193 .editor    Testing feedback field when "No" is selected.
    And the user clicks the button/link    jQuery=a:contains(Back to assessment overview)
    Then the user should see the text in the page    In scope? No
    And the user clicks the button/link    link=Scope
    And the user should see the text in the page    Technical feasibility studies
    And the user should see the text in the page    Testing feedback field when "No" is selected.

Scope section: Word count
    [Documentation]    INFUND-1483
    When the user enters text to a text field    css=#form-input-193 .editor    Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum. Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco ullamcoullamco ullamco ullamco
    Then the user should see the text in the page    Words remaining: 0

Scope section: Status in the overview
    [Documentation]    INFUND-1483
    [Tags]
    When the user clicks the button/link    jQuery=label:contains(Yes)
    And the user clicks the button/link    jquery=button:contains("Save and return to assessment overview")
    And the user should see the text in the page    In scope? Yes
    And the user should see the element    css=.column-third > img    #green flag

Autosave and edit the Application question - How many
    [Documentation]    INFUND-3552
    [Tags]
    Given the user navigates to the page    ${Assessment_overview_9}
    When the user clicks the button/link    link=1. How many
    Then the user should see the text in the page    Please review the answer provided and score the answer out of 20 points.
    the user selects the option from the drop-down menu  9    id=assessor-question-score
    the user enters text to a text field    css=#form-input-195 .editor   This is to test the feedback entry.
    Sleep    500ms
    And the user reloads the page
    the user should see the text in the page    This is to test the feedback entry.
    the user selects the option from the drop-down menu   3    id=assessor-question-score
    the user enters text to a text field    css=#form-input-195 .editor    This is to test the feedback entry is modified.
    Sleep    500ms
    And the user reloads the page
    And the modified text should be visible

Feedback should accept up to 100 words
    [Documentation]    INFUND-3402
    [Tags]
    Given the user navigates to the page    ${Application_question_url}
    Then the word count should be calculated correctly
    When the Assessor enters more than 100 in feedback
    And the user reloads the page
    Then the word count should remain the same

Navigation link should not appear for questions that are not part of an assessment
    [Documentation]    INFUND-4264
    [Tags]
    Given the user navigates to the page    ${Assessment_overview_9}
    When the user clicks the button/link    link=Application details
    Then the user should see the element    css=#content .next .pagination-part-title
    And the user clicks the button/link    css=#content .next .pagination-part-title
    And the user should see the text in the page    Project summary
    Then the user clicks the button/link    css=#content .next .pagination-part-title
    And the user should see the text in the page    Public description
    Then the user clicks the button/link    css=#content .next .pagination-part-title
    And the user should see the text in the page    Scope
    Then the user clicks the button/link    css=#content .next .pagination-part-title
    And the user should see the text in the page    How many
    And the user should not see the element    css=#content .next .pagination-part-title

Non-scorable question cannot be scored/edited
    [Documentation]    INFUND-3400
    [Tags]
    When the user clicks the button/link    link=Back to assessment overview
    And the user clicks the button/link     link=Application details
    And the user should see the text in the page    Project title
    Then the user should not see the element    jQuery=label:contains(Question score)
    And the user should not see the text in the page    Question score
    And the user clicks the button/link    jQuery=span:contains(Next)
    And the user should see the text in the page    second answer
    Then the user should not see the element    jQuery=label:contains(Question score)
    And the user should not see the text in the page    Question score
    And the user clicks the button/link    jQuery=span:contains(Next)
    And the user should see the text in the page    third answer
    Then the user should not see the element    jQuery=label:contains(Question score)
    And the user should not see the text in the page    Question score
    And the user clicks the button/link    jQuery=span:contains(Next)
    And the user should see the text in the page    fourth answer
    Then the user should not see the element    jQuery=label:contains(Question score)
    And the user should not see the text in the page    Question score

Finance summary
    [Documentation]    INFUND-3394
    [Tags]
    Given the user navigates to the page    ${Assessment_overview_9}
    When the user clicks the button/link    link=Finances overview
    Then the user should see the text in the page    Finances summary
    And the user should not see the element    css=input
    And the finance summary total should be correct
    And the project cost breakdown total should be correct
    And the user clicks the button/link    link=Back to assessment overview
    And the user should be redirected to the correct page    ${Assessment_overview_9}
    [Teardown]

Validation check in the Reject application modal
    [Documentation]    INFUND-3540
    [Tags]
    # TODO or pending due to INFUND-4375
    Given the user navigates to the page    ${Assessment_overview_11}
    And the user clicks the button/link    jQuery=.summary:contains("Unable to assess this application")
    And the user clicks the button/link    link=Reject this application
    When the user clicks the button/link    jquery=button:contains("Reject")
    Then the user should see an error    This field cannot be left blank
    And the user should see the element    id=rejectReason
    Then the user selects the option from the drop-down menu     ${empty}    id=rejectReason         # Note that using this empty option will actually select the 'Select a reason' option at the top of the dropdown menu
    And the user should see an error    This field cannot be left blank
    Then the user enters text to a text field    id=rejectComment    Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor. Aenean massa. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Donec quam felis, ultricies nec, pellentesque eu, pretium quis, sem. Nulla consequat massa quis enim. Donec pede justo, fringilla vel, aliquet nec, vulputate eget, arcu. In enim justo, rhoncus ut, imperdiet a, venenatis vitae, justo. Nullam dictum felis eu pede mollis pretium. Integer tincidunt. Cras dapibus. Vivamus elementum semper nisi. Aenean vulputate eleifend tellus. Aenean leo ligula, porttitor eu, consequat vitae, eleifend ac, enim. Aliquam lorem ante, dapibus in, viverra quis, feugiat a, tellus. Phasellus viverra nulla ut metus varius laoreet. Quisque rutrum. Aenean imperdiet. Etiam ultricies nisi vel augue. Curabitur ullamcorper ultricies nisi. Nam eget dui. Etiam rhoncus. Maecenas tempus, tellus eget condimentum rhoncus, sem quam semper libero, sit amet adipiscing sem neque sed ipsum. Nam quam nunc, blandit vel, luctus pulvinar, hendrerit id, lorem. Maecenas nec odio et ante tincidunt tempus. Donec vitae sapien ut libero venenatis faucibus. Nullam quis ante. Etiam sit amet orci eget eros faucibus tincidunt. Duis leo. Sed fringilla mauris sit amet nibh. Donec sodales sagittis magna. Sed consequat, leo eget bibendum sodales, augue velit cursus nunc, quis gravida magna mi a libero. Fusce vulputate eleifend sapien. Vestibulum purus quam, scelerisque ut, mollis sed, nonummy id, metus. Nullam accumsan lorem in dui. Cras ultricies mi eu turpis hendrerit fringilla. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae; In ac dui quis mi consectetuer lacinia. Nam pretium turpis et arcu. Duis arcu tortor, suscipit eget, imperdiet nec, imperdiet iaculis, ipsum. Sed aliquam ultrices mauris. Integer ante arcu, accumsan a, consectetuer eget, posuere ut, mauris. Praesent adipiscing. Phasellus ullamcorper ipsum rutrum nunc. Nunc nonummy metus. Vestibulum volutpat pretium libero. Cras id dui. Aenean ut


*** Keywords ***


the modified text should be visible
    wait until element contains    css=#form-input-195 .editor    This is to test the feedback entry is modified.

the word count should be calculated correctly
    Wait Until Element Contains    css=#form-input-195 .textarea-footer > span    91

the Assessor enters more than 100 in feedback
    Input Text    css=#form-input-195 .editor    This is to test the feedback entry is modified. Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris test @.Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris test @.Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris test @.Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris test @.
    Wait Until Element Contains    css=#form-input-195 .textarea-footer > span    -30

the word count should remain the same
    Wait Until Element Contains    css=#form-input-195 .textarea-footer > span    -30

the finance summary total should be correct
    Element Should Contain    css=#content div:nth-child(5) tr:nth-child(2) td:nth-child(2)    £7,680
    Element Should Contain    css=#content div:nth-child(5) tr:nth-child(1) td:nth-child(3)    60%
    Element Should Contain    css=#content div:nth-child(5) tr:nth-child(2) td:nth-child(4)    £4,608
    Element Should Contain    css=#content div:nth-child(5) tr:nth-child(2) td:nth-child(5)    £0
    Element Should Contain    css=#content div:nth-child(5) tr:nth-child(2) td:nth-child(6)    £3,072

the project cost breakdown total should be correct
    Element Should Contain    css=.form-group.project-cost-breakdown tr:nth-child(2) td:nth-child(2)    £7,680
    Element Should Contain    css=.form-group.project-cost-breakdown tr:nth-child(2) td:nth-child(3)    £6,400
    Element Should Contain    css=.form-group.project-cost-breakdown tr:nth-child(2) td:nth-child(4)    £1,280
    Element Should Contain    css=.form-group.project-cost-breakdown tr:nth-child(2) td:nth-child(5)    £0
    Element Should Contain    css=.form-group.project-cost-breakdown tr:nth-child(2) td:nth-child(6)    £0
    Element Should Contain    css=.form-group.project-cost-breakdown tr:nth-child(2) td:nth-child(7)    £0
    Element Should Contain    css=.form-group.project-cost-breakdown tr:nth-child(2) td:nth-child(8)    £0
    Element Should Contain    css=.form-group.project-cost-breakdown tr:nth-child(2) td:nth-child(9)    £0

Assessor selects research category
    Select From List By Index    id=research-category    1
    Mouse Out    id=research-category
