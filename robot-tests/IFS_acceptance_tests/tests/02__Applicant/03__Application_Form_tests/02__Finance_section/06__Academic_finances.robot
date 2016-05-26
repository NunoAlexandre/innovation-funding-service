*** Settings ***
Documentation     INFUND-917: As an academic partner i want to input my finances according to the JES field headings, so that i enter my figures into the correct sections
...
...
...               INFUND-918: As an academic partner i want to be able to mark my finances as complete, so that the lead partner can have confidence in my finances
...
...
...               INFUND-2399: As a Academic partner I want to be able to add my finances including decimals for accurate recording of my finances
Suite Setup       Guest user log-in    &{collaborator2_credentials}
Suite Teardown    User closes the browser
Force Tags        Finances
Resource          ../../../../resources/GLOBAL_LIBRARIES.robot
Resource          ../../../../resources/variables/GLOBAL_VARIABLES.robot
Resource          ../../../../resources/variables/User_credentials.robot
Resource          ../../../../resources/keywords/Login_actions.robot
Resource          ../../../../resources/keywords/User_actions.robot

*** Variables ***
${valid_pdf}      testing.pdf
${valid_pdf excerpt}    Adobe PDF is an ideal format for electronic document distribution
${text_file}      testing.txt
${too_large_pdf}    large.pdf

*** Test Cases ***
Academic finances should be editable when lead marks finances as complete
    [Documentation]    INFUND-2314
    [Tags]    Failing
    [Setup]    Lead applicant marks the finances as complete
    #we need to adjust the application in order to mark this as complete
    When the user navigates to the page    ${YOUR_FINANCES_URL}
    Then the user should not see the element    css=#incurred-staff[readonly]
    [Teardown]    Lead applicant marks the finances as incomplete

Academic finance validations
    [Documentation]    INFUND-2399
    [Tags]
    When the user navigates to the page    ${YOUR_FINANCES_URL}
    And the applicant enters invalid inputs
    Mark academic finances as complete
    Then the user should see an error    This field should be 0 or higher
    Then the user should see an error    This field cannot be left blank
    And the user should see the element    css=.error-summary-list
    And the field should not contain the currency symbol

Academic finance calculations
    [Documentation]    INFUND-917
    ...
    ...    INFUND-2399
    [Tags]
    When the user navigates to the page    ${YOUR_FINANCES_URL}
    When the academic partner fills the finances
    Then the calculations should be correct and the totals rounded to the second decimal

Large pdf upload not allowed
    [Documentation]    INFUND-2720
    [Tags]    Upload
    When the academic partner uploads a file    ${too_large_pdf}
    Then the user should get an error page    ${too_large_pdf_validation_error}

Non pdf uploads not allowed
    [Documentation]    INFUND-2720
    [Tags]
    [Setup]    The user navigates to the page    ${your_finances_url}
    When the academic partner uploads a file    ${text_file}
    Then the user should get an error page    ${wrong_filetype_validation_error}

Lead applicant can't upload a JeS file
    [Documentation]    INFUND-2720
    [Tags]
    [Setup]    Guest user log-in    &{lead_applicant_credentials}
    When the user navigates to the page    ${your_finances_url}
    Then the user should not see the element    name=jes-upload

Non-academic collaborator can't upload a JeS file
    [Documentation]    INFUND-2720
    [Tags]
    [Setup]    Guest user log-in    &{collaborator1_credentials}
    When the user navigates to the page    ${your_finances_url}
    Then the user should not see the element    name=jes-upload

Academics upload
    [Documentation]    INFUND-917
    [Tags]
    [Setup]    Guest user log-in    &{collaborator2_credentials}
    Given the user navigates to the page    ${your_finances_url}
    When the academic partner uploads a file    ${valid_pdf}
    Then the user should not see the text in the page    No file currently uploaded
    And the user should see the element    link=testing.pdf
    And the user waits for the file to be scanned by the anti virus software

Academic collaborator can view the file on the finances page
    [Documentation]    INFUND-917
    [Tags]
    [Setup]    Guest user log-in    &{collaborator2_credentials}
    Given the user navigates to the page    ${your_finances_url}
    When the user clicks the button/link    link=${valid_pdf}
    Then the user should see the text in the page    ${valid_pdf_excerpt}

Academic collaborator can view the file on the finances overview page
    [Documentation]    INFUND-917
    [Tags]
    Given the user navigates to the page    ${finances_overview_url}
    When the user clicks the button/link    link=testing.pdf
    Then the user should see the text in the page    ${valid_pdf_excerpt}

Lead applicant can't view the file on the finances page
    [Documentation]    INFUND-917
    [Tags]
    [Setup]    Guest user log-in    &{lead_applicant_credentials}
    When the user navigates to the page    ${your_finances_url}
    Then the user should not see the text in the page    ${valid_pdf}

Lead applicant can view the file on the finances overview page
    [Documentation]    INFUND-917
    [Tags]
    Given the user navigates to the page    ${finances_overview_url}
    And the user should see the text in the page    ${valid_pdf}
    When the user clicks the button/link    link=${valid_pdf}
    Then the user should see the text in the page    ${valid_pdf_excerpt}

Non-academic collaborator can't view the file on the finances page
    [Documentation]    INFUND-917
    [Tags]
    [Setup]    Guest user log-in    &{collaborator1_credentials}
    When the user navigates to the page    ${your_finances_url}
    Then the user should not see the text in the page    ${valid_pdf}

