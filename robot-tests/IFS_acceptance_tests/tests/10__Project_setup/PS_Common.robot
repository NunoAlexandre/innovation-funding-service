*** Settings ***
Resource    ../../resources/variables/GLOBAL_VARIABLES.robot
Resource    ../../resources/defaultResources.robot


*** Variables ***
#Project: London underground – enhancements to existing stock and logistics
# GOL = Grant Offer Letter
${Gabtype_Id}       52
${Gabtype_Name}     Gabtype
${Kazio_Id}         53
${Kazio_Name}       Kazio
${Cogilith_Id}      54
${Cogilith_Name}    Cogilith
${PS_GOL_Competition_Id}  10
${PS_GOL_APPLICATION_TITLE}      London underground - enhancements to existing stock and logistics
${PS_GOL_APPLICATION_NO}         ${application_ids["${PS_GOL_APPLICATION_TITLE}"]}
${PS_GOL_APPLICATION_NUMBER}     ${PS_GOL_APPLICATION_NO}
${PS_GOL_APPLICATION_HEADER}     ${PS_GOL_APPLICATION_TITLE}
${PS_GOL_APPLICATION_PROJECT}    ${project_ids['${PS_GOL_APPLICATION_TITLE}']}
${PS_GOL_APPLICATION_LEAD_ORGANISATION_ID}      ${Gabtype_ID}
${PS_GOL_APPLICATION_LEAD_ORGANISATION_NAME}    ${Gabtype_NAME}
${PS_GOL_APPLICATION_LEAD_PARTNER_EMAIL}        ${test_mailbox_one}+amy@gmail.com
${PS_GOL_APPLICATION_PM_EMAIL}                  ${test_mailbox_one}+amy@gmail.com
${PS_GOL_APPLICATION_FINANCE_CONTACT_EMAIL}     ${test_mailbox_one}+marian@gmail.com
${PS_GOL_APPLICATION_PARTNER_EMAIL}             ${test_mailbox_one}+karen@gmail.com
${PS_GOL_APPLICATION_ACADEMIC_EMAIL}            ${test_mailbox_one}+juan@gmail.com

#Project: High-speed rail and its effects on air quality
# MD = Mandatory Documents
${Ooba_Id}          49
${Ooba_Name}        Ooba
${Wordpedia_Id}     50
${Wordpedia_Name}   Wordpedia
${Jabbertype_Id}    51
${Jabbertype_Name}  Jabbertype
${PS_MD_Competition_Id}         10
${PS_MD_Competition_Name}       Rolling stock future developments
${PS_MD_APPLICATION_TITLE}      High-speed rail and its effects on air quality
${PS_MD_APPLICATION_NO}         ${application_ids["${PS_MD_APPLICATION_TITLE}"]}
${PS_MD_APPLICATION_NUMBER}     ${PS_MD_APPLICATION_NO}
${PS_MD_APPLICATION_HEADER}     ${PS_MD_APPLICATION_TITLE}
${PS_MD_APPLICATION_PROJECT}    ${project_ids['${PS_MD_APPLICATION_TITLE}']}
${PS_MD_APPLICATION_LEAD_ORGANISATION_ID}    ${Ooba_ID}
${PS_MD_APPLICATION_LEAD_ORGANISATION_NAME}  ${Ooba_Name}
${PS_MD_APPLICATION_LEAD_PARTNER_EMAIL}      ralph.young@ooba.example.com
${PS_MD_APPLICATION_PM_EMAIL}                ralph.young@ooba.example.com
${PS_MD_APPLICATION_PARTNER_EMAIL}           tina.taylor@wordpedia.example.com
${PS_MD_APPLICATION_ACADEMIC_EMAIL}          antonio.jenkins@jabbertype.example.com

#Project: Point control and automated monitoring
# SP = Spend Profile
${Katz_Id}         46
${Katz_Name}       Katz
${Meembee_Id}      47
${Meembee_Name}    Meembee
${Zooveo_Id}       48
${Zooveo_Name}     Zooveo
${PS_SP_Competition_Id}         10
${PS_SP_Competition_Name}       Rolling stock future developments
${PS_SP_APPLICATION_TITLE}      Point control and automated monitoring
${PS_SP_APPLICATION_NO}         ${application_ids["${PS_SP_APPLICATION_TITLE}"]}
${PS_SP_APPLICATION_NUMBER}     ${PS_SP_APPLICATION_NO}
${PS_SP_APPLICATION_HEADER}     ${PS_SP_APPLICATION_TITLE}
${PS_SP_APPLICATION_PROJECT}    ${project_ids['${PS_SP_APPLICATION_TITLE}']}
${PS_SP_APPLICATION_LEAD_ORGANISATION_ID}    ${Katz_Id}
${PS_SP_APPLICATION_LEAD_ORGANISATION_NAME}  ${Katz_Name}
${PS_SP_APPLICATION_LEAD_PARTNER_EMAIL}      theo.simpson@katz.example.com
${PS_SP_APPLICATION_PM_EMAIL}                phillip.ramos@katz.example.com
${PS_SP_APPLICATION_PARTNER_EMAIL}           kimberly.fowler@meembee.example.com
${PS_SP_APPLICATION_ACADEMIC_EMAIL}          craig.ortiz@zooveo.example.com

