*** Settings ***
Documentation     INFUND-4851 As a project manager I want to be able to submit an uploaded Grant Offer Letter so that Innovate UK can review my signed copy
...
...               INFUND-6059 As the contracts team I want to be able to send a Grant Offer Letter to the partners so that the project can begin
...
...               INFUND-4849 As a partner I want to be able to download a Grant Offer Letter and Appendices
...
...               INFUND-6091 As a partner / lead partner / project manager I want to get access to the GOL section in Project Setup when all other sections
...
...               INFUND-6377 As a member of the Competitions team I want to be able to select when the signed Grant Offer Letter has been approved so that Innovate UK can notify the Project Manager
...
...               INFUND-5998 As the contracts team I need to view, remove and/or re-upload the Grant Offer Letter
...
...               INFUND-6829 GOL uploaded but not submitted by PM shows wrong status
...
...               INFUND-7027 Partners can access the GOL before the internal user hits Send to proj team
...
...               INFUND-7049 Validation missing for PDF file upload in GOL upload page for internal user
...
...               INFUND-6375 As a partner I want to receive a notification when Project Setup has been successfully completed so that I am clear on what steps to take now the project is live
...
...               INFUND-6741 As the service delivery manager I want the service to generate a Grant Offer Letter once both the Spend Profiles and Other documents are approved so that the competitions team can review and publish to the project team
...
...               INFUND-7361 GOL is seen by internal user soon after the external user uploads it
...
...               INFUND-6048 As the contracts team I can have access to a generated Grant Offer Letter so that I can send it to the partners
...
...               INFUND-7170 Approved signed-GOL cannot be seen/downloaded by external users
...
...               INFUND-6780 As a project manager, I have the option to remove an uploaded signed GOL before submitting it, so that an can upload a different file if required
...
...               IFS-1577 Allow change of Project address until generation of GOL
...
...               IFS-1578 Allow change of Project Manager until generation of GOL
Suite Setup       all the other sections of the project are completed (except spend profile approval)
Suite Teardown    Close browser and delete emails
Force Tags        Project Setup    Upload
Resource          PS_Common.robot

*** Test Cases ***


External user cannot view the GOL section before spend profiles have been approved
    [Documentation]    INFUND-6741
    [Tags]
    [Setup]    log in as a different user    ${PS_GOL_APPLICATION_PM_EMAIL}  ${short_password}
    Given the user navigates to the page             ${server}/project-setup/project/${PS_GOL_APPLICATION_PROJECT}
    When the user should not see the element    css=li.waiting:nth-child(8)
    And the user should not see the element    link=Grant offer letter
    When the user clicks the button/link    link=status of my partners
    Then the user should see the element    css=#table-project-status tr:nth-of-type(2) td.status.na:nth-of-type(7)

GOL not generated before spend profiles have been approved
    [Documentation]    INFUND-6741
    [Tags]    HappyPath
    [Setup]    log in as a different user    &{Comp_admin1_credentials}
    When the user navigates to the page    ${server}/project-setup-management/competition/${PS_GOL_Competition_Id}/status
    Then the user should not see the element    css=#table-project-status tr:nth-of-type(7) td:nth-of-type(7).status.action
    And the user navigates to the page and gets a custom error message    ${server}/project-setup-management/project/${PS_GOL_APPLICATION_PROJECT}/grant-offer-letter/send    ${403_error_message}
    [Teardown]    proj finance approves the spend profiles  ${PS_GOL_APPLICATION_PROJECT}

