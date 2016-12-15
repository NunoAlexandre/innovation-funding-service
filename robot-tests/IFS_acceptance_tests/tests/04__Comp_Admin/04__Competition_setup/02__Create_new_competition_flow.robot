*** Settings ***
Documentation     INFUND-2945 As a Competition Executive I want to be able to create a new competition from the Competitions Dashboard so Innovate UK can create a new competition
...
...               INFUND-2982: Create a Competition: Step 1: Initial details
...
...               INFUND-2983: As a Competition Executive I want to be informed if the competition will fall under State Aid when I select a 'Competition type' in competition setup
...
...               INFUND-2986 Create a Competition: Step 3: Eligibility
...
...               INFUND-3182 As a Competition Executive I want to the ability to save progress on each tab in competition setup.
...
...               IFUND-3888 Rearrangement of Competitions setup
...
...               INFUND-3000 As a competitions team member I want to be able to configure application form questions during Competition Setup so that correct details are provided for each competition
...
...               INFUND-3002 As a Competition Executive and I have added all information in all obligatory fields I want to mark the competition ready for open
...
...               INFUND-2980 As a Competition Executive I want to see a newly created competition listed in the Competition Dashboard so that I can view and update further details
...
...               INFUND-2993 As a competitions team member I want to be able to add milestones when creating my competition so these can be used manage its progress
...
...               INFUND-4468 As a Competitions team member I want to include additional criteria in Competitions Setup so that the "Ready to Open" state cannot be set until these conditions are met
...
...               INFUND-4725 As a Competitions team member I want to be guided to complete all mandatory information in the Initial details section so that I can access the correct details in the other sections in Competition Setup.
...
...               INFUND-4892 As a Competitions team member I want to be prevented from making amendments to some Competition Setup details so that I do not affect affect other setup details that have been saved so far for this competition
...
...               INFUND-4894 As a competition executive I want have a remove button in order to remove the new added co-funder rows in the funding information section
...
...               INFUND-5639 As a Competitions team member I want to be able to view the Application process within the application question section in Competition Setup so that I can set up my competition using more convenient navigation
...
...               INFUND-5640 As a Competitions team member I want to be able to edit the Finances questions in Competition Setup so that I can include the appropriate sections required for the competition
...
...               INFUND-5632 As a Competitions team member I want to be able to view application questions separately in Competition Setup so that I can more easily manage all sections required for each question in one place
...
...               INFUND-5634 As a Competitions team member I want to be able to view setup questions in the Scope section of Competition Setup so that I can review the questions and guidance to be shown to the applicants
...
...               INFUND-5636 As a Competitions team member I want to be able to view setup questions in the Project Summary section of Competition Setup so that I can review the questions and guidance to be shown to the applicants
...
...
...               INFUND-5637 As a Competitions team member I want to be able to edit setup questions in the Project Summary section of Competition Setup so that I can amend the defaults if required for the competition
...
...               INFUND-5635 As a Competitions team member I want to be able to edit questions in the Scope section of Competition Setup so that I can amend the defaults if required for the competition
...
...               INFUND-5641 As a Competitions team member I want to be able to update the assessor setup questions so that I can amend the defaults if required for the competition
...
...               INFUND-5633 As a Competitions team member I want to be able to set up questions in the Application Details section of Competition Setup so that I can amend the defaults if necessary for the competitions
Suite Setup       Guest user log-in    &{Comp_admin1_credentials}
Suite Teardown    TestTeardown User closes the browser
Force Tags        CompAdmin
Resource          ../../../resources/defaultResources.robot

