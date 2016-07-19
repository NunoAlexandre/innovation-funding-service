*** Settings ***
Documentation     INFUND-832
...               INFUND-409
Suite Setup       Log in create a new invite application invite academic collaborators and accept the invite
Suite Teardown    TestTeardown User closes the browser
Force Tags        Upload    Applicant    Email
Resource          ../../../../resources/GLOBAL_LIBRARIES.robot
Resource          ../../../../resources/variables/GLOBAL_VARIABLES.robot
Resource          ../../../../resources/variables/User_credentials.robot
Resource          ../../../../resources/keywords/Login_actions.robot
Resource          ../../../../resources/keywords/User_actions.robot    # Note that all of these tests will require you to set an absolute path for the upload folder robot-tests/upload_files    # If you are using the run_tests_locally shellscript then this will attempt to swap in a valid path automatically    # But if you are running pybot manually you will need to add -v UPLOAD_FOLDER:/home/foo/bar/robot-tests/upload_files
Resource          ../../../../resources/keywords/SUITE_SET_UP_ACTIONS.robot

*** Variables ***
${download_link}    ${SERVER}/application/1/form/question/8/forminput/18/download
${virus_scanning_warning}    This file is awaiting virus scanning

*** Test Cases ***
Large pdf uploads not allowed
    [Documentation]    INFUND-832
    [Tags]    HappyPath
    [Setup]    Guest user log-in    &{lead_applicant_credentials}
    Given the user navigates to the page    ${DASHBOARD_URL}
    And the user clicks the button/link    link=Academic robot test application
    And the user clicks the button/link    link=5. Technical approach
    When the user uploads the file to the 'technical approach' question    ${too_large_pdf}
    Then the user should get an error page    ${too_large_pdf_validation_error}

Non pdf uploads not allowed
    [Documentation]    INFUND-832
    [Tags]
    Given the user navigates to the page    ${DASHBOARD_URL}
    And the user clicks the button/link    link=Academic robot test application
    And the user clicks the button/link    link=5. Technical approach
    When the user uploads the file to the 'technical approach' question    ${text_file}
    Then the user should get an error page    ${wrong_filetype_validation_error}

Lead applicant can upload a pdf file
    [Documentation]    INFUND-832
    [Tags]    HappyPath
    [Setup]
    Given the user navigates to the page    ${DASHBOARD_URL}
    And the user clicks the button/link    link=Academic robot test application
    And the user clicks the button/link    link=5. Technical approach
    Then the user uploads the file to the 'technical approach' question    ${valid_pdf}
    And the user should see the text in the page    ${valid_pdf}

Lead applicant can view a file
    [Documentation]    INFUND-2720
    [Tags]    HappyPath
    Given the user should see the element    link=${valid_pdf}
    And the file has been scanned for viruses
    When the applicant opens the uploaded file
    Then the user should not see an error in the page
    [Teardown]    The user goes back to the previous page

Lead applicant can download a pdf file
    [Documentation]    INFUND-2720
    [Tags]    Pending    HappyPath
    # TODO Pending until download functionality has been plugged in
    Given the user should see the text in the page    ${valid_pdf}
    When the user downloads the file from the link    ${valid_pdf}    ${download_link}
    Then the file should be downloaded    ${valid_pdf}
    [Teardown]    Remove File    ${valid_pdf}

Collaborators can view a file
    [Documentation]    INFUND-2306
    [Tags]    HappyPath
    [Setup]    Guest user log-in    ${test_mailbox_one}+academictest@gmail.com    Passw0rd123
    Given the user navigates to the page    ${DASHBOARD_URL}
    And the user clicks the button/link    link=Academic robot test application
    And the user clicks the button/link    link=5. Technical approach
    And the user should see the text in the page    ${valid_pdf}
    When the user clicks the button/link    link=${valid_pdf}
    Then the user should not see an error in the page
    [Teardown]    The user goes back to the previous page

Collaborators can download a pdf file
    [Documentation]    INFUND-2720
    [Tags]    Pending
    # TODO Pending until download functionality has been plugged in
    Given the user should see the text in the page    ${valid_pdf}
    When the user downloads the file from the link    ${valid_pdf}    ${download_link}
    Then the file should be downloaded    ${valid_pdf}
    [Teardown]    Remove File    ${valid_pdf}

Collaborators cannot upload a file if not assigned
    [Documentation]    INFUND-3007
    [Tags]
    When the user should see the text in the page    Appendix
    Then the user should not see the text in the page    Upload

Collaborators cannot remove a file if not assigned
    [Documentation]    INFUND-2720
    [Tags]    HappyPath
    When the user should see the text in the page    ${valid_pdf}
    Then the user should not see the text in the page    Remove

