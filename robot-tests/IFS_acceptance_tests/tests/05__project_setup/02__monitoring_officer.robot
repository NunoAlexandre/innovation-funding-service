*** Settings ***
Documentation     INFUND-2630 As a Competitions team member I want to be able to enter the details of a Monitoring Officer to assign them to the project and share their contact details with the consortium
...
...               INFUND-2632 As a Competitions team member I want to send an email to a Monitoring Officer so they are aware I have assigned them to a project
...
...               INFUND-2633 As a Competitions team member I want to send an email to the project manager so that they have the contact details of the Monitoring Officer I have assigned to their project
...
...               INFUND-2634 As a partner I want to be able to view details of the assigned Monitoring Officer for my project so I can contact them
...
...               INFUND-2621 As a contributor I want to be able to review the current Project Setup status of all partners in my project so I can get an indication of the overall status of the consortium
Suite Setup       Run Keywords    delete the emails from both test mailboxes
Suite Teardown    the user closes the browser
Force Tags        Project Setup
Resource          ../../resources/GLOBAL_LIBRARIES.robot
Resource          ../../resources/variables/GLOBAL_VARIABLES.robot
Resource          ../../resources/variables/User_credentials.robot
Resource          ../../resources/keywords/Login_actions.robot
Resource          ../../resources/keywords/User_actions.robot
Resource          ../../resources/variables/EMAIL_VARIABLES.robot
Resource          ../../resources/keywords/SUITE_SET_UP_ACTIONS.robot

*** Variables ***
${Successful_Monitoring_Officer_Page}    ${server}/project-setup-management/project/1/monitoring-officer

*** Test Cases ***
Before Monitoring Officer is assigned
    [Documentation]    INFUND-2634, INFUND-2621
    [Tags]    HappyPath
    [Setup]    Log in as user    steve.smith@empire.com    Passw0rd
    Given the user navigates to the page    ${project_in_setup_page}
    And the user should see the text in the page    Innovate UK will assign you a Monitoring Officer
    And the user should not see the element    jQuery=ul li.complete:nth-child(3)
    When the user clicks the button/link    link=Monitoring Officer
    Then the user should see the text in the page    Your project has not yet been assigned a Monitoring Officer.
    And the user should not see the text in the page    A Monitoring Officer has been assigned.
    When the user navigates to the page    ${project_in_setup_page}
    And the user clicks the button/link    link=What's the status of each of my partners?
    Then the user should see the element    jQuery=#table-project-status tr:nth-of-type(1) td.status.waiting:nth-of-type(2)

Comp admin can view the Supporting information details on MO page
    [Documentation]    INFUND-2630
    [Tags]    HappyPath
    [Setup]    Log in as user    &{Comp_admin1_credentials}
    When the user navigates to the page    ${Successful_Monitoring_Officer_Page}
    Then the user should see the text in the page    Monitoring Officer
    And the user should see the text in the page    Supporting information
    And the user should see the text in the page    best riffs
    And the user should see the text in the page    Earth Observation
    And the user should see the text in the page    Riff Street
    And the user should see the text in the page    Bath
    And the user should see the text in the page    BA1 5LR
    And Element Should Contain    jQuery=p:nth-child(11)    1st Jan 2017
    And the user should see the text in the page    test twenty
    And the user should see the text in the page    Vitruvius Stonework Limited
    And the user should see the text in the page    EGGS
    And the user should see the text in the page    Ludlow

MO server-side validation
    [Documentation]    INFUND-2630
    [Tags]    HappyPath
    When the user clicks the button/link    jQuery=.button:contains("Assign Monitoring Officer")
    Then the user should see an error    Please enter a first name
    And the user should see an error    Please enter a last name
    And the user should see an error    Please enter a valid email address
    And the user should see an error    Please enter an email address
    And the user should see an error    Please enter a phone number
    And the user should see an error    Please enter a valid phone number

MO client-side validation
    [Documentation]    INFUND-2630
    [Tags]    HappyPath
    When the user enters text to a text field    id=firstName    Abbey
    Then the user moves focus away from an element for MO    id=firstName
    And the user should not see the text in the page    Please enter a first name
    Then the user enters text to a text field    id=lastName    Abigail
    And the user moves focus away from an element for MO    id=lastName
    And the user should not see the text in the page    Please enter a last name
    Then the user enters text to a text field    id=emailAddress    ${test_mailbox_one}+monitoringofficer@gmail.com
    And the user moves focus away from an element for MO    id=emailAddress
    And the user should not see the text in the page    Please enter a valid email address
    And the user should not see the text in the page    Please enter an email address
    Then the user enters text to a text field    id=phoneNumber    07438620303
    And the user moves focus away from an element for MO    id=phoneNumber
    And the user should not see the text in the page    Please enter a phone number
    And the user should not see the text in the page    Please enter a valid phone number
    # Pending due to INFUND-4101
    #    And the user enters text to a text field    id=phoneNumber    0123
    #    Then the user clicks the button/link    jQuery=.button:contains("Assign Monitoring Officer")
    #    And the user clicks the button/link    jQuery=.modal-assign-mo button:contains("Assign Monitoring Officer")
    #    And the user should see an error    Input for your phone number has a minimum length of 8 characters

