*** Settings ***
Documentation     INFUND-6914 Create 'Public content' menu page for "Front Door" setup pages
...
...               INFUND-6916 As a Competitions team member I want to create a Public content summary page
...
...               INFUND-7602 Add / Remove sections for Competition setup > Public content
...
...               INFUND-7486 Create Competition > Summary tab for external "Front Door" view of competition summary
...
...               INFUND-7489 Create 'Competition' > 'Dates' tab for external "Front Door" view of competition dates
Suite Setup       Custom suite setup
Suite Teardown    TestTeardown User closes the browser
Force Tags        CompAdmin
Resource          ../../resources/defaultResources.robot
Resource          CompAdmin_Commons.robot

*** Variables ***
${public_content_competition_name}    Public content competition

*** Test Cases ***
User can view the public content
    [Documentation]    INFUND-6914
    [Tags]  HappyPath
    Given the internal user navigates to public content  ${public_content_competition_name}
    Then the user should see the element     link=Competition information and search
    And the user should see the element      link=Summary
    And the user should see the element      link=Eligibility
    And the user should see the element      link=Scope
    And the user should see the element      link=Dates
    And the user should see the element      link=How to apply
    And the user should see the element      link=Supporting information
    And the user should see the element      jQuery=button:contains("Publish public content"):disabled

Project Finance can also access the Public content sections
    [Documentation]  INFUND-7602
    [Tags]
    # This checks that also other int users have access to this area
    Given log in as a different user      &{internal_finance_credentials}
    When the internal user navigates to public content  ${public_content_competition_name}
    Then the user should not see an error in the page
    When the user visits the sub sections then he should not see any errors

External users do not have access to the Public content sections
    [Documentation]  INFUND-7602
    [Tags]
    Given log in as a different user     &{collaborator1_credentials}
    When run keyword and ignore error without screenshots  the user navigates to the page  ${public_content_overview}
    Then the user should see permissions error message

Competition information and search: server side validation
    [Documentation]    INFUND-6915
    [Tags]  HappyPath
    [Setup]  log in as a different user    &{Comp_admin1_credentials}
    Given the internal user navigates to public content  ${public_content_competition_name}
    Then the user clicks the button/link   link=Competition information and search
    When the user clicks the button/link            jQuery=.button:contains("Save and return")
    Then the user should see a summary error        Please enter a short description.
    Then the user should see a summary error        Please enter a project funding range.
    Then the user should see a summary error        Please enter an eligibility summary.
    Then the user should see a summary error        Please enter a valid set of keywords.

Competition information and search: Valid values
    [Documentation]    INFUND-6915, INFUND-8363
    [Tags]  HappyPath
    When the user enters text to a text field       id=short-description        Short public description
    And the user enters text to a text field        id=funding-range            Up to £1million
    And the user enters text to a text field        id=eligibility-summary      Summary of eligiblity
    When the user enters text to a text field       id=keywords  hellohellohellohellohellohellohellohellohellohellou
    And the user clicks the button/link             jQuery=button:contains("Save and return")
    Then the user should see the element            jQuery=.error-summary-list:contains("Each keyword must be less than 50 characters long.")
    And the user enters text to a text field        id=keywords  Search, Testing, Robot
    And the user clicks the button/link             jQuery=.button:contains("Save and return")
    Then the user should see the element            jQuery=li:nth-of-type(1) img.complete

Competition information and search: ReadOnly
    [Documentation]  INFUND-6915
    [Tags]
    When the user clicks the button/link  link=Competition information and search
    Then the user should see the element  jQuery=dt:contains("Short description") + dd:contains("Short public description")
    And the user should see the element   jQuery=dt:contains("Project funding range") + dd:contains("Up to £1million")
    And the user should see the element   jQuery=dt:contains("Eligibility summary") + dd:contains("Summary of eligiblity")
    And the user should see the element   jQuery=dt:contains("Keywords") + dd:contains("Search,Testing,Robot")
    When the user clicks the button/link  link=Edit
    Then the user should see the element  css=#short-description[value="Short public description"]
    And the user clicks the button/link   jQuery=.button:contains("Save and return")

Summary: server side validation and autosave
    [Documentation]    INFUND-6916, INFUND-7486
    [Tags]
    Given the user clicks the button/link           link=Summary
    And the user should see the text in the page    Text entered into this section will appear in the summary tab
    When the user clicks the button/link            jQuery=.button:contains("Save and return")
    Then the user should see a summary error        Please enter a funding type.
    And the user should see a summary error         Please enter a project size.
    And the user should see a summary error         Please enter a competition description.
    When the user enters valid data in the summary details
    And the user should see the element             jQuery=.buttonlink:contains("+ add new section")

