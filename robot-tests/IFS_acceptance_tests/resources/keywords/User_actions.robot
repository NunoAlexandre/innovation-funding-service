*** Settings ***
Resource          ../../resources/GLOBAL_LIBRARIES.robot
Resource          ../../resources/variables/GLOBAL_VARIABLES.robot
Resource          ../../resources/variables/User_credentials.robot
Resource          ../../resources/keywords/Login_actions.robot

*** Keywords ***
The user navigates to the page
    [Arguments]    ${TARGET_URL}
    Wait for autosave
    Go To    ${TARGET_URL}
    Run Keyword And Ignore Error    Confirm Action
    # Error checking
    Page Should Not Contain    Error
    Page Should Not Contain    something went wrong
    Page Should Not Contain    Page or resource not found
    Page Should Not Contain    You do not have the necessary permissions for your request
    # Header checking (INFUND-1892)
    Wait Until Element Is Visible    id=global-header
    Page Should Contain    BETA
    # "Contact us" checking (INFUND-1289)
    # Pending completion of INFUND-2544, INFUND-2545
    # Wait Until Page Contains Element    link=Contact Us
    # Page Should Contain Link    href=${SERVER}/info/contact

The user navigates to the assessor page
    [Arguments]    ${TARGET_URL}
    Wait for autosave
    Go To    ${TARGET_URL}
    # Error checking
    Page Should Not Contain    Error
    Page Should Not Contain    something went wrong

The user navigates to the page without the usual headers
    [Arguments]    ${TARGET_URL}
    Wait for autosave
    Go To    ${TARGET_URL}
    # Error checking
    Page Should Not Contain    Error
    Page Should Not Contain    something went wrong
    Page Should Not Contain    Page or resource not found
    Page Should Not Contain    You do not have the necessary permissions for your request

The user navigates to the page and gets a custom error message
    [Arguments]    ${TARGET_URL}    ${CUSTOM_ERROR_MESSAGE}
    Wait for autosave
    Go To    ${TARGET_URL}
    Page Should Contain    ${CUSTOM_ERROR_MESSAGE}

The user is on the page
    [Arguments]    ${TARGET_URL}
    Location Should Contain    ${TARGET_URL}
    # Error checking
    Page Should Not Contain    Error
    Page Should Not Contain    something went wrong
    Page Should Not Contain    Page or resource not found
    Page Should Not Contain    You do not have the necessary permissions for your request
    # Header checking (INFUND-1892)
    Wait Until Element Is Visible    id=global-header
    Page Should Contain    BETA
    # "Contact us" checking (INFUND-1289)
    # Pending completion of INFUND-2544, INFUND-2545
    # Wait Until Page Contains Element    link=Contact Us
    # Page Should Contain Link    href=${SERVER}/info/contact

The user should be redirected to the correct page
    [Arguments]    ${URL}
    Wait Until Keyword Succeeds    10    500ms    Location Should Contain    ${URL}
    Page Should Not Contain    error
    Page Should Not Contain    Page or resource not found
    Page Should Not Contain    You do not have the necessary permissions for your request
    # Header checking (INFUND-1892)
    Wait Until Element Is Visible    id=global-header
    Page Should Contain    BETA

the user should be redirected to the correct page without the usual headers
    [Arguments]    ${URL}
    Sleep    500ms
    Location Should Contain    ${URL}
    Page Should Not Contain    error
    Page Should Not Contain    Page or resource not found
    Page Should Not Contain    You do not have the necessary permissions for your request

the user should be redirected to the correct page without error checking
    [Arguments]    ${URL}
    Wait Until Keyword Succeeds    10    500ms    Location Should Contain    ${URL}
    # Header checking (INFUND-1892)
    Wait Until Element Is Visible    id=global-header
    Page Should Contain    BETA

the user reloads the page
    Wait for autosave
    Reload Page
    # Error checking
    Page Should Not Contain    Error
    Page Should Not Contain    something went wrong
    Page Should Not Contain    Page or resource not found
    Page Should Not Contain    You do not have the necessary permissions for your request
    # Header checking (INFUND-1892)
    Element Should Be Visible    id=global-header
    Page Should Contain    BETA

