*** Settings ***
Documentation     INFUND-2602 As a competition administrator I want a view of the Application Overview page that allows me to upload the assessor feedback document so that this can be shared with the applicants
Suite Setup       Log in as user    email=john.doe@innovateuk.test    password=Passw0rd
Suite Teardown    User closes the browser
Force Tags        Comp admin    Upload
Resource          ../../../resources/GLOBAL_LIBRARIES.robot
Resource          ../../../resources/variables/GLOBAL_VARIABLES.robot
Resource          ../../../resources/variables/User_credentials.robot
Resource          ../../../resources/keywords/Login_actions.robot
Resource          ../../../resources/keywords/User_actions.robot

*** Variables ***
${successful_application_overview}    ${server}/management/competition/3/application/16
${unsuccessful_application_overview}    ${server}/management/competition/3/application/17

*** Test Cases ***
Large pdf uploads not allowed
    [Documentation]    INFUND-2602
    [Tags]
    Given the user can see the option to upload a file on the page    ${successful_application_overview}
    When the user uploads the file    ${too_large_pdf}
    Then the user should get an error page    ${too_large_pdf_validation_error}

Non pdf uploads not allowed
    [Documentation]    INFUND-2602
    [Tags]
    Given the user can see the option to upload a file on the page    ${successful_application_overview}
    When the user uploads the file    ${text_file}
    Then the user should get an error page    ${wrong_filetype_validation_error}

Valid upload to a successful application
    [Documentation]    INFUND-2602
    [Tags]
    Given the user can see the option to upload a file on the page    ${successful_application_overview}
    And the user uploads the file    ${valid_pdf}

Comp admin can view the file
    [Documentation]    INFUND-2602
    [Tags]
    Given the user should see the text in the page    ${valid_pdf}
    And the file has been scanned for viruses
    When the user clicks the button/link    link=${valid_pdf}
    Then the user should see the text in the page    ${valid_pdf_excerpt}
    [Teardown]    The user navigates to the page    ${successful_application_overview}

Comp admin cannot upload more than one file
    [Documentation]    INFUND-2602
    [Tags]
    When the user should see the text in the page    ${valid_pdf}
    Then the user should not see the element    jQuery=.button:contains("Upload")

Comp admin can remove the file
    [Documentation]    INFUND-2602
    [Tags]
    Given the user should see the text in the page    ${valid_pdf}
    And the user should see the text in the page    Remove
    When the user clicks the button/link    name=removeAssessorFeedback
    Then the user should not see the text in the page    ${valid_pdf}
    And the user should see the text in the page    Upload

Comp admin can re-upload after removing
    [Documentation]    INFUND-2602
    [Tags]
    Given the user can see the option to upload a file on the page    ${successful_application_overview}
    And the user uploads the file    ${valid_pdf}
    [Teardown]    the user clicks the button/link    name=removeAssessorFeedback

Comp admin can upload a file to an unsuccessful application
    [Documentation]    INFUND-2602
    [Tags]
    Given the user can see the option to upload a file on the page    ${unsuccessful_application_overview}
    And the user uploads the file    ${valid_pdf}
    [Teardown]    the user clicks the button/link    name=removeAssessorFeedback

Comp admin can download the file
    [Documentation]    INFUND-2602
    [Tags]    Pending
    # Pending until download functionality has been plugged in
    Given the user should see the text in the page    ${valid_pdf}
    When the user downloads the file from the link    ${valid_pdf}    ${download_link}
    Then the file should be downloaded    ${valid_pdf}
    [Teardown]    Remove File    ${valid_pdf}

*** Keywords ***
the user uploads the file
    [Arguments]    ${upload_filename}
    Choose File    id=assessorFeedback    ${UPLOAD_FOLDER}/${upload_filename}
    Sleep    500ms