Summary: User enters valid values and saves
    [Documentation]    INFUND-6916, INFUND-7486
    [Tags]  HappyPath
    Given the internal user navigates to public content  ${public_content_competition_name}
    And the user clicks the button/link        link=Summary
    When the user enters valid data in the summary details
    And the user clicks the button/link        jQuery=button:contains("+ add new section")
    When the user enters text to a text field  css=#heading-0  A nice new Heading
    Then the user enters text to a text field   jQuery=.editor:eq(1)  Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum. Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco ullamcoullamco ullamco ullamco
    And the user uploads the file              css=#file-upload-0  ${valid_pdf}
    When the user clicks the button/link       jQuery=button:contains("Save and return")
    Then the user should be redirected to the correct page  ${public_content_overview}
    And the user should see the element      link=Summary
    And the user should see the element      css=img[title='The "Summary" section is marked as done']

Summary: Contains the correct values when viewed
    [Documentation]    INFUND-6916, INFUND-7486
    [Tags]
    When the user clicks the button/link      link=Summary
    Then the user should see the element      jQuery=dt:contains("Funding type") + dd:contains("Grant")
    And the user should see the element       jQuery=dt:contains("Project size") + dd:contains("10 millions")
    And the user should see the element       jQuery=h2:contains("A nice new Heading")
    And the user should see the element       jQuery=a:contains("${valid_pdf}")
    And the user should see the element       jQuery=.button:contains("Return to public content")
    When the user clicks the button/link      jQuery=.button-secondary:contains("Edit")
    And the user enters text to a text field  jQuery=.editor:eq(1)  Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.
    When the user clicks the button/link      jQuery=button:contains("Save and return")
    Then the user should see the element      css=img[title='The "Summary" section is marked as done']

Eligibility: Server side validation
    [Documentation]  INFUND-6916
    [Tags]  HappyPath
    When the user clicks the button/link  link=Eligibility
    And the user clicks the button/link   jQuery=button:contains("Save and return")
    Then the user should see a summary error  Please enter content.
    And the user should see a summary error   Please enter a heading.

Eligibility: Add, remove sections and submit
    [Documentation]    INFUND-6917 INFUND-7602
    [Tags]  HappyPath
    Then the user can add and remove multiple content groups
    When the user clicks the button/link                        jQuery=button:contains("Save and return")
    And the user should see the element  css=img[title='The "Eligibility" section is marked as done']

Scope: Add, remove sections and submit
    [Documentation]    INFUND-6918, INFUND-7602
    [Tags]  HappyPath
    When the user clicks the button/link                         link=Scope
    Then the user can add and remove multiple content groups
    When the user clicks the button/link                        jQuery=button:contains("Save and return")
    And the user should see the element  css=img[title='The "Scope" section is marked as done']

Dates: Add, remove dates and submit
    [Documentation]    INFUND-6919
    [Tags]  HappyPath
    When the user clicks the button/link                         link=Dates
    Then the user should see the text in the page                1 February ${nextyear}
    And the user should see the text in the page                 Competition opens
    And the user should see the text in the page                 Submission deadline, competition closed.
    And the user should see the text in the page                 Applicants notified
    And the user can add and remove multiple event groups
    And the user should see the element  css=img[title='The "Dates" section is marked as done']

How to apply: Add, remove sections and submit
    [Documentation]    INFUND-6920 INFUND-7602
    [Tags]  HappyPath
    When the user clicks the button/link                         link=How to apply
    Then the user can add and remove multiple content groups
    When the user clicks the button/link                        jQuery=button:contains("Save and return")
    And the user should see the element  css=img[title='The "How to apply" section is marked as done']

Supporting information: Add, remove sections and submit
    [Documentation]    INFUND-6921 INFUND-7602
    [Tags]  HappyPath
    When the user clicks the button/link                         link=Supporting information
    Then the user can add and remove multiple content groups
    When the user clicks the button/link                        jQuery=button:contains("Save and return")
    And the user should see the element  css=img[title='The "Supporting information" section is marked as done']

Publish public content: Publish once all sections are complete
    [Documentation]    INFUND-6914
    [Tags]  HappyPath
    Given the user should not see the text in the page  Last published
    When the user clicks the button/link                jQuery=button:contains("Publish public content")
    Then the user should see the element                jQuery=small:contains("Last published")
    And the user should not see the element             jQuery=button:contains("Publish public content")
    When the user clicks the button/link                link=Competition information and search
    And the user clicks the button/link                 link=Edit
    Then the user should not see the element            jQuery=button:contains("Save and return")
    And the user should see the element                 jQuery=button:contains("Publish and return")

