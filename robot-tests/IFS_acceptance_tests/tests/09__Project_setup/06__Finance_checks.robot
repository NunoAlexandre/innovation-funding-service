*** Settings ***
Documentation     INFUND-5190 As a member of Project Finance I want to view an amended Finance Checks summary page so that I can see the projects and organisations requiring Finance Checks for the Private Beta competition
...
...               INFUND-5193 As a member of Project Finance I want to be able to approve the finance details that have been updated in the Finance Checks so that these details can be used to generate the default spend profile
...
...               INFUND-5220 As a member of Project Finance I want to be able to view project costs for academic organisations so that I can review funding during the Finance Checks for the Private Beta competition
...
...               INFUND-5852 As a Project Finance team member I want a link to create the export of bank details for a competition so that this can be delivered to Finance for entry into the Innovate UK Finance SUN system
...
...               INFUND-6149 mailto link is broken on the internal finance eligibility page
...
...               INFUND-7016 Finance checks page is missing Project title
...
...               INFUND-7026 For internal user, in finance checks RAG is not N/A in case of academic
...
...               INFUND-4822 As a project finance team member I want to be able to view a summary of progress through the finance checks section for each partner so I can review and navigate to the sections
...
...               INFUND-4829 As a project finance team member I want to be able to confirm whether a full credit report has been used to confirm an applicant organisation's viability for funding so that this may be kept on record as part of the decision-making process
...
...               INFUND-4831 As a project finance team member I want to be able to confirm that the partner organisation is viable for funding so that no further viability checks need be carried out
...
...               INFUND-4856 As a project finance team member I want to be able to view the RAG rating indicating the effort level carried out for the viability checks of each partner organisation so that I can appraise colleagues who may be expected to carry out future checks.
...
...               INFUND-7076 Generate spend profile available before Viability checks are all approved or N/A
...
...               INFUND-7095 Create NOT_APPLICABLE Viability state (and set for Academic Orgs upon Project creation)
...
...               INFUND-4830 As a project finance team member I want to be able to confirm that the appropriate viability finance checks have been carried out so I can approve the partner organisation as viable for funding
...
...               INFUND-4825 As a project finance team member I want to view details of each partner organisation so I can review their viability for funding
...
...               INFUND-7613 Date and user stamp not showing
...
...               INFUND-4820 As a project finance team member I want a page containing summary information for each project so that I can manage the Finance Checks section for each project in Project Setup
...
...               INFUND-7718 Content: Breadcrumb content for main project page to projects in setup is incorrect
...
...               INFUND-4832 As a project finance team member I want to view details of the requested funding for each partner organisation so I can review their eligibility for funding
...
...               INFUND-4834 As a project finance team member I want to be able to amend the details stored in Finance Checks for a partner organisation so that I can ensure the detailed finances are appropriate for the project to meet funding eligibility requirements
...
...               INFUND-4833 As a project finance team member I want to be able to view partner finance details supplied in the application form so that I can review or edit them if appropriate
...
...               INFUND-4839 As a project finance team member I want to be able to confirm the partner organisation is eligible for funding so that no further eligibility checks need to be carried out
...
...               INFUND-4823 As a project finance team member I want to be able to view the RAG rating for the viability and eligibility of each partner organisation if available so that I can be appraised of the effort level that may be expected to carry out the finance checks.
...
...               INFUND-7573 Partner view - main page - Finance Checks
...
...               INFUND-5508 As a member of Project Finance I want to see the Finance Checks Overview table updating with approved funding amounts so that I can confirm any amended figures before generating the Spend Profile
...
...               INFUND-7574 Partner view updated finances - Finance Checks Eligibility
...
...               INFUND-4840 As a project finance team member I want to be able to post a query in the finance checks section so that the relevant finance contact can be given the opportunity to provide further details
...
...               INFUND-4843 As a partner I want to be able to respond to a query posted by project finance so that they can review the additional information requested
...
...               INFUND-4845 As a project finance team member I want to be able to post a note in the finance checks section so that colleagues reviewing the partner's progress can be kept informed of any further information needed to support the finance checks section
...
...               INFUND-4841 As a project finance team member I want to send an email to the relevant finance contact so that they can be notified when a query has been posted in finance checks
...
...               INFUND-7752 Internal user can further respond to an external parter's response to a query
...
...               INFUND-7753 Partner receives an email alerting them to a further response to an earlier query
...
...               INFUND-7756 Project finance can post an update to an existing note
...
...               INFUND-7577 Finance Checks - Overheads displayed in the expanded Overheads section of the partner’s project finances and Project Finance user can Edit, Save, Change selection from 0% to 20% to Calculate overhead, contains spreadsheet when uploaded

Suite Setup       Moving ${FUNDERS_PANEL_COMPETITION_NAME} into project setup
Suite Teardown    the user closes the browser
Force Tags        Project Setup
Resource          ../../resources/defaultResources.robot
Resource          PS_Variables.robot
Resource          ../04__Applicant/FinanceSection_Commons.robot

*** Variables ***
${la_fromage_overview}    ${server}/project-setup/project/${FUNDERS_PANEL_APPLICATION_1_PROJECT}
${FINANCES_OVERVIEW_NO_OVER_HEAD_RADIO_BUTTON}      label.block-label.selection-button-radio[for='cost-overheads-2406-rateType_0']

*** Test Cases ***
Project Finance user can see the finance check summary page
    [Documentation]    INFUND-4821, INFUND-5476, INFUND-5507, INFUND-7016, INFUND-4820, INFUND-7718
    [Tags]  HappyPath
    [Setup]    Log in as a different user        &{internal_finance_credentials}
    Given the user navigates to the page          ${server}/project-setup-management/project/${FUNDERS_PANEL_APPLICATION_1_PROJECT}/finance-check
    Then the user should see the element          jQuery=table.table-progress
    And the user should see the element          jQuery=h2:contains("Finance checks")
    And the user should see the text in the page  Overview
    And the user should see the text in the page    ${funders_panel_application_1_title}
    And the table row has expected values
    And the user should see the element    link=Projects in setup


Status of the Eligibility column (workaround for private beta competition)
    [Documentation]    INFUND-5190
    [Tags]
    Given the user navigates to the page    ${server}/project-setup-management/project/${FUNDERS_PANEL_APPLICATION_1_PROJECT}/finance-check
    Then The user should see the text in the page    Viability
    And The user should see the text in the page    Queries raised
    And The user should see the text in the page    Notes
    When the user should see the element    link=Review
    Then the user should see that the element is disabled    jQuery=.generate-spend-profile-main-button


Queries section is linked from eligibility and this selects eligibility on the query dropdown
    [Documentation]    INFUND-4840
    [Tags]
    Given the user navigates to the page    ${server}/project-setup-management/project/${FUNDERS_PANEL_APPLICATION_1_PROJECT}/finance-check/organisation/22/eligibility
    When the user clicks the button/link    jQuery=.button:contains("Queries")
    Then the user should see the text in the page    If you have a query with the finances, use this section
    When the user clicks the button/link    jQuery=.button:contains("Post a new query")
    Then the user should see the dropdown option selected    Eligibility    section
    [Teardown]    the user navigates to the page    ${server}/project-setup-management/project/${FUNDERS_PANEL_APPLICATION_1_PROJECT}/finance-check


Queries section is linked from viability and this selects viability on the query dropdown
    [Documentation]    INFUND-4840
    [Tags]
    Given the user clicks the button/link    jQuery=table.table-progress tr:nth-child(1) td:nth-child(2)    # Clicking the viability link for lead partner
    When the user clicks the button/link    jQuery=.button:contains("Queries")
    Then the user should see the text in the page    If you have a query with the finances, use this section
    When the user clicks the button/link    jQuery=.button:contains("Post a new query")
    Then the user should see the dropdown option selected    Viability    section
    [Teardown]    the user navigates to the page    ${server}/project-setup-management/project/${FUNDERS_PANEL_APPLICATION_1_PROJECT}/finance-check


Queries section is linked to from the main finance check summary page
    [Documentation]    INFUND-4840
    [Tags]
    When the user clicks the button/link    jQuery=table.table-progress tr:nth-child(1) td:nth-child(6)
    Then the user should see the text in the page    If you have a query with the finances, use this section

Queries section contains finance contact name, email and telephone
    [Documentation]    INFUND-4840
    [Tags]
    When the user should see the text in the page    Sarah Peacock
    And the user should see the text in the page    74373688727
    And the user should see the text in the page    ${test_mailbox_one}+fundsuccess@gmail.com

Viability and eligibility sections both available
    [Documentation]    INFUND-4840
    [Tags]
    When the user clicks the button/link    jQuery=.button:contains("Post a new query")
    Then the user should see the option in the drop-down menu    Viability    section
    And the user should see the option in the drop-down menu    Eligibility    section


Large pdf uploads not allowed
    [Documentation]    INFUND-4840
    [Tags]
    When the user uploads the file     name=attachment    ${too_large_pdf}
    Then the user should see the text in the page    ${too_large_pdf_validation_error}
    [Teardown]    the user goes back to the previous page

Non pdf uploads not allowed
    [Documentation]    INFUND-4840
    [Tags]
    When the user uploads the file      name=attachment    ${text_file}
    Then the user should see the text in the page    ${wrong_filetype_validation_error}


Project finance user can upload a pdf file
    [Documentation]    INFUND-4840
    [Tags]
    Then the user uploads the file      name=attachment    ${valid_pdf}
    And the user should see the text in the page    ${valid_pdf}

Project finance can remove the file
    [Documentation]    INFUND-4840
    [Tags]
    When the user clicks the button/link    name=removeAttachment
    Then the user should not see the text in the page    ${valid_pdf}
    And the user should not see an error in the page

Project finance can re-upload the file
    [Documentation]    INFUND-4840
    [Tags]
    When the user uploads the file    name=attachment    ${valid_pdf}
    Then the user should see the text in the page    ${valid_pdf}

Project finance user can view the file
    [Documentation]    INFUND-4840
    [Tags]
    Given the user should see the element    link=${valid_pdf}
    And the file has been scanned for viruses
    When the user clicks the button/link    link=${valid_pdf}
    Then the user should not see an error in the page
    And the user goes back to the previous page ignoring form submission
    [Teardown]    the user clicks the button/link   css=button[name='removeAttachment']:nth-last-of-type(1)


Project finance user can upload more than one file
    [Documentation]    INFUND-4840
    [Tags]
    When the user uploads the file      name=attachment    ${valid_pdf}
    Then the user should see the element    jQuery=a:contains("testing.pdf"):nth-of-type(2)


Project finance user can still view both files
    [Documentation]    INFUND-4840
    [Tags]
    When the user clicks the button/link    jQuery=a:contains("testing.pdf"):nth-of-type(1)
    Then the user should not see an error in the page
    And the user goes back to the previous page ignoring form submission
    And the user clicks the button/link   css=button[name='removeAttachment']:nth-last-of-type(1)
    When the user clicks the button/link    jQuery=a:contains("testing.pdf"):nth-of-type(2)
    Then the user should not see an error in the page
    And the user goes back to the previous page ignoring form submission
    And the user clicks the button/link   css=button[name='removeAttachment']:nth-last-of-type(1)

Post new query server side validations
    [Documentation]    INFUND-4840
    [Tags]
    When the user clicks the button/link    jQuery=.button:contains("Post Query")
    Then the user should see the element   jQuery=label[for="queryTitle"] span:nth-child(2) span:contains(This field cannot be left blank.)
    And the user should see the element    jQuery=label[for="query"] span:nth-child(2) span:contains(This field cannot be left blank.)

