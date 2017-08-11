*** Settings ***
Documentation  INFUND-6390 As an Applicant I will be invited to add project costs, organisation and funding details via links ƒin the 'Finances' section of my application
...
...            INFUND-6393 As an Applicant I will be invited to add Staff count and Turnover where the include projected growth table is set to 'No' within the Finances page of Competition setup
...
...            INFUND-6395 s an Applicant I will be invited to add Projected growth, and Organisation size where the include projected growth table is set to Yes within the Finances page of Competition setup
...
...            INFUND-6895 As an Lead Applicant I will be advised that changing my Research category after completing Funding level will reset the 'Funding level'
...
...            INFUND-9151 Update 'Application details' where a single 'Innovation area' set in 'Initial details'
...
...            IFS-40 As a comp executive I am able to select an 'Innovation area' of 'All' where the 'Innovation sector' is 'Open'
...
...            IFS-1015 As a Lead applicant with an existing account I am informed if my Organisation type is NOT eligible to lead
Suite Setup     Custom Suite Setup
Suite Teardown  Close browser and delete emails
Force Tags      Applicant  CompAdmin
Resource        ../../../resources/defaultResources.robot
Resource        ../Applicant_Commons.robot
Resource        ../../10__Project_setup/PS_Common.robot
Resource        ../../02__Competition_Setup/CompAdmin_Commons.robot

*** Variables ***
${compWithoutGrowth}         FromCompToNewAppl without GrowthTable
${applicationWithoutGrowth}  NewApplFromNewComp without GrowthTable
${compWithGrowth}            All-Innov-Areas With GrowthTable
${applicationWithGrowth}     All-Innov-Areas Application With GrowthTable
${newUsersEmail}             liam@innovate.com
${ineligibleMessage}         Your organisation is not eligible to start an application for this competition.

*** Test Cases ***
Comp Admin starts a new Competition
    [Documentation]    INFUND-6393
    [Tags]    HappyPath
    [Setup]  the user logs-in in new browser       &{Comp_admin1_credentials}
    # For the testing of the story INFUND-6393, we need to create New Competition in order to apply the new Comp Setup fields
    # Then continue with the applying to this Competition, in order to see the new Fields applied
    Given the user navigates to the page           ${CA_UpcomingComp}
    When the user clicks the button/link           jQuery=.button:contains("Create competition")
    Then the user fills in the CS Initial details  ${compWithoutGrowth}  ${month}  ${nextyear}
    And the user fills in the CS Funding Information
    And the user fills in the CS Eligibility
    And the user fills in the CS Milestones        ${month}  ${nextMonth}  ${nextyear}

Comp Admin fills in the Milestone Dates and can see them formatted afterwards
    [Documentation]    INFUND-7820
    [Tags]
    Given the user should see the element  jQuery=div:contains("Milestones") ~ .task-status-complete
    When the user clicks the button/link   link=Milestones
    Then the user should see the element   jQuery=button:contains("Edit")
    And the user should see the dates in full format
    Then the user clicks the button/link   link=Competition setup

Application Finances should not include project growth
    [Documentation]    INFUND-6393
    [Tags]
    The user decides about the growth table  no  No

Comp admin completes ths competition setup
    [Documentation]    INFUND-6393
    [Tags]    HappyPath
    Given the user should see the element  jQuery=h1:contains("Competition setup")
    Then the user marks the Application as done
    And the user fills in the CS Assessors
    When the user clicks the button/link  link=Public content
    Then the user fills in the Public content and publishes
    And the user clicks the button/link   link=Return to setup overview
    And the user should see the element   jQuery=div:contains("Public content") ~ .task-status-complete
    When the user clicks the button/link  jQuery=a:contains("Save")
    And the user navigates to the page    ${CA_UpcomingComp}
    Then the user should see the element  jQuery=h2:contains("Ready to open") ~ ul a:contains("${compWithoutGrowth}")

Competition is Open to Applications
    [Documentation]    INFUND-6393
    [Tags]    HappyPath  MySQL
    The competitions date changes so it is now Open  ${compWithoutGrowth}

Create new Application for this Competition
    [Tags]    HappyPath  MySQL
    Lead Applicant applies to the new created competition  ${compWithoutGrowth}