Status updates correctly for internal user's table
    [Documentation]    INFUND-4049 ,INFUND-5543
    [Tags]    Experian
    [Setup]    log in as a different user   &{Comp_admin1_credentials}
    When the user navigates to the page     ${server}/project-setup-management/competition/${PS_GOL_Competition_Id}/status
    Then the user should see the element    css=#table-project-status tr:nth-of-type(7) td:nth-of-type(1).status.ok       # Project details
    And the user should see the element     css=#table-project-status tr:nth-of-type(7) td:nth-of-type(2).status.ok       # MO
    And the user should see the element     css=#table-project-status tr:nth-of-type(7) td:nth-of-type(3).status.ok       # Bank details
    And the user should see the element     css=#table-project-status tr:nth-of-type(7) td:nth-of-type(4).status.ok       # Finance checks
    And the user should see the element     css=#table-project-status tr:nth-of-type(7) td:nth-of-type(5).status.ok       # Spend Profile
    And the user should see the element     css=#table-project-status tr:nth-of-type(7) td:nth-of-type(6).status.ok       # Other Docs
    And the user should see the element     css=#table-project-status tr:nth-of-type(7) td:nth-of-type(7).status.action   # GOL

IFS Admin user selects the grant offer letter
    [Documentation]  INFUND-6377, INFUND-6048, IFS-603
    [Tags]  HappyPath
    [Setup]  log in as a different user     &{ifs_admin_user_credentials}
    Given the user navigates to the page    ${server}/project-setup-management/competition/${PS_GOL_Competition_Id}/status
    When the user clicks the button/link    css=#table-project-status tr:nth-of-type(7) td:nth-of-type(7).status.action a
    Then the user navigates to the page     ${server}/project-setup-management/project/${PS_GOL_APPLICATION_PROJECT}/grant-offer-letter/send
    And the user should see the element     jQuery=h2:contains("Grant offer letter")
    And the user opens the link in new window   grant_offer_letter.pdf
    And the user should see the element     jQuery=button:contains("Remove")

Project Finance can download GOL
    [Documentation]  INFUND-6377
    [Tags]  HappyPath    Download
    [Setup]  log in as a different user     &{internal_finance_credentials}
    Given the user navigates to the page    ${server}/project-setup-management/project/${PS_GOL_APPLICATION_PROJECT}/grant-offer-letter/send
    Then the user downloads the file        ${internal_finance_credentials["email"]}  ${server}/project-setup-management/project/${PS_GOL_APPLICATION_PROJECT}/grant-offer-letter/grant-offer-letter  ${DOWNLOAD_FOLDER}/grant_offer_letter.pdf
    [Teardown]    remove the file from the operating system    grant_offer_letter.pdf

Lead should not be able to see GOL until it is sent by IUK
    [Documentation]  INFUND-7027
    [Tags]
    [Setup]    log in as a different user            ${PS_GOL_APPLICATION_PM_EMAIL}  ${short_password}
    Given the user navigates to the page             ${server}/project-setup/project/${PS_GOL_APPLICATION_PROJECT}
    And the user should see the element              css=li.waiting:nth-child(8)
    When the user clicks the button/link             link=status of my partners
    Then the user should see the text in the page    Project team status
    And the user should see the element              css=#table-project-status tr:nth-of-type(1) td.status.waiting:nth-of-type(7)
    When the user clicks the button/link             link=Project setup status
    Then the user should not see the element         link=Grant offer letter

Lead cannot change project manager and project address after GOL generation
    [Documentation]  INFUND-1577, IFS-1578
    [Tags]
    Given the user navigates to the page and gets a custom error message  ${server}/project-setup/project/${PS_GOL_APPLICATION_PROJECT}/details/project-manager  ${403_error_message}
    When the user navigates to the page and gets a custom error message  ${server}/project-setup/project/${PS_GOL_APPLICATION_PROJECT}/details/project-address  ${403_error_message}