Questions can be assigned with appendices
    [Documentation]    INFUND-832
    ...    INFUND-409
    [Tags]
    [Setup]    Guest user log-in    &{lead_applicant_credentials}
    Given the user navigates to the page    ${DASHBOARD_URL}
    And the user clicks the button/link    link=Academic robot test application
    And the user clicks the button/link    link=5. Technical approach
    And the user should see the text in the page    ${valid_pdf}
    When the user assigns the question to the collaborator    Arsene Wenger
    Then the user should not see the text in the page    Remove
    And the user clicks the button/link    link=Application Overview
    Then the user clicks the button/link    link=6. Innovation
    And the user assigns the question to the collaborator    Arsene Wenger

Collaborators can view a file when the question is assigned
    [Documentation]    INFUND_2720
    [Tags]
    [Setup]    Guest user log-in    ${test_mailbox_one}+academictest@gmail.com    Passw0rd123
    Given the user navigates to the page    ${DASHBOARD_URL}
    And the user clicks the button/link    link=Academic robot test application
    And the user clicks the button/link    link=5. Technical approach
    And the user should see the element    link=${valid_pdf}
    When the user clicks the button/link    link=${valid_pdf}
    Then the user should not see an error in the page
    [Teardown]    The user goes back to the previous page

Collaborator can download a file when the question is assigned
    [Documentation]    INFUND-2720
    [Tags]    Pending
    # TODO Pending until download functionality has been plugged in
    Given the user should see the text in the page    ${valid_pdf}
    When the user downloads the file from the link    ${valid_pdf}    ${download_link}
    Then the file should be downloaded    ${valid_pdf}

Collaborator can remove a file when the question is assigned
    [Documentation]    INFUND-2720
    [Tags]    Pending
    # TODO pending INFUND-3259
    Given the user navigates to the page    ${DASHBOARD_URL}
    And the user clicks the button/link    link=Academic robot test application
    And the user clicks the button/link    link=5. Technical approach
    And the user should see the text in the page    ${valid_pdf}
    When the user can remove the uploaded file    ${valid_pdf}
    Then the user can re-assign the question back to the lead applicant

Collaborators can upload a file when the question is assigned
    [Documentation]    INFUND_3007
    [Tags]
    Given the user navigates to the page    ${DASHBOARD_URL}
    And the user clicks the button/link    link=Academic robot test application
    And the user clicks the button/link    link=6. Innovation
    When the user should see the text in the page    Upload
    Then the user uploads the file to the 'Innovation' question    ${valid_pdf}
    And the user can re-assign the question back to the lead applicant

Appendices available only for the correct questions
    [Documentation]    INFUND-832
    [Tags]
    [Setup]    Guest user log-in    &{lead_applicant_credentials}
    the user cannot see the option to upload a file on the question    link=1. Business opportunity
    the user cannot see the option to upload a file on the question    link=2. Potential market
    the user cannot see the option to upload a file on the question    link=3. Project exploitation
    the user cannot see the option to upload a file on the question    link=4. Economic benefit
    # the user can see the option to upload a file on the question    link=6. Innovation [Have commented this for implementing INFUND-3007 by Pradha]
    the user cannot see the option to upload a file on the question    link=7. Risks
    the user can see the option to upload a file on the question    link=8. Project team
    the user cannot see the option to upload a file on the question    link=9. Funding
    the user cannot see the option to upload a file on the question    link=10. Adding value

Quarantined files are not returned to the user and the user is informed
    [Documentation]    INFUND-2683
    ...    INFUND-2684
    [Tags]
    [Setup]    Guest user log-in    &{lead_applicant_credentials}
    Given the user navigates to the page    ${project_team_url}
    When the user should see the text in the page    test_quarantine.pdf
    And the user clicks the button/link    link=test_quarantine.pdf
    Then the user should see the text in the page    File not available for download
    And the user should see the text in the page    This file has been quarantined by the virus scanner

*** Keywords ***
the user logs out
    logout as user

the collaborator logs in
    log in as user    &{collaborator1_credentials}

the user uploads the file to the 'technical approach' question
    [Arguments]    ${file_name}
    Choose File    name=formInput[14]    ${UPLOAD_FOLDER}/${file_name}
    Sleep    500ms

the user uploads the file to the 'Innovation' question
    [Arguments]    ${file_name}
    Choose File    name=formInput[17]    ${UPLOAD_FOLDER}/${file_name}
    Sleep    500ms

the user can re-assign the question back to the lead applicant
    the user reloads the page
    the user clicks the button/link    name=assign_question
    the user reloads the page

the user cannot see the option to upload a file on the question
    [Arguments]    ${QUESTION}
    Given the user navigates to the page    ${DASHBOARD_URL}
    And the user clicks the button/link    link=Academic robot test application
    And the user clicks the button/link    ${QUESTION}
    the user should not see the text in the page    Upload

the user can see the option to upload a file on the question
    [Arguments]    ${QUESTION}
    Given the user navigates to the page    ${DASHBOARD_URL}
    And the user clicks the button/link    link=Academic robot test application
    And the user clicks the button/link    ${QUESTION}
    the user should see the text in the page    Upload

The applicant opens the uploaded file
    When the user clicks the button/link    link=${valid_pdf}
    Run Keyword And Ignore Error    Confirm Action