Applicant visits his Finances
    [Documentation]    INFUND-6393
    [Tags]
    Given the user should see the element          jQuery=h1:contains("Application overview")
    When the user clicks the button/link           link=Your finances
    Then the user should see the element           jQuery=li:contains("Your project costs") > .action-required
    And the user should see the element            jQuery=li:contains("Your organisation") > .action-required
    And the the user should see that the funding depends on the research area
    And the user should see his finances empty
    [Teardown]    the user clicks the button/link  jQuery=a:contains("Return to application overview")

Applicant fills in the Application Details
    [Documentation]    INFUND-6895  INFUND-9151
    [Tags]    HappyPath
    The user fills in the Application details  ${applicationWithoutGrowth}

Turnover and Staff count fields
    [Documentation]    INFUND-6393
    [Tags]
    Given the user clicks the button/link         link=Your finances
    Then the user clicks the button/link          link=Your organisation
    And the user should see the text in the page  Turnover (£)
    And the user should see the text in the page  Full time employees
    And the user should see the text in the page  Number of full time employees at your organisation.

Once the project growth table is selected
    [Documentation]    INFUND-6393 IFS-40
    [Tags]
    [Setup]    log in as a different user                &{Comp_admin1_credentials}
    Given the user navigates to the page                 ${CA_UpcomingComp}
    When the user clicks the button/link                 jQuery=.button:contains("Create competition")
    # For the testing of story IFS-40, turning this competition into Sector with All innovation areas
    Then the user fills in the Open-All Initial details  ${compWithGrowth}  ${month}  ${nextyear}
    And the user fills in the CS Funding Information
    And the user fills in the CS Eligibility
    And the user fills in the CS Milestones              ${month}  ${nextMonth}  ${nextyear}
    When the user decides about the growth table         yes    Yes
    Then the user marks the Application as done
    And the user fills in the CS Assessors
    When the user clicks the button/link                 link=Public content
    Then the user fills in the Public content and publishes
    And the user clicks the button/link                  link=Return to setup overview
    And the user should see the element                  jQuery=div:contains("Public content") ~ .task-status-complete
    When the user clicks the button/link                 jQuery=a:contains("Save")
    And the user navigates to the page                   ${CA_UpcomingComp}
    Then the user should see the element                 jQuery=h2:contains("Ready to open") ~ ul a:contains("${compWithGrowth}")
    [Teardown]  The competitions date changes so it is now Open  ${compWithGrowth}

As next step the Applicant cannot see the turnover field
    [Documentation]    INFUND-6393, INFUND-6395
    [Tags]    MySQL
    Given Lead Applicant applies to the new created competition  ${compWithGrowth}
    When the user clicks the button/link                         link=Your finances
    And the user clicks the button/link                          link=Your organisation
    Then the user should not see the text in the page            Turnover (£)
    And the user should see the text in the page                 Full time employees
    And the user should see the text in the page                 How many full-time employees did you have on the project at the close of your last financial year?

Organisation server side validation when no
    [Documentation]    INFUND-6393
    [Tags]    HappyPath
    [Setup]    log in as a different user           &{lead_applicant_credentials}
    Given the user navigates to Your-finances page  ${applicationWithoutGrowth}
    Then the user clicks the button/link            link=Your organisation
    When the user clicks the button/link            jQuery=button:contains("Mark as complete")
    Then the user should see the element            jQuery=.error-summary-list:contains("Enter your organisation size.")
    When the user enters text to a text field       jQuery=label:contains("Turnover") + input    -42
    And the user enters text to a text field        jQuery=label:contains("employees") + input    15.2
    And the user clicks the button/link             jQuery=button:contains("Mark as complete")
    Then the user should see the element            jQuery=.error-summary li:contains("This field should be 0 or higher.")
    And the user should see the element             jQuery=.error-summary li:contains("This field can only accept whole numbers.")
    And the user should not see the element         jQuery=h1:contains("Your finances")
    # Checking that by marking as complete, the user doesn't get redirected to the main finances page

Organisation client side validation when no
    [Documentation]    INFUND-6393
    [Tags]
    Given the user selects medium organisation size
    When the user enters text to a text field           jQuery=label:contains("Turnover") + input  -33
    And the user moves focus to the element             jQuery=label:contains("Full time employees") + input
    Then the user should see a field and summary error  This field should be 0 or higher.
    And the user enters text to a text field            jQuery=label:contains("Full time employees") + input  ${empty}
    When the user moves focus to the element            jQuery=button:contains("Mark as complete")
    Then the user should see a field and summary error  This field cannot be left blank.
    When the user enters text to a text field           jQuery=label:contains("Turnover") + input  150
    And the user enters text to a text field            jQuery=label:contains("employees") + input  0
    And the user moves focus to the element             jQuery=button:contains("Mark as complete")
    Then the user should not see the element            css=.error-message