the user selects the checkbox
    [Arguments]    ${checkbox}
    Select Checkbox    ${checkbox}
    # Error checking
    Page Should Not Contain    Error
    Page Should Not Contain    something went wrong
    Page Should Not Contain    Page or resource not found
    Page Should Not Contain    You do not have the necessary permissions for your request
    # Header checking (INFUND-1892)
    Element Should Be Visible    id=global-header
    Page Should Contain    BETA

the user unselects the checkbox
    [Arguments]    ${checkbox}
    Unselect Checkbox    ${checkbox}
    # Error checking
    Page Should Not Contain    Error
    Page Should Not Contain    something went wrong
    Page Should Not Contain    Page or resource not found
    Page Should Not Contain    You do not have the necessary permissions for your request
    # Header checking (INFUND-1892)
    Element Should Be Visible    id=global-header
    Page Should Contain    BETA

the user selects the radio button
    [Arguments]    ${RADIO_BUTTON}    ${RADIO_BUTTON_OPTION}
    the user should see the element    ${RADIO_BUTTON}
    Select Radio Button    ${RADIO_BUTTON}    ${RADIO_BUTTON_OPTION}
    # Error checking
    Page Should Not Contain    Error
    Page Should Not Contain    something went wrong
    Page Should Not Contain    Page or resource not found
    Page Should Not Contain    You do not have the necessary permissions for your request
    # Header checking (INFUND-1892)
    Element Should Be Visible    id=global-header
    Page Should Contain    BETA

the user sees that the radio button is selected
    [Arguments]    ${RADIO_BUTTON}    ${SELECTION}
    wait until element is visible    ${RADIO_BUTTON}
    Radio Button Should Be Set To    ${RADIO_BUTTON}    ${SELECTION}
    # Error checking
    Page Should Not Contain    Error
    Page Should Not Contain    something went wrong
    Page Should Not Contain    Page or resource not found
    Page Should Not Contain    You do not have the necessary permissions for your request
    # Header checking (INFUND-1892)
    Element Should Be Visible    id=global-header
    Page Should Contain    BETA

the user selects the option from the drop-down menu
    [Arguments]    ${option}    ${drop-down}
    wait until element is visible    ${drop-down}
    Select From List    ${drop-down}    ${option}
    mouse out    ${drop-down}
    # Error checking
    Page Should Not Contain    Error
    Page Should Not Contain    something went wrong
    Page Should Not Contain    Page or resource not found
    Page Should Not Contain    You do not have the necessary permissions for your request
    # Header checking (INFUND-1892)
    Element Should Be Visible    id=global-header
    Page Should Contain    BETA

the user moves focus to the element
    [Arguments]    ${element}
    # Error checking
    Page Should Not Contain    Error
    Page Should Not Contain    something went wrong
    Page Should Not Contain    Page or resource not found
    Page Should Not Contain    You do not have the necessary permissions for your request
    # Header checking (INFUND-1892)
    Element Should Be Visible    id=global-header
    Page Should Contain    BETA

    wait until element is visible    ${element}
    focus    ${element}

the user moves the mouse away from the element
    [Arguments]    ${element}
    # Error checking
    Page Should Not Contain    Error
    Page Should Not Contain    something went wrong
    Page Should Not Contain    Page or resource not found
    Page Should Not Contain    You do not have the necessary permissions for your request
    # Header checking (INFUND-1892)
    Element Should Be Visible    id=global-header
    Page Should Contain    BETA

    wait until element is visible    ${element}
    mouse out    ${element}




the user should see the dropdown option selected
    [Arguments]    ${option}    ${drop-down}
    List Selection Should Be    ${drop-down}    ${option}
    # Error checking
    Page Should Not Contain    Error
    Page Should Not Contain    something went wrong
    Page Should Not Contain    Page or resource not found
    Page Should Not Contain    You do not have the necessary permissions for your request
    # Header checking    (INFUND-1892)
    Element Should Be Visible    id=global-header
    Page Should Contain    BETA

the user submits the form
    Submit Form
    Page Should Not Contain    Error
    Page Should Not Contain Button    something went wrong
    Page Should Not Contain    Page or resource not found
    Page Should Not Contain    You do not have the necessary permissions for your request
    # Header checking (INFUND-1892)
    Element Should Be Visible    id=global-header
    Page Should Contain    BETA