#Project: Grade crossing manufacture and supply
# BD = Bank Details
${Vitruvius_Id}       32
${Vitruvius_Name}     Vitruvius Stonework Limited
${A_B_Cad_Services_Id}    44
${A_B_Cad_Services_Name}  A B Cad Services
${Armstrong_Butler_Id}       45
${Armstrong_Butler_Name}     Armstrong & Butler Ltd
${PS_BD_Competition_Id}         10
${PS_BD_Competition_Name}       Rolling stock future developments
${PS_BD_APPLICATION_TITLE}      Grade crossing manufacture and supply
${PS_BD_APPLICATION_NUMBER}     ${application_ids["${PS_BD_APPLICATION_TITLE}"]}
${PS_BD_APPLICATION_HEADER}     ${PS_BD_APPLICATION_TITLE}
${PS_BD_APPLICATION_PROJECT}    ${project_ids['${PS_BD_APPLICATION_TITLE}']}
${PS_BD_APPLICATION_LEAD_ORGANISATION_ID}    ${Vitruvius_Id}
${PS_BD_APPLICATION_LEAD_ORGANISATION_NAME}  ${Vitruvius_Name}
${PS_BD_APPLICATION_LEAD_PARTNER_EMAIL}      diane.scott@vitruvius.example.com
${PS_BD_APPLICATION_PM_EMAIL}                diane.scott@vitruvius.example.com
${PS_BD_APPLICATION_LEAD_FINANCE}            Diane Scott
${PS_BD_APPLICATION_LEAD_TELEPHONE}          49692921151
${PS_BD_APPLICATION_PARTNER_EMAIL}           ryan.welch@abcad.example.com
${PS_BD_APPLICATION_PARTNER_FINANCE}         Ryan Welch
${PS_BD_APPLICATION_ACADEMIC_EMAIL}          sara.armstrong@armstrong.example.com
${PS_BD_APPLICATION_ACADEMIC_FINANCE}        Sara Armstrong

#Project: New materials for lighter stock
# EF = Experian feedback
${Ntag_Id}        41
${Ntag_Name}      Ntag
${Ntag_No}        18451018
${Ntag_Street}    39357 Fisk Drive
${Jetpulse_Id}    42
${Jetpulse_Name}  Jetpulse
${Wikivu_Id}      43
${Wikivu_Name}    Wikivu
${PS_EF_Competition_Id}         10
${PS_EF_Competition_Name}       Rolling stock future developments
${PS_EF_APPLICATION_TITLE}      New materials for lighter stock
${PS_EF_APPLICATION_NO}         ${application_ids["${PS_EF_APPLICATION_TITLE}"]}
${PS_EF_APPLICATION_NUMBER}     ${PS_EF_APPLICATION_NO}
${PS_EF_APPLICATION_HEADER}     ${PS_EF_APPLICATION_TITLE}
${PS_EF_APPLICATION_PROJECT}    ${project_ids['${PS_EF_APPLICATION_TITLE}']}
${PS_EF_APPLICATION_LEAD_ORGANISATION_ID}    ${Ntag_Id}
${PS_EF_APPLICATION_LEAD_ORGANISATION_NAME}  ${Ntag_Name}
${PS_EF_APPLICATION_LEAD_PARTNER_EMAIL}      steven.hicks@ntag.example.com
${PS_EF_APPLICATION_PM_EMAIL}                steven.hicks@ntag.example.com
${PS_EF_APPLICATION_PARTNER_EMAIL}           robert.perez@jetpulse.example.com
${PS_EF_APPLICATION_ACADEMIC_EMAIL}          bruce.perez@wikivu.example.com

