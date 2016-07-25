*** Settings ***
Documentation     INFUND-2945 As a Competition Executive I want to be able to create a new competition from the Competitions Dashboard so Innovate UK can create a new competition
...
...               INFUND-2982: Create a Competition: Step 1: Initial details
...
...               INFUND-2983: As a Competition Executive I want to be informed if the competition will fall under State Aid when I select a 'Competition type' in competition setup
...
...               INFUND-2984: As a Competition Executive I want the competition code field in the 'Initial details' tab in competition setup to generate based on open date and number of competitions in that month
...
...               INFUND-2986 Create a Competition: Step 3: Eligibility
...
...               INFUND-3182 As a Competition Executive I want to the ability to save progress on each tab in competition setup.
...
...               IFUND-3888 Rearrangement of Competitions setup
Suite Setup       Guest user log-in    &{Comp_admin1_credentials}
Suite Teardown    TestTeardown User closes the browser
Force Tags        CompAdmin
Resource          ../../../resources/GLOBAL_LIBRARIES.robot
Resource          ../../../resources/variables/GLOBAL_VARIABLES.robot
Resource          ../../../resources/variables/User_credentials.robot
Resource          ../../../resources/keywords/Login_actions.robot
Resource          ../../../resources/keywords/User_actions.robot
Resource          ../../../resources/keywords/SUITE_SET_UP_ACTIONS.robot

*** Test Cases ***
User can navigate to the competition setup form
    [Documentation]    INFUND-2945 As a Competition Executive I want to be able to create a new competition from the
    ...    Competitions Dashboard so Innovate UK can create a new competition.
    ...
    ...    INFUND-2982 Create a Competition: Step 1: Initial details
    ...
    ...    INFUND-2983 As a Competition Executive I want to be informed if the competition will fall under
    ...    State Aid when I select a 'Competition type' in competition setup.
    ...
    ...    INFUND-2986 Create a Competition: Step 3: Eligibility
    ...    IFUND-3888
    [Tags]    HappyPath
    When the user clicks the button/link    jQuery=.button:contains("Create competition")
    Then the user navigates to the page    ${COMP_MANAGEMENT_COMP_SETUP}
    When the user clicks the button/link    link=Initial Details
    Then the user redirects to the page    Initial details    This will create a new Competition
    And the user should not see the element    css=#stateAid

Server-side validations
    [Documentation]    INFUND-2982, IFUND-3888
    [Tags]
    # TODO update when story INFUND-3002 is completed
    When the user clicks the button/link    jQuery=.button:contains("Done")
    Then the user should see an error    Please enter a title
    And the user should see an error    Please select a competition type
    And the user should see an error    Please select an innovation sector
    And the user should see an error    Please select an innovation area
    And the user should see an error    Please enter an opening year
    And the user should see an error    Please enter an opening day
    And the user should see an error    Please enter an opening month
    And the user should see an error    Please select a lead technologist
    And the user should see an error    Please select a competition executive
    When the user navigates to the page    ${COMP_MANAGEMENT_COMP_SETUP}
    And the user clicks the button/link    link=Funding Information
    Then the user redirects to the page    Funding information    Reporting fields
    When the user clicks the button/link    jQuery=.button:contains("Done")
    ## Fill in for Funder, Budget
    Then the user should see an error    Please enter a PAF number
    And the user should see an error    Please enter a budget code
    ## Fill in for Activity code
    And the user should see an error    Please generate a competition code
    When the user clicks the button/link    jQuery=.button:contains("Generate code")
    Then the user should see an error    Please set a start date for your competition before generating the competition code, you can do this in the Initial Details section

Initial details correct state aid status
    [Documentation]    INFUND-2982, INFUND-2983, INFUND-3888
    [Tags]    HappyPath
    Given the user navigates to the page    ${COMP_MANAGEMENT_COMP_SETUP}/section/initial
    And the user selects the option from the drop-down menu    Health and life sciences    id=innovationSectorCategoryId
    When the user selects the option from the drop-down menu    Programme    id=competitionTypeId
    Then the user should see the element    css=.yes
    When the user selects the option from the drop-down menu    Special    id=competitionTypeId
    Then the user should see the element    css=.no
    When the user selects the option from the drop-down menu    Additive Manufacturing    id=competitionTypeId
    Then the user should see the element    css=.yes
    When the user selects the option from the drop-down menu    SBRI    id=competitionTypeId
    Then the user should see the element    css=.no