the user follows the flow to register their organisation
    Given the user navigates to the page    ${COMPETITION_DETAILS_URL}
    When the user clicks the button/link    jQuery=.column-third .button:contains("Apply now")
    And the user clicks the button/link    jQuery=.button:contains("Create account")
    And the user clicks the button/link    jQuery=.button:contains("Create")
    And the user enters text to a text field    id=organisationSearchName    Innovate
    And the user clicks the button/link    id=org-search
    And the user clicks the button/link    link=INNOVATE LTD
    And the user selects the checkbox    address-same
    And the user clicks the button/link    jQuery=.button:contains("Save organisation and continue")
    And the user clicks the button/link    jQuery=.button:contains("Save")

the user edits the 'Project Summary' question
    focus    css=#form-input-11 .editor
    Clear Element Text    css=#form-input-11 .editor
    Input Text    css=#form-input-11 .editor    I am a robot
    sleep    1s

Question should be editable
    [Arguments]    ${Mark_question_as_incomplete}
    ${status}    ${value}=    Run Keyword And Ignore Error    Element Should Be Visible    ${Mark_question_as_incomplete}
    Run Keyword If    '${status}' == 'PASS'    Click Element    ${Mark_question_as_incomplete}
    sleep    2s

Switch to the first browser
    Switch browser    1

Create new application
    Wait for autosave
    go to    ${CREATE_APPLICATION_PAGE}
    Input Text    id=application_name    Form test application
    Click Element    css=#content > form > input
    Page Should Not Contain    Page or resource not found
    Page Should Not Contain    You do not have the necessary permissions for your request

The user enters text to a text field
    [Arguments]    ${TEXT_FIELD}    ${TEXT_INPUT}
    Wait Until Element Is Visible    ${TEXT_FIELD}
    Clear Element Text    ${TEXT_FIELD}
    input text    ${TEXT_FIELD}    ${TEXT_INPUT}
    Mouse Out    ${TEXT_FIELD}

the user sees the text in the element
    [Arguments]    ${element}    ${text}
    wait until element is visible    ${element}
    Wait Until Keyword Succeeds    10    500ms    element should contain    ${element}    ${text}


the user sees the text in the text field
    [Arguments]    ${textfield}    ${text}
    wait until element is visible    ${textfield}
    wait until keyword succeeds    10    500ms    textfield should contain    ${textfield}    ${text}


the user clears the text from the element
    [Arguments]    ${element}
    wait until element is visible    ${element}
    clear element text    ${element}

    Page Should Not Contain    Error
    Page Should Not Contain    something went wrong
    Page Should Not Contain    Page or resource not found
    Page Should Not Contain    You do not have the necessary permissions for your request
    # Header checking    (INFUND-1892)
    Element Should Be Visible    id=global-header
    Page Should Contain    BETA

The user clicks the button/link
    [Arguments]    ${BUTTON}
    wait until element is visible    ${BUTTON}
    Focus    ${BUTTON}
    Wait for autosave
    click element    ${BUTTON}

The user should see the text in the page
    [Arguments]    ${VISIBLE_TEXT}
    wait until page contains    ${VISIBLE_TEXT}
    Page Should Not Contain    Error
    Page Should Not Contain    Page or resource not found
    Page Should Not Contain    You do not have the necessary permissions for your request
    Page Should Not Contain    something went wrong

The user should see permissions error message
    wait until page contains    You do not have the necessary permissions for your request
    Page Should Contain    You do not have the necessary permissions for your request


The user should not see the text in the page
    [Arguments]    ${NOT_VISIBLE_TEXT}
    sleep    100ms
    Wait Until Page Does Not Contain    ${NOT_VISIBLE_TEXT}

the user should not see an error in the page
    Page Should Not Contain    Error
    Page Should Not Contain    something went wrong
    Page Should Not Contain    Page or resource not found
    Page Should Not Contain    You do not have the necessary permissions for your request

The user should see an error
    [Arguments]    ${ERROR_TEXT}
    Run Keyword And Ignore Error    Mouse Out    css=input
    Run Keyword And Ignore Error    Focus    jQuery=Button:contains("Mark as complete")
    sleep    100ms
    wait until page contains element    css=.error-message
    Wait Until Page Contains    ${ERROR_TEXT}