#Project: Magic material
${PROJECT_SETUP_COMPETITION}    8
${PROJECT_SETUP_COMPETITION_NAME}    New designs for a circular economy
${PROJECT_SETUP_APPLICATION_1_TITLE}    Magic material
${PROJECT_SETUP_APPLICATION_1}    ${application_ids["${PROJECT_SETUP_APPLICATION_1_TITLE}"]}
${PROJECT_SETUP_APPLICATION_1_NUMBER}    ${PROJECT_SETUP_APPLICATION_1}
${PROJECT_SETUP_APPLICATION_1_HEADER}    ${PROJECT_SETUP_APPLICATION_1_TITLE}
${PROJECT_SETUP_APPLICATION_1_PROJECT}   ${project_ids['${PROJECT_SETUP_APPLICATION_1_TITLE}']}
${PROJECT_SETUP_APPLICATION_1_LEAD_ORGANISATION_ID}    ${EMPIRE_LTD_ID}
${PROJECT_SETUP_APPLICATION_1_LEAD_ORGANISATION_NAME}    ${EMPIRE_LTD_NAME}
${PROJECT_SETUP_APPLICATION_1_LEAD_ORGANISATION_COMPANY_NUMBER}    60674010
${PROJECT_SETUP_APPLICATION_1_LEAD_COMPANY_TURNOVER}    4560000
${PROJECT_SETUP_APPLICATION_1_LEAD_COMPANY_HEADCOUNT}    1230
${PROJECT_SETUP_APPLICATION_1_LEAD_PARTNER_EMAIL}  ${lead_applicant_credentials["email"]}
${PROJECT_SETUP_APPLICATION_1_PM_EMAIL}    ${test_mailbox_one}+projectsetuppm@gmail.com
${PROJECT_SETUP_APPLICATION_1_PARTNER_ID}    39
${PROJECT_SETUP_APPLICATION_1_PARTNER_NAME}    Ludlow
${PROJECT_SETUP_APPLICATION_1_PARTNER_COMPANY_NUMBER}    53532322
${PROJECT_SETUP_APPLICATION_1_PARTNER_COMPANY_TURNOVER}    1230000
${PROJECT_SETUP_APPLICATION_1_PARTNER_COMPANY_HEADCOUNT}    4560
${PROJECT_SETUP_APPLICATION_1_PARTNER_EMAIL}            ${collaborator1_credentials["email"]}
${PROJECT_SETUP_APPLICATION_1_ACADEMIC_PARTNER_ID}      40
${PROJECT_SETUP_APPLICATION_1_ACADEMIC_PARTNER_NAME}    EGGS
${PROJECT_SETUP_APPLICATION_1_ACADEMIC_PARTNER_EMAIL}   ${collaborator2_credentials["email"]}
${PROJECT_SETUP_APPLICATION_1_ADDITIONAL_PARTNER_NAME}    HIVE IT LIMITED
${PROJECT_SETUP_APPLICATION_1_ADDITIONAL_PARTNER_EMAIL}   ewan+1@hiveit.co.uk

${project_in_setup_page}                ${server}/project-setup/project/${PROJECT_SETUP_APPLICATION_1_PROJECT}
${project_in_setup_details_page}        ${project_in_setup_page}/details
${project_in_setup_team_status_page}    ${project_in_setup_page}/team-status
${project_start_date_page}              ${project_in_setup_details_page}/start-date
${project_address_page}                 ${project_in_setup_details_page}/project-address
${internal_project_summary}             ${server}/project-setup-management/competition/${PROJECT_SETUP_COMPETITION}/status



#Bank details
${account_one}   51406795
${sortCode_one}  404745
${account_two}   12345677
${sortCode_two}  000004

*** Keywords ***
project finance submits monitoring officer
    [Arguments]    ${project_id}  ${fname}  ${lname}  ${email}  ${phone_number}
    log in as a different user              &{internal_finance_credentials}
    the user navigates to the page          ${server}/project-setup-management/project/${project_id}/monitoring-officer
    the user enters text to a text field    id=firstName    ${fname}
    the user enters text to a text field    id=lastName    ${lname}
    The user enters text to a text field    id=emailAddress    ${email}
    The user enters text to a text field    id=phoneNumber    ${phone_number}
    the user clicks the button/link         jQuery=.button[type="submit"]:contains("Assign Monitoring Officer")
    the user clicks the button/link         jQuery=.modal-assign-mo button:contains("Assign Monitoring Officer")

partner submits his bank details
    [Arguments]  ${email}  ${project}  ${account_number}  ${sort_code}
    log in as a different user                       ${email}    ${short_password}
    the user navigates to the page                   ${server}/project-setup/project/${project}/bank-details
    the user enters text to a text field             id=bank-acc-number  ${account_number}
    the user enters text to a text field             id=bank-sort-code  ${sort_code}
    the user clicks the button twice                 jQuery=div:nth-child(2) label.selection-button-radio[for="address-use-org"]
    the user should see the element                  jQuery=#registeredAddress h3:contains("Confirm billing address")
    wait until keyword succeeds without screenshots  30  500ms  the user clicks the button/link  jQuery=.button:contains("Submit bank account details")
    wait until keyword succeeds without screenshots  30  500ms  the user clicks the button/link  jQuery=.button[name="submit-app-details"]
    wait until element is not visible without screenshots  30  500ms  jQuery=.button[name="submit-app-details"]  # Added this wait so to give extra execution time