Non lead should not be able to see GOL until it is sent by IUK
    [Documentation]  INFUND-7027
    [Tags]
    [Setup]    log in as a different user            ${PS_GOL_APPLICATION_PARTNER_EMAIL}  ${short_password}
    Given the user navigates to the page             ${server}/project-setup/project/${PS_GOL_APPLICATION_PROJECT}
    Then the user should not see the element         css=li.complete:nth-child(8)
    And the user should not see the element          css=li.require-action:nth-child(8)
    When the user clicks the button/link             link=status of my partners
    Then the user should see the text in the page    Project team status
    And the user should see the element              css=#table-project-status tr:nth-of-type(2) td.status.na:nth-of-type(7)
    When the user clicks the button/link             link=Project setup status
    Then the user should not see the element         link=Grant offer letter

Project finance user removes the grant offer letter
    [Documentation]    INFUND-6377, INFUND-5988
    [Tags]    HappyPath
    [Setup]  log in as a different user     &{internal_finance_credentials}
    Given the user navigates to the page    ${server}/project-setup-management/project/${PS_GOL_APPLICATION_PROJECT}/grant-offer-letter/send
    Then the user can remove the uploaded file  removeGrantOfferLetterClicked  grant_offer_letter.pdf
    And the user should see the element         css=label[for="grantOfferLetter"]

Comp Admin cannot upload big or non-pdf grant offer letter
    [Documentation]  INFUND-7049
    [Tags]
    [Setup]  log in as a different user    &{Comp_admin1_credentials}
    Given the user navigates to the page   ${server}/project-setup-management/project/${PS_GOL_APPLICATION_PROJECT}/grant-offer-letter/send
    When the user uploads a file           grantOfferLetter  ${too_large_pdf}
    Then the user should see the text in the page    ${too_large_pdf_validation_error}
    When the user navigates to the page   ${server}/project-setup-management/project/${PS_GOL_APPLICATION_PROJECT}/grant-offer-letter/send
    And the user uploads a file           grantOfferLetter  ${text_file}
    Then the user should see the text in the page    ${wrong_filetype_validation_error}


Comp Admin user uploads new grant offer letter
    [Documentation]    INFUND-6377, INFUND-5988
    [Tags]    HappyPath
    [Setup]  log in as a different user    &{Comp_admin1_credentials}
    Given the user navigates to the page   ${server}/project-setup-management/project/${PS_GOL_APPLICATION_PROJECT}/grant-offer-letter/send
    Then the user uploads a file           grantOfferLetter  ${valid_pdf}
    And the user should see the element    jQuery=button:contains("Remove")
    When the user uploads a file           annex  ${valid_pdf}
    And the user clicks the button/link    id=send-gol
    And the user clicks the button/link    jQuery=.modal-accept-send-gol .button:contains("Publish to project team")
    Then the user should not see the element  jQuery=.button:contains("Publish to project team")
    And the user should not see the element   jQuery=button:contains("Remove")
    When the user navigates to the page      ${server}/project-setup-management/competition/${PS_GOL_Competition_Id}/status
    Then the user should see the element     css=#table-project-status tr:nth-of-type(7) td:nth-of-type(7).status.waiting   # GOL
    And the user reads his email             ${PS_GOL_APPLICATION_LEAD_PARTNER_EMAIL}  Your grant offer letter is available  We are pleased to inform you that your grant offer letter is now ready for you to sign

PM can view the grant offer letter page
    [Documentation]    INFUND-4848, INFUND-6091
    [Tags]    HappyPath
    [Setup]    log in as a different user    ${PS_GOL_APPLICATION_PM_EMAIL}  ${short_password}
    Given the user clicks the button/link    link=${PS_GOL_APPLICATION_HEADER}
    Then the user should see the element     css=li.require-action:last-of-type
    When the user clicks the button/link     link=Grant offer letter
    Then the user should see the text in the page    The grant offer letter has been provided by Innovate UK.
    And the user should see the element    jQuery=label:contains(+ Upload)
    And the user goes back to the previous page
    When the user clicks the button/link    link=status of my partners
    Then the user should see the text in the page    Project team status
    And the user should see the element     css=#table-project-status tr:nth-of-type(1) td.status.action:nth-of-type(7)

