*** Variables ***
${BROWSER}        chrome
${SERVER_BASE}    ifs-local-dev
${PROTOCOL}       https://
${SERVER}         ${PROTOCOL}${SERVER_BASE}
${RUNNING_ON_DEV}    ${EMPTY}
${LOGIN_URL}      ${SERVER}/
${LOGGED_OUT_URL_FRAGMENT}    idp/profile/SAML2/Redirect/SSO
${DASHBOARD_URL}    ${SERVER}/applicant/dashboard
${SUMMARY_URL}    ${SERVER}/application/1/summary
${QUESTION11_URL}    ${SERVER}/application-form/1/section/1/#question-11
${APPLICATION_OVERVIEW_URL}    ${SERVER}/application/1
${APPLICATION_OVERVIEW_URL_APPLICATION_2}    ${SERVER}/application/2
${APPLICATION_SUBMITTED_URL}    ${SERVER}/application/1/submit
${APPLICATION_2_SUMMARY_URL}    ${SERVER}/application/2/summary
${ECONOMIC_BENEFIT_URL_APPLICATION_2}    ${SERVER}/application/2/form/question/4
${APPLICATION_3_OVERVIEW_URL}    ${SERVER}/application/3
${ECONOMIC_BENEFIT_URL_APPLICATION_3}    ${SERVER}/application/3/form/question/4
${applicant_dashboard_url}    ${SERVER}/applicant/dashboard
${assessor_dashboard_url}    ${SERVER}/assessment/assessor/dashboard
${COMPETITION_DETAILS_URL}    ${SERVER}/competition/1/details/
${LOG_OUT}        ${LOGIN_URL}/Logout
${APPLICATION_QUESTIONS_SECTION_URL}    ${SERVER}/application-form/1/section/2/
${SEARCH_COMPANYHOUSE_URL}    ${SERVER}/organisation/create/find-business
${APPLICATION_DETAILS_URL}    ${SERVER}/application/1/form/question/9
${PROJECT_SUMMARY_URL}    ${SERVER}/application/1/form/question/11
${PROJECT_SUMMARY_EDIT_URL}    ${SERVER}/application/1/form/question/edit/11
${PUBLIC_DESCRIPTION_URL}    ${SERVER}/application/1/form/question/12
${SCOPE_URL}      ${SERVER}/application/1/form/question/13
${BUSINESS_OPPORTUNITY_URL}    ${SERVER}/application/1/form/question/1
${POTENTIAL_MARKET_URL}    ${SERVER}/application/1/form/question/2
${PROJECT_EXPLOITATION_URL}    ${SERVER}/application/1/form/question/3
${ECONOMIC_BENEFIT_URL}    ${SERVER}/application/1/form/question/4
${TECHNICAL_APPROACH_URL}    ${SERVER}/application/1/form/question/5
${INNOVATION_URL}    ${SERVER}/application/1/form/question/6
${RISKS_URL}      ${SERVER}/application/1/form/question/7
${PROJECT_TEAM_URL}    ${SERVER}/application/1/form/question/8
${FUNDING_URL}    ${SERVER}/application/1/form/question/15
${ADDING_VALUE_URL}    ${SERVER}/application/1/form/question/16
${YOUR_FINANCES_URL}    ${SERVER}/application/1/form/section/7
${YOUR_FINANCES_URL_APPLICATION_2}    ${SERVER}/application/2/form/section/7
${FINANCES_OVERVIEW_URL}    ${SERVER}/application/1/form/section/8
${FINANCES_OVERVIEW_URL_APPLICATION_2}    ${SERVER}/application/2/form/section/8
${ACCOUNT_CREATION_FORM_URL}    ${SERVER}/registration/register?organisationId=1
${ELIGIBILITY_INFO_URL}    ${SERVER}/competition/1/info/eligibility
${CHECK_ELIGIBILITY}    ${SERVER}/application/create/check-eligibility/1
${SPEED_BUMP_URL}    ${SERVER}/application/create-authenticated/1
${YOUR_DETAILS}    ${SERVER}/application/create/your-details
${POSTCODE_LOOKUP_URL}    ${SERVER}/organisation/create/selected-organisation/05063042#
${EDIT_PROFILE_URL}    ${SERVER}/profile/edit
${APPLICATION_TEAM_URL}    ${SERVER}/application/1/contributors
${MANAGE_CONTRIBUTORS_URL}    ${SERVER}/application/1/contributors/invite
${COMP_MANAGEMENT_APPLICATIONS_LIST}    ${SERVER}/management/competition/1
${COMP_MANAGEMENT_APPLICATION_1_OVERVIEW}    ${SERVER}/management/competition/1/application/1
${COMP_MANAGEMENT_COMP_SETUP}    ${SERVER}/management/competition/setup/8
${NEWLY_CREATED_APPLICATION_YOUR_FINANCES_URL}    ${SERVER}/application/24/form/section/7
${CONFIRM_ORGANISATION_URL}    ${SERVER}/organisation/create/confirm-organisation
${PROJECT_FINANCE_DASHBOARD_URL}    ${SERVER}/management/projectfinancedashboard
${SUCCESSFUL_PROJECT_PAGE}    ${server}/project/4
${SUCCESSFUL_PROJECT_PAGE_DETAILS}    ${server}/project/4/details
${project_start_date_page}    ${server}/project/4/details/start-date
${project_address_page}    ${server}/project/4/details/project-address
${project_manager_page}    ${server}/project/4/details/start-date
${404_error_message}    Page Not Found
${403_error_message}    You do not have the necessary permissions for your request
${wrong_filetype_validation_error}    Please upload a file in .pdf format only
${too_large_pdf_validation_error}    the size of file or request being submitted is too large
${REGISTRATION_SUCCESS}    ${SERVER}/registration/success
${verify_link_1}    ${SERVER}/registration/verify-email/4a5bc71c9f3a2bd50fada434d888579aec0bd53fe7b3ca3fc650a739d1ad5b1a110614708d1fa083
${verify_link_2}    ${SERVER}/registration/verify-email/5f415b7ec9e9cc497996e251294b1d6bccfebba8dfc708d87b52f1420c19507ab24683bd7e8f49a0
${verify_link_3}    ${SERVER}/registration/verify-email/8223991f065abb7ed909c8c7c772fbdd24c966d246abd63c2ff7eeba9add3bafe42b067b602f761b
${REGISTRATION_VERIFIED}    ${SERVER}/registration/verified
${UPLOAD_FOLDER}    uploaded_files
${VIRTUAL_DISPLAY}    ${EMPTY}
${POSTCODE_LOOKUP_IMPLEMENTED}    ${EMPTY}
${LOCAL_MAIL_SENDING_IMPLEMENTED}    'YES'
${COMP_ADMINISTRATOR_DASHBOARD}    ${SERVER}/management/dashboard
${COMP_ADMINISTRATOR_OPEN}    ${SERVER}/management/competition/1
${COMP_ADMINISTRATOR_IN_ASSESSMENT}    ${SERVER}/management/competition/2
${DOWNLOAD_FOLDER}    ../download_files
${empty_field_warning_message}    This field cannot be left blank
${OPEN_COMPETITION_LINK}    Connected digital additive manufacturing
${valid_pdf}      testing.pdf
${too_large_pdf}    large.pdf
${text_file}      testing.txt
${valid_pdf excerpt}    Adobe PDF is an ideal format for electronic document distribution
${TEST_MAILBOX_ONE}    worth.email.test
${TEST_MAILBOX_TWO}    worth.email.test.two
${test_mailbox_one_password}    testtest1
${test_mailbox_two_password}    testtest1
${Providing_Sustainable_Childcare_Application_Overview}    ${server}/management/competition/1/application/2
${unsuccessful_login_message}    Your sign in was unsuccessful because of the following issue(s)