Mark Organisation as complete when no
    [Documentation]    INFUND-6393
    [Tags]    HappyPath
    Given the user enters text to a text field    jQuery=label:contains("employees") + input    42
    And the user enters text to a text field      jQuery=label:contains("Turnover") + input    17506
    And the user selects medium organisation size
    When the user clicks the button/link          jQuery=button:contains("Mark as complete")
    Then the user should see the element          jQuery=li:contains("Your organisation") > .task-status-complete
    When the user clicks the button/link          link=Your organisation
    Then The user should not see the element      css=input
    and the user should see the element           jQuery=button:contains("Edit")
    And the user clicks the button/link           jQuery=a:contains("Return to finances")

The Lead applicant is able to edit and re-submit when no
    [Documentation]    INFUND-8518
    [Tags]
    The user can edit resubmit and read only of the organisation

Funding subsection opens when Appl details and organisation info are provided
    [Documentation]    INFUND-6895
    [Tags]    HappyPath
    Given the user navigates to the page    ${dashboard_url}
    And the user clicks the button/link     link=${applicationWithoutGrowth}
    When the user should see the element    jQuery=li:contains("Application details") > .task-status-complete
    And the user clicks the button/link     link=Your finances
    And the user should see the element     jQuery=li:contains("Your organisation") > .task-status-complete
    Then the user should see the element    jQuery=li:contains("Your funding") > .action-required

Organisation server side validation when yes
    [Documentation]    INFUND-6393
    [Tags]
    [Setup]  the user navigates to the growth table finances
    Given the user clicks the button/link  link=Your organisation
    When the user clicks the button/link   jQuery=button:contains("Mark as complete")
    And the user should see the element    jQuery=.error-summary-list li:contains("This field cannot be left blank.")
    And the user should see the element    jQuery=.error-message:contains("This field cannot be left blank.")
    And the user should see the element    jQuery=.error-summary-list li:contains("Please enter a valid date.")
    And the user should see the element    jQuery=.error-message:contains("Please enter a valid date.")
    And The user should see a field error  This field cannot be left blank.
    And The user should see a field error  Please enter a valid date.
    #And The user should see a field error    Enter your organisation size
    #TODO Enable the above checks when IFS-535 is ready

Organisation client side validation when yes
    [Documentation]    INFUND-6395
    [Tags]
    When the user enters text to a text field                 css=input[name$="month"]    42
    Then the user should see a field and summary error        Please enter a valid date.
    When the user enters text to a text field                 css=input[name$="month"]    12
    And the user enters text to a text field                  css=input[name$="year"]    ${nextyear}
    Then the user should see a field and summary error        Please enter a past date.
    When the user enters text to a text field                 css=input[name$="year"]    2016
    And the user enters value to field                        Annual turnover    ${EMPTY}
    Then the user should see a field and summary error        This field cannot be left blank.
    When the user enters value to field                       Annual turnover    8.5
    And the user moves focus to the element                   jQuery=td:contains("Annual profit") + td input
    Then the user should see a field and summary error        This field can only accept whole numbers.
    And the user enters value to field                        Annual profit    -5
    When the user enters value to field                       Annual export    ${empty}
    Then the user should see a field and summary error        This field cannot be left blank.
    And the user enters value to field                        Research and development spend    2147483647
    When the user enters text to a text field                 jQuery=label:contains("employees") + input    22.4
    Then the user should see a field and summary error        This field can only accept whole numbers.
    When the user enters text to a text field                 jQuery=label:contains("employees") + input    1
    Then the user should not see the element                  jQuery=span:contains("employees") + .error-message

Mark Organisation as complete when yes
    [Documentation]    INFUND-6393
    [Tags]
    [Setup]    the user navigates to the growth table finances
    Given the user clicks the button/link        link=Your organisation
    And the user selects medium organisation size
    Then the user enters text to a text field    css=input[name$="month"]    12
    And the user enters text to a text field     css=input[name$="year"]    2016
    And the user populates the project growth table
    When the user enters text to a text field    jQuery=label:contains("employees") + input    4
    # TODO pending due to IFS-484
    #    And the user clicks the button/link     jQuery=a:contains("Return to finances")
    #    And the user clicks the button/link     link=Your organisation
    #    Then the user should see the element    jQuery=td:contains("Research and development spend") + td input[value="15000"]
    When the user clicks the button/link         jQuery=button:contains("Mark as complete")
    Then the user should see the element         jQuery=li:contains("Your organisation") > .task-status-complete