Initial details client-side validations
    [Documentation]    INFUND-2982, INFUND-3888
    [Tags]    HappyPath
    When the user enters text to a text field    id=title    Competition title
    Then the user should not see the error any more    Please enter a title
    When the user selects the option from the drop-down menu    Additive Manufacturing    id=competitionTypeId
    Then the user should not see the error any more    Please select a competition type
    When the user selects the option from the drop-down menu    Health and life sciences    id=innovationSectorCategoryId
    Then the user should not see the error any more    Please select an innovation sector
    When the user selects the option from the drop-down menu    Advanced Therapies    id=innovationAreaCategoryId
    Then the user should not see the error any more    Please select an innovation area
    When the user enters text to a text field    id=openingDateDay    01
    Then the user should not see the error any more    Please enter an opening day
    When the user enters text to a text field    Id=openingDateMonth    12
    Then the user should not see the error any more    Please enter an opening month
    When the user enters text to a text field    id=openingDateYear    2017
    Then the user should not see the error any more    Please enter an opening year
    When the user selects the option from the drop-down menu    Competition Technologist One    id=leadTechnologistUserId
    Then the user should not see the error any more    Please select a lead technologist
    When the user selects the option from the drop-down menu    Competition Executive Two    id=executiveUserId
    Then the user should not see the error any more    Please select a competition executive
    ##    State aid value is tested in 'Initial details correct state aid status'
    When the user clicks the button/link    jQuery=.button:contains("Done")
    Then the user should see the text in the page    Competition Executive Two
    And the user should see the text in the page    1/12/2017
    And the user should see the text in the page    Competition Technologist One
    And the user should see the text in the page    Competition title
    And the user should see the text in the page    Health and life sciences
    And the user should see the text in the page    Advanced Therapies
    And the user should see the text in the page    Additive Manufacturing
    And the user should see the text in the page    Yes
    And the user should see the element    jQuery=.button:contains("Edit")
    When the user clicks the button/link    jQuery=.button:contains("Return to setup overview")
    Then the user navigates to the page    ${COMP_MANAGEMENT_COMP_SETUP}

Funding Information can be saved
    [Documentation]    INFUND-2985, INFUND-3182, IFUND-3888
    [Tags]
    Given the user navigates to the page    ${COMP_MANAGEMENT_COMP_SETUP}
    And the user clicks the button/link    link=Funding Information
    And the user should see the text in the page    Funding information
    When the user enters text to a text field    id=funder    FunderName
    And the user enters text to a text field    id=funderBudget    20000
    Then the total should be correct    £ 20,000
    When the user clicks the button/link    jQuery=Button:contains("+Add co-funder")
    Then the user should see the element    jQuery=Button:contains("+Add co-funder")
    And the user should see the element    css=#co-funder-row-0
    And the user enters text to a text field    id=0-funder    FunderName2
    And the user enters text to a text field    id=0-funderBudget    1000
    Then the total should be correct    £ 21,000
    When the user enters text to a text field    id=pafNumber    2016
    And the user enters text to a text field    id=budgetCode    2004
    And the user enters text to a text field    id=activityCode    4242
    When the user clicks the button/link    jQuery=.button:contains("Generate code")
    Then the user should not see the error any more    Please generate a competition code
    ## Validation for the Competition code connection with the Start date is tested in 'Server-side validations'
    And the user clicks the button/link    jQuery=.button:contains("Done")
    And the user should see the text in the page    FunderName
    And the user should see the text in the page    FunderName2
    And the user should see the text in the page    £21,000.00
    And the user should see the text in the page    2016
    And the user should see the text in the page    2004
    And the user should see the text in the page    4242
    And the user should see the text in the page    1712-1