Partners should not be able to send the Grant Offer
    [Documentation]    INFUND-4851, INFUND-6133
    [Tags]
    [Setup]    log in as a different user    ${PS_GOL_APPLICATION_PARTNER_EMAIL}    ${short_password}
    Given the user clicks the button/link    link=${PS_GOL_APPLICATION_HEADER}
    And the user clicks the button/link    link=Grant offer letter
    Then the user should not see the element    jQuery=label:contains(+ Upload)
    And the user should not see the element    jQuery=.button:contains("Send to Innovate UK")

Links to other sections in Project setup dependent on project details (applicable for Lead/ partner)
    [Documentation]    INFUND-4428
    [Tags]    HappyPath
    [Setup]    Log in as a different user    ${PS_GOL_APPLICATION_PARTNER_EMAIL}  ${short_password}
    Given the user clicks the button/link    link=${PS_GOL_APPLICATION_HEADER}
    Then the user should see the element     css=ul li.complete:nth-child(1)
    And the user should see the text in the page    Successful application
    Then the user should see the element    link = Monitoring Officer
    And the user should see the element    link = Bank details
    And the user should see the element    link = Finance checks
    And the user should see the element    link= Spend profile
    And the user should see the element    link = Grant offer letter

PM should not be able to upload big Grant Offer files
    [Documentation]    INFUND-4851, INFUND-4972
    [Tags]
    [Setup]    log in as a different user    ${PS_GOL_APPLICATION_PM_EMAIL}  ${short_password}
    Given the user clicks the button/link    link=${PS_GOL_APPLICATION_HEADER}
    And the user clicks the button/link    link=Grant offer letter
    When the user uploads a file             signedGrantOfferLetter    ${too_large_pdf}
    Then the user should see the text in the page    ${too_large_pdf_validation_error}
    And the user goes back to the previous page

PM should be able upload a file and then access the Send button
    [Documentation]    INFUND-4851, INFUND-4972, INFUND-6829
    [Tags]    HappyPath
    [Setup]    log in as a different user    ${PS_GOL_APPLICATION_PM_EMAIL}  ${short_password}
    Given the user clicks the button/link    link=${PS_GOL_APPLICATION_HEADER}
    And the user clicks the button/link    link=Grant offer letter
    When the user uploads a file             signedGrantOfferLetter   ${valid_pdf}
    Then the user should see the text in the page    ${valid_pdf}
    When the user reloads the page
    Then the user should see the element    jQuery=.button:contains("Send to Innovate UK")
    And the user clicks the button/link    link=Project setup status
    And the user should see the element    css=li.require-action:nth-child(8)
    When the user clicks the button/link    link=status of my partners
    Then the user should see the text in the page    Project team status
    And the user should see the element     css=#table-project-status tr:nth-of-type(1) td.status.action:nth-of-type(7)

Project finance cannot access the GOL before it is sent by PM
    [Documentation]    INFUND-7361
    [Tags]    HappyPath
    [Setup]  log in as a different user     &{internal_finance_credentials}
    Given the user navigates to the page    ${server}/project-setup-management/project/${PS_GOL_APPLICATION_PROJECT}/grant-offer-letter/send
    Then the user should see the text in the page  Awaiting upload by the Project Manager

PM can view the generated Grant Offer Letter
    [Documentation]    INFUND-6059, INFUND-4849
    [Tags]    HappyPath
    [Setup]    log in as a different user    ${PS_GOL_APPLICATION_PM_EMAIL}  ${short_password}
    Given the user navigates to the page  ${server}/project-setup/project/${PS_GOL_APPLICATION_PROJECT}/
    Then the user should see the element  css=ul li.require-action:nth-child(8)
    When the user clicks the button/link  link=Grant offer letter
    Then the user should see the element  jQuery=h2:contains("Grant offer letter")