The Lead Applicant is able to edit and re-submit when yes
    [Documentation]    INFUND-8518
    [Tags]
    Given the user can edit resubmit and read only of the organisation

Lead applicant can see all innovation areas
    [Documentation]  IFS-40
    [Tags]
    Given the user navigates to the page         ${DASHBOARD_URL}
    And the user clicks the button/link          jQuery=a:contains('Untitled application'):last
    And the user clicks the button/link          link=Application details
    #The fact that the link is present means that the innovation area is not pre-defined
    When the user clicks the button/link         css=#researchArea
    Then the user should see the element         jQuery=label[for^="innovationAreaChoice"]:contains("Biosciences")           # from sector Health and life sciences
    And the user should see the element          jQuery=label[for^="innovationAreaChoice"]:contains("Forming technologies")  # from sector Materials and manufacturing
    And the user should see the element          jQuery=label[for^="innovationAreaChoice"]:contains("Space technology")      # from sector Emerging and enabling
    And the user should see the element          jQuery=label[for^="innovationAreaChoice"]:contains("Offshore wind")         # from sector Infrastructure systems
    And the user should see the element          jQuery=label[for^="innovationAreaChoice"]:contains("Marine transport")      # from sector Transport
    When the user selects the radio button       innovationAreaChoice  19  # Bio
    And the user clicks the button/link          css=button[name="save-innovation-area"]
    Then the user should see the element         jQuery=label[for="researchArea"] + *:contains("Biosciences")
    [Teardown]  the user clicks the button/link  jQuery=button:contains("Save and return to application overview")

Applicant can view and edit project growth table
    [Documentation]    INFUND-6395
    [Tags]
    Given the user navigates to the growth table finances
    When the user clicks the button/link                link=Your organisation
    Then the user should view the project growth table
    And the user can edit the project growth table
    And the user populates the project growth table
    and the user clicks the button/link                 jQuery=button:contains("Mark as complete")

The Lead Applicant fills in the Application Details for App with Growth
    [Documentation]  This step is required for following test cases
    [Tags]
    Given the user clicks the button/link          link=Application overview
    And the user fills in the Application details  ${applicationWithGrowth}

Newly created collaborator can view and edit project Growth table
    [Documentation]    INFUND-8426
    [Tags]
    [Setup]    Invite a non-existing collaborator in Application with Growth table
    Given the user navigates to Your-finances page  ${applicationWithGrowth}
    And the user clicks the button/link             link=Your organisation
    And the user selects medium organisation size
    Then the user enters text to a text field       css=input[name$="month"]    12
    And the user enters text to a text field        css=input[name$="year"]    2016
    And the user populates the project growth table
    And the user clicks the button/link             jQuery=button:contains("Mark as complete")
    And the user should not see an error in the page

Invite Collaborator in Application with Growth table
    [Documentation]    INFUND-8518 INFUND-8561
    [Tags]  Email  MySQL
    [Setup]
    Given the lead applicant invites an existing user  ${compWithGrowth}  ${collaborator1_credentials["email"]}
    When log in as a different user                    &{collaborator1_credentials}
    Then the user reads his email and clicks the link  ${collaborator1_credentials["email"]}  Invitation to collaborate in ${compWithGrowth}  You will be joining as part of the organisation  3
    When the user should see the element               jQuery=h2:contains("We have found an account with the invited email address")
    Then the user clicks the button/link               link=Continue or sign in
    And the user clicks the button/link                link=Confirm and accept invitation

Non-lead can mark Organisation as complete
    [Documentation]    INFUND-8518 INFUND-8561
    [Tags]
    Given the user navigates to Your-finances page  ${applicationWithGrowth}
    And the user clicks the button/link             link=Your organisation
    Then the user selects medium organisation size
    And the user enters text to a text field        css=input[name$="month"]    12
    And the user enters text to a text field        css=input[name$="year"]    2016
    Then the user populates the project growth table
    And the user enters text to a text field        jQuery=label:contains("employees") + input    42
    When the user clicks the button/link            jQuery=button:contains("Mark as complete")
    Then the user should see the element            jQuery=li:contains("Your organisation") > .task-status-complete