the guest user enters the log in credentials
    [Arguments]    ${USER_NAME}    ${PASSWORD}
    Input Text    id=username    ${USER_NAME}
    Input Password    id=password    ${PASSWORD}

The user should see the element
    [Arguments]    ${ELEMENT}
    Wait Until Element Is Visible    ${ELEMENT}

The user should not see the element
    [Arguments]    ${NOT_VISIBLE_ELEMENT}
    sleep    500ms
    Element Should Not Be Visible    ${NOT_VISIBLE_ELEMENT}

The user should get an error page
    [Arguments]    ${ERROR_TEXT}
    wait until page contains element    css=.error
    wait until page contains    ${ERROR_TEXT}

The user should see the browser notification
    [Arguments]    ${MESSAGE}
    # Note - this keyword has been implemented to prevent failures on sauce labs
    # from different browsers not showing the notifications correctly
    Run keyword if    '${REMOTE_URL}' == ''    the user should see the notification    ${MESSAGE}

The user should see the notification
    [Arguments]    ${MESSAGE}
    Wait Until Element Is Visible    css=div.event-alert
    Wait Until Page Contains    ${MESSAGE}

The applicant assigns the question to the collaborator
    [Arguments]    ${TEXT_AREA}    ${TEXT}    ${NAME}
    focus    ${TEXT_AREA}
    The user enters text to a text field    ${TEXT_AREA}    ${TEXT}
    When the user clicks the button/link    css=.assign-button
    Then the user clicks the button/link    jQuery=button:contains("${NAME}")

the user assigns the question to the collaborator
    [Arguments]    ${name}
    Wait Until Element Is Not Visible    css=div.event-alert
    The user clicks the button/link    css=.assign-button
    The user clicks the button/link    jQuery=button:contains("${NAME}")
    Reload Page

The user goes back to the previous page
    Wait for autosave
    Go Back

browser validations have been disabled
    Execute Javascript    jQuery('form').attr('novalidate','novalidate');jQuery('[maxlength]').removeAttr('maxlength');

The user verifies their email
    [Arguments]    ${verify_link}
    Go To    ${verify_link}
    Page Should Contain    Account verified

the user can remove the uploaded file
    [Arguments]    ${file_name}
    Reload Page
    Click Button    name=remove_uploaded_file
    Wait Until Page Does Not Contain    Remove
    Page Should Contain    Upload
    Page Should Not Contain    ${file_name}

The element should be disabled
    [Arguments]    ${ELEMENT}
    Element Should Be Disabled    ${ELEMENT}

the user opens the mailbox and verifies the email from
    run keyword if    ${docker}==1    the user opens the mailbox and verifies the local email from
    run keyword if    ${docker}!=1    the user opens the mailbox and verifies the remote email from

the user opens the mailbox and verifies the remote email from
    Open Mailbox    server=imap.googlemail.com    user=${test_mailbox_one}@gmail.com    password=${test_mailbox_one_password}
    ${LATEST} =    wait for email
    ${HTML}=    get email body    ${LATEST}
    log    ${HTML}
    ${LINK}=    Get Links From Email    ${LATEST}
    log    ${LINK}
    ${VERIFY_EMAIL}=    Get From List    ${LINK}    1
    log    ${VERIFY_EMAIL}
    go to    ${VERIFY_EMAIL}
    Capture Page Screenshot
    Delete All Emails
    close mailbox

the user opens the mailbox and verifies the local email from
    Open Mailbox    server=ifs-local-dev    port=9876    user=smtp    password=smtp    is_secure=False
    ${LATEST} =    wait for email
    ${HTML}=    get email body    ${LATEST}
    log    ${HTML}
    ${LINK}=    Get Links From Email    ${LATEST}
    log    ${LINK}
    ${VERIFY_EMAIL}=    Get From List    ${LINK}    1
    log    ${VERIFY_EMAIL}
    go to    ${VERIFY_EMAIL}
    Capture Page Screenshot
    Delete All Emails
    close mailbox


the user opens the mailbox and verifies the email
    run keyword if    ${docker}==1    the user opens the local mailbox and verifies the email
    run keyword if    ${docker}!=1    the user opens the remote mailbox and verifies the email