PM can download the grant offer letter
    [Documentation]    INFUND-5998
    [Tags]  HappyPath    Download
    Given the user navigates to the page    ${server}/project-setup/project/${PS_GOL_APPLICATION_PROJECT}/offer
    Then the user downloads the file        ${PS_GOL_APPLICATION_PM_EMAIL}    ${server}/project-setup/project/${PS_GOL_APPLICATION_PROJECT}/offer/grant-offer-letter  ${DOWNLOAD_FOLDER}/grant_offer_letter.pdf
    [Teardown]    remove the file from the operating system    grant_offer_letter.pdf


Other external users can see the uploaded Grant Offer letter
    [Documentation]    INFUND-6059
    [Tags]    HappyPath
    Given log in as a different user      ${PS_GOL_APPLICATION_PARTNER_EMAIL}  ${short_password}
    And the user navigates to the page    ${server}/project-setup/project/${PS_GOL_APPLICATION_PROJECT}/
    Then the user should see the element  css=ul li.waiting:nth-child(8)
    When the user clicks the button/link  link=Grant offer letter
    Then the user should see the element  jQuery=h2:contains("Grant offer letter")

Non lead partner can download the grant offer letter
    [Documentation]    INFUND-5998
    [Tags]  HappyPath    Download
    Given the user navigates to the page    ${server}/project-setup/project/${PS_GOL_APPLICATION_PROJECT}/offer
    Then the user downloads the file        ${PS_GOL_APPLICATION_PARTNER_EMAIL}    ${server}/project-setup/project/${PS_GOL_APPLICATION_PROJECT}/offer/grant-offer-letter  ${DOWNLOAD_FOLDER}/grant_offer_letter.pdf
    [Teardown]    remove the file from the operating system    grant_offer_letter.pdf

Non lead partner can download the annex
    [Documentation]    INFUND-5998
    [Tags]  HappyPath    Download
    Given the user navigates to the page    ${server}/project-setup/project/${PS_GOL_APPLICATION_PROJECT}/offer
    Then the user downloads the file        ${PS_GOL_APPLICATION_PARTNER_EMAIL}    ${server}/project-setup/project/${PS_GOL_APPLICATION_PROJECT}/offer/additional-contract  ${DOWNLOAD_FOLDER}/annex.pdf
    [Teardown]    remove the file from the operating system    annex.pdf

Academic users can see the uploaded Grant Offer letter
    [Documentation]    INFUND-5998
    [Tags]    HappyPath
    Given log in as a different user      ${PS_GOL_APPLICATION_ACADEMIC_EMAIL}  ${short_password}
    And the user navigates to the page    ${server}/project-setup/project/${PS_GOL_APPLICATION_PROJECT}/
    Then the user should see the element  css=ul li.waiting:nth-child(8)
    When the user clicks the button/link  link=Grant offer letter
    Then the user should see the element  jQuery=h2:contains("Grant offer letter")

Academic partner can download the grant offer letter
    [Documentation]    INFUND-5998
    [Tags]  HappyPath    Download
    Given the user navigates to the page    ${server}/project-setup/project/${PS_GOL_APPLICATION_PROJECT}/offer
    Then the user downloads the file        ${PS_GOL_APPLICATION_ACADEMIC_EMAIL}    ${server}/project-setup/project/${PS_GOL_APPLICATION_PROJECT}/offer/grant-offer-letter  ${DOWNLOAD_FOLDER}/grant_offer_letter.pdf
    [Teardown]    remove the file from the operating system    grant_offer_letter.pdf

Academic partner can download the annex
    [Documentation]    INFUND-5998
    [Tags]  HappyPath    Download
    Given the user navigates to the page    ${server}/project-setup/project/${PS_GOL_APPLICATION_PROJECT}/offer
    Then the user downloads the file        ${PS_GOL_APPLICATION_ACADEMIC_EMAIL}    ${server}/project-setup/project/${PS_GOL_APPLICATION_PROJECT}/offer/additional-contract  ${DOWNLOAD_FOLDER}/annex.pdf
    [Teardown]    remove the file from the operating system    annex.pdf