Non-lead can can edit and remark Organisation as Complete
    [Documentation]    INFUND-8518 INFUND-8561
    [Tags]
    Given the user can edit resubmit and read only of the organisation

RTOs are not allowed to apply on Competition where only Businesses are allowed to lead
    [Documentation]  IFS-1015
    [Tags]  HappyPath
    Given the logged in user should not be able to apply in a competition he has not right to  antonio.jenkins@jabbertype.example.com  ${compWithoutGrowth}
    When the user should see the element           jQuery=h1:contains("Research")
    Then the user should see the text in the page  ${ineligibleMessage}

Business organisation is not allowed to apply on Comp where only RTOs are allowed to lead
    [Documentation]  IFS-1015
    [Tags]  HappyPath
    Given the logged in user should not be able to apply in a competition he has not right to  theo.simpson@katz.example.com  ${OPEN_COMPETITION_NAME}
    When the user clicks the button/link  link=Start new application
    Then the user should see the text in the page  ${ineligibleMessage}

*** Keywords ***
Custom Suite Setup
    ${tomorrowday} =    get tomorrow day
    Set suite variable  ${tomorrowday}
    ${month} =          get tomorrow month
    set suite variable  ${month}
    ${nextMonth} =  get next month
    set suite variable  ${nextMonth}
    ${nextMonthWord} =  get next month as word
    set suite variable  ${nextMonthWord}
    ${nextyear} =       get next year
    Set suite variable  ${nextyear}

the user should see the dates in full format
    the user should see the element  jQuery=td:contains("Allocate assessors") ~ td:contains("3 ${nextMonthWord} ${nextyear}")

the the user should see that the funding depends on the research area
    the user should see the element  jQuery=h3:contains("Your funding") + p:contains("You must select a research category in"):contains("application details")

the user should see his finances empty
    the user should see the element  jQuery=thead:contains("Total project costs") ~ *:contains("£0")

the user selects feasibility studies and no to resubmission
    the user clicks the button/link   jQuery=label:contains("Research category")
    the user clicks the button twice  jQuery=label[for^="researchCategoryChoice"]:contains("Feasibility studies")
    the user clicks the button/link   jQuery=button:contains(Save)
    the user clicks the button twice  jQuery=label[for="application.resubmission-no"]

the user decides about the growth table
    [Arguments]    ${edit}    ${read}
    the user should see the element   jQuery=h1:contains("Competition setup")
    the user clicks the button/link   link=Application
    the user clicks the button/link   link=Finances
    the user clicks the button/link   jQuery=a:contains("Edit this question")
    the user clicks the button twice  jQuery=label[for="include-growth-table-${edit}"]
    the user enters text to a text field  css=.editor  Funding rules for the competition added
    the user clicks the button/link   jQuery=button:contains("Save and close")
    the user clicks the button/link   link=Finances
    the user should see the element   jQuery=dt:contains("Include project growth table") + dd:contains("${read}")
    the user clicks the button/link   link=Application
    the user clicks the button/link   link=Competition setup

The competitions date changes so it is now Open
    [Arguments]  ${competition}
    Connect to Database  @{database}
    Change the open date of the Competition in the database to one day before  ${competition}
    the user navigates to the page   ${CA_Live}
    the user should see the element  jQuery=h2:contains("Open") ~ ul a:contains("${competition}")

Lead Applicant applies to the new created competition
    [Arguments]  ${competition}
    log in as a different user       &{lead_applicant_credentials}
    the user navigates to the eligibility of the competition  ${competition}
    the user clicks the button/link  jQuery=a:contains("Sign in")
    the user clicks the button/link  jQuery=a:contains("Begin application")

the user navigates to the eligibility of the competition
    [Arguments]  ${competition}
    ${competitionId} =               get comp id from comp title    ${competition}
    the user navigates to the page   ${server}/application/create/check-eligibility/${competitionId}

the user enters value to field
    [Arguments]  ${field}  ${value}
    the user enters text to a text field  jQuery=td:contains("${field}") + td input  ${value}

the user should see an error message in the field
    [Arguments]  ${field}  ${errmsg}
    the user should see the element  jQuery=span:contains("${field}") + *:contains("${errmsg}")