the user opens the remote mailbox and verifies the email
    Open Mailbox    server=imap.googlemail.com    user=worth.email.test@gmail.com    password=testtest1
    ${LATEST} =    wait for email
    ${HTML}=    get email body    ${LATEST}
    log    ${HTML}
    ${LINK}=    Get Links From Email    ${LATEST}
    log    ${LINK}
    ${VERIFY_EMAIL}=    Get From List    ${LINK}    1
    log    ${VERIFY_EMAIL}
    go to    ${VERIFY_EMAIL}
    Capture Page Screenshot
    Delete All Emails
    close mailbox

the user opens the local mailbox and verifies the email
    Open Mailbox    server=ifs-local-dev    port=9876    user=smtp    password=smtp    is_secure=False
    ${LATEST} =    wait for email
    ${HTML}=    get email body    ${LATEST}
    log    ${HTML}
    ${LINK}=    Get Links From Email    ${LATEST}
    log    ${LINK}
    ${VERIFY_EMAIL}=    Get From List    ${LINK}    1
    log    ${VERIFY_EMAIL}
    go to    ${VERIFY_EMAIL}
    Capture Page Screenshot
    Delete All Emails
    close mailbox


the user opens the mailbox and accepts the invitation to collaborate
    run keyword if    ${docker}==1    the user opens the local mailbox and accepts the invitation to collaborate
    run keyword if    ${docker}!=1    the user opens the remote mailbox and accepts the invitation to collaborate


the user opens the remote mailbox and accepts the invitation to collaborate
    Open Mailbox    server=imap.googlemail.com    user=${test_mailbox_one}@gmail.com    password=${test_mailbox_one_password}
    ${LATEST} =    wait for email
    ${HTML}=    get email body    ${LATEST}
    log    ${HTML}
    ${LINK}=    Get Links From Email    ${LATEST}
    log    ${LINK}
    ${IFS_LINK}=    Get From List    ${LINK}    1
    log    ${IFS_LINK}
    go to    ${IFS_LINK}
    Capture Page Screenshot
    Delete All Emails
    close mailbox

the user opens the local mailbox and accepts the invitation to collaborate
    Open Mailbox    server=ifs-local-dev    port=9876    user=smtp    password=smtp    is_secure=False
    ${LATEST} =    wait for email
    ${HTML}=    get email body    ${LATEST}
    log    ${HTML}
    ${LINK}=    Get Links From Email    ${LATEST}
    log    ${LINK}
    ${IFS_LINK}=    Get From List    ${LINK}    1
    log    ${IFS_LINK}
    go to    ${IFS_LINK}
    Capture Page Screenshot
    Delete All Emails
    close mailbox


the user downloads the file from the link
    [Arguments]    ${filename}    ${download_link}
    ${ALL_COOKIES} =    Get Cookies
    Log    ${ALL_COOKIES}
    Download File    ${ALL_COOKIES}    ${download_link}    ${filename}
    wait until keyword succeeds    300ms    1 seconds    Download should be done


Download should be done
    [Documentation]    Verifies that the directory has only one folder
    ...    Returns path to the file
    ${files}    List Files In Directory    ${DOWNLOAD_FOLDER}
    Length Should Be    ${files}    1    Should be only one file in the download folder
    ${file}    Join Path    ${DOWNLOAD_FOLDER}    ${files[0]}
    Log    File was successfully downloaded to ${file}
    [Return]    ${file}

the file should be downloaded
    [Arguments]    ${filename}
    File Should Exist    ${filename}
    File Should Not Be Empty    ${filename}

the file has been scanned for viruses
    Sleep    5s

the user can see the option to upload a file on the page
    [Arguments]    ${url}
    The user navigates to the page    ${url}
    Page Should Contain    Upload

the user cannot see the option to upload a file on the page
    [Arguments]    ${url}
    The user navigates to the page    ${url}
    the user should not see the text in the page    Upload

Delete the emails from both test mailboxes
    run keyword if    ${docker}==1    delete the emails from the local test mailbox    # Note that all emails come through to the same local mailbox, so we only need to delete from one mailbox here
    run keyword if    ${docker}!=1    delete the emails from both remote test mailboxes


