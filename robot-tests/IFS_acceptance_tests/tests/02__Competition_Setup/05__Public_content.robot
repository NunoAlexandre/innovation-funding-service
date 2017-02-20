*** Settings ***
Documentation     INFUND-6914 Create 'Public content' menu page for "Front Door" setup pages
...
...               INFUND-6916 As a Competitions team member I want to create a Public content summary page
...
...               INFUND-7602 Add / Remove sections for Competition setup > Public content
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
    [Tags]
    [Setup]  log in as a different user    &{Comp_admin1_credentials}
    Given the internal user navigates to public content  ${public_content_competition_name}
    Then the user clicks the button/link   link=Competition information and search
    When the user clicks the button/link            jQuery=.button:contains("Save and return")
    Then the user should see a summary error        Please enter a short description.
    Then the user should see a summary error        Please enter a project funding range.
    Then the user should see a summary error        Please enter an eligibility summary.
    Then the user should see a summary error        Please enter a valid set of keywords.

Competition information and search: Valid values
    [Documentation]    INFUND-6915
    [Tags]  HappyPath
    When the user enters text to a text field       id=short-description        Short public description
    And the user enters text to a text field        id=funding-range            Up to £1million
    And the user enters text to a text field        id=eligibility-summary      Summary of eligiblity
    And the user enters text to a text field        id=keywords                 Search, Testing, Robot
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

Summary: Contains the correct options
    [Documentation]    INFUND-6916
    [Tags]  HappyPath
    Given the user clicks the button/link           link=Summary
    And the user should see the text in the page    Text entered into this section will appear in the summary tab
    Then the user should see the element            css=.editor
    and the user should see the element             jQuery=label:contains("Grant")
    And the user should see the element             jQuery=label:contains("Procurement")
    And the user should see the text in the page    Project size
    And the user should see the element             id=project-size
    And the user should see the element             jQuery=.buttonlink:contains("+ add new section")

Summary: server side validation
    [Documentation]    INFUND-6916
    When the user clicks the button/link                jQuery=.button:contains("Save and return")
    Then the user should see a summary error            Please enter a funding type.
    And the user should see a summary error             Please enter a project size.
    And the user should see a summary error             Please enter a competition description.

Summary: User enters valid values and saves
    [Documentation]    INFUND-6916
    [Tags]  HappyPath
    When the user enters valid data in the summary details
    Then the user should be redirected to the correct page    ${public_content_overview}
    And the user should see the element      link=Summary
    And the user should see the element      css=img[title='The "Summary" section is marked as done']

Summary: Contains the correct values when viewed
    [Documentation]    INFUND-6916
    [Tags]
    When the user clicks the button/link                link=Summary
    Then the user should see the text in the page       Text entered into this section will appear in the summary tab
    And the user should see the text in the page        Grant
    And the user should see the text in the page        10
    And the user should see the element                 jQuery=.button:contains("Return to public content")
    And the user should see the element                 jQuery=.button-secondary:contains("Edit")
    Then the user clicks the button/link                link=Public content

Summary: Add, remove sections and submit
    [Documentation]    INFUND-6916
    [Tags]  HappyPath
    When the user clicks the button/link                    link=Summary
    And the user clicks the button/link                 jQuery=.button-secondary:contains("Edit")
    Then the user can add and remove multiple content groups for summary
    When the user clicks the button/link                        jQuery=button:contains("Save and return")
    And the user should see the element  css=img[title='The "Summary" section is marked as done']

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
    [Documentation]    INFUND-6918 INFUND-7602
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
    Then the user should see the text in the page       Last published
    And the user should not see the element             jQuery=button:contains("Publish public content")
    When the user clicks the button/link                link=Competition information and search
    And the user clicks the button/link                 link=Edit
    Then the user should not see the element            jQuery=button:contains("Save and return")
    And the user should see the element                 jQuery=button:contains("Publish and return")

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

User creates a new competition
    [Arguments]    ${competition_name}
    Given the user navigates to the page    ${CA_UpcomingComp}
    When the user clicks the button/link    jQuery=.button:contains("Create competition")
    When the user fills in the CS Initial details      ${competition_name}  01  02  ${nextyear}

the user enters valid data in the summary details
    The user enters text to a text field   css=.editor    Summary Description
    the user selects the radio button       fundingType    Grant
    the user enters text to a text field    id=project-size   10
    the user clicks the button/link         jQuery=.button:contains("Save and return")

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