MO details can be added
    [Documentation]    INFUND-2630
    ...
    ...    INFUND-2632
    [Tags]    HappyPath
    When standard verification for email address follows
    And the user enters text to a text field    id=emailAddress    ${test_mailbox_two}+monitoringofficer@gmail.com
    Then standard verification for Phone number follows
    And the user moves focus away from an element for MO    id=phoneNumber
    And the user clicks the button/link    jQuery=.button:contains("Assign Monitoring Officer")
    And the user clicks the button/link    jQuery=.modal-assign-mo button:contains("Cancel")
    Then the user should not see the text in the page    A Monitoring Officer has been assigned.
    And the user clicks the button/link    jQuery=.button:contains("Assign Monitoring Officer")
    And the user clicks the button/link    jQuery=.modal-assign-mo button:contains("Assign Monitoring Officer")
    Then The user should see the element    css=.success-alert
    And the user should see the text in the page    A Monitoring Officer has been assigned.
    And Logout as user
    Then Log in as user    steve.smith@empire.com    Passw0rd
    And the user navigates to the page    ${project_in_setup_page}
    And the user should see the element    jQuery=ul li.complete:nth-child(3)
    And Element Should Contain    jQuery=ul li.complete:nth-child(3) p    Your Monitoring Officer for this project is Abbey Abigail.

MO details(email step)
    [Documentation]    INFUND-2630
    ...
    ...    INFUND-2632
    ...
    ...    INFUND-2633
    [Tags]    Email
    # Note that assigning a monitoring officer will send emails out to both the new MO and the PM - this test checks for both emails
    When Open mailbox and confirm received email    ${test_mailbox_two}@gmail.com    ${test_mailbox_two_password}    has been assigned to you    New Monitoring Officer assignment
    And Open mailbox and confirm received email    worth.email.test@gmail.com    testtest1    has been assigned a Monitoring officer    Monitoring Officer assigned to your project

MO details can be edited and viewed in the Project setup status page
    [Documentation]    INFUND-2630, INFUND-2621
    ...
    ...    INFUND-2634
    [Tags]    HappyPath
    [Setup]    Log in as user    &{Comp_admin1_credentials}
    Given the user navigates to the page    ${Successful_Monitoring_Officer_Page}
    When the user clicks the button/link    link=Change Monitoring Officer
    And the user edits the MO details
    And the user can see the changed MO details
    And Logout as user
    When Log in as user    steve.smith@empire.com    Passw0rd
    Then the user navigates to the page    ${project_in_setup_page}
    And the user should see the element    jQuery=ul li.complete:nth-child(3)
    And Element Should Contain    jQuery=ul li.complete:nth-child(3) p    Your Monitoring Officer for this project is Grace Harper.
    And the user clicks the button/link    link=Monitoring Officer
    Then the user should see the text in the page    Your project has been assigned a Monitoring Officer
    And the user should see the text in the page    Grace Harper
    And the user should see the text in the page    ${test_mailbox_two}+monitoringofficer@gmail.com
    And the user should see the text in the page    08549731414
    When the user navigates to the page    ${project_in_setup_page}
    And the user clicks the button/link    link=What's the status of each of my partners?
    Then the user should see the element    jQuery=#table-project-status tr:nth-of-type(1) td.status.ok:nth-of-type(2)

MO details edit(email step)
    [Documentation]    INFUND-2630
    ...
    ...    INFUND-2634
    [Tags]    Email
    # Note that assigning a monitoring officer will send emails out to both the new MO and the PM - this test checks for both emails
    When Open mailbox and confirm received email    ${test_mailbox_two}+monitoringofficer@gmail.com    ${test_mailbox_two_password}    has been assigned to you    New Monitoring Officer assignment
    And Open mailbox and confirm received email    worth.email.test@gmail.com    testtest1    has been assigned a Monitoring officer    Monitoring Officer assigned to your project