delete the emails from both remote test mailboxes
    Open Mailbox    server=imap.googlemail.com    user=${test_mailbox_one}@gmail.com    password=${test_mailbox_one_password}
    Delete All Emails
    close mailbox
    Open Mailbox    server=imap.googlemail.com    user=${test_mailbox_two}@gmail.com    password=${test_mailbox_two_password}
    Delete All Emails
    close mailbox

Delete the emails from the main test mailbox
    run keyword if    ${docker}==1    delete the emails from the local test mailbox
    run keyword if    ${docker}!=1    delete the emails from the main remote test mailbox


delete the emails from the main remote test mailbox
    Open Mailbox    server=imap.googlemail.com    user=worth.email.test@gmail.com    password=testtest1
    Delete All Emails
    close mailbox

delete the emails from the local test mailbox
    Open Mailbox    server=ifs-local-dev    port=9876    user=smtp    password=smtp    is_secure=False
    Delete All Emails
    close mailbox


Delete the emails from both main test mailboxes
    run keyword if    ${docker}==1    delete the emails from the local test mailbox    # Note that all emails come through to the same local mailbox, so we only need to delete from one mailbox here
    run keyword if    ${docker}!=1    delete the emails from both main remote test mailboxes


delete the emails from both main remote test mailboxes
    Open Mailbox    server=imap.googlemail.com    user=worth.email.test@gmail.com    password=testtest1
    Delete All Emails
    close mailbox
    Open Mailbox    server=imap.googlemail.com    user=worth.email.test.two@gmail.com    password=testtest1
    Delete All Emails
    close mailbox

the user should get a confirmation email
    [Arguments]    ${user}    ${password}    ${content}
    run keyword if    ${docker}==1    the user should get a local confirmation email     ${content}
    run keyword if    ${docker}!=1    the user should get a remote confirmation email    ${user}    ${password}    ${content}

the user should get a remote confirmation email
    [Arguments]    ${user}    ${password}    ${content}
    Open Mailbox    server=imap.googlemail.com    user=${user}    password=${password}
    ${LATEST} =    wait for email
    ${HTML}=    get email body    ${LATEST}
    log    ${HTML}
    ${MATCHES1}=    Get Matches From Email    ${LATEST}    ${content}
    log    ${MATCHES1}
    Should Not Be Empty    ${MATCHES1}
    Delete All Emails
    close mailbox

the user should get a local confirmation email
    [Arguments]    ${content}
    Open Mailbox    server=ifs-local-dev    port=9876    user=smtp    password=smtp    is_secure=False
    ${LATEST} =    wait for email
    ${HTML}=    get email body    ${LATEST}
    log    ${HTML}
    ${MATCHES1}=    Get Matches From Email    ${LATEST}    ${content}
    log    ${MATCHES1}
    Should Not Be Empty    ${MATCHES1}
    Delete All Emails
    close mailbox


the user enters the details and clicks the create account
    [Arguments]    ${REG_EMAIL}
    Wait Until Page Contains Element    link=terms and conditions
    Page Should Contain Element    xpath=//a[contains(@href, '/info/terms-and-conditions')]
    Input Text    id=firstName    Stuart
    Input Text    id=lastName    ANDERSON
    Input Text    id=phoneNumber    23232323
    Input Text    id=email    ${REG_EMAIL}
    Input Password    id=password    Passw0rd123
    Input Password    id=retypedPassword    Passw0rd123
    Select Checkbox    termsAndConditions
    Submit Form

the user fills the create account form
    [Arguments]    ${NAME}    ${LAST_NAME}
    Input Text    id=firstName    ${NAME}
    Input Text    id=lastName    ${LAST_NAME}
    Input Text    id=phoneNumber    0612121212
    Input Password    id=password    Passw0rd123
    Input Password    id=retypedPassword    Passw0rd123
    Select Checkbox    termsAndConditions
    Submit Form

the address fields should be filled
    # postcode lookup implemented on some machines but not others, so check which is running:
    Run Keyword If    '${POSTCODE_LOOKUP_IMPLEMENTED}' != 'NO'    the address fields should be filled with valid data
    Run Keyword If    '${POSTCODE_LOOKUP_IMPLEMENTED}' == 'NO'    the address fields should be filled with dummy data