PM can view the uploaded Annex file
    [Documentation]    INFUND-4851, INFUND-4849
    [Tags]    HappyPath
    [Setup]    log in as a different user    ${PS_GOL_APPLICATION_PM_EMAIL}  ${short_password}
    Given the user navigates to the page     ${server}/project-setup/project/${PS_GOL_APPLICATION_PROJECT}/offer
    When the user opens the link in new window   ${valid_pdf}
    Then the user goes back to the previous tab

PM can download the annex
    [Documentation]    INFUND-5998
    [Tags]  HappyPath    Download
    Given the user navigates to the page    ${server}/project-setup/project/${PS_GOL_APPLICATION_PROJECT}/offer
    Then the user downloads the file        ${PS_GOL_APPLICATION_PM_EMAIL}    ${server}/project-setup/project/${PS_GOL_APPLICATION_PROJECT}/offer/additional-contract  ${DOWNLOAD_FOLDER}/annex.pdf
    [Teardown]    remove the file from the operating system    annex.pdf

PM can remove the signed grant offer letter
    [Documentation]    INFUND-6780
    [Tags]
    When the user clicks the button/link    name=removeSignedGrantOfferLetterClicked
    Then the user should not see the text in the page    Remove
    And the user should not see the text in the page    jQuery=.upload-section a:contains("${valid_pdf}")


PM can upload new signed grant offer letter
    [Documentation]    INFUND-6780
    [Tags]
    When the user uploads a file    signedGrantOfferLetter    ${valid_pdf}
    And the user reloads the page
    Then the user should see the element    jQuery=.button:contains("Send to Innovate UK")
    And the user should not see the element    jQuery=[disabled='disabled'].button:contains(Send signed offer letter)


PM Sends the Grant Offer letter
    [Documentation]    INFUND-4851, INFUND-6091, INFUND-5998
    [Tags]    HappyPath
    When the user clicks the button/link    jQuery=.button:contains("Send to Innovate UK")
    Then the user clicks the button/link    jQuery=button:contains("Send to Innovate UK")
    And the user should not see an error in the page
    When the user navigates to the page     ${server}/project-setup/project/${PS_GOL_APPLICATION_PROJECT}
    Then the user should see the element    css=li.waiting:nth-child(8)

PM can download the signed grant offer letter
    [Documentation]    INFUND-7170
    [Tags]  HappyPath    Download
    Given the user navigates to the page    ${server}/project-setup/project/${PS_GOL_APPLICATION_PROJECT}/offer
    Then the user should see the text in the page   Signed grant offer letter
    And the user downloads the file        ${PS_GOL_APPLICATION_PM_EMAIL}    ${server}/project-setup/project/${PS_GOL_APPLICATION_PROJECT}/offer/signed-grant-offer-letter  ${DOWNLOAD_FOLDER}/signedGOL.pdf
    [Teardown]    remove the file from the operating system    signedGOL.pdf

PM cannot remove the signed grant offer letter after submission
    [Documentation]    INFUND-6780
    When the user should not see the element    name=removeSignedGrantOfferLetterClicked
    Then the user should not see the text in the page    Remove


PM's status should be updated
    [Documentation]    INFUND-4851, INFUND-6091, INFUND-5998
    [Tags]    HappyPath
    Given the user navigates to the page   ${server}/project-setup/project/${PS_GOL_APPLICATION_PROJECT}
    And the user clicks the button/link    link=status of my partners
    Then the user should see the text in the page    Project team status
    And the user should see the element     css=#table-project-status tr:nth-of-type(1) td.status.waiting:nth-of-type(7)