MO details accessible/seen by all partners
    [Documentation]    INFUND-2634, INFUND-2621
    [Tags]    HappyPath
    Given Log in as user    jessica.doe@ludlow.co.uk    Passw0rd
    When the user navigates to the page    ${project_in_setup_page}
    Then the user should see the element    jQuery=ul li.complete:nth-child(3)
    And Element Should Contain    jQuery=ul li.complete:nth-child(3) p    Your Monitoring Officer for this project is Grace Harper.
    And the user clicks the button/link    link=Monitoring Officer
    Then the user should see the text in the page    Your project has been assigned a Monitoring Officer
    And the user should see the text in the page    Grace Harper
    And the user should see the text in the page    ${test_mailbox_two}+monitoringofficer@gmail.com
    And the user should see the text in the page    08549731414
    When the user navigates to the page    ${project_in_setup_page}
    And the user clicks the button/link    link=What's the status of each of my partners?
    Then the user should see the element    jQuery=#table-project-status tr:nth-of-type(1) td.status.ok:nth-of-type(2)
    Then Logout as user
    When Log in as user    steve.smith@empire.com    Passw0rd
    And the user navigates to the page    ${project_in_setup_page}
    Then the user should see the element    jQuery=ul li.complete:nth-child(3)
    And Element Should Contain    jQuery=ul li.complete:nth-child(3) p    Your Monitoring Officer for this project is Grace Harper.
    And the user clicks the button/link    link=Monitoring Officer
    Then the user should see the text in the page    Your project has been assigned a Monitoring Officer
    And the user should see the text in the page    Grace Harper
    And the user should see the text in the page    ${test_mailbox_two}+monitoringofficer@gmail.com
    And the user should see the text in the page    08549731414
    When the user navigates to the page    ${project_in_setup_page}
    And the user clicks the button/link    link=What's the status of each of my partners?
    Then the user should see the element    jQuery=#table-project-status tr:nth-of-type(1) td.status.ok:nth-of-type(2)

*** Keywords ***
standard verification for email address follows
    the user enters text to a text field    id=emailAddress    ${EMPTY}
    the user should see an error    Please enter an email address
    the user enters text to a text field    id=emailAddress    ${invalid_email_plain}
    the user should see an error    Please enter a valid email address
    the user enters text to a text field    id=emailAddress    ${invalid_email_symbols}
    the user should see an error    Please enter a valid email address
    the user enters text to a text field    id=emailAddress    ${invalid_email_no_username}
    the user should see an error    Please enter a valid email address
    the user enters text to a text field    id=emailAddress    ${invalid_email_format}
    the user should see an error    Please enter a valid email address
    the user enters text to a text field    id=emailAddress    ${invalid_email_no_at}
    the user should see an error    Please enter a valid email address

standard verification for Phone number follows
    the user enters text to a text field    id=phoneNumber    ${EMPTY}
    the user should see an error    Please enter a phone number
    the user enters text to a text field    id=phoneNumber    invalidphone
    the user should see an error    Please enter a valid phone number
    # Pending due to INFUND-2101
    #    And the user enters text to a text field    id=phoneNumber    0123
    #    Then the user clicks the button/link    jQuery=.button:contains("Assign Monitoring Officer")
    #    And the user clicks the button/link    jQuery=.modal-assign-mo button:contains("Assign Monitoring Officer")
    #    And the user should see an error    Input for your phone number has a minimum length of 8 characters
    Then the user enters text to a text field    id=phoneNumber    07438620303
    #    And the user moves focus away from an element for MO    id=phoneNumber

the user moves focus away from an element for MO
    [Arguments]    ${element}
    mouse out    ${element}
    focus    jQuery=.button:contains("Assign Monitoring Officer")

the user edits the MO details
    Input Text    id=firstName    Grace
    Input Text    id=lastName    Harper
    #    Input Text    id=emailAddress    ${test_mailbox_one}+monitoringofficer@gmail.com
    Input Text    id=phoneNumber    08549731414
    the user clicks the button/link    jQuery=.button:contains("Assign Monitoring Officer")
    the user clicks the button/link    jQuery=.modal-assign-mo button:contains("Assign Monitoring Officer")

the user can see the changed MO details
    The user should see the element    css=.success-alert
    the user should see the text in the page    A Monitoring Officer has been assigned.
    Textfield Should Contain    id=firstName    Grace
    Textfield Should Contain    id=lastName    Harper
    # Textfield Should Contain    id=emailAddress    ${test_mailbox_one}+monitoringofficer@gmail.com
    #    Element Should Contain    id=phoneNumber    08549731414