the address fields should be filled with valid data
    Textfield Should Contain    id=addressForm.selectedPostcode.addressLine1    Am Reprographics
    Textfield Should Contain    id=addressForm.selectedPostcode.addressLine2    King William House
    Textfield Should Contain    id=addressForm.selectedPostcode.addressLine3    13 Queen Square
    Textfield Should Contain    id=addressForm.selectedPostcode.town    Bristol
    Textfield Should Contain    id=addressForm.selectedPostcode.county    City of Bristol
    Textfield Should Contain    id=addressForm.selectedPostcode.postcode    BS1 4NT

the address fields should be filled with dummy data
    Textfield Should Contain    id=addressForm.selectedPostcode.addressLine1    Montrose House 1
    Textfield Should Contain    id=addressForm.selectedPostcode.addressLine2    Clayhill Park
    Textfield Should Contain    id=addressForm.selectedPostcode.addressLine3    Cheshire West and Chester
    Textfield Should Contain    id=addressForm.selectedPostcode.town    Neston
    Textfield Should Contain    id=addressForm.selectedPostcode.county    Cheshire
    Textfield Should Contain    id=addressForm.selectedPostcode.postcode    CH64 3RU

the user cannot see a validation error in the page
    Element Should Not Be Visible    css=.error

the user submits their information
    Execute Javascript    jQuery('form').attr('novalidate','novalidate');
    Focus    name=termsAndConditions
    Select Checkbox    termsAndConditions
    Submit Form

the user logs out if they are logged in
    run keyword and ignore error    log out as user

the user cannot login with their new details
    [Arguments]    ${email}    ${password}
    The user navigates to the page    ${LOGIN_URL}
    Input Text    id=username    ${email}
    Input Password    id=password    ${password}
    Click Button    css=button[name="_eventId_proceed"]
    Page Should Contain    ${unsuccessful_login_message}
    Page Should Contain    Your username/password combination doesn't seem to work

the user cannot login with either password
    The user navigates to the page    ${LOGIN_URL}
    Input Text    id=username    ${valid_email}
    Input Password    id=password    ${correct_password}
    Click Button    css=button[name="_eventId_proceed"]
    Page Should Contain    ${unsuccessful_login_message}
    Page Should Contain    Your username/password combination doesn't seem to work
    go to    ${LOGIN_URL}
    Input Text    id=username    ${valid_email}
    Input Password    id=password    ${incorrect_password}
    Click Button    css=button[name="_eventId_proceed"]
    Page Should Contain    ${unsuccessful_login_message}
    Page Should Contain    Your username/password combination doesn't seem to work

we create a new user
    [Arguments]    ${EMAIL_INVITED}
    The user navigates to the page    ${COMPETITION_DETAILS_URL}
    The user clicks the button/link    jQuery=.column-third .button:contains("Apply now")
    The user clicks the button/link    jQuery=.button:contains("Create account")
    The user clicks the button/link    jQuery=.button:contains("Create")
    The user enters text to a text field    id=organisationSearchName    Innovate
    The user clicks the button/link    id=org-search
    The user clicks the button/link    LINK=INNOVATE LTD
    select Checkbox    address-same
    The user clicks the button/link    jQuery=.button:contains("Save organisation and continue")
    The user clicks the button/link    jQuery=.button:contains("Save")
    The user enters the details and clicks the create account    ${EMAIL_INVITED}
    The user should be redirected to the correct page    ${REGISTRATION_SUCCESS}
    And the user opens the mailbox and verifies the email from
    The user should be redirected to the correct page    ${REGISTRATION_VERIFIED}
    The user clicks the button/link    jQuery=.button:contains("Sign in")
    The guest user inserts user email & password    ${EMAIL_INVITED}    Passw0rd123
    The guest user clicks the log-in button
    the user closes the browser

the lead applicant invites a registered user
    [Arguments]    ${EMAIL_LEAD}    ${EMAIL_INVITED}
    run keyword if    ${smoke_test}!=1    invite a registered user    ${EMAIL_LEAD}    ${EMAIL_INVITED}
    run keyword if    ${smoke_test}==1    invite a new academic    ${EMAIL_LEAD}    ${EMAIL_INVITED}