Internal Dashboard should be updated
    [Documentation]    INFUND-4851, INFUND-6091, INFUND-5998
    [Tags]    HappyPath
    [Setup]    log in as a different user    &{Comp_admin1_credentials}
    When the user navigates to the page      ${server}/project-setup-management/competition/${PS_GOL_Competition_Id}/status
    Then the user should see the element     css=#table-project-status tr:nth-of-type(7) td:nth-of-type(7).status.action

Internal user can download the signed GOL
    [Documentation]    INFUND-6377
    [Tags]  Download
    Given the user navigates to the page  ${server}/project-setup-management/project/${PS_GOL_APPLICATION_PROJECT}/grant-offer-letter/send
    Then the user should see the element  jQuery=h2:contains("Signed grant offer letter") + .upload-section a
    And the user downloads the file  ${Comp_admin1_credentials["email"]}  ${server}/project-setup-management/project/${PS_GOL_APPLICATION_PROJECT}/grant-offer-letter/signed-grant-offer-letter  ${DOWNLOAD_FOLDER}/testing.pdf
    [Teardown]    remove the file from the operating system  testing.pdf

Comp Admin can accept the signed grant offer letter
    [Documentation]  INFUND-6377
    [Tags]
    [Setup]  the user navigates to the page  ${server}/project-setup-management/competition/${PS_GOL_Competition_Id}/status
    Given the user clicks the button/link    css=#table-project-status tr:nth-of-type(7) td:nth-of-type(7).status.action a
    Then the user navigates to the page      ${server}/project-setup-management/project/${PS_GOL_APPLICATION_PROJECT}/grant-offer-letter/send
    And the user should see the element      jQuery=#content .button:contains("Accept signed grant offer letter")
    When the user clicks the button/link     jQuery=#content .button:contains("Accept signed grant offer letter")
    Then the user should see the element     jQuery=h2:contains("Accept signed grant offer letter")
    When the user clicks the button/link     jQuery=.modal-accept-signed-gol button:contains("Cancel")
    Then the user should not see an error in the page

Internal user accepts signed grant offer letter
    [Documentation]    INFUND-5998, INFUND-6377
    [Tags]    HappyPath
    [Setup]    log in as a different user    &{internal_finance_credentials}
    Given the user navigates to the page      ${server}/project-setup-management/competition/${PS_GOL_Competition_Id}/status
    When the user clicks the button/link     jQuery=#table-project-status tr:nth-of-type(7) td:nth-of-type(7).status.action a:contains("Review")
    Then the user should not see the text in the page  "Confirm receipt of signed grant offer letter"
    And the user clicks the button/link    jQuery=#content .button:contains("Accept signed grant offer letter")
    And the user clicks the button/link     jQuery=.modal-accept-signed-gol .button:contains("Accept signed grant offer letter")
    Then the user should see the element    jQuery=.success-alert h2:contains("The grant offer letter has been received and accepted.")
    And the user should not see the element     jQuery=#content .button:contains("Accept signed grant offer letter")
    When the user navigates to the page       ${server}/project-setup-management/competition/${PS_GOL_Competition_Id}/status
    Then the user should see the element  css=#table-project-status tr:nth-of-type(7) td:nth-of-type(7).status.ok

Project manager's status should be updated
    [Documentation]   INFUND-5998, INFUND-6377
    [Tags]    HappyPath
    [Setup]    log in as a different user    ${PS_GOL_APPLICATION_PM_EMAIL}  ${short_password}
    Given the user navigates to the page  ${server}/project-setup/project/${PS_GOL_APPLICATION_PROJECT}
    And the user should see the element    css=li.complete:nth-child(8)
    And the user should see the element      link=_connect

Non lead's status should be updated
    [Documentation]   INFUND-5998, INFUND-6377
    [Tags]
    [Setup]    log in as a different user    ${PS_GOL_APPLICATION_PARTNER_EMAIL}  ${short_password}
    Given the user navigates to the page  ${server}/project-setup/project/${PS_GOL_APPLICATION_PROJECT}
    And the user should see the element    css=li.complete:nth-child(8)
    And the user should see the element      link=_connect