Post new query client side validations
    [Documentation]    INFUND-4840
    [Tags]
    When the user enters text to a text field    id=queryTitle    this is a title
    Then the user should not see the element    jQuery=label[for="queryTitle"] span:nth-child(2) span:contains(This field cannot be left blank.)
    When the user enters text to a text field    css=.editor    this is some query text
    Then the user should not see the element    jQuery=label[for="query] span:nth-child(2) span:contains(This field cannot be left blank.)


Word count validations
    [Documentation]    INFUND-4840
    [Tags]
    When the user enters text to a text field    css=.editor    Lorem ipsum dolor sit amet, consectetur adipiscing elit. Proin elementum condimentum ex, ut tempus nisi. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aenean sed pretium tellus. Vestibulum sollicitudin semper scelerisque. Sed tristique, erat in gravida gravida, felis tortor fermentum ligula, vitae gravida velit ipsum vel magna. Aenean in pharetra ex. Integer porttitor suscipit lectus eget ornare. Maecenas sed metus quis sem dapibus vestibulum vel vitae purus. Etiam sodales nisl at enim tempus, sed malesuada elit accumsan. Aliquam faucibus neque vitae commodo rhoncus. Sed orci sem, varius vitae justo quis, cursus porttitor lectus. Pellentesque eu nibh nunc. Duis laoreet enim et justo sagittis, at posuere lectus laoreet. Suspendisse rutrum odio id iaculis varius. Phasellus gravida, mi vel vehicula dignissim, lectus nunc eleifend justo, elementum lacinia enim tellus a nulla. Pellentesque consectetur sollicitudin ante, ac vehicula lorem laoreet laoreet. Fusce consequat libero mi. Quisque luctus risus neque, ut gravida quam tincidunt id. Aliquam id ante arcu. Nulla ut est ipsum. Praesent accumsan efficitur malesuada. Ut tempor auctor felis eu dapibus. Sed felis quam, aliquet sit amet urna nec, consectetur feugiat nibh. Nam id libero nec augue convallis euismod quis vitae nibh. Integer lectus velit, malesuada ut neque mollis, mattis euismod diam. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae; Etiam aliquet porta enim sit amet rhoncus. Curabitur ornare turpis eros, sodales hendrerit tellus rutrum a. Ut efficitur feugiat turpis, eu ultrices velit pharetra non. Curabitur condimentum lacus ac ligula auctor egestas. Aliquam feugiat tellus neque, a ornare tortor imperdiet at. Integer varius turpis eu mi efficitur, at imperdiet ex posuere. Suspendisse blandit, mi at mollis placerat, magna nibh malesuada nisi, ultrices semper augue enim sit amet nisi. Donec molestie tellus vitae risus interdum, nec finibus risus interdum. Integer purus justo, fermentum id urna eu, aliquam rutrum erat. Phasellus volutpat odio metus, sed interdum magna luctus ac. Nam ullamcorper maximus sapien vitae dapibus. Vivamus ullamcorper quis sapien et mattis. Aenean aliquam arcu lacus, vel mollis ligula ultrices nec. Sed cursus placerat tortor elementum tincidunt. Pellentesque at arcu ut felis euismod vestibulum pulvinar nec neque. Quisque ipsum purus, tincidunt quis iaculis eu, malesuada nec lectus. Vivamus tempor, enim quis vestibulum convallis, ex odio pharetra tellus, eget posuere justo ligula sit amet dolor. Cras scelerisque neque id porttitor semper. Sed ut ultrices lorem. Pellentesque sed libero a velit vestibulum fermentum id et velit. Vivamus turpis risus, venenatis ac quam nec, pulvinar fringilla libero. Donec eget vestibulum orci, id lacinia mi. Aenean sed lectus viverra est feugiat suscipit. Proin eget justo turpis. Nullam maximus fringilla sapien, at pharetra odio pretium ut. Cras imperdiet mauris at bibendum dapibus.
    Then the user should see the text in the page    Maximum word count exceeded. Please reduce your word count to 400.
    When the user enters text to a text field    css=.editor    this is some query text
    Then the user should not see the text in the page    Maximum word count exceeded. Please reduce your word count to 400.

New query can be cancelled
    [Documentation]    INFUND-4840
    [Tags]
    When the user clicks the button/link    jQuery=a:contains("Cancel")
    Then the user should not see the text in the page    ${valid_pdf}
    And the user should not see the element    id=queryTitle
    And the user should not see the element    css=.editor


Query can be re-entered
    [Documentation]    INFUND-4840
    [Tags]
    When the user clicks the button/link    jQuery=.button:contains("Post a new query")
    And the user enters text to a text field    id=queryTitle    this is a title
    And the user enters text to a text field    css=.editor    this is some query text
    And the user uploads the file    name=attachment    ${valid_pdf}
    And the user uploads the file    name=attachment    ${valid_pdf}


New query can be posted
    [Documentation]    INFUND-4840
    [Tags]
    When the user clicks the button/link    jQuery=.button:contains("Post Query")
    Then the user should see the text in the page    Lee Bowman - Innovate UK (Finance team)


Query sections are no longer editable
    [Documentation]    INFUND-4840
    [Tags]
    When the user should not see the element    css=.editor

Queries raised column updates to 'awaiting response'
    [Documentation]    INFUND-4840
    [Tags]
    When the user clicks the button/link    link=Finance checks
    Then the user should see the element    jQuery=table.table-progress tr:nth-child(1) td:nth-child(6) a:contains("Awaiting response")


Finance contact receives an email when new query is posted
    [Documentation]    INFUND-4841
    [Tags]    Email
    Then the user reads his email    ${test_mailbox_one}+fundsuccess@gmail.com    Query regarding your finances    We have raised a query around your project finances.


Project finance user can add another query
    [Documentation]    INFUND-4840
    [Tags]
    Given the user clicks the button/link    jQuery=table.table-progress tr:nth-child(1) td:nth-child(6)
    When the user clicks the button/link    jQuery=.button:contains("Post a new query")
    And the user enters text to a text field    id=queryTitle    another query title
    And the user enters text to a text field    css=.editor    another query body
    And the user clicks the button/link    jQuery=.button:contains("Post Query")
    Then the user should not see an error in the page

Queries show in reverse chronological order
    [Documentation]    INFUND-4840
    [Tags]
    When the user should see the element    jQuery=h2:nth-of-type(4):contains("this is a title")    #
    And the user should see the element    jQuery=h2:nth-of-type(3):contains("another query title")

Non finance contact can view query
    [Documentation]    INFUND-4843
    [Tags]
    Given log in as a different user    steve.smith@empire.com    ${short_password}
    When the user clicks the button/link    link=${FUNDERS_PANEL_APPLICATION_1_HEADER}
    Then the user should see the element    link=Finance checks
    And the user should see the element    jQuery=ul li.require-action:nth-child(5)

Finance checks section status updated for finance contact
    [Documentation]    INFUND-4843
    [Tags]
    Given log in as a different user    ${test_mailbox_one}+fundsuccess@gmail.com    ${short_password}
    When the user clicks the button/link    link=${FUNDERS_PANEL_APPLICATION_1_HEADER}
    Then the user should see the element    link=Finance checks
     And the user should see the element    jQuery=ul li.require-action:nth-child(5)


Finance contact can view query
    [Documentation]    INFUND-4843
    [Tags]
    When the user clicks the button/link    link=Finance checks
    Then the user should see the text in the page    this is a title
    And the user should see the text in the page    this is some query text


Finance contact can view the project finance user's uploads
    [Documentation]    INFUND-4843
    [Tags]
    When the user clicks the button/link    jQuery=a:contains("${valid_pdf}"):nth-of-type(1)
    Then the user should not see an error in the page
    And the user goes back to the previous page
    When the user clicks the button/link    jQuery=a:contains("${valid_pdf}"):nth-of-type(2)
    Then the user should not see an error in the page
    And the user goes back to the previous page


Queries show in reverse chronological order for finance contact
    [Documentation]    INFUND-4843
    [Tags]
    When the user should see the element    jQuery=#content h2:nth-of-type(3):contains("this is a title")
    Then the user should see the element    jQuery=#content h2:nth-of-type(2):contains("another query title")


Large pdf uploads not allowed for query response
    [Documentation]    INFUND-4843
    [Tags]
    Given the user clicks the button/link    jQuery=.button.button-secondary:eq(0)
    When the user uploads the file     name=attachment    ${too_large_pdf}
    Then the user should see the text in the page    ${too_large_pdf_validation_error}
    [Teardown]    the user goes back to the previous page

#TODO Pending tag to be removed with resolution of INFUND-8855
Non pdf uploads not allowed for query response
    [Documentation]    INFUND-4843
    [Tags]  Pending
    When the user uploads the file      name=attachment    ${text_file}
    Then the user should see the text in the page    ${wrong_filetype_validation_error}


Finance contact can upload a pdf file
    [Documentation]    INFUND-4843
    [Tags]
    Then the user uploads the file      name=attachment   ${valid_pdf}
    And the user should see the text in the page    ${valid_pdf}

Finance contact can remove the file
    [Documentation]    INFUND-4840
    [Tags]
    When the user clicks the button/link    name=removeAttachment
    Then the user should not see the element    jQuery=.extra-margin a:contains("${valid_pdf}")
    And the user should not see an error in the page

Finance contact can re-upload the file
    [Documentation]    INFUND-4840
    [Tags]
    When the user uploads the file    name=attachment    ${valid_pdf}
    Then the user should see the element    jQuery=.extra-margin a:contains("${valid_pdf}")

Finance contact can view the file
    [Documentation]    INFUND-4843
    [Tags]
    Given the user should see the element    link=${valid_pdf}
    And the file has been scanned for viruses
    When the user clicks the button/link    jQuery=.extra-margin a:contains("${valid_pdf}")
    Then the user should not see an error in the page
    [Teardown]    the user goes back to the previous page ignoring form submission

Finance contact can upload more than one file
    [Documentation]    INFUND-4843
    [Tags]
    Then the user uploads the file      name=attachment    ${valid_pdf}
    And the user should see the element    jQuery=.extra-margin a:contains("testing.pdf"):nth-of-type(2)

Finance contact can still view both files
    [Documentation]    INFUND-4843
    [Tags]
    When the user clicks the button/link    jQuery=.extra-margin a:contains("${valid_pdf}"):nth-of-type(1)
    Then the user should not see an error in the page
    And the user goes back to the previous page ignoring form submission
    When the user clicks the button/link    jQuery=.extra-margin a:contains("${valid_pdf}"):nth-of-type(2)
    Then the user should not see an error in the page
    And the user goes back to the previous page ignoring form submission


Response to query server side validations
    [Documentation]    INFUND-4843
    [Tags]
    When the user clicks the button/link    jQuery=.button:contains("Post response")
    Then the user should see the text in the page    This field cannot be left blank.


Response to query client side validations
    [Documentation]    INFUND-4843
    [Tags]
    When the user enters text to a text field    css=.editor    this is some response text
    And the user moves focus to the element    jQuery=.button:contains("Post response")
    Then the user should not see the text in the page    This field cannot be left blank.

Word count validations for response
    [Documentation]    INFUND-4843
    When the user enters text to a text field    css=.editor    Lorem ipsum dolor sit amet, consectetur adipiscing elit. Proin elementum condimentum ex, ut tempus nisi. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aenean sed pretium tellus. Vestibulum sollicitudin semper scelerisque. Sed tristique, erat in gravida gravida, felis tortor fermentum ligula, vitae gravida velit ipsum vel magna. Aenean in pharetra ex. Integer porttitor suscipit lectus eget ornare. Maecenas sed metus quis sem dapibus vestibulum vel vitae purus. Etiam sodales nisl at enim tempus, sed malesuada elit accumsan. Aliquam faucibus neque vitae commodo rhoncus. Sed orci sem, varius vitae justo quis, cursus porttitor lectus. Pellentesque eu nibh nunc. Duis laoreet enim et justo sagittis, at posuere lectus laoreet. Suspendisse rutrum odio id iaculis varius. Phasellus gravida, mi vel vehicula dignissim, lectus nunc eleifend justo, elementum lacinia enim tellus a nulla. Pellentesque consectetur sollicitudin ante, ac vehicula lorem laoreet laoreet. Fusce consequat libero mi. Quisque luctus risus neque, ut gravida quam tincidunt id. Aliquam id ante arcu. Nulla ut est ipsum. Praesent accumsan efficitur malesuada. Ut tempor auctor felis eu dapibus. Sed felis quam, aliquet sit amet urna nec, consectetur feugiat nibh. Nam id libero nec augue convallis euismod quis vitae nibh. Integer lectus velit, malesuada ut neque mollis, mattis euismod diam. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae; Etiam aliquet porta enim sit amet rhoncus. Curabitur ornare turpis eros, sodales hendrerit tellus rutrum a. Ut efficitur feugiat turpis, eu ultrices velit pharetra non. Curabitur condimentum lacus ac ligula auctor egestas. Aliquam feugiat tellus neque, a ornare tortor imperdiet at. Integer varius turpis eu mi efficitur, at imperdiet ex posuere. Suspendisse blandit, mi at mollis placerat, magna nibh malesuada nisi, ultrices semper augue enim sit amet nisi. Donec molestie tellus vitae risus interdum, nec finibus risus interdum. Integer purus justo, fermentum id urna eu, aliquam rutrum erat. Phasellus volutpat odio metus, sed interdum magna luctus ac. Nam ullamcorper maximus sapien vitae dapibus. Vivamus ullamcorper quis sapien et mattis. Aenean aliquam arcu lacus, vel mollis ligula ultrices nec. Sed cursus placerat tortor elementum tincidunt. Pellentesque at arcu ut felis euismod vestibulum pulvinar nec neque. Quisque ipsum purus, tincidunt quis iaculis eu, malesuada nec lectus. Vivamus tempor, enim quis vestibulum convallis, ex odio pharetra tellus, eget posuere justo ligula sit amet dolor. Cras scelerisque neque id porttitor semper. Sed ut ultrices lorem. Pellentesque sed libero a velit vestibulum fermentum id et velit. Vivamus turpis risus, venenatis ac quam nec, pulvinar fringilla libero. Donec eget vestibulum orci, id lacinia mi. Aenean sed lectus viverra est feugiat suscipit. Proin eget justo turpis. Nullam maximus fringilla sapien, at pharetra odio pretium ut. Cras imperdiet mauris at bibendum dapibus. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Proin elementum condimentum ex, ut tempus nisi. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aenean sed pretium tellus. Vestibulum sollicitudin semper scelerisque. Sed tristique, erat in gravida gravida, felis tortor fermentum ligula, vitae gravida velit ipsum vel magna. Aenean in pharetra ex. Integer porttitor suscipit lectus eget ornare. Maecenas sed metus quis sem dapibus vestibulum vel vitae purus. Etiam sodales nisl at enim tempus, sed malesuada elit accumsan. Aliquam faucibus neque vitae commodo rhoncus. Sed orci sem, varius vitae justo quis, cursus porttitor lectus. Pellentesque eu nibh nunc. Duis laoreet enim et justo sagittis, at posuere lectus laoreet. Suspendisse rutrum odio id iaculis varius. Phasellus gravida, mi vel vehicula dignissim, lectus nunc eleifend justo, elementum lacinia enim tellus a nulla. Pellentesque consectetur sollicitudin ante, ac vehicula lorem laoreet laoreet. Fusce consequat libero mi. Quisque luctus risus neque, ut gravida quam tincidunt id. Aliquam id ante arcu. Nulla ut est ipsum. Praesent accumsan efficitur malesuada. Ut tempor auctor felis eu dapibus. Sed felis quam, aliquet sit amet urna nec, consectetur feugiat nibh. Nam id libero nec augue convallis euismod quis vitae nibh. Integer lectus velit, malesuada ut neque mollis, mattis euismod diam. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae; Etiam aliquet porta enim sit amet rhoncus. Curabitur ornare turpis eros, sodales hendrerit tellus rutrum a. Ut efficitur feugiat turpis, eu ultrices velit pharetra non. Curabitur condimentum lacus ac ligula auctor egestas. Aliquam feugiat tellus neque, a ornare tortor imperdiet at. Integer varius turpis eu mi efficitur, at imperdiet ex posuere. Suspendisse blandit, mi at mollis placerat, magna nibh malesuada nisi, ultrices semper augue enim sit amet nisi. Donec molestie tellus vitae risus interdum, nec finibus risus interdum. Integer purus justo, fermentum id urna eu, aliquam rutrum erat. Phasellus volutpat odio metus, sed interdum magna luctus ac. Nam ullamcorper maximus sapien vitae dapibus. Vivamus ullamcorper quis sapien et mattis. Aenean aliquam arcu lacus, vel mollis ligula ultrices nec. Sed cursus placerat tortor elementum tincidunt. Pellentesque at arcu ut felis euismod vestibulum pulvinar nec neque. Quisque ipsum purus, tincidunt quis iaculis eu, malesuada nec lectus. Vivamus tempor, enim quis vestibulum convallis, ex odio pharetra tellus, eget posuere justo ligula sit amet dolor. Cras scelerisque neque id porttitor semper. Sed ut ultrices lorem. Pellentesque sed libero a velit vestibulum fermentum id et velit. Vivamus turpis risus, venenatis ac quam nec, pulvinar fringilla libero. Donec eget vestibulum orci, id lacinia mi. Aenean sed lectus viverra est feugiat suscipit. Proin eget justo turpis. Nullam maximus fringilla sapien, at pharetra odio pretium ut. Cras imperdiet mauris at bibendum dapibus.
    And the user moves focus to the element    jQuery=.button:contains("Post response")
    Then the user should see the text in the page    Maximum word count exceeded. Please reduce your word count to 400.
    And the user should see the text in the page    This field cannot contain more than 4,000 characters.
    When the user enters text to a text field    css=.editor    this is some response text
    Then the user should not see the text in the page    Maximum word count exceeded. Please reduce your word count to 400.
    And the user should not see the text in the page    This field cannot contain more than 4,000 characters.

Query response can be posted
    [Documentation]    INFUND-4843
    [Tags]
    When the user clicks the button/link    jQuery=.button:contains("Post response")

Query section now becomes read-only
    [Documentation]    INFUND-4843
    [Tags]
    When the user should not see the element    css=.editor

Respond to older query
    [Documentation]    INFUND-4843
    [Tags]
    Given the user clicks the button/link    jQuery=.button.button-secondary:eq(0)
    When the user enters text to a text field    css=.editor    this is some response text for other query
    When the user clicks the button/link    jQuery=.button:contains("Post response")
    When the user should not see the element    css=.editor

Finance checks section status changes to hourglass
    [Documentation]    INFUND-4843
    [Tags]
    When the user clicks the button/link    link=Project setup status
    Then the user should see the element    jQuery=ul li.waiting:nth-child(5)

Queries raised column updates to 'view'
    [Documentation]    INFUND-4843
    [Tags]
    Given log in as a different user    &{internal_finance_credentials}
    When the user navigates to the page    ${server}/project-setup-management/project/${FUNDERS_PANEL_APPLICATION_1_PROJECT}/finance-check
    Then the user should not see the element    link=Awaiting response
    And the user should see the element    jQuery=table.table-progress tr:nth-child(1) td:nth-child(6) a:contains("View")

Project finance user can view the response
    [Documentation]    INFUND-4843
    [Tags]
    [Setup]    log in as a different user    &{internal_finance_credentials}
    Given the user navigates to the page    ${server}/project-setup-management/project/${FUNDERS_PANEL_APPLICATION_1_PROJECT}/finance-check
    When the user clicks the button/link    jQuery=table.table-progress tr:nth-child(1) td:nth-child(6)
    Then the user should see the text in the page    this is some response text

Project finance user can view the finance contact's uploaded files
    [Documentation]    INFUND-4843
    [Tags]
    When the user clicks the button/link    jQuery=a:contains("${valid_pdf}"):nth-of-type(3)
    Then the user should not see an error in the page
    And the user goes back to the previous page ignoring form submission
    When the user clicks the button/link    jQuery=a:contains("${valid_pdf}"):nth-of-type(4)
    Then the user should not see an error in the page
    And the user goes back to the previous page ignoring form submission

Project finance user can continue the conversation
    [Documentation]    INFUND-7752
    [Tags]
    When the user clicks the button/link    jQuery=.button.button-secondary:eq(0)
    And the user enters text to a text field    css=.editor    this is a response to a response
    And the user clicks the button/link    jQuery=.button:contains("Post response")
    Then the user should not see an error in the page
    And the user should not see the element    css=.editor

Finance contact receives an email when a new response is posted
    [Documentation]    INFUND-7753
    [Tags]    Email
    Then the user reads his email    ${test_mailbox_one}+fundsuccess@gmail.com    You have a reply to your query    We have replied to a query regarding your finances

Finance contact can view the new response
    [Documentation]    INFUND-7752
    [Tags]
    Given log in as a different user    ${test_mailbox_one}+fundsuccess@gmail.com    ${short_password}
    When the user clicks the button/link    link=${FUNDERS_PANEL_APPLICATION_1_HEADER}
    And the user clicks the button/link    link=Finance checks
    Then the user should see the text in the page    this is a response to a response

Link to notes from viability section
    [Documentation]    INFUND-4845
    [Tags]
    Given log in as a different user    &{internal_finance_credentials}
    When the user navigates to the page    ${server}/project-setup-management/project/${FUNDERS_PANEL_APPLICATION_1_PROJECT}/finance-check
    And the user clicks the button/link    jQuery=table.table-progress tr:nth-child(1) td:nth-child(2)
    And the user clicks the button/link    jQuery=.button:contains("Notes")
    Then the user should see the text in the page    Use this section to make notes related to the finance checks
    And the user should see the element    jQuery=.button:contains("Create a new note")

Link to notes from eligibility section
    [Documentation]    INFUND-4845
    [Tags]
    Given the user navigates to the page    ${server}/project-setup-management/project/${FUNDERS_PANEL_APPLICATION_1_PROJECT}/finance-check/organisation/22/eligibility
    And the user clicks the button/link    jQuery=.button:contains("Notes")
    Then the user should see the text in the page    Use this section to make notes related to the finance checks
    And the user should see the element    jQuery=.button:contains("Create a new note")

Link to notes from main finance checks summary page
    [Documentation]    INFUND-4845
    [Tags]
    When the user navigates to the page    ${server}/project-setup-management/project/${FUNDERS_PANEL_APPLICATION_1_PROJECT}/finance-check
    And the user clicks the button/link    jQuery=table.table-progress tr:nth-child(1) td:nth-child(7)
    Then the user should see the text in the page    Use this section to make notes related to the finance checks
    And the user should see the element    jQuery=.button:contains("Create a new note")

Large pdf uploads not allowed for notes
    [Documentation]    INFUND-4845
    [Tags]
    Given the user clicks the button/link    jQuery=.button:contains("Create a new note")
    When the user uploads the file     name=attachment    ${too_large_pdf}
    Then the user should see the text in the page    ${too_large_pdf_validation_error}
    [Teardown]    the user goes back to the previous page

Non pdf uploads not allowed for notes
    [Documentation]    INFUND-4845
    [Tags]
    When the user uploads the file      name=attachment    ${text_file}
    Then the user should see the text in the page    ${wrong_filetype_validation_error}

Finance contact can upload a pdf file to notes
    [Documentation]    INFUND-4845
    [Tags]
    Then the user uploads the file      name=attachment   ${valid_pdf}
    And the user should see the text in the page    ${valid_pdf}

Finance contact can remove the file from notes
    [Documentation]    INFUND-4845
    [Tags]
    When the user clicks the button/link    name=removeAttachment
    Then the user should not see the element    jQuery=.extra-margin a:contains("${valid_pdf}")
    And the user should not see an error in the page

Finance contact can re-upload the file to notes
    [Documentation]    INFUND-4845
    [Tags]
    When the user uploads the file    name=attachment    ${valid_pdf}
    Then the user should see the element    jQuery=.extra-margin a:contains("${valid_pdf}")

Finance contact can view the file in notes
    [Documentation]    INFUND-4845
    [Tags]
    Given the user should see the element    link=${valid_pdf}
    And the file has been scanned for viruses
    When the user clicks the button/link    jQuery=.extra-margin a:contains("${valid_pdf}")
    Then the user should not see an error in the page
    And the user goes back to the previous page ignoring form submission
    [Teardown]    the user clicks the button/link   css=button[name='removeAttachment']:nth-last-of-type(1)

Finance contact can upload more than one file to notes
    [Documentation]    INFUND-4845
    [Tags]
    Then the user uploads the file      name=attachment    ${valid_pdf}
    And the user should see the element    jQuery=.extra-margin li:nth-of-type(2) a:contains("${valid_pdf}")

Finance contact can still view both files in notes
    [Documentation]    INFUND-4845
    [Tags]
    When the user clicks the button/link    jQuery=.extra-margin li:nth-of-type(1) a:contains("${valid_pdf}")
    Then the user should not see an error in the page
    And the user goes back to the previous page ignoring form submission
    When the user clicks the button/link    jQuery=.extra-margin li:nth-of-type(2) a:contains("${valid_pdf}")
    Then the user should not see an error in the page
    And the user goes back to the previous page ignoring form submission
    And the user clicks the button/link   css=button[name='removeAttachment']:nth-last-of-type(1)


Create new note server side validations
    [Documentation]    INFUND-4845
    [Tags]
    When the user clicks the button/link    jQuery=.button:contains("Save note")
    Then the user should see the element   jQuery=label[for="noteTitle"] span:nth-child(2) span:contains(This field cannot be left blank.)
    And the user should see the element    jQuery=label[for="note"] span:nth-child(2) span:contains(This field cannot be left blank.)

Create new note client side validations
    [Documentation]    INFUND-4845
    [Tags]
    When the user enters text to a text field    id=noteTitle    this is a title
    Then the user should not see the element    jQuery=label[for="noteTitle"] span:nth-child(2) span:contains(This field cannot be left blank.)
    When the user enters text to a text field    css=.editor    this is some note text
    Then the user should not see the element    jQuery=label[for="note"] span:nth-child(2) span:contains(This field cannot be left blank.)


Word count validations for notes
    [Documentation]    INFUND-4845
    [Tags]
    When the user enters text to a text field    css=.editor    Lorem ipsum dolor sit amet, consectetur adipiscing elit. Proin elementum condimentum ex, ut tempus nisi. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aenean sed pretium tellus. Vestibulum sollicitudin semper scelerisque. Sed tristique, erat in gravida gravida, felis tortor fermentum ligula, vitae gravida velit ipsum vel magna. Aenean in pharetra ex. Integer porttitor suscipit lectus eget ornare. Maecenas sed metus quis sem dapibus vestibulum vel vitae purus. Etiam sodales nisl at enim tempus, sed malesuada elit accumsan. Aliquam faucibus neque vitae commodo rhoncus. Sed orci sem, varius vitae justo quis, cursus porttitor lectus. Pellentesque eu nibh nunc. Duis laoreet enim et justo sagittis, at posuere lectus laoreet. Suspendisse rutrum odio id iaculis varius. Phasellus gravida, mi vel vehicula dignissim, lectus nunc eleifend justo, elementum lacinia enim tellus a nulla. Pellentesque consectetur sollicitudin ante, ac vehicula lorem laoreet laoreet. Fusce consequat libero mi. Quisque luctus risus neque, ut gravida quam tincidunt id. Aliquam id ante arcu. Nulla ut est ipsum. Praesent accumsan efficitur malesuada. Ut tempor auctor felis eu dapibus. Sed felis quam, aliquet sit amet urna nec, consectetur feugiat nibh. Nam id libero nec augue convallis euismod quis vitae nibh. Integer lectus velit, malesuada ut neque mollis, mattis euismod diam. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae; Etiam aliquet porta enim sit amet rhoncus. Curabitur ornare turpis eros, sodales hendrerit tellus rutrum a. Ut efficitur feugiat turpis, eu ultrices velit pharetra non. Curabitur condimentum lacus ac ligula auctor egestas. Aliquam feugiat tellus neque, a ornare tortor imperdiet at. Integer varius turpis eu mi efficitur, at imperdiet ex posuere. Suspendisse blandit, mi at mollis placerat, magna nibh malesuada nisi, ultrices semper augue enim sit amet nisi. Donec molestie tellus vitae risus interdum, nec finibus risus interdum. Integer purus justo, fermentum id urna eu, aliquam rutrum erat. Phasellus volutpat odio metus, sed interdum magna luctus ac. Nam ullamcorper maximus sapien vitae dapibus. Vivamus ullamcorper quis sapien et mattis. Aenean aliquam arcu lacus, vel mollis ligula ultrices nec. Sed cursus placerat tortor elementum tincidunt. Pellentesque at arcu ut felis euismod vestibulum pulvinar nec neque. Quisque ipsum purus, tincidunt quis iaculis eu, malesuada nec lectus. Vivamus tempor, enim quis vestibulum convallis, ex odio pharetra tellus, eget posuere justo ligula sit amet dolor. Cras scelerisque neque id porttitor semper. Sed ut ultrices lorem. Pellentesque sed libero a velit vestibulum fermentum id et velit. Vivamus turpis risus, venenatis ac quam nec, pulvinar fringilla libero. Donec eget vestibulum orci, id lacinia mi. Aenean sed lectus viverra est feugiat suscipit. Proin eget justo turpis. Nullam maximus fringilla sapien, at pharetra odio pretium ut. Cras imperdiet mauris at bibendum dapibus.
    Then the user should see the text in the page    Maximum word count exceeded. Please reduce your word count to 400.
    When the user enters text to a text field    css=.editor    this is some note text
    Then the user should not see the text in the page    Maximum word count exceeded. Please reduce your word count to 400.

New note can be cancelled
    [Documentation]    INFUND-4845
    [Tags]
    When the user clicks the button/link    jQuery=a:contains("Cancel")
    Then the user should not see the text in the page    ${valid_pdf}
    And the user should not see the element    id=noteTitle
    And the user should not see the element    css=.editor


Note can be re-entered
    [Documentation]    INFUND-4845
    [Tags]
    When the user clicks the button/link    jQuery=.button:contains("Create a new note")
    And the user enters text to a text field    id=noteTitle    this is a title
    And the user enters text to a text field    css=.editor    this is some note text
    And the user uploads the file    name=attachment    ${valid_pdf}
    And the user uploads the file    name=attachment    ${valid_pdf}

New note can be posted
    [Documentation]    INFUND-4845
    [Tags]
    When the user clicks the button/link    jQuery=.button:contains("Save note")
    Then the user should see the text in the page    Lee Bowman - Innovate UK (Finance team)

Note sections are no longer editable
    [Documentation]    INFUND-4845
    [Tags]
    When the user should not see the element    css=.editor
    And the user should not see the element    id=noteTitle


Project finance user can comment on the note
    [Documentation]    INFUND-7756
    [Tags]
    When the user should see the text in the page    this is a title
    And the user should see the text in the page    this is some note text
    And the user should see the element    id=post-new-comment

Large pdf uploads not allowed for note comments
    [Documentation]    INFUND-7756
    [Tags]
    Given the user clicks the button/link    id=post-new-comment
    When the user uploads the file     name=attachment    ${too_large_pdf}
    Then the user should see the text in the page    ${too_large_pdf_validation_error}
    [Teardown]    the user goes back to the previous page ignoring form submission

Non pdf uploads not allowed for note comments
    [Documentation]    INFUND-7756
    [Tags]
    When the user uploads the file      name=attachment    ${text_file}
    Then the user should see the text in the page    ${wrong_filetype_validation_error}


Finance contact can upload a pdf file to note comments
    [Documentation]    INFUND-7756
    [Tags]
    Then the user uploads the file      name=attachment   ${valid_pdf}
    And the user should see the text in the page    ${valid_pdf}

Finance contact can remove the file from note comments
    [Documentation]    INFUND-7756
    [Tags]
    When the user clicks the button/link    name=removeAttachment
    Then the user should not see the element    jQuery=.extra-margin a:contains("${valid_pdf}")
    And the user should not see an error in the page

Finance contact can re-upload the file to note comments
    [Documentation]    INFUND-7756
    [Tags]
    When the user uploads the file    name=attachment    ${valid_pdf}
    Then the user should see the element    jQuery=.extra-margin a:contains("${valid_pdf}")

Finance contact can view the file in note comments
    [Documentation]    INFUND-7756
    [Tags]
    Given the user should see the element    link=${valid_pdf}
    And the file has been scanned for viruses
    When the user clicks the button/link    jQuery=.extra-margin a:contains("${valid_pdf}")
    Then the user should not see an error in the page
    And the user goes back to the previous page ignoring form submission
    [Teardown]   the user clicks the button/link   css=button[name='removeAttachment']:nth-last-of-type(1)

Finance contact can upload more than one file to note comments
    [Documentation]    INFUND-7756
    [Tags]
    Then the user uploads the file      name=attachment    ${valid_pdf}
    And the user should see the element    jQuery=.extra-margin li:nth-of-type(2) a:contains("${valid_pdf}")

Finance contact can still view both files in note comments
    [Documentation]    INFUND-7756
    [Tags]
    When the user clicks the button/link    jQuery=.extra-margin li:nth-of-type(1) a:contains("${valid_pdf}")
    Then the user should not see an error in the page
    And the user goes back to the previous page ignoring form submission
    When the user clicks the button/link    jQuery=.extra-margin li:nth-of-type(2) a:contains("${valid_pdf}")
    Then the user should not see an error in the page
    And the user goes back to the previous page ignoring form submission
    And the user clicks the button/link   css=button[name='removeAttachment']:nth-last-of-type(1)


Note comments server side validations
    [Documentation]    INFUND-7756
    [Tags]
    When the user clicks the button/link    jQuery=.button:contains("Save comment")
    Then the user should see the element    jQuery=label[for="comment"] span:nth-child(2) span:contains(This field cannot be left blank.)


Note comments client side validations
    [Documentation]    INFUND-7756
    [Tags]
    When the user enters text to a text field    css=.editor    this is some comment text
    And the user moves focus to the element    jQuery=.button:contains("Save comment")
    Then the user should not see the element    jQuery=label[for="comment"] span:nth-child(2) span:contains(This field cannot be left blank.)

Word count validations for note comments
    [Documentation]    INFUND-7756
    When the user enters text to a text field    css=.editor    Lorem ipsum dolor sit amet, consectetur adipiscing elit. Proin elementum condimentum ex, ut tempus nisi. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aenean sed pretium tellus. Vestibulum sollicitudin semper scelerisque. Sed tristique, erat in gravida gravida, felis tortor fermentum ligula, vitae gravida velit ipsum vel magna. Aenean in pharetra ex. Integer porttitor suscipit lectus eget ornare. Maecenas sed metus quis sem dapibus vestibulum vel vitae purus. Etiam sodales nisl at enim tempus, sed malesuada elit accumsan. Aliquam faucibus neque vitae commodo rhoncus. Sed orci sem, varius vitae justo quis, cursus porttitor lectus. Pellentesque eu nibh nunc. Duis laoreet enim et justo sagittis, at posuere lectus laoreet. Suspendisse rutrum odio id iaculis varius. Phasellus gravida, mi vel vehicula dignissim, lectus nunc eleifend justo, elementum lacinia enim tellus a nulla. Pellentesque consectetur sollicitudin ante, ac vehicula lorem laoreet laoreet. Fusce consequat libero mi. Quisque luctus risus neque, ut gravida quam tincidunt id. Aliquam id ante arcu. Nulla ut est ipsum. Praesent accumsan efficitur malesuada. Ut tempor auctor felis eu dapibus. Sed felis quam, aliquet sit amet urna nec, consectetur feugiat nibh. Nam id libero nec augue convallis euismod quis vitae nibh. Integer lectus velit, malesuada ut neque mollis, mattis euismod diam. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae; Etiam aliquet porta enim sit amet rhoncus. Curabitur ornare turpis eros, sodales hendrerit tellus rutrum a. Ut efficitur feugiat turpis, eu ultrices velit pharetra non. Curabitur condimentum lacus ac ligula auctor egestas. Aliquam feugiat tellus neque, a ornare tortor imperdiet at. Integer varius turpis eu mi efficitur, at imperdiet ex posuere. Suspendisse blandit, mi at mollis placerat, magna nibh malesuada nisi, ultrices semper augue enim sit amet nisi. Donec molestie tellus vitae risus interdum, nec finibus risus interdum. Integer purus justo, fermentum id urna eu, aliquam rutrum erat. Phasellus volutpat odio metus, sed interdum magna luctus ac. Nam ullamcorper maximus sapien vitae dapibus. Vivamus ullamcorper quis sapien et mattis. Aenean aliquam arcu lacus, vel mollis ligula ultrices nec. Sed cursus placerat tortor elementum tincidunt. Pellentesque at arcu ut felis euismod vestibulum pulvinar nec neque. Quisque ipsum purus, tincidunt quis iaculis eu, malesuada nec lectus. Vivamus tempor, enim quis vestibulum convallis, ex odio pharetra tellus, eget posuere justo ligula sit amet dolor. Cras scelerisque neque id porttitor semper. Sed ut ultrices lorem. Pellentesque sed libero a velit vestibulum fermentum id et velit. Vivamus turpis risus, venenatis ac quam nec, pulvinar fringilla libero. Donec eget vestibulum orci, id lacinia mi. Aenean sed lectus viverra est feugiat suscipit. Proin eget justo turpis. Nullam maximus fringilla sapien, at pharetra odio pretium ut. Cras imperdiet mauris at bibendum dapibus. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Proin elementum condimentum ex, ut tempus nisi. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aenean sed pretium tellus. Vestibulum sollicitudin semper scelerisque. Sed tristique, erat in gravida gravida, felis tortor fermentum ligula, vitae gravida velit ipsum vel magna. Aenean in pharetra ex. Integer porttitor suscipit lectus eget ornare. Maecenas sed metus quis sem dapibus vestibulum vel vitae purus. Etiam sodales nisl at enim tempus, sed malesuada elit accumsan. Aliquam faucibus neque vitae commodo rhoncus. Sed orci sem, varius vitae justo quis, cursus porttitor lectus. Pellentesque eu nibh nunc. Duis laoreet enim et justo sagittis, at posuere lectus laoreet. Suspendisse rutrum odio id iaculis varius. Phasellus gravida, mi vel vehicula dignissim, lectus nunc eleifend justo, elementum lacinia enim tellus a nulla. Pellentesque consectetur sollicitudin ante, ac vehicula lorem laoreet laoreet. Fusce consequat libero mi. Quisque luctus risus neque, ut gravida quam tincidunt id. Aliquam id ante arcu. Nulla ut est ipsum. Praesent accumsan efficitur malesuada. Ut tempor auctor felis eu dapibus. Sed felis quam, aliquet sit amet urna nec, consectetur feugiat nibh. Nam id libero nec augue convallis euismod quis vitae nibh. Integer lectus velit, malesuada ut neque mollis, mattis euismod diam. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae; Etiam aliquet porta enim sit amet rhoncus. Curabitur ornare turpis eros, sodales hendrerit tellus rutrum a. Ut efficitur feugiat turpis, eu ultrices velit pharetra non. Curabitur condimentum lacus ac ligula auctor egestas. Aliquam feugiat tellus neque, a ornare tortor imperdiet at. Integer varius turpis eu mi efficitur, at imperdiet ex posuere. Suspendisse blandit, mi at mollis placerat, magna nibh malesuada nisi, ultrices semper augue enim sit amet nisi. Donec molestie tellus vitae risus interdum, nec finibus risus interdum. Integer purus justo, fermentum id urna eu, aliquam rutrum erat. Phasellus volutpat odio metus, sed interdum magna luctus ac. Nam ullamcorper maximus sapien vitae dapibus. Vivamus ullamcorper quis sapien et mattis. Aenean aliquam arcu lacus, vel mollis ligula ultrices nec. Sed cursus placerat tortor elementum tincidunt. Pellentesque at arcu ut felis euismod vestibulum pulvinar nec neque. Quisque ipsum purus, tincidunt quis iaculis eu, malesuada nec lectus. Vivamus tempor, enim quis vestibulum convallis, ex odio pharetra tellus, eget posuere justo ligula sit amet dolor. Cras scelerisque neque id porttitor semper. Sed ut ultrices lorem. Pellentesque sed libero a velit vestibulum fermentum id et velit. Vivamus turpis risus, venenatis ac quam nec, pulvinar fringilla libero. Donec eget vestibulum orci, id lacinia mi. Aenean sed lectus viverra est feugiat suscipit. Proin eget justo turpis. Nullam maximus fringilla sapien, at pharetra odio pretium ut. Cras imperdiet mauris at bibendum dapibus.
    And the user moves focus to the element    jQuery=.button:contains("Save comment")
    Then the user should see the text in the page    Maximum word count exceeded. Please reduce your word count to 400.    # subject to change of course
    And the user should see the text in the page    This field cannot contain more than 4,000 characters.
    When the user enters text to a text field    css=.editor    this is some comment text
    Then the user should not see the text in the page    Maximum word count exceeded. Please reduce your word count to 400.    # subject to change of course
    And the user should not see the text in the page    This field cannot contain more than 4,000 characters.

Note comment can be posted
    [Documentation]    INFUND-7756
    [Tags]
    When the user clicks the button/link    jQuery=.button:contains("Save comment")

Note comment section now becomes read-only
    [Documentation]    INFUND-7756
    [Tags]
    When the user should not see the element    css=.editor

Project Finance user can Edit, Save, Change selection from 0% to 20% for Lead-Partner to Calculate overhead, contains spreadsheet when uploaded
    [Documentation]     INFUND-7577
    [Tags]    HappyPath
    Given the user navigates to the page    ${server}/project-setup-management/project/${FUNDERS_PANEL_APPLICATION_1_PROJECT}/finance-check
    When the user clicks the button/link    css=a.eligibility-0
    When the user clicks the button/link    jQuery=section:nth-of-type(2) button:contains("Overhead costs")
    Then the user should see the element    jQuery=label[data-target="overhead-none"]
    And the user should see the element     jQuery=label[data-target="overhead-default-percentage"]
    And the user should see the element     jQuery=label[data-target="overhead-total"]
    And the user clicks the button/link     jQuery=section:nth-of-type(2) a:contains("Edit")
    When the user clicks the button/link    jQuery=label[data-target="overhead-default-percentage"]
    Then the user should see the element    jQuery=section:nth-of-type(2) button span:contains("£ 924")
    And the user should see the element     jQuery=p:contains("There is no need to provide any further supporting documentation or calculations. Actual costs can be claimed up to a maximum of this calculated figure.")
    Then the user should see the element    jQuery=section:nth-of-type(2) input[id^="section-total"][id$="default"]
    And the user should see the element     jQuery=section:nth-of-type(2) button:contains("Save")
    And the user clicks the button/link     jQuery=section:nth-of-type(2) button:contains("Save")
    Then the user should see the element    jQuery=section:nth-of-type(2) button span:contains("20%")
    And the user should see the element     jQuery=section:nth-of-type(2) button span:contains("£ 924")
    And the user should see the element     jQuery=section:nth-of-type(2) a:contains("Edit")
    Then the user should see the element    jQuery=label[for="total-cost"]
    And the user should see the element     jQuery=input[id^="total-cost"][value="£ 302,279"]
    When the user clicks the button/link    jQuery=section:nth-of-type(2) a:contains("Edit")
    And the user clicks the button/link     jQuery=label[data-target="overhead-none"]
    Then the user should see the element    jQuery=h3:contains("No overhead costs")
    And the user should see the element     jQuery=p:contains("You are not currently applying for overhead costs")
    And the user should see the element     jQuery=section:nth-of-type(2) button:contains("Save")
    Then the user clicks the button/link    jQuery=section:nth-of-type(2) button:contains("Save")
    Then the user should see the element    jQuery=section:nth-of-type(2) button span:contains("0%")
    And the user should see the element     jQuery=section:nth-of-type(2) button:contains("£ 0")
    When the user clicks the button/link    jQuery=section:nth-of-type(2) a:contains("Edit")
    Then the user clicks the button/link    jQuery=label[data-target="overhead-total"]
    And the user should see the element     jQuery=h3:contains("Uploaded spreadsheet")
    And the user should see the element     jQuery=section:nth-of-type(2) label:nth-child(1):contains("Enter the total cost of overheads as calculated in the spreadsheet £")
    When the user clicks the button/link    jQuery=section:nth-of-type(2) button:contains("Save")
    Then the user should see the element    jQuery=section:nth-of-type(2) button span:contains("0%")
    And the user should see the element     jQuery=section:nth-of-type(2) button span:contains("£ 0")
    When the user clicks the button/link    jQuery=section:nth-of-type(2) a:contains("Edit")
    Then the user enters text to a text field     jQuery=section:nth-of-type(2) input[id^="cost-overheads"][id$="calculate"]  rkk6382DJJ%$^&*£@W
    And the user clicks the button/link     jQuery=section:nth-of-type(2) button:contains("Save")
    Then the user clicks the button/link    jQuery=.button-secondary:contains("Return to finance checks")

Project Finance user can provide overhead value for Lead-Partner manually instead of calculations from spreadsheet.
    [Documentation]     INFUND-7577
    [Tags]    HappyPath
    When the user clicks the button/link    css=a.eligibility-0
    When the user clicks the button/link    jQuery=section:nth-of-type(2) a:contains("Edit")
    Then the user enters text to a text field     jQuery=section:nth-of-type(2) input[id^="cost-overheads"][id$="calculate"]  1954
    And the user clicks the button/link     jQuery=section:nth-of-type(2) button:contains("Save")
    Then the user should see the element    jQuery=section:nth-of-type(2) button span:contains("£ 1,954")
    And the user should see the element     jQuery=section:nth-of-type(2) button span:contains("42%")
    When the user clicks the button/link    jQuery=section:nth-of-type(2) button:contains("Overhead costs")
    Then the user should see the element    jQuery=label[for="total-cost"]
    And the user should see the element     jQuery=input[id^="total-cost"][value="£ 303,309"]
    Then the user clicks the button/link    jQuery=.button-secondary:contains("Return to finance checks")

Project Finance user can Edit, Save, Change selection from 0% to 20% for Partner to Calculate overhead, contains spreadsheet when uploaded
    [Documentation]     INFUND-7577
    [Tags]    HappyPath
    When the user clicks the button/link    css=a.eligibility-1
    When the user clicks the button/link    jQuery=section:nth-of-type(2) button:contains("Overhead costs")
    Then the user should see the element    jQuery=label[data-target="overhead-none"]
    And the user should see the element     jQuery=label[data-target="overhead-default-percentage"]
    And the user should see the element     jQuery=label[data-target="overhead-total"]
    And the user clicks the button/link     jQuery=section:nth-of-type(2) a:contains("Edit")
    When the user clicks the button/link    jQuery=label[data-target="overhead-default-percentage"]
    Then the user should see the element    jQuery=section:nth-of-type(2) button span:contains("£ 616")
    And the user should see the element     jQuery=p:contains("There is no need to provide any further supporting documentation or calculations. Actual costs can be claimed up to a maximum of this calculated figure.")
    Then the user should see the element    jQuery=section:nth-of-type(2) input[id^="section-total"][id$="default"]
    And the user should see the element     jQuery=section:nth-of-type(2) button:contains("Save")
    And the user clicks the button/link     jQuery=section:nth-of-type(2) button:contains("Save")
    Then the user should see the element    jQuery=section:nth-of-type(2) button span:contains("20%")
    And the user should see the element     jQuery=section:nth-of-type(2) button span:contains("£ 616")
    And the user should see the element     jQuery=section:nth-of-type(2) a:contains("Edit")
    Then the user should see the element    jQuery=label[for="total-cost"]
    And the user should see the element     jQuery=input[id^="total-cost"][value="£ 201,520"]
    #the above value should be £201,519
    When the user clicks the button/link    jQuery=section:nth-of-type(2) a:contains("Edit")
    And the user clicks the button/link     jQuery=label[data-target="overhead-none"]
    Then the user should see the element    jQuery=h3:contains("No overhead costs")
    And the user should see the element     jQuery=p:contains("You are not currently applying for overhead costs")
    And the user should see the element     jQuery=section:nth-of-type(2) button:contains("Save")
    Then the user clicks the button/link    jQuery=section:nth-of-type(2) button:contains("Save")
    Then the user should see the element    jQuery=section:nth-of-type(2) button span:contains("0%")
    And the user should see the element     jQuery=section:nth-of-type(2) button:contains("£ 0")
    When the user clicks the button/link    jQuery=section:nth-of-type(2) a:contains("Edit")
    Then the user clicks the button/link    jQuery=label[data-target="overhead-total"]
    And the user should see the element     jQuery=h3:contains("Uploaded spreadsheet")
    And the user should see the element     jQuery=section:nth-of-type(2) label:nth-child(1):contains("Enter the total cost of overheads as calculated in the spreadsheet £")
    When the user clicks the button/link    jQuery=section:nth-of-type(2) button:contains("Save")
    Then the user should see the element    jQuery=section:nth-of-type(2) button span:contains("0%")
    And the user should see the element     jQuery=section:nth-of-type(2) button span:contains("£ 0")
    When the user clicks the button/link    jQuery=section:nth-of-type(2) a:contains("Edit")
    Then the user enters text to a text field     jQuery=section:nth-of-type(2) input[id^="cost-overheads"][id$="calculate"]  430KFL$%£@%&*@£$%^0
    And the user clicks the button/link     jQuery=section:nth-of-type(2) button:contains("Save")
    Then the user clicks the button/link    jQuery=.button-secondary:contains("Return to finance checks")

Project Finance user can provide overhead value for Partner manually instead of calculations from spreadsheet.
    [Documentation]     INFUND-7577
    [Tags]    HappyPath
    When the user clicks the button/link    css=a.eligibility-1
    When the user clicks the button/link    jQuery=section:nth-of-type(2) a:contains("Edit")
    Then the user enters text to a text field     jQuery=section:nth-of-type(2) input[id^="cost-overheads"][id$="calculate"]  1078
    And the user clicks the button/link     jQuery=section:nth-of-type(2) button:contains("Save")
    Then the user should see the element    jQuery=section:nth-of-type(2) button span:contains("£ 1,078")
    And the user should see the element     jQuery=section:nth-of-type(2) button span:contains("35%")
    When the user clicks the button/link    jQuery=section:nth-of-type(2) button:contains("Overhead costs")
    Then the user should see the element    jQuery=label[for="total-cost"]
    And the user should see the element     jQuery=input[id^="total-cost"][value="£ 201,981"]
    Then the user clicks the button/link    jQuery=.button-secondary:contains("Return to finance checks")

Project Finance user can view academic Jes form
    [Documentation]     INFUND-5220,    INFUND-7577
    [Tags]    HappyPath
    # note that we are viewing the file above rather than the same project as the other tests in this suite due to INFUND-6724
    When the user clicks the button/link    css=a.eligibility-2
    Then the user should see the text in the page    Download Je-S form
    When the user clicks the button/link    link=jes-form80.pdf
    Then the user should not see an error in the page
    [Teardown]    the user navigates to the page    ${server}/project-setup-management/project/${FUNDERS_PANEL_APPLICATION_1_PROJECT}/finance-check


Viability checks are populated in the table
    [Documentation]    INFUND-4822, INFUND-7095
    [Tags]
    And the user should see the text in the element    jQuery=table.table-progress tr:nth-child(1) td:nth-child(2)    Review
    And the user should see the text in the element    jQuery=table.table-progress tr:nth-child(1) td:nth-child(3)    Not set
    And the user should see the text in the element    jQuery=table.table-progress tr:nth-child(2) td:nth-child(2)    Review
    And the user should see the text in the element    jQuery=table.table-progress tr:nth-child(2) td:nth-child(3)    Not set
    And the user should see the text in the element    jQuery=table.table-progress tr:nth-child(3) td:nth-child(2)    N/A
    And the user should see the text in the element    jQuery=table.table-progress tr:nth-child(3) td:nth-child(3)    N/A

Project finance user can see the viability check page for the lead partner
    [Documentation]    INFUND-4831, INFUND-4830, INFUND-4825
    [Tags]
    when the user clicks the button/link    jQuery=table.table-progress tr:nth-child(1) td:nth-child(2) a:contains("Review")    # clicking the review button for the lead partner
    Then the user should see the text in the page    ${PROJECT_SETUP_APPLICATION_1_LEAD_ORGANISATION_NAME}
    And the user should see the text in the page    ${PROJECT_SETUP_APPLICATION_1_LEAD_ORGANISATION_COMPANY_NUMBER}


Project finance user can see the lead partner's information
    [Documentation]    INFUND-4825, INFUND-7577
    [Tags]
    # Note the below figures aren't calculated, but simply brought forward from user-entered input during the application phase
    When the user should see the text in the element    jQuery=.table-overview tr:nth-child(1) td:nth-child(1)    £303,309
    When the user should see the text in the element    jQuery=.table-overview tr:nth-child(1) td:nth-child(2)    30%
    When the user should see the text in the element    jQuery=.table-overview tr:nth-child(1) td:nth-child(3)    £212,316
    When the user should see the text in the element    jQuery=.table-overview tr:nth-child(1) td:nth-child(4)    £87,291
    When the user should see the text in the element    jQuery=.table-overview tr:nth-child(1) td:nth-child(5)    £3,702

Checking the approve viability checkbox enables RAG selection but not confirm viability button
    [Documentation]    INFUND-4831, INFUND-4856, INFUND-4830
    [Tags]
    When the user selects the checkbox    project-viable
    Then the user should see the element    id=rag-rating
    And the user should see the element    jQuery=.button.disabled:contains("Confirm viability")


RAG choices update on the finance checks page
    [Documentation]    INFUND-4822, INFUND-4856
    [Tags]
    When the rag rating updates on the finance check page for lead for viability   Green
    And the rag rating updates on the finance check page for lead for viability   Amber
    And the rag rating updates on the finance check page for lead for viability   Red
    When the user selects the option from the drop-down menu    --    id=rag-rating
    Then the user should see the element    jQuery=.button.disabled:contains("Confirm viability")
    [Teardown]    the user selects the option from the drop-down menu    Green    id=rag-rating

Credit report information saves when leaving the page
    [Documentation]    INFUND-4829
    [Tags]
    When the user selects the checkbox    creditReportConfirmed
    And the user clicks the button/link    jQuery=.button-secondary:contains("Save and return to finance checks")
    And the user clicks the button/link    jQuery=table.table-progress tr:nth-child(1) td:nth-child(2) a:contains("Review")
    Then checkbox should be selected    creditReportConfirmed

Clicking cancel on the viability modal
    [Documentation]    INFUND-4822, INFUND-4830
    [Tags]
    When the user clicks the button/link    jQuery=.button:contains("Confirm viability")
    And the user clicks the button/link    jQuery=.buttonlink.js-close    # Clicking the cancel link on the modal
    Then the user should see the element    id=rag-rating
    And the user should see the checkbox    creditReportConfirmed
    And the user should see the checkbox    confirmViabilityChecked
    And the user should see the element    jQuery=.button-secondary:contains("Save and return to finance checks")


Confirming viability should show credit report info on a readonly page
    [Documentation]    INFUND-4829, INFUND-4830
    [Tags]
    When the user clicks the button/link    jQuery=.button:contains("Confirm viability")
    And the user clicks the button/link    name=confirm-viability    # Clicking the confirm button on the modal
    Then the user should see the element    jQuery=.button-secondary:contains("Return to finance checks")
    And the user should not see the element    id=rag-rating
    And the user should not see the checkbox    confirmViabilityChecked
    And the user should see the text in the page    A credit report has been used together with the viability information shown here. This information is kept in accordance with Innovate UK audit requirements.
    And the user should see that the checkbox is disabled    creditReportConfirmed


Confirming viability should update on the finance checks page
    [Documentation]    INFUND-4831, INFUND-4822
    [Tags]
    When the user clicks the button/link    link=Finance checks
    Then the user should see the element    jQuery=table.table-progress tr:nth-child(1) td:nth-child(2) a:contains("Approved")

Project finance user can see the viability checks for the industrial partner
    [Documentation]    INFUND-4831, INFUND-4830
    [Tags]
    When the user clicks the button/link    jQuery=table.table-progress tr:nth-child(2) td:nth-child(2) a:contains("Review")
    Then the user should see the text in the page    ${PROJECT_SETUP_APPLICATION_1_PARTNER_NAME}
    And the user should see the text in the page    ${PROJECT_SETUP_APPLICATION_1_PARTNER_COMPANY_NUMBER}

Checking the approve viability checkbox enables RAG selection but not confirm viability button for partner
    [Documentation]    INFUND-4831, INFUND-4856, INFUND-4830
    [Tags]
    When the user selects the checkbox    project-viable
    Then the user should see the element    id=rag-rating
    And the user should see the element    jQuery=.button.disabled:contains("Confirm viability")

RAG choices update on the finance checks page for partner
    [Documentation]    INFUND-4822, INFUND-4856
    [Tags]
    When the rag rating updates on the finance check page for partner for viability    Green
    And the rag rating updates on the finance check page for partner for viability      Amber
    And the rag rating updates on the finance check page for partner for viability      Red
    When the user selects the option from the drop-down menu    --    id=rag-rating
    Then the user should see the element    jQuery=.button.disabled:contains("Confirm viability")
    [Teardown]    the user selects the option from the drop-down menu    Green    id=rag-rating

Credit report information saves when leaving the page for partner
    [Documentation]    INFUND-4829
    [Tags]
    When the user selects the checkbox    creditReportConfirmed
    And the user clicks the button/link    jQuery=.button-secondary:contains("Save and return to finance checks")
    And the user clicks the button/link    jQuery=table.table-progress tr:nth-child(2) td:nth-child(2) a:contains("Review")
    Then checkbox should be selected    creditReportConfirmed

Clicking cancel on the viability modal for partner
    [Documentation]    INFUND-4822, INFUND-4830
    [Tags]
    When the user clicks the button/link    jQuery=.button:contains("Confirm viability")
    And the user clicks the button/link    jQuery=.buttonlink.js-close    # Clicking the cancel link on the modal
    Then the user should see the element    id=rag-rating
    And the user should see the checkbox    creditReportConfirmed
    And the user should see the checkbox    confirmViabilityChecked
    And the user should see the element    jQuery=.button-secondary:contains("Save and return to finance checks")

Confirming viability should show credit report info on a readonly page for partner
    [Documentation]    INFUND-4829, INFUND-4830
    [Tags]
    ${today} =  get today
    When the user clicks the button/link    jQuery=.button:contains("Confirm viability")
    And the user clicks the button/link    name=confirm-viability    # Clicking the confirm button on the modal
    Then the user should see the element    jQuery=.button-secondary:contains("Return to finance checks")
    And the user should see the text in the page  The partner's finance viability has been approved by Lee Bowman, ${today}
    And the user should not see the element    id=rag-rating
    And the user should not see the checkbox    confirmViabilityChecked
    And the user should see the text in the page    A credit report has been used together with the viability information shown here. This information is kept in accordance with Innovate UK audit requirements.
    And the user should see that the checkbox is disabled    creditReportConfirmed

Confirming viability should update on the finance checks page for partner
    [Documentation]    INFUND-4831, INFUND-4822
    [Tags]
    When the user clicks the button/link    link=Finance checks
    Then the user should see the element    jQuery=table.table-progress tr:nth-child(2) td:nth-child(2) a:contains("Approved")


Eligibility checks are populated in the table
    [Documentation]    INFUND-4823
    [Tags]
    And the user should see the text in the element    jQuery=table.table-progress tr:nth-child(1) td:nth-child(4)    Review
    And the user should see the text in the element    jQuery=table.table-progress tr:nth-child(1) td:nth-child(5)    Not set
    And the user should see the text in the element    jQuery=table.table-progress tr:nth-child(2) td:nth-child(4)    Review
    And the user should see the text in the element    jQuery=table.table-progress tr:nth-child(2) td:nth-child(5)    Not set
    And the user should see the text in the element    jQuery=table.table-progress tr:nth-child(3) td:nth-child(4)    Review
    And the user should see the text in the element    jQuery=table.table-progress tr:nth-child(3) td:nth-child(5)    Not set

Project finance user can see the Eligibility check page for the lead partner
    [Documentation]    INFUND-4823
    [Tags]
    When the user clicks the button/link    jQuery=table.table-progress tr:nth-child(1) td:nth-child(4) a:contains("Review")    # clicking the review button for the lead partner
    Then the user should see the text in the page    ${PROJECT_SETUP_APPLICATION_1_LEAD_ORGANISATION_NAME}

Project finance user can see the lead partner's information about eligibility
    [Documentation]    INFUND-4832, INFUND-7577
    [Tags]
    # Note the below figures aren't calculated, but simply brought forward from user-entered input during the application phase
    When the user should see the text in the element    jQuery=.table-overview tbody tr:nth-child(1) td:nth-child(1)    3 months
    When the user should see the text in the element    jQuery=.table-overview tbody tr:nth-child(1) td:nth-child(2)    £ 303,309
    When the user should see the text in the element    jQuery=.table-overview tbody tr:nth-child(1) td:nth-child(3)    30%
    When the user should see the text in the element    jQuery=.table-overview tbody tr:nth-child(1) td:nth-child(4)    £ 90,993
    When the user should see the text in the element    jQuery=.table-overview tbody tr:nth-child(1) td:nth-child(5)    £ 3,702
    When the user should see the text in the element    jQuery=.table-overview tbody tr:nth-child(1) td:nth-child(6)    £ 208,614


Finance checks eligibility validations
    [Documentation]    INFUND-4833
    [Tags]
    When the user clicks the button/link             jQuery=section:nth-of-type(1) button:contains("Labour")
    And the user clicks the button/link              jQuery=section:nth-of-type(1) a:contains("Edit")
    When the user enters text to a text field        css=[name^="labour-labourDaysYearly"]    -230
    Then the user should see the text in the page    This field should be 1 or higher
    When the user clicks the button/link             jQuery=section:nth-of-type(1) button[name=save-eligibility]
    Then the user should see the text in the page    This field should be 1 or higher
    And the user clicks the button/link             jQuery=section:nth-of-type(1) button:contains("Labour")
    And the user reloads the page
    When the user clicks the button/link             jQuery=section:nth-of-type(3) button:contains("Materials")
    And the user clicks the button/link              jQuery=section:nth-of-type(3) a:contains("Edit")
    When the user clicks the button/link             jQuery=section:nth-of-type(3) button[name=add_cost]
    When the user enters text to a text field        css=#material-costs-table tbody tr:nth-of-type(4) td:nth-of-type(2) input    100
    And the user clicks the button/link              jQuery=section:nth-of-type(3) button[name=save-eligibility]
    Then the user should see the text in the page    This field cannot be left blank
    And the user clicks the button/link             jQuery=section:nth-of-type(3) button:contains("Materials")
    And the user reloads the page
    When the user clicks the button/link             jQuery=section:nth-of-type(4) button:contains("Capital usage")
    And the user clicks the button/link              jQuery=section:nth-of-type(4) a:contains("Edit")
    When the user enters text to a text field        css=section:nth-of-type(4) #capital_usage div:nth-child(1) div:nth-of-type(6) input   200
    Then the user should see the text in the page    This field should be 100 or lower
    And the user clicks the button/link             jQuery=section:nth-of-type(4) button:contains("Capital usage")
    And the user reloads the page
    When the user clicks the button/link             jQuery=section:nth-of-type(6) button:contains("Travel and subsistence")
    And the user clicks the button/link              jQuery=section:nth-of-type(6) a:contains("Edit")
    When the user clicks the button/link            jQuery=section:nth-of-type(6) button[name=add_cost]
    And the user enters text to a text field         css=#travel-costs-table tbody tr:nth-of-type(4) td:nth-of-type(2) input    123
    When the user clicks the button/link             jQuery=section:nth-of-type(6) button[name=save-eligibility]
    Then the user should see the text in the page     This field cannot be left blank
    And the user clicks the button/link             jQuery=section:nth-of-type(6) button:contains("Travel and subsistence")
    And the user reloads the page
    When the user clicks the button/link             jQuery=section:nth-of-type(7) button:contains("Other costs")
    And the user clicks the button/link              jQuery=section:nth-of-type(7) a:contains("Edit")
    When the user clicks the button/link            jQuery=section:nth-of-type(7) button[name=add_cost]
    And the user enters text to a text field        jQuery=#other-costs-table tr:nth-child(2) td:nth-child(2) input  5000
    When the user clicks the button/link           jQuery=section:nth-of-type(7) button[name=save-eligibility]
    Then the user should see the text in the page    This field cannot be left blank
    And the user clicks the button/link             jQuery=section:nth-of-type(7) button:contains("Other costs")
    When the user clicks the button/link             link=Finance checks
    When the user clicks the button/link             jQuery=table.table-progress tr:nth-child(1) td:nth-child(4) a:contains("Review")

Project finance user can amend all sections of eligibility for lead
    [Documentation]    INFUND-4834
    [Tags]
    When Project finance user amends labour details in eligibility for lead
    And Project finance user amends materials details in eligibility for lead
    And Project finance user amends capital usage details in eligibility for lead
    And Project finance user amends subcontracting usage details in eligibility for lead
    And Project finance user amends travel details in eligibility for lead
    And Project finance user amends other costs details in eligibility for lead

Checking the approve eligibility checkbox enables RAG selection but not Approve eligibility button
    [Documentation]    INFUND-4839
    [Tags]
    When the user selects the checkbox    project-eligible
    Then the user should see the element    id=rag-rating
    And the user should see the element    jQuery=.button.disabled:contains("Approve eligible costs")


RAG choices update on the finance checks page for eligibility
    [Documentation]    INFUND-4839, INFUND-4823
    [Tags]
    When the rag rating updates on the finance check page for lead for eligibility   Green
    And the rag rating updates on the finance check page for lead for eligibility    Amber
    And the rag rating updates on the finance check page for lead for eligibility   Red
    When the user selects the option from the drop-down menu    --    id=rag-rating
    Then the user should see the element    jQuery=.button.disabled:contains("Approve eligible costs")

Clicking cancel on the eligibility modal
    [Documentation]    INFUND-4839
    [Tags]
    When the user selects the option from the drop-down menu    Green    id=rag-rating
    And the user clicks the button/link    jQuery=.button:contains("Approve eligible costs")
    And the user clicks the button/link    jQuery=.buttonlink.js-close    # Clicking the cancel link on the modal
    Then the user should see the element    id=rag-rating
    And the user should see the checkbox    project-eligible
    And the user should see the element    jQuery=.button-secondary:contains("Return to finance checks")

Confirming eligibility should show info on a readonly page
    [Documentation]    INFUND-4839, INFUND-7574
    [Tags]
    ${today} =  get today
    When the user clicks the button/link    jQuery=.button:contains("Approve eligible costs")
    And the user clicks the button/link    name=confirm-eligibility    # Clicking the confirm button on the modal
    Then the user should see the element    jQuery=a.button-secondary:contains("Return to finance checks")
    And the user should see the text in the page  The partner's finance eligibility has been approved by Lee Bowman, ${today}
    And the user should not see the element    id=rag-rating
    And the user should not see the checkbox    project-eligible

Confirming eligibility should update on the finance checks page
    [Documentation]    INFUND-4823
    [Tags]
    When the user clicks the button/link    jQuery=.button-secondary:contains("Return to finance checks")
    Then the user should see the element    jQuery=table.table-progress tr:nth-child(1) td:nth-child(4) a:contains("Approved")

Project finance user can see updated finance overview after lead changes to eligibility
    [Documentation]    INFUND-5508
    [Tags]
    When the user navigates to the page    ${server}/project-setup-management/project/${FUNDERS_PANEL_APPLICATION_1_PROJECT}/finance-check
    Then the user should see the text in the element    jQuery=.table-overview tr:nth-child(1) td:nth-child(3)    £ 406,806
    And the user should see the text in the element    jQuery=.table-overview tr:nth-child(1) td:nth-child(4)    £ 116,565
    And the user should see the text in the element    jQuery=.table-overview tr:nth-child(1) td:nth-child(6)    29%


Project finance user can see the Eligibility check page for the partner
    [Documentation]    INFUND-4823
    [Tags]
    Given the user navigates to the page    ${server}/project-setup-management/project/${FUNDERS_PANEL_APPLICATION_1_PROJECT}/finance-check
    When the user clicks the button/link    jQuery=table.table-progress tr:nth-child(2) td:nth-child(4) a:contains("Review")
    Then the user should see the text in the page    ${PROJECT_SETUP_APPLICATION_1_PARTNER_NAME}

Project finance user can see the partner's information about eligibility
    [Documentation]    INFUND-4832, INFUND-7577
    [Tags]
    # Note the below figures aren't calculated, but simply brought forward from user-entered input during the application phase
    When the user should see the text in the element    jQuery=.table-overview tbody tr:nth-child(1) td:nth-child(1)    3 months
    When the user should see the text in the element    jQuery=.table-overview tbody tr:nth-child(1) td:nth-child(2)    £ 201,981
    When the user should see the text in the element    jQuery=.table-overview tbody tr:nth-child(1) td:nth-child(3)    30%
    When the user should see the text in the element    jQuery=.table-overview tbody tr:nth-child(1) td:nth-child(4)    £ 60,594
    When the user should see the text in the element    jQuery=.table-overview tbody tr:nth-child(1) td:nth-child(5)    £ 2,468
    When the user should see the text in the element    jQuery=.table-overview tbody tr:nth-child(1) td:nth-child(6)    £ 138,919


Project finance user can amend all sections of eligibility for partner
    [Documentation]    INFUND-4834
    [Tags]
    When Project finance user amends labour details in eligibility for partner
    And Project finance user amends materials details in eligibility for partner
    And Project finance user amends capital usage details in eligibility for partner
    And Project finance user amends subcontracting usage details in eligibility for partner
    And Project finance user amends travel details in eligibility for partner
    And Project finance user amends other costs details in eligibility for partner

Project finance user can see the eligibility checks for the industrial partner
    [Documentation]    INFUND-4823
    [Tags]
    When the user clicks the button/link   link=Finance checks
    And the user clicks the button/link    jQuery=table.table-progress tr:nth-child(2) td:nth-child(4) a:contains("Review")
    Then the user should see the text in the page    ${PROJECT_SETUP_APPLICATION_1_PARTNER_NAME}

Checking the approve eligibility checkbox enables RAG selection but not confirm viability button for partner
    [Documentation]    INFUND-4839
    [Tags]
    When the user selects the checkbox    project-eligible
    Then the user should see the element    id=rag-rating
    And the user should see the element    jQuery=.button.disabled:contains("Approve eligible costs")

RAG choices update on the finance checks page for eligibility for partner
    [Documentation]    INFUND-4839, INFUND-4823
    [Tags]
    When the rag rating updates on the finance check page for partner for eligibility   Green
    And the rag rating updates on the finance check page for partner for eligibility    Amber
    And the rag rating updates on the finance check page for partner for eligibility    Red
    When the user selects the option from the drop-down menu    --    id=rag-rating
    Then the user should see the element    jQuery=.button.disabled:contains("Approve eligible costs")

Clicking cancel on the eligibility modal for partner
    [Documentation]    INFUND-4839
    [Tags]
    When the user selects the option from the drop-down menu    Green    id=rag-rating
    And the user clicks the button/link    jQuery=.button:contains("Approve eligible costs")
    And the user clicks the button/link    jQuery=.buttonlink.js-close    # Clicking the cancel link on the modal
    Then the user should see the element    id=rag-rating
    And the user should see the checkbox    project-eligible
    And the user should see the element    jQuery=.button-secondary:contains("Return to finance checks")

Confirming eligibility should show info on a readonly page for partner
    [Documentation]    INFUND-4839, INFUND-7574
    [Tags]
    ${today} =  get today
    When the user clicks the button/link    jQuery=.button:contains("Approve eligible costs")
    And the user clicks the button/link    name=confirm-eligibility    # Clicking the confirm button on the modal
    Then the user should see the element    jQuery=.button-secondary:contains("Return to finance checks")
    And the user should see the text in the page  The partner's finance eligibility has been approved by Lee Bowman, ${today}
    And the user should not see the element    id=rag-rating
    And the user should not see the checkbox    project-eligible

Confirming eligibility should update on the finance checks page
    [Documentation]    INFUND-4823, INFUND-7076
    [Tags]
    When the user clicks the button/link    link=Finance checks
    Then the user should see the element    jQuery=table.table-progress tr:nth-child(2) td:nth-child(4) a:contains("Approved")
    And The user should see the element    jQuery=.generate-spend-profile-main-button
    And the user should see the element    xpath=//*[@class='button generate-spend-profile-main-button' and @disabled='disabled']

Project finance user can see updated finance overview after partner changes to eligibility
    [Documentation]    INFUND-5508
    [Tags]
    When the user navigates to the page    ${server}/project-setup-management/project/${FUNDERS_PANEL_APPLICATION_1_PROJECT}/finance-check/
    Then the user should see the text in the element    jQuery=.table-overview tr:nth-child(1) td:nth-child(3)    £ 311,081
    And the user should see the text in the element    jQuery=.table-overview tr:nth-child(1) td:nth-child(4)    £ 87,847
    And the user should see the text in the element    jQuery=.table-overview tr:nth-child(1) td:nth-child(6)    28%

Project finance can approve academic eligibility
    [Documentation]    INFUND-4428
    [Tags]      HappyPath
    ${today} =  get today
    When the user clicks the button/link     jQuery=table.table-progress tr:nth-child(3) td:nth-child(4) a:contains("Review")
    Then the user should see the text in the page   Je-S Form overview
    When the user selects the checkbox    project-eligible
    When the user selects the option from the drop-down menu    Green    id=rag-rating
    And the user clicks the button/link    jQuery=.button:contains("Approve eligible costs")
    And the user clicks the button/link    name=confirm-eligibility    # Clicking the confirm button on the modal
    And the user should see the text in the page  The partner's finance eligibility has been approved by Lee Bowman, ${today}
    And the user should not see the element    id=rag-rating
    And the user should not see the checkbox    project-eligible
    When the user clicks the button/link    link=Finance checks


Links to other sections in Project setup dependent on project details (applicable for Lead/ partner)
    [Documentation]    INFUND-4428
    [Tags]
    [Setup]    Log in as a different user    jessica.doe@ludlow.co.uk    Passw0rd
    When the user clicks the button/link    link=${FUNDERS_PANEL_APPLICATION_1_HEADER}
    And the user should see the element    jQuery=ul li.complete:nth-child(1)
    And the user should see the text in the page    Successful application
    And the user should see the element    jQuery=ul li.complete:nth-child(2)
    And the user should see the element    jQuery=ul li.complete:nth-child(4)
    And the user should see the element    jQuery=ul li.complete:nth-child(5)
    And the user should see the element    jQuery=ul li.read-only:nth-child(6)

Status updates correctly for internal user's table
     [Documentation]    INFUND-4049,INFUND-5543
     [Tags]      HappyPath
     [Setup]    log in as a different user   &{Comp_admin1_credentials}
     When the user navigates to the page    ${server}/project-setup-management/competition/${FUNDERS_PANEL_COMPETITION}/status
     Then the user should see the element    jQuery=#table-project-status tr:nth-of-type(1) td:nth-of-type(1).status.ok      # Project details
     And the user should see the element    jQuery=#table-project-status tr:nth-of-type(1) td:nth-of-type(2).status.action      # MO
     And the user should see the element    jQuery=#table-project-status tr:nth-of-type(1) td:nth-of-type(3).status       # Bank details
     And the user should see the element    jQuery=#table-project-status tr:nth-of-type(1) td:nth-of-type(4).status.action     # Finance checks are actionable from the start-workaround for Private beta assessment
     And the user should see the element    jQuery=#table-project-status tr:nth-of-type(1) td:nth-of-type(5).status            # Spend Profile
     And the user should see the element    jQuery=#table-project-status tr:nth-of-type(1) td:nth-of-type(6).status.waiting  # Other Docs
     And the user should see the element    jQuery=#table-project-status tr:nth-of-type(1) td:nth-of-type(7).status          # GOL

Other internal users do not have access to Finance checks
    [Documentation]    INFUND-4821
    [Tags]    HappyPath
    [Setup]    Log in as a different user    john.doe@innovateuk.test    Passw0rd
    # This is added to HappyPath because CompAdmin should NOT have access to FC page
    Then the user navigates to the page and gets a custom error message    ${server}/project-setup-management/project/${FUNDERS_PANEL_APPLICATION_1_PROJECT}/finance-check    You do not have the necessary permissions for your request


Finance contact can access the external view of the finance checks page
    [Documentation]    INFUND-7573
    [Tags]    HappyPath
    [Setup]    Log in as a different user    ${test_mailbox_one}+fundsuccess@gmail.com    Passw0rd
    Given the user clicks the button/link    link=${FUNDERS_PANEL_APPLICATION_1_HEADER}
    When the user clicks the button/link    link=Finance checks
    And the user should not see an error in the page


Non finance contact can view finance checks page
    [Documentation]    INFUND-7573
    [Tags]
    [Setup]    Log in as a different user    steve.smith@empire.com    Passw0rd
    When the user clicks the button/link    link=${FUNDERS_PANEL_APPLICATION_1_HEADER}
    Then the user should see the element    link=Finance checks
    And the user navigates to the page    ${server}/project-setup/project/${FUNDERS_PANEL_APPLICATION_1_PROJECT}/partner-organisation/${EMPIRE_LTD_ID}/finance-checks


*** Keywords ***
the table row has expected values
    the user sees the text in the element    jQuery=.table-overview tbody td:nth-child(2)    3 months
    the user sees the text in the element    jQuery=.table-overview tbody td:nth-child(3)    £ 503,248
    the user sees the text in the element    jQuery=.table-overview tbody td:nth-child(4)    £ 145,497
    the user sees the text in the element    jQuery=.table-overview tbody td:nth-child(5)    £ 6,170
    the user sees the text in the element    jQuery=.table-overview tbody td:nth-child(6)    29%

Moving ${FUNDERS_PANEL_COMPETITION_NAME} into project setup
    the project finance user moves ${FUNDERS_PANEL_COMPETITION_NAME} into project setup if it isn't already
    the users fill out project details
    bank details are approved for all businesses

the project finance user moves ${FUNDERS_PANEL_COMPETITION_NAME} into project setup if it isn't already
    guest user log-in    lee.bowman@innovateuk.test    Passw0rd
    the user navigates to the page    ${COMP_MANAGEMENT_PROJECT_SETUP}
    ${update_comp}    ${value}=    Run Keyword And Ignore Error Without Screenshots    the user should not see the text in the page    ${FUNDERS_PANEL_COMPETITION_NAME}
    run keyword if    '${update_comp}' == 'PASS'    the project finance user moves ${FUNDERS_PANEL_COMPETITION_NAME} into project setup

the project finance user moves ${FUNDERS_PANEL_COMPETITION_NAME} into project setup
    the user navigates to the page    ${server}/management/competition/${FUNDERS_PANEL_COMPETITION}/funding
    the user moves focus to the element     jQuery=label[for="app-row-1"]
    the user selects the checkbox       app-row-1
    the user moves focus to the element     jQuery=label[for="app-row-2"]
    the user selects the checkbox       app-row-2
    the user clicks the button/link     jQuery=button:contains("Successful")
    #the user clicks the button/link     xpath=//*[@id="content"]/form[1]/div[1]/div[2]/fieldset/button[1]
    the user should see the element    jQuery=td:contains("Successful")
    the user clicks the button/link     jQuery=a:contains("Competition")
    the user clicks the button/link     jQuery=button:contains("Manage funding notifications")
    the user selects the checkbox      ids[0]
    the user selects the checkbox      ids[1]
    the user clicks the button/link     xpath=//*[@id="content"]/form/div[1]/div[2]/fieldset/button[1]
    the user enters text to a text field     id=subject   testEmail
    the user enters text to a text field     css=[labelledby="message"]      testMessage
    the user clicks the button/link     jQuery=button:contains("Send email to all applicants")
    the user should see the text in the page    Manage funding applications

the users fill out project details
    When Log in as a different user    jessica.doe@ludlow.co.uk    Passw0rd
    Then the user navigates to the page    ${la_fromage_overview}
    And the user clicks the button/link    link=Project details
    Then the user should see the text in the page    Finance contacts
    And the user should see the text in the page    Partner
    And the user clicks the button/link    link=Ludlow
    And the user selects the radio button    financeContact    financeContact1
    And the user clicks the button/link    jQuery=.button:contains("Save")
    When Log in as a different user    pete.tom@egg.com    Passw0rd
    Then the user navigates to the page    ${la_fromage_overview}
    And the user clicks the button/link    link=Project details
    Then the user should see the text in the page    Finance contacts
    And the user should see the text in the page    Partner
    And the user clicks the button/link    link=EGGS
    And the user selects the radio button    financeContact    financeContact1
    And the user clicks the button/link    jQuery=.button:contains("Save")
    When Log in as a different user    steve.smith@empire.com    Passw0rd
    Then the user navigates to the page    ${la_fromage_overview}
    And the user clicks the button/link    link=Project details
    Then the user should see the text in the page    Finance contacts
    And the user should see the text in the page    Partner
    And the user clicks the button/link    link=${FUNDERS_PANEL_APPLICATION_1_LEAD_ORGANISATION_NAME}
    And the user selects the radio button    financeContact    financeContact1
    And the user clicks the button/link    jQuery=.button:contains("Save")
    And the user clicks the button/link    link=Project Manager
    And the user selects the radio button    projectManager    projectManager1
    And the user clicks the button/link    jQuery=.button:contains("Save")
    And the user clicks the button/link    link=Project address
    And the user selects the radio button    addressType    REGISTERED
    And the user clicks the button/link    jQuery=.button:contains("Save")
    the user clicks the button/link    jQuery=.button:contains("Mark as complete")
    the user clicks the button/link    jQuery=button:contains("Submit")

the user fills in project costs
    Input Text    name=costs[0].value    £ 8,000
    Input Text    name=costs[1].value    £ 2,000
    Input Text    name=costs[2].value    £ 10,000
    Input Text    name=costs[3].value    £ 10,000
    Input Text    name=costs[4].value    £ 10,000
    Input Text    name=costs[5].value    £ 10,000
    Input Text    name=costs[6].value    £ 10,000
    the user moves focus to the element    css=[for="costs-reviewed"]
    the user sees the text in the element    css=#content tfoot td    £ 60,000
    the user should see that the element is disabled    jQuery=.button:contains("Approve eligible costs")

bank details are approved for all businesses
    partners submit bank details
    the project finance user has approved bank details

partners submit bank details
    partner submits his bank details  ${PROJECT_SETUP_APPLICATION_1_LEAD_PARTNER_EMAIL}
    partner submits his bank details  ${PROJECT_SETUP_APPLICATION_1_PARTNER_EMAIL}
    partner submits his bank details  ${PROJECT_SETUP_APPLICATION_1_ACADEMIC_PARTNER_EMAIL}

partner submits his bank details
    [Arguments]  ${email}
    log in as a different user            ${email}    ${short_password}
    the user navigates to the page        ${server}/project-setup/project/${FUNDERS_PANEL_APPLICATION_1_PROJECT}/bank-details
    the user enters text to a text field  id=bank-acc-number  51406795
    the user enters text to a text field  id=bank-sort-code  404745
    the user selects the radio button     addressType    REGISTERED
    the user clicks the button/link       jQuery=.button:contains("Submit bank account details")
    the user clicks the button/link       jQuery=.button:contains("Submit")

the project finance user has approved bank details
    Guest user log-in  &{internal_finance_credentials}
    the project finance user approves bank details for    ${PROJECT_SETUP_APPLICATION_1_LEAD_ORGANISATION_NAME}
    the project finance user approves bank details for    ${PROJECT_SETUP_APPLICATION_1_PARTNER_NAME}
    the project finance user approves bank details for    ${PROJECT_SETUP_APPLICATION_1_ACADEMIC_PARTNER_NAME}

the project finance user approves bank details for
    [Arguments]    ${org_name}
    the user navigates to the page            ${server}/project-setup-management/project/${FUNDERS_PANEL_APPLICATION_1_PROJECT}/review-all-bank-details
    the user clicks the button/link           link=${org_name}
    the user should see the text in the page  ${org_name}
    the user clicks the button/link           jQuery=.button:contains("Approve bank account details")
    the user clicks the button/link           jQuery=.button:contains("Approve account")
    the user should not see the element       jQuery=.button:contains("Approve bank account details")
    the user should see the text in the page  The bank details provided have been approved.

project finance approves Viability for
    [Arguments]  ${partner}
    Given the user navigates to the page    ${server}/project-setup-management/project/${FUNDERS_PANEL_APPLICATION_1_PROJECT}/finance-check
    And the user should see the element     jQuery=table.table-progress tr:nth-child(${partner}) td:nth-child(2) a:contains("Review")
    When the user clicks the button/link    jQuery=table.table-progress tr:nth-child(${partner}) td:nth-child(2) a:contains("Review")
    Then the user should see the element    jQuery=h2:contains("Credit report")
    And the user selects the checkbox       costs-reviewed
    When the user should see the element    jQuery=h2:contains("Approve viability")
    Then the user selects the checkbox      project-viable
    And the user moves focus to the element  link=Contact us
    When the user selects the option from the drop-down menu  Green  id=rag-rating
    Then the user clicks the button/link    css=#confirm-button
    And the user clicks the button/link     jQuery=.modal-confirm-viability .button:contains("Confirm viability")

the rag rating updates on the finance check page for lead for viability
   [Arguments]    ${rag_rating}
   When the user selects the option from the drop-down menu    ${rag_rating}    id=rag-rating
   And the user clicks the button/link    jQuery=.button-secondary:contains("Save and return to finance checks")
   Then the user should see the text in the element    jQuery=table.table-progress tr:nth-child(1) td:nth-child(3)    ${rag_rating}
   And the user clicks the button/link    jQuery=table.table-progress tr:nth-child(1) td:nth-child(2) a:contains("Review")
   And the user should see the element    jQuery=.button:contains("Confirm viability"):not(.disabled)    # Checking here both that the button exists and that it isn't disabled

the rag rating updates on the finance check page for partner for viability
   [Arguments]    ${rag_rating}
   When the user selects the option from the drop-down menu    ${rag_rating}    id=rag-rating
   And the user clicks the button/link    jQuery=.button-secondary:contains("Save and return to finance checks")
   Then the user should see the text in the element    jQuery=table.table-progress tr:nth-child(2) td:nth-child(3)    ${rag_rating}
   And the user clicks the button/link    jQuery=table.table-progress tr:nth-child(2) td:nth-child(2) a:contains("Review")
   And the user should see the element    jQuery=.button:contains("Confirm viability"):not(.disabled)    # Checking here both that the button exists and that it isn't disabled

the rag rating updates on the finance check page for lead for eligibility
   [Arguments]    ${rag_rating}
   When the user selects the option from the drop-down menu    ${rag_rating}    id=rag-rating
   And the user clicks the button/link    jQuery=.button-secondary:contains("Return to finance checks")
   Then the user should see the text in the element    jQuery=table.table-progress tr:nth-child(1) td:nth-child(5)    ${rag_rating}
   And the user clicks the button/link    jQuery=table.table-progress tr:nth-child(1) td:nth-child(4) a:contains("Review")
   And the user should see the element    jQuery=.button:contains("Approve eligible costs"):not(.disabled)    # Checking here both that the button exists and that it isn't disabled

the rag rating updates on the finance check page for partner for eligibility
   [Arguments]    ${rag_rating}
   When the user selects the option from the drop-down menu    ${rag_rating}    id=rag-rating
   And the user clicks the button/link    jQuery=.button-secondary:contains("Return to finance checks")
   Then the user should see the text in the element    jQuery=table.table-progress tr:nth-child(2) td:nth-child(5)    ${rag_rating}
   And the user clicks the button/link    jQuery=table.table-progress tr:nth-child(2) td:nth-child(4) a:contains("Review")
   And the user should see the element    jQuery=.button:contains("Approve eligible costs"):not(.disabled)    # Checking here both that the button exists and that it isn't disabled


verify total costs of project
    [Arguments]    ${total_costs}
    the user should see the text in the element      jQuery=.table-overview tbody tr:nth-child(1) td:nth-child(2)     ${total_costs}

verify percentage and total
    [Arguments]  ${section}  ${percentage}  ${total}
    the user should see the element           jQuery=section:nth-of-type(${section}) button span:contains("${percentage}")
    the user should see the element            jQuery=section:nth-of-type(${section}) input[data-calculation-rawvalue^='${total}']

the user adds data into labour row
    [Arguments]  ${row_number}  ${descrption}  ${salary}  ${days}
    the user enters text to a text field        css=.labour-costs-table tr:nth-of-type(${row_number}) td:nth-of-type(1) input    ${descrption}
    the user enters text to a text field        css=.labour-costs-table tr:nth-of-type(${row_number}) td:nth-of-type(2) input    ${salary}
    the user enters text to a text field        css=.labour-costs-table tr:nth-of-type(${row_number}) td:nth-of-type(4) input    ${days}

the user adds data into materials row
    [Arguments]  ${row_number}  ${item}  ${qty}  ${cost_of_item}
    the user enters text to a text field        css=#material-costs-table tbody tr:nth-of-type(${row_number}) td:nth-of-type(1) input    ${item}
    the user enters text to a text field        css=#material-costs-table tbody tr:nth-of-type(${row_number}) td:nth-of-type(2) input    ${qty}
    the user enters text to a text field        css=#material-costs-table tbody tr:nth-of-type(${row_number}) td:nth-of-type(3) input    ${cost_of_item}

the user adds capital usage data into row
    [Arguments]  ${row_number}  ${description}  ${net_value}  ${residual_value}  ${utilization}
    the user enters text to a text field        css=section:nth-of-type(4) #capital_usage div:nth-child(${row_number}) div:nth-of-type(1) textarea   ${description}
    Click Element                               css=section:nth-of-type(4) #capital_usage div:nth-child(${row_number}) div:nth-of-type(2) label:nth-of-type(1)
    the user enters text to a text field        css=section:nth-of-type(4) #capital_usage div:nth-child(${row_number}) div:nth-of-type(3) input    12
    the user enters text to a text field        css=section:nth-of-type(4) #capital_usage div:nth-child(${row_number}) div:nth-of-type(4) input  ${net_value}
    the user enters text to a text field        css=section:nth-of-type(4) #capital_usage div:nth-child(${row_number}) div:nth-of-type(5) input   ${residual_value}
    the user enters text to a text field        css=section:nth-of-type(4) #capital_usage div:nth-child(${row_number}) div:nth-of-type(6) input   ${utilization}

the user adds subcontracting data into row
    [Arguments]  ${row_number}  ${name}  ${cost}
    the user enters text to a text field        css=section:nth-of-type(5) #subcontracting div:nth-child(${row_number}) div:nth-of-type(1) input   ${name}
    the user enters text to a text field        css=section:nth-of-type(5) #subcontracting div:nth-child(${row_number}) div:nth-of-type(2) input   UK
    the user enters text to a text field        css=section:nth-of-type(5) #subcontracting div:nth-child(${row_number}) div:nth-of-type(3) textarea   Develop
    the user enters text to a text field        css=section:nth-of-type(5) #subcontracting div:nth-child(${row_number}) div:nth-of-type(4) input   ${cost}


the user adds travel data into row
    [Arguments]  ${row_number}  ${description}  ${number_of_times}  ${cost}
    the user enters text to a text field        css=#travel-costs-table tbody tr:nth-of-type(${row_number}) td:nth-of-type(1) input    ${description}
    the user enters text to a text field        css=#travel-costs-table tbody tr:nth-of-type(${row_number}) td:nth-of-type(2) input    ${number_of_times}
    the user enters text to a text field        css=#travel-costs-table tbody tr:nth-of-type(${row_number}) td:nth-of-type(3) input    ${cost}

Project finance user amends labour details in eligibility for partner
    When the user clicks the button/link            jQuery=section:nth-of-type(1) button:contains("Labour")
    Then the user should see the element            jQuery=section:nth-of-type(1) button span:contains("2%")
    When the user clicks the button/link            jQuery=section:nth-of-type(1) a:contains("Edit")
    Then the user should see the element            css=.labour-costs-table tr:nth-of-type(1) td:nth-of-type(2) input
    When the user clears the text from the element  css=[name^="labour-labourDaysYearly"]
    And the user enters text to a text field        css=[name^="labour-labourDaysYearly"]    230
    And the user adds data into labour row          1  test  120000  100
    Then verify percentage and total                1  20%  53734
    When the user clicks the button/link            jQuery=section:nth-of-type(1) button:contains("Add another role")
    And the user adds data into labour row          13  test  14500  100
    Then verify percentage and total                1  22%  60039
    When the user clicks the button/link            css=.labour-costs-table tr:nth-of-type(3) td:nth-of-type(5) button
    Then verify percentage and total                1  22%  59778
    When the user clicks the button/link            jQuery=section:nth-of-type(1) button[name=save-eligibility]
    Then verify total costs of project              £ 257,600
    And the user should see the element             jQuery=section:nth-of-type(1) a:contains("Edit")
    And the user should not see the element         jQuery=section:nth-of-type(1) button[name=save-eligibility]


Project finance user amends materials details in eligibility for partner
    When the user clicks the button/link            jQuery=section:nth-of-type(3) button:contains("Materials")
    Then verify percentage and total                3  39%  100200
    When the user clicks the button/link            jQuery=section:nth-of-type(3) a:contains("Edit")
    And the user adds data into materials row       1  test  10  100
    Then verify percentage and total                3  25%  51100
    When the user clicks the button/link            jQuery=section:nth-of-type(3) button[name=add_cost]
    And the user adds data into materials row       3  test  10  100
    Then verify percentage and total                3  25%  52100
    When the user clicks the button/link            css=#material-costs-table tbody tr:nth-of-type(2) td:nth-of-type(5) button
    Then verify percentage and total                3  1%  2000
    When the user clicks the button/link            jQuery=section:nth-of-type(3) button[name=save-eligibility]
    Then verify total costs of project              £ 159,400
    And the user should see the element            jQuery=section:nth-of-type(1) a:contains("Edit")
    And the user should not see the element        jQuery=section:nth-of-type(3) button[name=save-eligibility]

Project finance user amends capital usage details in eligibility for partner
    When the user clicks the button/link            jQuery=section:nth-of-type(4) button:contains("Capital usage")
    Then the user should see the element            jQuery=section:nth-of-type(4) button span:contains("0%")
    When the user clicks the button/link            jQuery=section:nth-of-type(4) a:contains("Edit")
    And the user adds capital usage data into row   1  test  10600  500  50
    Then verify percentage and total                4  3%  5326
    When the user clicks the button/link            jQuery=section:nth-of-type(4) button[name=add_cost]
    And the user adds capital usage data into row   3  test  10600  500  50
    Then verify percentage and total                4  6%  10376
    When the user clicks the button/link            css=section:nth-of-type(4) #capital_usage div:nth-child(2) button
    Then verify percentage and total                 4  6%  10100
    When the user clicks the button/link           jQuery=section:nth-of-type(4) button[name=save-eligibility]
    Then verify total costs of project             £ 168,948
    And the user should see the element           jQuery=section:nth-of-type(4) a:contains("Edit")
    And the user should not see the element       jQuery=section:nth-of-type(4) button[name=save-eligibility]


Project finance user amends subcontracting usage details in eligibility for partner
    When the user clicks the button/link            jQuery=section:nth-of-type(5) button:contains("Subcontracting costs")
    Then the user should see the element            jQuery=section:nth-of-type(5) button span:contains("53%")
    And the user should see the element            jQuery=section:nth-of-type(5) input[value*='90,000']
    When the user clicks the button/link            jQuery=section:nth-of-type(5) a:contains("Edit")
    And the user adds subcontracting data into row   1  test  10600
    Then verify percentage and total                 5  41%  55600
    When the user clicks the button/link            jQuery=section:nth-of-type(5) button[name=add_cost]
    And the user adds subcontracting data into row   3  test  9400
    Then verify percentage and total                 5  45%  65000
    When the user clicks the button/link            css=section:nth-of-type(5) #subcontracting div:nth-child(2) button
    Then verify percentage and total                 5  18%  20000
    When the user clicks the button/link           jQuery=section:nth-of-type(5) button[name=save-eligibility]
    Then verify total costs of project              £ 98,948
    And the user should see the element           jQuery=section:nth-of-type(5) a:contains("Edit")
    And the user should not see the element       jQuery=section:nth-of-type(5) button[name=save-eligibility]

Project finance user amends travel details in eligibility for partner
    Given the user clicks the button/link           jQuery=section:nth-of-type(6) button:contains("Travel and subsistence")
    Then the user should see the element            jQuery=section:nth-of-type(6) button span:contains("6%")
    And the user should see the element            jQuery=section:nth-of-type(6) input[value*='5,970']
    When the user clicks the button/link            jQuery=section:nth-of-type(6) a:contains("Edit")
    And the user adds travel data into row          1  test  10  100
    Then verify percentage and total                 6  4%  3985
    When the user clicks the button/link            jQuery=section:nth-of-type(6) button[name=add_cost]
    And the user adds travel data into row          3  test  10  100
    Then verify percentage and total                 6  5%  4985
    When the user clicks the button/link            css=#travel-costs-table tbody tr:nth-of-type(2) td:nth-of-type(5) button
    Then verify percentage and total                 6  2%  2000
    When the user clicks the button/link           jQuery=section:nth-of-type(6) button[name=save-eligibility]
    Then verify total costs of project            £ 94,978
    And the user should see the element           jQuery=section:nth-of-type(6) a:contains("Edit")
    And the user should not see the element       jQuery=section:nth-of-type(6) button[name=save-eligibility]

Project finance user amends other costs details in eligibility for partner
    When the user clicks the button/link            jQuery=section:nth-of-type(7) button:contains("Other costs")
    Then the user should see the element            jQuery=section:nth-of-type(7) button span:contains("1%")
    And the user should see the element            jQuery=section:nth-of-type(7) input[value*='1,100']
    When the user clicks the button/link            jQuery=section:nth-of-type(7) a:contains("Edit")
    And the user enters text to a text field        jQuery=#other-costs-table tr:nth-child(1) td:nth-child(1) textarea  some other costs
    And the user enters text to a text field        jQuery=#other-costs-table tr:nth-child(1) td:nth-child(2) input  5000
    Then verify percentage and total                 7  6%  5550
    When the user clicks the button/link            jQuery=section:nth-of-type(7) button[name=add_cost]
    And the user enters text to a text field        jQuery=#other-costs-table tr:nth-child(3) td:nth-child(1) textarea  some other costs
    And the user enters text to a text field        jQuery=#other-costs-table tr:nth-child(3) td:nth-child(2) input  5750
    Then verify percentage and total                 7  11%  11300
    When the user should see the element           css=#other-costs-table tr:nth-of-type(2) td:nth-of-type(3) button
    When the user clicks the button/link           jQuery=section:nth-of-type(7) button[name=save-eligibility]
    Then verify total costs of project            £ 105,178
    And the user should see the element           jQuery=section:nth-of-type(7) a:contains("Edit")
    And the user should not see the element       jQuery=section:nth-of-type(7) button[name=save-eligibility]

Project finance user amends labour details in eligibility for lead
    When the user clicks the button/link            jQuery=section:nth-of-type(1) button:contains("Labour")
    Then the user should see the element            jQuery=section:nth-of-type(1) button span:contains("2%")
    When the user clicks the button/link            jQuery=section:nth-of-type(1) a:contains("Edit")
    Then the user should see the element            css=.labour-costs-table tr:nth-of-type(1) td:nth-of-type(2) input
    When the user clears the text from the element  css=[name^="labour-labourDaysYearly"]
    And the user enters text to a text field        css=[name^="labour-labourDaysYearly"]    230
    And the user adds data into labour row          1  test  120000  100
    Then verify percentage and total                1  15%  5455
    When the user clicks the button/link            jQuery=section:nth-of-type(1) button:contains("Add another role")
    And the user adds data into labour row          19  test  14500  100
    Then verify percentage and total                1  16%  60863
    When the user clicks the button/link            css=.labour-costs-table tr:nth-of-type(3) td:nth-of-type(5) button
    Then verify percentage and total                1  16%  60602
    When the user clicks the button/link            jQuery=section:nth-of-type(1) button[name=save-eligibility]
    Then verify total costs of project              £ 357,335
    And the user should see the element             jQuery=section:nth-of-type(1) a:contains("Edit")
    And the user should not see the element         jQuery=section:nth-of-type(1) button[name=save-eligibility]


Project finance user amends materials details in eligibility for lead
    When the user clicks the button/link            jQuery=section:nth-of-type(3) button:contains("Materials")
    Then verify percentage and total                3  42%  150300
    When the user clicks the button/link            jQuery=section:nth-of-type(3) a:contains("Edit")
    And the user adds data into materials row       1  test  10  100
    Then verify percentage and total                3  33%  101200
    When the user clicks the button/link            jQuery=section:nth-of-type(3) button[name=add_cost]
    And the user adds data into materials row       4  test  10  100
    Then verify percentage and total                3  33%  102200
    When the user clicks the button/link            css=#material-costs-table tbody tr:nth-of-type(2) td:nth-of-type(5) button
    Then verify percentage and total                3  19%  52100
    When the user clicks the button/link            jQuery=section:nth-of-type(3) button[name=save-eligibility]
    Then verify total costs of project              £ 259,135
    And the user should see the element            jQuery=section:nth-of-type(1) a:contains("Edit")
    And the user should not see the element        jQuery=section:nth-of-type(3) button[name=save-eligibility]

Project finance user amends capital usage details in eligibility for lead
    When the user clicks the button/link            jQuery=section:nth-of-type(4) button:contains("Capital usage")
    Then the user should see the element            jQuery=section:nth-of-type(4) button span:contains("0%")
    And the user should see the element            jQuery=section:nth-of-type(4) input[value*='828']
    When the user clicks the button/link            jQuery=section:nth-of-type(4) a:contains("Edit")
    And the user adds capital usage data into row   1  test  10600  500  50
    Then verify percentage and total                4  2%  5602
    When the user clicks the button/link            jQuery=section:nth-of-type(4) button[name=add_cost]
    And the user adds capital usage data into row   4  test  10600  500  50
    Then verify percentage and total                4  4%  10652
    When the user clicks the button/link            css=section:nth-of-type(4) #capital_usage div:nth-child(2) button
    Then verify percentage and total                 4  4%  10376
    When the user clicks the button/link           jQuery=section:nth-of-type(4) button[name=save-eligibility]
    Then verify total costs of project             £ 268,683
    And the user should see the element           jQuery=section:nth-of-type(4) a:contains("Edit")
    And the user should not see the element       jQuery=section:nth-of-type(4) button[name=save-eligibility]


Project finance user amends subcontracting usage details in eligibility for lead
    When the user clicks the button/link            jQuery=section:nth-of-type(5) button:contains("Subcontracting costs")
    Then the user should see the element            jQuery=section:nth-of-type(5) button span:contains("50%")
    And the user should see the element            jQuery=section:nth-of-type(5) input[value*='135,000']
    When the user clicks the button/link            jQuery=section:nth-of-type(5) a:contains("Edit")
    And the user adds subcontracting data into row   1  test  10600
    Then verify percentage and total                 5  43%  100600
    When the user clicks the button/link            jQuery=section:nth-of-type(5) button[name=add_cost]
    And the user adds subcontracting data into row   4  test  9400
    Then verify percentage and total                 5  45%  110000
    When the user clicks the button/link            css=section:nth-of-type(5) #subcontracting div:nth-child(2) button
    When the user clicks the button/link           jQuery=section:nth-of-type(5) button[name=save-eligibility]
    Then verify total costs of project              £ 198,683
    And the user should see the element           jQuery=section:nth-of-type(5) a:contains("Edit")
    And the user should not see the element       jQuery=section:nth-of-type(5) button[name=save-eligibility]

Project finance user amends travel details in eligibility for lead
    Given the user clicks the button/link           jQuery=section:nth-of-type(6) button:contains("Travel and subsistence")
    Then the user should see the element            jQuery=section:nth-of-type(6) button span:contains("5%")
    And the user should see the element            jQuery=section:nth-of-type(6) input[value*='8,955']
    When the user clicks the button/link            jQuery=section:nth-of-type(6) a:contains("Edit")
    And the user adds travel data into row          1  test  10  100
    Then verify percentage and total                 6  4%  6970
    When the user clicks the button/link            jQuery=section:nth-of-type(6) button[name=add_cost]
    And the user adds travel data into row          4  test  10  100
    Then verify percentage and total                 6  4%  7970
    When the user clicks the button/link            css=#travel-costs-table tbody tr:nth-of-type(2) td:nth-of-type(5) button
    Then verify percentage and total                 6  2%  4985
    When the user clicks the button/link           jQuery=section:nth-of-type(6) button[name=save-eligibility]
    Then verify total costs of project            £ 194,713
    And the user should see the element           jQuery=section:nth-of-type(6) a:contains("Edit")
    And the user should not see the element       jQuery=section:nth-of-type(6) button[name=save-eligibility]

Project finance user amends other costs details in eligibility for lead
    When the user clicks the button/link            jQuery=section:nth-of-type(7) button:contains("Other costs")
    Then the user should see the element            jQuery=section:nth-of-type(7) button span:contains("1%")
    And the user should see the element            jQuery=section:nth-of-type(7) input[value*='1,650']
    When the user clicks the button/link            jQuery=section:nth-of-type(7) a:contains("Edit")
    And the user enters text to a text field        jQuery=#other-costs-table tr:nth-child(1) td:nth-child(1) textarea  some other costs
    And the user enters text to a text field        jQuery=#other-costs-table tr:nth-child(1) td:nth-child(2) input  5000
    Then verify percentage and total                 7  3%  6100
    When the user clicks the button/link            jQuery=section:nth-of-type(7) button[name=add_cost]
    And the user enters text to a text field        jQuery=#other-costs-table tr:nth-child(4) td:nth-child(1) textarea  some other costs
    And the user enters text to a text field        jQuery=#other-costs-table tr:nth-child(4) td:nth-child(2) input  5750
    Then verify percentage and total                 7  6%  11850
    When the user should see the element           css=#other-costs-table tr:nth-of-type(2) td:nth-of-type(3) button
    When the user clicks the button/link           jQuery=section:nth-of-type(7) button[name=save-eligibility]
    Then verify total costs of project            £ 204,913
    And the user should see the element           jQuery=section:nth-of-type(7) a:contains("Edit")
    And the user should not see the element       jQuery=section:nth-of-type(7) button[name=save-eligibility]


the user goes back to the previous page ignoring form submission
    the user goes back to the previous page
    the user reloads the page