the user selects medium organisation size
    the user selects the radio button  financePosition-organisationSize  ${MEDIUM_ORGANISATION_SIZE}
    the user selects the radio button  financePosition-organisationSize  ${MEDIUM_ORGANISATION_SIZE}

the user populates the project growth table
    the user enters value to field    Annual turnover    65000
    the user enters value to field    Annual profit    2000
    the user enters value to field    Annual export    3000
    the user enters value to field    Research and development spend    15000

the user should view the project growth table
    the user should see the text in the element    css=table.extra-margin-bottom tr:nth-of-type(1) th:nth-of-type(1)    Section
    the user should see the text in the element    css=table.extra-margin-bottom tr:nth-of-type(1) th:nth-of-type(2)    Last financial year (£)
    the user should see the text in the element    jQuery=tr:nth-child(1) td:nth-child(1) span    Annual turnover
    the user should see the element                jQuery=td input[value="65000"]
    the user should see the text in the element    jQuery=tr:nth-child(2) td:nth-child(1) span    Annual profits
    the user should see the element                jQuery=td input[value="2000"]
    the user should see the text in the element    jQuery=tr:nth-child(3) td:nth-child(1) span    Annual export
    the user should see the element                jQuery=td input[value="3000"]
    the user should see the text in the element    jQuery=tr:nth-child(4) td:nth-child(1) span    Research and development spend
    the user should see the element                jQuery=td input[value="15000"]

the user can edit the project growth table
    the user clicks the button/link         jQuery=button.buttonlink:contains('Edit')
    the user selects the radio button       financePosition-organisationSize    ${SMALL_ORGANISATION_SIZE}
    the user enters text to a text field    jQuery=tr:nth-child(1) .form-control    4000
    the user enters text to a text field    jQuery=td input[value="65000"]    5000

the applicant enters valid inputs
    The user clicks the button/link         jquery=li:nth-last-child(1) button:contains('Add additional partner organisation')
    The user enters text to a text field    name=organisations[1].organisationName    ${PROJECT_SETUP_APPLICATION_1_PARTNER_NAME}
    The user enters text to a text field    name=organisations[1].invites[0].personName    Jessica Doe
    The user enters text to a text field    name=organisations[1].invites[0].email    ${collaborator1_credentials["email"]}
    focus                                   jquery=button:contains("Save changes")
    The user clicks the button/link         jquery=button:contains("Save changes")

the user can edit resubmit and read only of the organisation
    the user should see the element         jQuery=li:contains("Your organisation") > .task-status-complete
    the user clicks the button/link         link=Your organisation
    the user clicks the button/link         jQuery=button:contains("Edit")
    the user enters text to a text field    jQuery=label:contains("employees") + input    2
    the user clicks the button/link         jQuery=button:contains("Mark as complete")
    the user should not see an error in the page
    the user should see the element         jQuery=li:contains("Your organisation") > .task-status-complete
    the user clicks the button/link         link=Your organisation
    the user should see the element         jQuery=dt:contains("employees") + dd:contains("2")

the lead applicant invites an existing user
    [Arguments]    ${comp_title}    ${EMAIL_INVITED}
    log in as a different user            &{lead_applicant_credentials}
    the user navigates to the page        ${dashboard_url}
    the user clicks the button/link       jquery=.in-progress a:contains("${applicationWithGrowth}")
    the user fills in the inviting steps  ${EMAIL_INVITED}

the user navigates to the growth table finances
    the user navigates to the page   ${DASHBOARD_URL}
    the user clicks the button/link  jQuery=.in-progress a:contains("Untitled application"):last
    the user clicks the button/link  link=Your finances

Invite a non-existing collaborator in Application with Growth table
    the user should see the element       jQuery=h1:contains("Application overview")
    the user fills in the inviting steps  ${newUsersEmail}
    newly invited collaborator can create account and sign in

the user fills in the inviting steps
    [Arguments]  ${email}
    the user clicks the button/link       link=view team members and add collaborators
    the user clicks the button/link       link=Add partner organisation
    the user enters text to a text field  css=#organisationName  New Organisation's Name
    the user enters text to a text field  css=input[id="applicants0.name"]  Partner's name
    the user enters text to a text field  css=input[id="applicants0.email"]  ${email}
    the user clicks the button/link       jQuery=button:contains("Add organisation and invite applicants")