Non lead can see the GOL approved
    [Documentation]  INFUND-6377
    [Tags]
    Given the user navigates to the page  ${server}/project-setup/project/${PS_GOL_APPLICATION_PROJECT}/offer
    Then the user should see the element  jQuery=.grant-offer-download:contains("testing.pdf")
    And the user should see the element   jQuery=.success-alert p:contains("Your signed grant offer letter has been received and accepted by Innovate UK")

Non lead can download the GOL
    [Documentation]  INFUND-6377
    [Tags]  Download
    Given the user navigates to the page  ${server}/project-setup/project/${PS_GOL_APPLICATION_PROJECT}/offer
    Then the user downloads the file      ${PS_GOL_APPLICATION_PARTNER_EMAIL}  ${server}/project-setup/project/${PS_GOL_APPLICATION_PROJECT}/offer/grant-offer-letter  ${DOWNLOAD_FOLDER}/testing.pdf
    [Teardown]    remove the file from the operating system    testing.pdf

Non lead cannot see the signed GOL
    [Documentation]    INFUND-7170
    [Tags]
    Given the user navigates to the page    ${server}/project-setup/project/${PS_GOL_APPLICATION_PROJECT}/offer
    Then the user should not see the text in the page   Signed grant offer letter
    When the user navigates to the page and gets a custom error message    ${server}/project-setup/project/${PS_GOL_APPLICATION_PROJECT}/offer/signed-grant-offer-letter    ${403_error_message}

PM receives an email when the GOL is approved
    [Documentation]    INFUND-6375
    [Tags]    Email    HappyPath
    Then the user reads his email    ${PS_GOL_APPLICATION_PM_EMAIL}    Grant offer letter approval    Innovate UK has reviewed and accepted the signed grant offer letter which was uploaded for your project.

Lead finance contact receives an email when the GOL is approved
    [Documentation]    INFUND-6375
    [Tags]    Email    HappyPath
    Then the user reads his email    ${PS_GOL_APPLICATION_FINANCE_CONTACT_EMAIL}    Grant offer letter approval    Innovate UK has reviewed and accepted the signed grant offer letter which was uploaded for your project.


Industrial finance contact receives an email when the GOL is approved
    [Documentation]    INFUND-6375
    [Tags]    Email    HappyPath
    Then the user reads his email    ${PS_GOL_APPLICATION_PARTNER_EMAIL}    Grant offer letter approval    Innovate UK has reviewed and accepted the signed grant offer letter which was uploaded for your project.

Academic finance contact receives an email when the GOL is approved
    [Documentation]    INFUND-6375
    [Tags]    Email    HappyPath
    Then the user reads his email    ${PS_GOL_APPLICATION_ACADEMIC_EMAIL}    Grant offer letter approval    Innovate UK has reviewed and accepted the signed grant offer letter which was uploaded for your project.


*** Keywords ***
the user uploads a file
    [Arguments]  ${name}  ${file}
    choose file    name=${name}    ${upload_folder}/${file}

all the other sections of the project are completed (except spend profile approval)
    the user logs-in in new browser              &{internal_finance_credentials}
    project finance approves bank details for ${PS_GOL_APPLICATION_TITLE}
    project manager submits other documents      ${PS_GOL_APPLICATION_PM_EMAIL}  ${short_password}  ${PS_GOL_APPLICATION_PROJECT}
    project finance approves other documents     ${PS_GOL_APPLICATION_PROJECT}
    project finance generates the Spend Profile  ${Gabtype_Id}  ${Kazio_Id}  ${Cogilith_Id}  ${PS_GOL_APPLICATION_PROJECT}
    log in as a different user                   ${PS_GOL_APPLICATION_PM_EMAIL}  ${short_password}
    all partners submit their Spend Profile
