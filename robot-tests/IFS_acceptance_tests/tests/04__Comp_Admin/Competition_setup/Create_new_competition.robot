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
Suite Setup       Guest user log-in    &{Comp_admin1_credentials}
Suite Teardown    TestTeardown User closes the browser
Force Tags        Comp admin
Resource          ../../../resources/GLOBAL_LIBRARIES.robot
Resource          ../../../resources/variables/GLOBAL_VARIABLES.robot
Resource          ../../../resources/variables/User_credentials.robot
Resource          ../../../resources/keywords/Login_actions.robot
Resource          ../../../resources/keywords/User_actions.robot
Resource          ../../../resources/keywords/SUITE_SET_UP_ACTIONS.robot

*** Test Cases ***
User can navigate to the competition setup form
    [Documentation]    INFUND-2945
    ...
    ...    INFUND-2982
    ...
    ...    INFUND-2983
    [Tags]
    When the user clicks the button/link    jQuery=.button:contains("Create competition")
    Then the user redirects to the page    Competition Setup    Step 1: Initial competition details
    And the user should not see the element    css=#stateAid

Initial details server-side validations
    [Documentation]    INFUND-2982
    When the user clicks the button/link    jQuery=.button:contains("Done")
    Then the user should see an error    Please select a competition executive
    And the user should see an error    Please enter a opening month
    And the user should see an error    Please enter a opening year
    And the user should see an error    Please enter a opening day
    And the user should see an error    Please enter a title
    And the user should see an error    Please select a innovation sector
    And the user should see an error    Please select a innovation area
    And the user should see an error    Please select a competition type
    And the user should see an error    Please select a lead technologist
    And the user should see an error    Please enter a PAF number
    And the user should see an error   Please enter a budget code
    And the user clicks the button/link    jQuery=.button:contains("Generate competition code")
    # Then the user should see the text in the page    Please fill in a correct date before generating the competition code

Initial details client-side validations
    [Documentation]    INFUND-2982
    When the user selects the option from the drop-down menu    Competition Executive One    id=executiveUserId
    Then the user should not see the text in the page    Please select a competition executive
    When the user enters text to a text field    id=title    Competition title
    And the user selects the option from the drop-down menu    Health and life sciences    id=innovationSectorCategoryId
    Then the user should not see the text in the page    Please select a innovation sector
    And the user should not see the text in the page    Please enter a title
    When the user selects the option from the drop-down menu    Advanced Therapies    id=innovationAreaCategoryId
    Then the user should not see the text in the page    Please select a innovation area
    When the user selects the option from the drop-down menu    Additive Manufacturing    id=competitionTypeId
    Then the user should not see the text in the page    Please select a competition type
    When the user selects the option from the drop-down menu    Competition Technologist One    id=LeadTechnologistUserId
    Then the user should not see the text in the page    Please select a lead technologist
    When the user enters text to a text field    id=pafNumber    2016
    And the user enters text to a text field    Id=budgetCode    2004
    And the user moves focus to a different part of the page
    Then the user should not see the text in the page    Please enter a budget code
    And the user should not see the text in the page      Please enter a PAF number

The user should see the correct State aid status
    [Documentation]    INFUND-2982
    ...
    ...    INFUND-2983
    [Tags]
    Given the user selects the option from the drop-down menu    Health and life sciences    id=innovationSectorCategoryId
    When the user selects the option from the drop-down menu    Programme    id=competitionTypeId
    Then the user should see the element    css=.yes
    When the user selects the option from the drop-down menu     SBRI       id=competitionTypeId
    Then the user should see the element    css=.no

Competition code generation
    [Documentation]    INFUND-2984
    When the user enters text to a text field    id=openingDateDay    01
    And the user enters text to a text field    Id=openingDateMonth    12
    And the user enters text to a text field    id=openingDateYear    2016
    And the user clicks the button/link    jQuery=.button:contains("Generate competition code")
    Then the competition code should be correct



Additional information can be saved
    [Documentation]     INFUND-2985
    Given the user clicks the button/link    link=Additional Information
    And the user should see the text in the page      Additional competition information
    When the user enters text to a text field   id=activityCode     lorem
    When the user enters text to a text field   id=innovateBudget   ipsum
    And the user enters text to a text field    id=coFunders        dolor
    When the user enters text to a text field   id=coFundersBudget    sit
    And the user clicks the button/link      jQuery=.button:contains("Done")
    Then the user should see the element     css=.marked-as-complete
    And the user clicks the button/link      link=Initial Details
    And the user clicks the button/link      link=Additional Information
    Then the user should see the text in the page      lorem
    And the user should see the text in the page      ipsum
    And the user should see the text in the page     dolor
    And the user should see the text in the page      sit


Additional information can be edited again
    [Documentation]      INFUND-2985
    Given the user clicks the button/link      jQuery=.button:contains("Edit")
    And the user enters text to a text field      id=activityCode    amet
    And the user clicks the button/link      jQuery=.button:contains("Done")
    Then the user should see the element     css=.marked-as-complete
    And the user clicks the button/link      link=Initial Details
    And the user clicks the button/link      link=Additional Information
    Then the user should see the text in the page      amet
    And the user should see the text in the page      ipsum
    And the user should see the text in the page     dolor
    And the user should see the text in the page      sit
    And the user should not see the text in the page     lorem


Eligibility
    [Documentation]    INFUND-3048
    [Tags]    Pending
    When the user clicks the button/link            link=Eligibility
    Then the user should see the text in the page   Stream
    And the user selects the radio button               multipleStream  'comp-stream-no' ${EMPTY}
    Then the user should see an error    ${empty_field_warning_message}
    And the user should see the text in the page        Research categories
    Then the user selects the checkbox                  name=researchCategoryId ${EMPTY}
    And the user should see an error    ${empty_field_warning_message}
    And The user should see the text in the page    Single or collaborative?
    Then the user selects the radio button          singleOrCollaborative  'single' ${EMPTY}
    And the user should see an error    ${empty_field_warning_message}
    And The user should see the text in the page    Lead applicant
    Then the user selects the radio button          leadApplicantType  'business' ${EMPTY}
    And the user should see an error    ${empty_field_warning_message}
    And The user should see the text in the page    Research participation
    Then the user selects the option from the drop-down menu  id=researchParticipation ${EMPTY}
    And the user should see an error    ${empty_field_warning_message}




*** Keywords ***
the competition code should be correct
    the user should not see the element   jQuery=.button:contains("Generate competition code")
    ${input_value} =    Get Value    id=competitionCode
    Should Be Equal As Strings    ${input_value}    1612-1

the user moves focus to a different part of the page
    focus       link=Sign out