Newly invited collaborator can create account and sign in
    logout as user
    the user reads his email and clicks the link  ${newUsersEmail}  Invitation to collaborate in ${compWithGrowth}  You will be joining as part of the organisation  3
    the user clicks the button/link               jQuery=a:contains("Yes, accept invitation")
    the user should see the element               jquery=h1:contains("Choose your organisation type")
    the user completes the new account creation

the user completes the new account creation
    the user selects the radio button       organisationType    radio-4
    the user clicks the button/link         jQuery=button:contains("Continue")
    the user should see the element         jQuery=span:contains("Create your account")
    the user enters text to a text field    id=organisationSearchName    innovate
    the user should see the element         jQuery=a:contains("Back to choose your organisation type")
    the user clicks the button/link         jQuery=button:contains("Search")
    wait for autosave
    the user clicks the button/link         jQuery=a:contains("INNOVATE LTD")
    the user should see the element         jQuery=h3:contains("Organisation type")
    the user selects the checkbox           address-same
    wait for autosave
    the user clicks the button/link         jQuery=button:contains("Continue")
    the user should not see an error in the page
    the user clicks the button/link                      jQuery=.button:contains("Save and continue")
    the user should be redirected to the correct page    ${SERVER}/registration/register
    the user enters text to a text field                 jQuery=input[id="firstName"]    liam
    the user enters text to a text field                 JQuery=input[id="lastName"]    smithson
    the user enters text to a text field                 jQuery=input[id="phoneNumber"]    077712567890
    the user enters text to a text field                 jQuery=input[id="password"]    ${correct_password}
    the user selects the checkbox                        termsAndConditions
    the user clicks the button/link                      jQuery=button:contains("Create account")
    the user should see the text in the page             Please verify your email address
    the user reads his email and clicks the link         ${newUsersEmail}  Please verify your email address  Once verified you can sign into your account.
    the user should be redirected to the correct page    ${REGISTRATION_VERIFIED}
    the user clicks the button/link                      link=Sign in
    the user should see the text in the page              Sign in
    the user enters text to a text field                 jQuery=input[id="username"]  ${newUsersEmail}
    the user enters text to a text field                 jQuery=input[id="password"]  ${correct_password}
    the user clicks the button/link                      jQuery=button:contains("Sign in")

the user fills in the Open-All Initial details
    [Arguments]  ${compTitle}  ${month}  ${nextyear}
    the user clicks the button/link                      link=Initial details
    the user enters text to a text field                 css=#title  ${compTitle}
    the user selects the option from the drop-down menu  Sector  id=competitionTypeId
    the user selects the option from the drop-down menu  Open  id=innovationSectorCategoryId
    the user selects the option from the drop-down menu  All  css=select[id^=innovationAreaCategory]
    the user enters text to a text field                 css=#openingDateDay  1
    the user enters text to a text field                 css=#openingDateMonth  ${month}
    the user enters text to a text field                 css=#openingDateYear  ${nextyear}
    the user selects the option from the drop-down menu  Ian Cooper  id=innovationLeadUserId
    the user selects the option from the drop-down menu  Robert Johnson  id=executiveUserId
    the user clicks the button/link                      jQuery=button:contains("Done")
    the user clicks the button/link                      link=Competition setup
    the user should see the element                      jQuery=div:contains("Initial details") ~ .task-status-complete

the user fills in the Application details
    # I am not using a global keyword, because in this suite we are testing differently the innovation areas
    [Arguments]  ${appTitle}
    the user should see the element       jQuery=h1:contains("Application overview")
    the user clicks the button/link       link=Application details
    the user enters text to a text field  css=#application_details-title  ${appTitle}
    The user should not see the element   link=Choose your innovation area
    the user selects feasibility studies and no to resubmission
    the user enters text to a text field  css=#application_details-startdate_day    ${tomorrowday}
    the user enters text to a text field  css=#application_details-startdate_month    ${month}
    the user enters text to a text field  css=#application_details-startdate_year    ${nextyear}
    the user enters text to a text field  css=#application_details-duration    24
    The user clicks the button/link       jQuery=button[name="mark_as_complete"]
    the user clicks the button/link       link=Application overview
    the user should see the element       jQuery=li:contains("Application details") > .task-status-complete

the logged in user should not be able to apply in a competition he has not right to
    [Arguments]  ${email}  ${competition}
    log in as a different user       ${email}  ${short_password}
    the user clicks the button/link  id=proposition-name
    the user clicks the button/link  link=${competition}
    the user clicks the button/link  link=Start new application