Non-academic collaborator can view the file on the finances overview page
    [Documentation]    INFUND-917
    [Tags]
    Given the user navigates to the page    ${finances_overview_url}
    And the user should see the text in the page    ${valid_pdf}
    When the user clicks the button/link    link=${valid_pdf}
    Then the user should see the text in the page    ${valid_pdf_excerpt}

Academic finances JeS link showing
    [Documentation]    INFUND-2402
    [Tags]    Academic
    [Setup]    Guest user log-in    &{collaborator2_credentials}
    When the user navigates to the page    ${your_finances_url}
    Then the user can see the link for more JeS details

Mark all as complete
    [Documentation]    INFUND-918
    Given the user reloads the page
    When the user clicks the button/link    jQuery=.button:contains("Mark all as complete")
    Then the user should be redirected to the correct page    ${APPLICATION_OVERVIEW_URL}
    And the user navigates to the page    ${FINANCES_OVERVIEW_URL}
    And the user should see the element    css=.finance-summary tr:nth-of-type(3) img[src="/images/field/tick-icon.png"]

User should not be able to edit or upload the form
    [Documentation]    INFUND-2437
    [Tags]
    When the user navigates to the page    ${YOUR_FINANCES_URL}
    Then the user should not see the element    jQuery=button:contains("Remove")
    And the user should see the element    css=#incurred-staff[readonly]

Academic finance overview
    [Documentation]    INFUND-917
    ...
    ...    INFUND-2399
    [Tags]
    Given the user navigates to the page    ${FINANCES_OVERVIEW_URL}
    Then the finance table should be correct
    When the user clicks the button/link    link=testing.pdf
    Then the user should see the text in the page    Adobe Acrobat PDF Files

*** Keywords ***
the academic partner fills the finances
    [Documentation]    INFUND-2399
    Input Text    id=incurred-staff    999.999
    Input Text    id=travel    999.999
    Input Text    id=other    999.999
    Input Text    id=investigators    999.999
    Input Text    id=estates    999.999
    Input Text    id=other-direct    999.999
    Input Text    id=indirect    999.999
    Input Text    id=exceptions-staff    999.999
    Input Text    id=exceptions-other-direct    999.999
    Input Text    id=tsb-ref    123123
    Mouse Out    css=input
    Sleep    300ms

the calculations should be correct and the totals rounded to the second decimal
    Textfield Value Should Be    id=subtotal-directly-allocated    £ 3,000
    Textfield Value Should Be    id=subtotal-exceptions    £ 2,000
    Textfield Value Should Be    id=total    £ 9,000

the academic partner uploads a file
    [Arguments]    ${file_name}
    Choose File    name=jes-upload    ${UPLOAD_FOLDER}/${file_name}
    Sleep    500ms

the finance table should be correct
    Element Should Contain    css=.project-cost-breakdown tr:nth-of-type(3) td:nth-of-type(1)    £9,000
    Element Should Contain    css=.project-cost-breakdown tr:nth-of-type(3) td:nth-of-type(2)    £3,000
    Element Should Contain    css=.project-cost-breakdown tr:nth-of-type(3) td:nth-of-type(3)    £1,000
    Element Should Contain    css=.project-cost-breakdown tr:nth-of-type(3) td:nth-of-type(4)    £1,000
    Element Should Contain    css=.project-cost-breakdown tr:nth-of-type(3) td:nth-of-type(6)    £0
    Element Should Contain    css=.project-cost-breakdown tr:nth-of-type(3) td:nth-of-type(7)    £1,000
    Element Should Contain    css=.project-cost-breakdown tr:nth-of-type(3) td:nth-of-type(8)    £3,000

Lead applicant marks the finances as complete
    Given guest user log-in    steve.smith@empire.com    Passw0rd
    And the user navigates to the page    ${YOUR_FINANCES_URL}
    When the user clicks the button/link    jQuery=.button:contains("Mark all as complete")
    And close browser
    And Switch to the first browser

Lead applicant marks the finances as incomplete
    Given guest user log-in    steve.smith@empire.com    Passw0rd
    And the user navigates to the page    ${YOUR_FINANCES_URL}
    And the user clicks the button/link    jQuery=button:contains("Edit")
    And Close Browser
    And Switch to the first browser

the user reloads the page
    Reload Page

the user can see the link for more JeS details
    Element Should Be Visible    link=Je-S website
    Page Should Contain Element    xpath=//a[contains(@href,'https://je-s.rcuk.ac.uk')]

the applicant enters invalid inputs
    Input Text    id=incurred-staff    100£
    Input Text    id=travel    -89
    Input Text    id=other    999.999
    Input Text    id=investigators    999.999
    Input Text    id=estates    999.999
    Input Text    id=other-direct    999.999
    Input Text    id=indirect    999.999
    Input Text    id=exceptions-staff    999.999
    Input Text    id=exceptions-other-direct    999.999
    Input Text    id=tsb-ref    ${EMPTY}

the field should not contain the currency symbol
    Textfield Value Should Be    id=incurred-staff    100

Mark academic finances as complete
    Focus    jQuery=.button:contains("Mark all as complete")
    And the user clicks the button/link    jQuery=.button:contains("Mark all as complete")

the user waits for the file to be scanned by the anti virus software
    Sleep       5s
    # this sleep statement is necessary as we wait for the antivirus scanner to work. Please do not remove during refactoring!