invite a registered user
    [Arguments]    ${EMAIL_LEAD}    ${EMAIL_INVITED}
    the guest user opens the browser
    the user navigates to the page    ${COMPETITION_DETAILS_URL}
    the user clicks the button/link    jQuery=.column-third .button:contains("Apply now")
    the user clicks the button/link    jQuery=.button:contains("Create account")
    the user clicks the button/link    jQuery=.button:contains("Create")
    the user enters text to a text field    id=organisationSearchName    Innovate
    the user clicks the button/link    id=org-search
    the user clicks the button/link    LINK=INNOVATE LTD
    the user selects the checkbox    address-same
    the user clicks the button/link    jQuery=.button:contains("Save organisation and continue")
    the user clicks the button/link    jQuery=.button:contains("Save")
    the user enters the details and clicks the create account    ${EMAIL_LEAD}
    the user should be redirected to the correct page    ${REGISTRATION_SUCCESS}
    the user opens the mailbox and verifies the email from
    the user should be redirected to the correct page    ${REGISTRATION_VERIFIED}
    the user clicks the button/link    jQuery=.button:contains("Sign in")
    the guest user inserts user email & password    ${EMAIL_LEAD}    Passw0rd123
    the guest user clicks the log-in button
    the user clicks the button/link    link=${OPEN_COMPETITION_LINK}
    the user clicks the button/link    jquery=li:nth-last-child(1) button:contains('Add additional partner organisation')
    Input Text    name=organisations[1].organisationName    innovate
    Input Text    name=organisations[1].invites[0].personName    Partner name
    Input Text    css=li:nth-last-child(2) tr:nth-of-type(1) td:nth-of-type(2) input    ${EMAIL_INVITED}
    the user clicks the button/link    jQuery=.button:contains("Begin application")
    the user should see the text in the page    Application overview
    the user closes the browser
    the guest user opens the browser

invite a new academic
    [Arguments]    ${EMAIL_LEAD}    ${EMAIL_INVITED}
    guest user log-in    ${EMAIL_LEAD}    Passw0rd123
    the user clicks the button/link    link=${application_name}
    the user clicks the button/link    link=View team members and add collaborators
    the user clicks the button/link    jQuery=.button:contains("Invite new contributors")
    the user clicks the button/link    jQuery=.button:contains("Add additional partner organisation")
    the user enters text to a text field    name=organisations[1].organisationName    university of liverpool
    the user enters text to a text field    name=organisations[1].invites[0].personName    Academic User
    the user enters text to a text field    css=li:nth-last-child(2) tr:nth-of-type(1) td:nth-of-type(2) input    ${EMAIL_INVITED}
    the user clicks the button/link    jQuery=.button:contains("Save Changes")

Open mailbox and confirm received email
    [Arguments]    ${USER}    ${PASSWORD}    ${PATTERN}
    run keyword if    ${docker}==1    open local mailbox and confirm received email    ${PATTERN}
    run keyword if    ${docker}!=1    open remote mailbox and confirm received email    ${USER}    ${PASSWORD}    ${PATTERN}



open remote mailbox and confirm received email
    [Arguments]    ${USER}    ${PASSWORD}    ${PATTERN}
    [Documentation]    This Keyword searches the correct email using regex
    Open Mailbox    server=imap.googlemail.com    user=${USER}    password=${PASSWORD}
    ${WHICH_EMAIL}=    wait for email    toEmail=${USER}    timeout=150
    ${EMAIL_MATCH}=    Get Matches From Email    ${WHICH_EMAIL}    ${PATTERN}
    Should Not Be Empty    ${EMAIL_MATCH}
    Delete All Emails
    close mailbox


open local mailbox and confirm received email
    [Arguments]    ${PATTERN}
    [Documentation]    This Keyword searches the correct email using regex
    Open Mailbox    server=ifs-local-dev    port=9876    user=smtp    password=smtp    is_secure=False
    ${WHICH_EMAIL}=    wait for email    timeout=150
    ${EMAIL_MATCH}=    Get Matches From Email    ${WHICH_EMAIL}    ${PATTERN}
    Should Not Be Empty    ${EMAIL_MATCH}
    close mailbox