*** Test Cases ***
User can create a new competition
    [Documentation]    INFUND-2945
    ...
    ...    INFUND-2982
    ...
    ...    INFUND-2983
    ...
    ...    INFUND-2986
    ...
    ...    IFUND-3888
    ...
    ...    INFUND-3002
    ...
    ...    INFUND-2980
    ...
    ...    INFUND-4725
    [Tags]    HappyPath
    Given the user clicks the button/link    id=section-3
    When the user clicks the button/link    jQuery=.button:contains("Create competition")
    And The user should not see the element    jQuery('.button:contains("Save as Ready To Open")
    And The user should not see the element    link=Funding information
    And The user should not see the element    link=Eligibility
    And The user should not see the element    link=Milestones
    And The user should not see the element    link=Application Questions
    And The user should not see the element    link=Application Finances
    And The user should not see the element    link=Assessors
    And The user should not see the element    link=Description and brief

New competition shows in Preparation section
    [Documentation]    INFUND-2980
    Given The user clicks the button/link    link=All competitions
    And The user clicks the button/link    id=section-3
    Then the competition should show in the correct section    css=section:nth-of-type(1) li:nth-child(2)    No competition title defined    #this keyword checks if the new application shows in the second line of the "In preparation" competitions

Initial details: correct state aid status
    [Documentation]    INFUND-2982
    ...
    ...    INFUND-2983
    ...
    ...    INFUND-3888
    ...
    ...    INFUND-4979
    [Tags]    Pending
    [Setup]
    #TODO This ticket marked as pending because atm there is no SBRI competition type.
    #Change the test setup
    Given the user should not see the element    css=#stateAid
    When the user selects the option from the drop-down menu    SBRI    id=competitionTypeId
    Then the user should see the element    css=.no
    When the user selects the option from the drop-down menu    Special    id=competitionTypeId
    Then the user should see the element    css=.no
    When the user selects the option from the drop-down menu    Additive Manufacturing    id=competitionTypeId
    Then the user should see the element    css=.yes
    When the user selects the option from the drop-down menu    Sector    id=competitionTypeId
    Then the user should see the element    css=.yes

Initial details: User enters valid values and marks as done
    [Documentation]    INFUND-2982
    ...
    ...    INFUND-3888
    ...
    ...    INFUND-2983
    [Tags]    HappyPath
    [Setup]    the user navigates to the page    ${COMP_MANAGEMENT_COMP_SETUP}
    Given The user clicks the button/link    link=Initial details
    And The user enters valid data in the initial details
    And the user moves focus and waits for autosave
    When the user clicks the button/link    jQuery=.button:contains("Done")
    Then the user should see the text in the page    Toby Reader
    And the user should see the text in the page    1/12/2017
    And the user should see the text in the page    Ian Cooper
    And the user should see the text in the page    Competition title
    And the user should see the text in the page    Health and life sciences
    And the user should see the text in the page    Advanced Therapies
    And the user should see the text in the page    Programme
    And the user should see the text in the page    NO
    And the user should see the element    jQuery=.button:contains("Edit")

Initial details: Comp Type and Date should not be editable
    [Documentation]    INFUND-2985
    ...
    ...    INFUND-3182
    ...
    ...    INFUND-4892
    [Tags]    HappyPath
    When the user clicks the button/link    jQuery=.button:contains("Edit")
    And the user enters text to a text field    id=title    Test competition
    And The element should be disabled    id=competitionTypeId
    And The element should be disabled    id=openingDateDay
    And the user clicks the button/link    jQuery=.button:contains("Done")
    Then the user should see the text in the page    1/12/2017
    And the user should see the text in the page    Ian Cooper
    And the user should see the text in the page    Test competition
    And the user should see the text in the page    Health and life sciences
    And the user should see the text in the page    Advanced Therapies
    And the user should see the text in the page    Programme
    And the user should see the text in the page    NO

Initial details: should have a green check
    [Documentation]    INFUND-3002
    [Tags]    HappyPath
    When The user clicks the button/link    link=Competition setup
    Then the user should see the element    jQuery=img.section-status:eq(0)
    And the user should not see the element    jQuery=.button:contains("Save as Ready To Open")

User should have access to all the sections
    [Documentation]    INFUND-4725
    Given the user navigates to the page    ${COMP_MANAGEMENT_COMP_SETUP}
    Then The user should see the element    link=Funding information
    And The user should see the element    link=Eligibility
    And The user should see the element    link=Milestones
    And The user should see the element    link=Application
    And The user should see the element    link=Assessors

New application shows in Preparation section with the new name
    [Documentation]    INFUND-2980
    Given the user navigates to the page    ${COMP_MANAGEMENT_COMP_SETUP}
    And The user clicks the button/link    link=All competitions
    And The user clicks the button/link    id=section-3
    Then the competition should show in the correct section    css=section:nth-of-type(1) > ul    Test competition    #This keyword checks if the new competition shows in the "In preparation" test

Funding information: calculations
    [Documentation]    INFUND-2985
    ...
    ...    INFUND-4894
    [Tags]    HappyPath
    [Setup]    the user navigates to the page    ${COMP_MANAGEMENT_COMP_SETUP}
    Given the user clicks the button/link    link=Funding information
    And the user clicks the button/link    jQuery=.button:contains("Generate code")
    And the user enters text to a text field    id=funders0.funder    FunderName
    And the user enters text to a text field    id=0-funderBudget    20000
    And the user enters text to a text field    id=pafNumber    2016
    And the user enters text to a text field    id=budgetCode    2004
    And the user enters text to a text field    id=activityCode    4242
    When the user clicks the button/link    jQuery=Button:contains("+Add co-funder")
    and the user should see the element    jQuery=Button:contains("+Add co-funder")
    And the user should see the element    jQuery=Button:contains("Remove")
    And the user enters text to a text field    id=1-funder    FunderName2
    And the user enters text to a text field    id=1-funderBudget    1000
    Then the total should be correct    £ 21,000
    When the user clicks the button/link    jQuery=Button:contains("Remove")
    Then the total should be correct    £ 20,000

Funding information: can be saved
    [Documentation]    INFUND-3182
    [Tags]    HappyPath
    Given the user moves focus and waits for autosave
    When the user clicks the button/link    jQuery=.button:contains("Done")
    Then the user should see the text in the page    FunderName
    And the user should see the text in the page    £20,000
    And the user should see the text in the page    2016
    And the user should see the text in the page    2004
    And the user should see the text in the page    4242
    And the user should see the text in the page    1712-1
    And the user should see the element    jQuery=.button:contains("Edit")

Funding information: can be edited
    [Documentation]    INFUND-3002
    [Tags]
    When the user clicks the button/link    jQuery=.button:contains("Edit")
    And the user enters text to a text field    id=funders0.funder    testFunder
    And the user moves focus and waits for autosave
    When the user clicks the button/link    jQuery=.button:contains("Done")
    Then the user should see the text in the page    testFunder

Funding information: should have a green check
    [Documentation]    INFUND-3002
    [Tags]    HappyPath
    When The user clicks the button/link    link=Competition setup
    Then the user should see the element    jQuery=img.section-status:eq(1)
    And the user should not see the element    jQuery=.button:contains("Save as Ready To Open")

Eligibility: Contain the correct options
    [Documentation]    INFUND-2989
    ...
    ...    INFUND-2990
    [Tags]    HappyPath
    [Setup]    the user navigates to the page    ${COMP_MANAGEMENT_COMP_SETUP}
    Given the user clicks the button/link    link=Eligibility
    And the user should see the text in the page    Please choose the project type.
    Then the user should see the element    jQuery=label:contains(Single or Collaborative)
    When the user should see the element    jQuery=label:contains(Collaborative)
    And the user should see the element    jQuery=label:contains(Business)
    And the user should see the element    jQuery=label:contains(Research)
    And the user should see the element    jQuery=label:contains(Either)
    And the user should see the element    jQuery=div:nth-child(7) label:contains("Yes")
    And the user should see the element    jQuery=div:nth-child(7) label:contains("No")
    And the user should see the element    jQuery=label:contains(Technical feasibility)
    And the user should see the element    jQuery=label:contains(Industrial research)
    And the user should see the element    jQuery=label:contains(Experimental development)
    And the resubmission should not have a default selection

Eligibility: Mark as Done then Edit again
    [Documentation]    INFUND-3051
    ...
    ...    INFUND-3872
    ...
    ...    INFUND-3002
    [Tags]    HappyPath
    Given the user selects the checkbox    id=research-categories-33
    And the user selects the checkbox    id=research-categories-34
    And the user selects the checkbox    id=research-categories-35
    And the user selects the radio button    singleOrCollaborative    single
    And the user selects the radio button    leadApplicantType    business
    And the user selects the option from the drop-down menu    50%    name=researchParticipationAmountId
    And the user moves focus and waits for autosave
    And the user selects the radio button    resubmission    no
    When the user clicks the button/link    jQuery=.button:contains("Done")
    Then the user should see the text in the page    Yes
    And the user should see the text in the page    Single
    And the user should see the text in the page    Business
    And the user should see the text in the page    50%
    And the user should see the text in the page    Technical feasibility, Industrial research, Experimental development
    And The user should not see the element    id=streamName
    When the user clicks the button/link    link=Competition setup
    When the user clicks the button/link    link=Eligibility
    And the user clicks the button/link    jQuery=.button:contains("Edit")
    And the user clicks the button/link    jQuery=.button:contains("Done")

Eligibility: Should have a Green Check
    [Documentation]    INFUND-3002
    [Tags]    HappyPath
    When The user clicks the button/link    link=Competition setup
    Then the user should see the element    jQuery=img.section-status:eq(2)
    And the user should not see the element    jQuery=.button:contains("Save as Ready To Open")

Milestones: Page should contain the correct fields
    [Documentation]    INFUND-2993
    [Tags]    HappyPath
    [Setup]    the user navigates to the page    ${COMP_MANAGEMENT_COMP_SETUP}
    When the user clicks the button/link    link=Milestones
    Then The user should see the text in the page    1. Open date
    And the user should see the text in the page    2. Briefing event
    And the user should see the text in the page    3. Submission date
    And the user should see the text in the page    4. Allocate accessors
    And the user should see the text in the page    5. Assessor briefing
    And the user should see the text in the page    6. Assessor accepts
    And the user should see the text in the page    7. Assessor deadline
    And the user should see the text in the page    8. Line draw
    And the user should see the text in the page    9. Assessment panel
    And the user should see the text in the page    10. Panel date
    And the user should see the text in the page    11. Funders panel
    And the user should see the text in the page    12. Notifications
    And the user should see the text in the page    13. Release feedback
    And the pre-field date should be correct

Milestones: Correct Weekdays should show
    [Documentation]    INFUND-2993
    [Tags]    HappyPath
    Given the user fills the milestones with valid data
    When the user clicks the button/link    jQuery=button:contains(Done)
    Then the weekdays should be correct

Milestones: Green check should show
    [Documentation]    INFUND-2993
    [Tags]
    When The user clicks the button/link    link=Competition setup
    Then the user should see the element    css=li:nth-child(4) .section-status
    And the user should not see the element    jQuery=.button:contains("Save as Ready To Open")

Application: Application process Page
    [Documentation]    INFUND-3000 INFUND-5639
    [Tags]    HappyPath
    [Setup]    go to    ${COMP_MANAGEMENT_COMP_SETUP}
    When The user clicks the button/link    link=Application
    Then The user should see the text in the page    Programme competition questions
    And the user should see the element    link=Business opportunity
    And the user should see the element    link=Potential market
    And the user should see the element    link=Project exploitation
    And the user should see the element    link=Economic benefit
    And the user should see the element    link=Technical approach
    And the user should see the element    link=Risks
    And the user should see the element    link=Project team
    And the user should see the element    link=Funding
    And the user should see the element    link=Adding value
    And the user should see the element    link=Application details
    And the user should see the element    link=Project summary
    And the user should see the element    link=Public description
    And the user should see the element    link=Scope
    And the user should see the element    link=Finances

Application: Business opportunity
    [Documentation]    INFUND-5632 IN-5685 IN-5630
    When the user clicks the button/link    link=Business opportunity
    Then the user should see the element    jQuery=h1:contains("Business opportunity")
    And the user should see the text in the page    You can edit this question for the applicant as well as the guidance for assessors.
    When the user clicks the button/link    jQuery=a:contains("Edit this question")
    And the user edits the assessed question information
    And the user clicks the button/link    id=remove-guidance-row-2
    And The user clicks the button/link    jQuery=.button[value="Save and close"]
    And the user clicks the button/link    link=Business opportunity
    And the user sees the correct assessed question information
    And the user should not see the text in the page    The business opportunity is plausible
    [Teardown]    The user clicks the button/link    link=Application

Application: Application details
    [Documentation]    INFUND-5633
    Given the user clicks the button/link    link=Application details
    And the user should see the element    jQuery=h1:contains("Application details")
    And the user should see the text in the page    These are the default questions included in the application details section.
    When the user clicks the button/link    jQuery=a:contains("Edit this question")
    And the user selects the radio button    useResubmissionQuestion    false
    And The user clicks the button/link    jQuery=button:contains("Save and close")
    And the user clicks the button/link    link=Application details
    Then The user should see the text in the page    Application details
    And the user should see the text in the page    No
    [Teardown]    The user clicks the button/link    link=Application

Application: Scope
    [Documentation]    INFUND-5634
    ...
    ...    INFUND-5635
    Given the user clicks the button/link    link=Scope
    And the user should see the element    jQuery=h1:contains("Scope")
    And the user should see the text in the page    You can edit this question for the applicant as well as the guidance for assessors.
    When the user clicks the button/link    jQuery=a:contains("Edit this question")
    And The user fills the empty question fields
    And The user clicks the button/link    jQuery=.button[value="Save and close"]
    And the user clicks the button/link    link=Scope
    Then The user should see the text in the page    Scope
    And the user checks the question fields


Application: Scope Assessment questions
    [Documentation]    INFUND-5631    INFUND-6044
    Given the user clicks the button/link    jQuery=a:contains("Edit this question")
    And the user fills the scope assessment questions
    When the user clicks the button/link    jQuery=.button[value="Save and close"]
    And the user clicks the button/link    link=Scope
    Then the user checks the scope assessment questions
    And the user clicks the button/link    jQuery=a:contains("Edit this question")
    And the user selects the radio button    question.writtenFeedback    0
    When the user clicks the button/link    jQuery=.button[value="Save and close"]
    And the user clicks the button/link    link=Scope
    Then the user should not see the text in the page    Guidance for assessing scope section
    [Teardown]    The user clicks the button/link    link=Application

Application: Project Summary
    [Documentation]    INFUND-5636
    ...
    ...    INFUND-5637
    Given the user clicks the button/link    link=Project summary
    And the user should see the element    jQuery=h1:contains("Project summary")
    And the user should see the text in the page    You can edit this question for the applicant as well as the guidance for assessors.
    When the user clicks the button/link    jQuery=a:contains("Edit this question")
    And The user fills the empty question fields
    And The user clicks the button/link    jQuery=.button[value="Save and close"]
    And the user clicks the button/link    link=Project summary
    Then The user should see the text in the page    Project summary
    And the user checks the question fields
    [Teardown]    The user clicks the button/link    link=Application

Application: Finances Form
    [Documentation]    INFUND-5640 INFUND-6039
    Given the user clicks the button/link    link=Finances
    And the user should see the element    jQuery=h1:contains("Application finances")
    And the user should see the text in the page    Each partner is required to complete the following finance sections
    When The user clicks the button/link    jQuery=a:contains("Edit this question")
    And The user clicks the button/link    jQuery=label:contains("Light finances")
    And The user clicks the button/link    jQuery=label:contains("No")
    And The user clicks the button/link    jQuery=button:contains("Save and close")
    And the user clicks the button/link    link=Finances
    Then the finance information should be correct
    [Teardown]    The user clicks the button/link    link=Application

Application: Mark as done and the Edit again
    [Documentation]    INFUND-3000
    [Tags]    HappyPath    Pending
    [Setup]    The user clicks the button/link    jQuery=.grid-row div:nth-child(2) label:contains(Yes)
    # Pending INFUND-5964
    Given the user moves focus and waits for autosave
    When The user clicks the button/link    jQuery=.button[value="Save and close"]
    Then The user should see the text in the page    Test title
    And the user should see the text in the page    Subtitle test
    And the user should see the text in the page    Test guidance title
    And the user should see the text in the page    Guidance text test
    And the user should see the text in the page    150
    And the user should see the text in the page    Yes
    And The user clicks the button/link    jQuery=button:contains(Done)

Application: should have a green check
    [Tags]    HappyPath
    When The user clicks the button/link    jQuery=.button:contains("Done")
    And The user clicks the button/link    link=Competition setup
    Then the user should see the element    css=ul > li:nth-child(5) > img
    And The user should see the element    jQuery=.button:contains("Save as Ready To Open")

Ready To Open button is visible when the user re-opens a section
    [Documentation]    INFUND-4468
    [Tags]
    [Setup]
    Given The user should see the element    jQuery=.button:contains("Save as Ready To Open")
    When The user clicks the button/link    link=Initial details
    And the user clicks the button/link    jQuery=.button:contains("Edit")
    And The user clicks the button/link    link=Competition setup
    Then the user should not see the element    jQuery=.button:contains("Save as Ready To Open")
    [Teardown]    Run keywords    Given The user clicks the button/link    link=Initial details
    ...    AND    The user clicks the button/link    jQuery=.button:contains("Done")
    ...    AND    And The user clicks the button/link    link=Competition setup

User should be able to Save the Competition as Open
    [Documentation]    INFUND-4468
    ...
    ...    INFUND-3002
    [Tags]    HappyPath
    When the user clicks the button/link    jQuery=.button:contains("Save as Ready To Open")
    And the user clicks the button/link    link=All competitions
    And the user clicks the button/link    id=section-3
    Then the competition should show in the correct section    css=section:nth-of-type(2) ul    Test competition
    # The above line checks that the section 'Ready to Open' there is a competition named Test competition

Assessor: Contain the correct options
    [Documentation]    INFUND-5641
    [Tags]    HappyPath
    [Setup]    the user navigates to the page    ${COMP_MANAGEMENT_COMP_SETUP}
    Given the user clicks the button/link    link=Assessors
    And the user should see the text in the page    How many assessors are required for each application?
    Then the user should see the element    jQuery=label:contains(1)
    When the user should see the element    jQuery=label:contains(3)
    And the user should see the element    jQuery=label:contains(5)
    And the user should see the text in the page    How much do assessors receive per application
    And the user should see the element    id=assessorPay

Assessor: Mark as Done then Edit again
     [Documentation]    INFUND-5641
     [Tags]    HappyPath
    When the user selects the checkbox    id=assessors-62
    And the user enters text to a text field    id=assessorPay    100
    And the user clicks the button/link    jQuery=.button:contains("Done")
    Then the user should see the text in the page    3
    And the user should see the text in the page    100
    When the user clicks the button/link    jQuery=.button:contains("Edit")
    Then the user clicks the button/link    jQuery=.button:contains("Done")

Assessor: Should have a Green Check
    [Documentation]    INFUND-5641
    [Tags]    HappyPath
    When The user clicks the button/link    link=Competition setup
    Then the user should see the element    css=li:nth-child(6) .section-status
    And the user clicks the button/link    jQuery=.button:contains("Save as Ready To Open")
    And the user clicks the button/link    link=All competitions
    And the user clicks the button/link    id=section-3
    And the competition should show in the correct section    css=section:nth-of-type(2) ul    Test competition

*** Keywords ***
the user moves focus and waits for autosave
    focus    link=Sign out
    sleep    500ms
    Wait For Autosave

the total should be correct
    [Arguments]    ${Total}
    mouse out    css=input
    Focus    jQuery=Button:contains("Done")
    Wait Until Element Contains    css=.no-margin    ${Total}

the user fills the milestones with valid data
    The user enters text to a text field    name=milestoneEntries[OPEN_DATE].day    10
    The user enters text to a text field    name=milestoneEntries[OPEN_DATE].month    1
    The user enters text to a text field    name=milestoneEntries[OPEN_DATE].year    2019
    The user enters text to a text field    name=milestoneEntries[BRIEFING_EVENT].day    11
    The user enters text to a text field    name=milestoneEntries[BRIEFING_EVENT].month    1
    The user enters text to a text field    name=milestoneEntries[BRIEFING_EVENT].year    2019
    The user enters text to a text field    name=milestoneEntries[SUBMISSION_DATE].day    12
    The user enters text to a text field    name=milestoneEntries[SUBMISSION_DATE].month    1
    The user enters text to a text field    name=milestoneEntries[SUBMISSION_DATE].year    2019
    The user enters text to a text field    name=milestoneEntries[ALLOCATE_ASSESSORS].day    13
    The user enters text to a text field    name=milestoneEntries[ALLOCATE_ASSESSORS].month    1
    The user enters text to a text field    name=milestoneEntries[ALLOCATE_ASSESSORS].year    2019
    The user enters text to a text field    name=milestoneEntries[ASSESSOR_BRIEFING].day    14
    The user enters text to a text field    name=milestoneEntries[ASSESSOR_BRIEFING].month    1
    The user enters text to a text field    name=milestoneEntries[ASSESSOR_BRIEFING].year    2019
    The user enters text to a text field    name=milestoneEntries[ASSESSOR_ACCEPTS].day    15
    The user enters text to a text field    name=milestoneEntries[ASSESSOR_ACCEPTS].month    1
    The user enters text to a text field    name=milestoneEntries[ASSESSOR_ACCEPTS].year    2019
    The user enters text to a text field    name=milestoneEntries[ASSESSOR_DEADLINE].day    16
    The user enters text to a text field    name=milestoneEntries[ASSESSOR_DEADLINE].month    1
    The user enters text to a text field    name=milestoneEntries[ASSESSOR_DEADLINE].year    2019
    The user enters text to a text field    name=milestoneEntries[LINE_DRAW].day    17
    The user enters text to a text field    name=milestoneEntries[LINE_DRAW].month    1
    The user enters text to a text field    name=milestoneEntries[LINE_DRAW].year    2019
    The user enters text to a text field    name=milestoneEntries[ASSESSMENT_PANEL].day    18
    The user enters text to a text field    name=milestoneEntries[ASSESSMENT_PANEL].month    1
    The user enters text to a text field    name=milestoneEntries[ASSESSMENT_PANEL].year    2019
    The user enters text to a text field    name=milestoneEntries[PANEL_DATE].day    19
    The user enters text to a text field    name=milestoneEntries[PANEL_DATE].month    1
    The user enters text to a text field    name=milestoneEntries[PANEL_DATE].year    2019
    The user enters text to a text field    name=milestoneEntries[FUNDERS_PANEL].day    20
    The user enters text to a text field    name=milestoneEntries[FUNDERS_PANEL].month    1
    The user enters text to a text field    name=milestoneEntries[FUNDERS_PANEL].year    2019
    The user enters text to a text field    name=milestoneEntries[NOTIFICATIONS].day    21
    The user enters text to a text field    name=milestoneEntries[NOTIFICATIONS].month    1
    The user enters text to a text field    name=milestoneEntries[NOTIFICATIONS].year    2019
    The user enters text to a text field    name=milestoneEntries[RELEASE_FEEDBACK].day    22
    The user enters text to a text field    name=milestoneEntries[RELEASE_FEEDBACK].month    1
    The user enters text to a text field    name=milestoneEntries[RELEASE_FEEDBACK].year    2019
    Focus    jQuery=button:contains(Done)
    sleep    500ms

the weekdays should be correct
    element should contain    css=tr:nth-child(1) td:nth-child(2)    Thu
    element should contain    css=tr:nth-child(2) td:nth-child(2)    Fri
    element should contain    css=tr:nth-child(3) td:nth-child(2)    Sat
    element should contain    css=tr:nth-child(4) td:nth-child(2)    Sun
    element should contain    css=tr:nth-child(5) td:nth-child(2)    Mon
    element should contain    css=tr:nth-child(6) td:nth-child(2)    Tue
    element should contain    css=tr:nth-child(7) td:nth-child(2)    Wed
    element should contain    css=tr:nth-child(8) td:nth-child(2)    Thu
    element should contain    css=tr:nth-child(9) td:nth-child(2)    Fri
    element should contain    css=tr:nth-child(10) td:nth-child(2)    Sat
    element should contain    css=tr:nth-child(11) td:nth-child(2)    Sun
    element should contain    css=tr:nth-child(12) td:nth-child(2)    Mon
    element should contain    css=tr:nth-child(13) td:nth-child(2)    Tue

the pre-field date should be correct
    Element Should Contain    css=#milestone-OPEN_DATE~ .js-addWeekDay    Fri
    ${YEAR} =    Get Value    css=.date-group:nth-child(1) .year .width-small
    Should Be Equal As Strings    ${YEAR}    2017
    ${MONTH} =    Get Value    css=.date-group:nth-child(1) .month .width-small
    Should Be Equal As Strings    ${MONTH}    12
    ${DAY} =    Get Value    css=.date-group:nth-child(1) .day .width-small
    Should Be Equal As Strings    ${DAY}    1

the resubmission should not have a default selection
    the user sees that the radio button is not selected    resubmission

The user enters valid data in the initial details
    Given the user enters text to a text field    id=title    Competition title
    And the user selects the option from the drop-down menu    Programme    id=competitionTypeId
    And the user selects the option from the drop-down menu    Health and life sciences    id=innovationSectorCategoryId
    And the user selects the option from the drop-down menu    Advanced Therapies    id=innovationAreaCategoryId
    And the user enters text to a text field    id=openingDateDay    01
    And the user enters text to a text field    Id=openingDateMonth    12
    And the user enters text to a text field    id=openingDateYear    2017
    And the user selects the option from the drop-down menu    Ian Cooper    id=leadTechnologistUserId
    And the user selects the option from the drop-down menu    Toby Reader    id=executiveUserId

The competition should show in the correct section
    [Arguments]    ${SECTION}    ${COMP_NAME}
    Element should contain    ${SECTION}    ${COMP_NAME}

The finance information should be correct
    the user should see the text in the page    Light finances
    the user should see the text in the page    No

the user fills the scope assessment questions
    The user clicks the button/link    jQuery=Button:contains("+Add guidance row")
    The user enters text to a text field    id=guidancerow-2-subject    New subject
    The user enters text to a text field    id=guidancerow-2-justification    This is a justification
    The user enters text to a text field    id=question.assessmentGuidance    Guidance for assessing scope section
    The user clicks the button/link    id=remove-guidance-row-0

the user checks the scope assessment questions
    The user should see the text in the page    New subject
    The user should see the text in the page    This is a justification
    The user should not see the text in the page    One or more of the above requirements have not been satisfied.
    The user should see the text in the page    Written feedback
    The user should see the text in the page    Guidance for assessing scope section
    The user should see the text in the page    Scope 'Y/N' question
    The user should see the text in the page    Research category question

the user edits the assessed question information
    the user enters text to a text field    id=question.scoreTotal    100
    the user enters text to a text field    id=question.assessmentGuidance    Business opportunity guidance
    the user clicks the button/link    jQuery=Button:contains("+Add guidance row")
    the user enters text to a text field    id=guidancerow-5-scorefrom    11
    the user enters text to a text field    id=guidancerow-5-scoreto    12
    the user enters text to a text field    id=guidancerow-5-justification    This is a justification

the user sees the correct assessed question information
    the user should see the text in the page    Assessment of this question
    #the user should see the text in the page    Business opportunity guidance
    the user should see the text in the page    11
    the user should see the text in the page    12
    the user should see the text in the page    This is a justification
    the user should see the text in the page    100
    the user should see the text in the page    Written feedback
    the user should see the text in the page    Scored
    the user should see the text in the page    Out of
    the user should not see the text in the page    The business opportunity is plausible