The user is able to edit and publish again
    [Documentation]  INFUND-6914
    [Tags]
    Given the user enters text to a text field  id=eligibility-summary  Some other summary
    And the user clicks the button/link         jQuery=button:contains("Publish and return")
    When the user should see all sections completed
    Then the user should see the element        jQuery=small:contains("${today}")
    And the user should not see the element     jQuery=button:contains("Publish and return")
    When the user clicks the button/link        link=Return to setup overview
    Then the user should see the element        JQuery=p:contains("${today}")

Make Competition searchable in front door page
    [Documentation]  INFUND-6923
    [Tags]  HappyPath
    # Is added in HP cause is required for the next step
    Given the user navigates to the page  ${ca_upcomingcomp}
    And the user clicks the button/link   link=${public_content_competition_name}
    Then the user fills in the CS Milestones  ${day}  ${month}  ${nextyear}
    [Teardown]  the user closes the browser

Guest user can filter competitions by Keywords
    [Documentation]  INFUND-6923
    [Tags]  HappyPath
    [Setup]  The guest user opens the browser
    Given the user navigates to the page  ${frontDoor}
    When the user enters text to a text field  id=keywords  Robot
    And the user clicks the button/link        jQuery=button:contains("Update results")
    Then the user should see the element       jQuery=a:contains("${public_content_competition_name}")

Guest user can see the updated Summary information
    [Documentation]  INFUND-7486
    [Tags]
    Given the user clicks the button/link  link=Public content competition
    And the user clicks the button/link    link=Summary
    Then the user should see the element   jQuery=.column-third:contains("Description") ~ .column-two-thirds:contains("This is a Summary description")
    And the user should see the element    jQuery=.column-third:contains("Funding type") ~ .column-two-thirds:contains("Grant")
    And the user should see the element    jQuery=.column-third:contains("Project size") ~ .column-two-thirds:contains("10 millions")
    And the user should see the element    jQuery=.column-third:contains("A nice new Heading") ~ .column-two-thirds:contains("Ut enim ad minim veniam,")

The guest user is able to download the file in the Summary
    [Documentation]  INFUND-7486
    [Tags]  Pending
    # TODO Pending due to INFUND-8536

The guest user can see updated date information
   [Documentation]    INFUND-7489
   [Tags]
   [Setup]    the month is converted to text
   Given the user clicks the button/link    link=Dates
   And the user should see the element    jQuery=dt:contains("1 February ${nextyear}") + dd:contains("Competition opens")
   And the user should see the element    jQuery=dt:contains(${newdate}) + dd:contains("Competition closes")
   And the user should see the element    jQuery=dt:contains(${newdate}) + dd:contains("Applicants notified")
   And the user should see the element    jQuery=dt:contains("12 December ${nextyear}") + dd:contains("Content 1")
   And the user should see the element    jQuery=dt:contains("20 December ${nextyear}") + dd:contains("Content 2")

*** Keywords ***
Custom suite setup
    Connect to Database  @{database}
    Guest user log-in    &{Comp_admin1_credentials}
    ${nextyear} =  get next year
    Set suite variable  ${nextyear}
    User creates a new competition   ${public_content_competition_name}
    ${competitionId}=  get comp id from comp title  ${public_content_competition_name}
    ${public_content_overview}=    catenate    ${server}/management/competition/setup/public-content/${competitionId}
    Set suite variable  ${public_content_overview}
    ${today} =  get today
    set suite variable  ${today}
    ${day} =  get tomorrow day
    Set suite variable  ${day}
    ${month} =  get tomorrow month
    set suite variable  ${month}

User creates a new competition
    [Arguments]    ${competition_name}
    Given the user navigates to the page    ${CA_UpcomingComp}
    When the user clicks the button/link    jQuery=.button:contains("Create competition")
    When the user fills in the CS Initial details      ${competition_name}  01  02  ${nextyear}

the user enters valid data in the summary details
    The user enters text to a text field    css=.editor  This is a Summary description
    the user selects the radio button       fundingType    Grant
    the user enters text to a text field    id=project-size   10 millions