Additional information can be edited again
    [Documentation]    INFUND-2985, INFUND-3182
    [Tags]    Pending
    # TODO Additional info, doesnt exist anymore. Update after story INFUND-3002
    Given the user clicks the button/link    jQuery=.button:contains("Edit")
    And the user enters text to a text field    id=activityCode    amet
    And the user clicks the button/link    jQuery=.button:contains("Done")
    And the user clicks the button/link    link=Initial Details
    And the user clicks the button/link    link=Additional Information
    Then the user should see the text in the page    amet
    And the user should see the text in the page    ipsum
    And the user should see the text in the page    dolor
    And the user should see the text in the page    sit
    And the user should not see the text in the page    lorem
    [Teardown]    the user clicks the button/link    jQuery=.button:contains("Edit")

Eligibility server-side validations
    [Documentation]    INFUND-2986
    [Tags]
    [Setup]    the user navigates to the page    ${COMP_MANAGEMENT_COMP_SETUP}
    Given the user clicks the button/link    link=Eligibility
    And the user should see the text in the page    Stream
    Given the user selects the radio button    multipleStream    yes
    When the user clicks the button/link    jQuery=.button:contains("Done")
    Then the user should see the text in the page    Please select at least one research category
    And the user should see the text in the page    Please select a collaboration level
    And the user should see the text in the page    Please select a lead applicant type
    And the user should see the text in the page    A stream name is required

Eligibility client-side validations
    [Documentation]    INFUND-2986, INFUND-2988, INFUND-3888
    [Tags]
    Given the user selects the radio button    multipleStream    yes
    When the user selects the checkbox    id=research-categories-33
    And the user selects the checkbox    id=research-categories-34
    And the user selects the checkbox    id=research-categories-35
    And the user moves focus to a different part of the page
    When the user selects the radio button    singleOrCollaborative    single
    And the user selects the radio button    leadApplicantType    business
    And the user selects the option from the drop-down menu    30%    name=researchParticipationAmountId
    And the user moves focus to a different part of the page
    Then the user should not see the text in the page    Please select a collaboration level
    And the user should not see the text in the page    Please select a lead applicant type
    And the user should not see the text in the page    Please select at least one research category
    And the user enters text to a text field    id=streamName    Test stream name
    And the user moves focus to a different part of the page
    And the user should not see the text in the page    A stream name is required

Eligibility information can be marked as done then edit again
    [Documentation]    INFUND-3051
    ...
    ...    INFUND-3872
    [Tags]
    When the user clicks the button/link    jQuery=.button:contains("Done")
    Then the user should see the text in the page    Multiple Stream
    And The user should not see the element    id=streamName
    When the user clicks the button/link    jQuery=.button:contains("Edit")
    And the user clicks the button/link    jQuery=.button:contains("Done")
    [Teardown]    The user clicks the button/link    jQuery=.button:contains("Edit")

Eligibility page should contain the correct options
    [Documentation]    INFUND-2989, INFUND-2990
    When the user should see the element    xpath=//input[@type='radio' and @name='multipleStream' and @value='yes']
    When the user should see the element    xpath=//input[@type='radio' and @name='multipleStream' and @value='no']
    When the user should see the element    xpath=//input[@type='radio' and @name='singleOrCollaborative' and @value='single']
    When the user should see the element    xpath=//input[@type='radio' and @name='singleOrCollaborative' and @value='collaborative']
    When the user should see the element    xpath=//input[@type='radio' and @name='singleOrCollaborative' and @value='single-or-collaborative']
    When the user should see the element    xpath=//input[@type='radio' and @name='leadApplicantType' and @value='business']
    When the user should see the element    xpath=//input[@type='radio' and @name='leadApplicantType' and @value='research']
    When the user should see the element    xpath=//input[@type='radio' and @name='leadApplicantType' and @value='either']

*** Keywords ***
the user moves focus to a different part of the page
    focus    link=Sign out

the user should not see the error any more
    [Arguments]    ${ERROR_TEXT}
    run keyword and ignore error    mouse out    css=input
    sleep    300ms
    Wait Until Page Does Not Contain Element    css=.error-message
    Wait Until Page Does Not Contain    ${ERROR_TEXT}

the total should be correct
    [Arguments]    ${Total}
    mouse out     css=input
    Focus    jQuery=Button:contains("Done")
    Wait Until Element Contains    css=.no-margin    ${Total}
