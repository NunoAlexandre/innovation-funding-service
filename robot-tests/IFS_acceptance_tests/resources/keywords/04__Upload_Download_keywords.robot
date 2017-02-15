*** Settings ***
Resource          ../defaultResources.robot

*** Keywords ***
The user downloads the file
    [Documentation]    Makes use of a download script that logs in, grabs a cookie and downloads
    ...     the file all in one package
    [Arguments]    ${user}    ${url}    ${filename}
    Run and Return RC    ./download.py ${user} ${short_password} ${url} ${filename}
    Wait Until Keyword Succeeds Without Screenshots    30s    200ms    Download should be done


Download should be done
    [Documentation]    Verifies that the directory has only one file
    ...    Returns path to the file
    ${files}    List Files In Directory    ${DOWNLOAD_FOLDER}
    Length Should Be    ${files}    2    Should be only one file in the download folder
    ${file}    Join Path    ${DOWNLOAD_FOLDER}    ${files[0]}
    Log    File was successfully downloaded to ${file}
    [Return]    ${file}

the file should be downloaded
    [Arguments]    ${filename}
    File Should Exist    ${filename}
    File Should Not Be Empty    ${filename}

remove the file from the operating system
   [Arguments]    ${filename}
    remove file    ${download_folder}/${filename}

the file has been scanned for viruses
    Sleep    5s    # this sleep statement is necessary as we wait for the antivirus scanner to work. Please do not remove during refactoring!

the user cannot see the option to upload a file on the page
    [Arguments]    ${url}
    The user navigates to the page    ${url}
    the user should not see the text in the page    Upload

the user can see the option to upload a file on the page
    [Arguments]    ${url}
    The user navigates to the page    ${url}
    Page Should Contain    Upload

the user can remove the uploaded file
    [Arguments]  ${name}  ${file_name}
    Reload Page
    Click Button    name=${name}
    Wait Until Page Does Not Contain Without Screenshots    Remove
    Page Should Contain    Upload
    Page Should Not Contain    ${file_name}