the user can add and remove multiple content groups
    When the user enters text to a text field   id=heading-0    Heading 1
    And the user enters text to a text field    jQuery=.editor:eq(0)     Content 1
    And the user uploads the file               id=file-upload-0  ${valid_pdf}
    Then the user should see the element        jQuery=.uploaded-file:contains("testing.pdf")
    And the user clicks the button/link         jQuery=button:contains("remove")
    And the user clicks the button/link         jQuery=button:contains("+ add new section")
    And the user enters text to a text field    id=heading-1    Heading 2
    And the user enters text to a text field    jQuery=.editor:eq(1)     Content 2
    And the user uploads the file               id=file-upload-1  ${valid_pdf}
    And the user clicks the button/link         jQuery=button:contains("+ add new section")
    And the user enters text to a text field    id=heading-2    Heading 3
    And the user enters text to a text field    jQuery=.editor:eq(2)     Content 3
    When the user uploads the file              id=file-upload-2  ${text_file}
    Then the user should see the element        jQuery=.error-summary-list:contains("Please upload a file in .pdf format only.")
    #    And the user uploads the file               id=file-upload-2  ${too_large_pdf}
    #    Then the user should see the element        jQuery=h1:contains("Attempt to upload a large file")
    #    and the user goes back to the previous page
    #    And the user should not see an error in the page
    # I comment those lines out due to TODO INFUND-8358
    And the user clicks the button/link         jQuery=button:contains("Remove section"):eq(1)
    Then the user should not see the element    id=heading-2
    And the user should not see the element     jQuery=.editor:eq(2)

the user can add and remove multiple content groups for summary
    When the user clicks the button/link        jQuery=button:contains("+ add new section")
    And the user clicks the button/link         jQuery=button:contains("Save and return")
    Then the user should see a summary error    Please enter a heading.
    And the user should see a summary error     Please enter content.
    When the user enters text to a text field   id=heading-0    Heading 1
    And the user enters text to a text field    jQuery=.editor:eq(1)     Content 1
    And the user clicks the button/link         jQuery=button:contains("+ add new section")
    And the user enters text to a text field    id=heading-1    Heading 2
    And the user enters text to a text field    jQuery=.editor:eq(2)     Content 2
    And the user clicks the button/link         jQuery=button:contains("+ add new section")
    And the user enters text to a text field    id=heading-2    Heading 3
    And the user enters text to a text field    jQuery=.editor:eq(3)     Content 3
    And the user clicks the button/link         jQuery=button:contains("Remove section"):eq(2)
    Then the user should not see the element    id=heading-2
    And the user should not see the element     jQuery=.editor:eq(3)

the user can add and remove multiple event groups
    When the user clicks the button/link        jQuery=button:contains("+ add new event")
    And the user clicks the button/link         jQuery=button:contains("Save and return")
    Then the user should see a summary error    Please enter a valid date.
    And the user should see a summary error     Please enter valid content.
    And the user enters text to a text field    id=dates-0-day      60
    And the user enters text to a text field    id=dates-0-month    -6
    And the user clicks the button/link         jQuery=button:contains("Save and return")
    Then the user should see a summary error    must be between 1 and 31
    And the user should see a summary error     must be between 1 and 12
    When the user enters text to a text field   id=dates-0-day      12
    And the user enters text to a text field    id=dates-0-month    12
    And the user enters text to a text field    id=dates-0-year     ${nextyear}
    And the user enters text to a text field    jQuery=.editor:eq(0)     Content 1
    And the user clicks the button/link         jQuery=button:contains("+ add new event")
    And the user enters text to a text field    id=dates-1-day      20
    And the user enters text to a text field    id=dates-1-month    12
    And the user enters text to a text field    id=dates-1-year     ${nextyear}
    And the user enters text to a text field    jQuery=.editor:eq(1)     Content 2
    And the user clicks the button/link         jQuery=button:contains("+ add new event")
    And the user enters text to a text field    id=dates-2-day      30
    And the user enters text to a text field    id=dates-2-month    12
    And the user enters text to a text field    id=dates-2-year     ${nextyear}
    And the user enters text to a text field    jQuery=.editor:eq(2)     Content 3
    And the user clicks the button/link         jQuery=button:contains("Remove event"):eq(2)
    Then the user should not see the element    id=dates-2-day
    And the user should not see the element     id=dates-2-month
    And the user should not see the element     id=dates-2-year
    And the user should not see the element     jQuery=.editor:eq(2)
    And the user clicks the button/link         jQuery=button:contains("Save and return")

the user visits the sub sections then he should not see any errors
    the user visits  Competition information and search
    the user visits  Summary
    the user visits  Eligibility
    the user visits  Scope
    the user visits  Dates
    the user visits  How to apply
    the user visits  Supporting information

the user visits
    [Arguments]  ${section}
    the user clicks the button/link  link=${section}
    the user should see the element  jQuery=h1:contains("${section}")
    the user should not see an error in the page
    the user clicks the button/link  link=Public content

the user should see all sections completed
    :FOR  ${i}  IN RANGE  1  8
    \    the user should see the element  jQuery=li:nth-child(${i}) img.complete

the month is converted to text
    ${fulldate} =  Catenate    ${nextyear}  ${month}   ${day}
    ${newdate} =    Convert Date     ${fulldate}    result_format=%-d %B %Y    exclude_millis=true
    set suite variable    ${newdate}