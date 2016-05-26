*** Settings ***
Documentation     INFUND-1231: As a collaborator registering my company as Academic, I want to be able to enter full or partial details of the Academic organisation's name so I can select my Academic organisation from a list
Suite Setup       The guest user opens the browser
Suite Teardown    TestTeardown User closes the browser
Force Tags
Resource          ../../../resources/GLOBAL_LIBRARIES.robot
Resource          ../../../resources/variables/GLOBAL_VARIABLES.robot
Resource          ../../../resources/variables/User_credentials.robot
Resource          ../../../resources/keywords/Login_actions.robot
Resource          ../../../resources/keywords/User_actions.robot

*** Test Cases ***
Academic organisations search (empty & invalid inputs)
    [Documentation]    INFUND-1231
    [Tags]    HappyPath    Email
    Given we create a new user    worth.email.test+invitedacademics@gmail.com
    Given the lead applicant invites a registered user    worth.email.test+invite3@gmail.com    worth.email.test+inviteacademics@gmail.com
    When the user opens the mailbox and accepts the invitation to collaborate
    And the user clicks the button/link    jQuery=.button:contains("Create")
    When the user selects the radio button    organisationType    2
    And the user clicks the button/link    jQuery=.button:contains("Continue")
    When the user selects the radio button    organisationType    5
    And the user clicks the button/link    jQuery=.button:contains("Continue")
    And the user clicks the button/link    jQuery=.button:contains("Search")
    Then the user should see an error    ${empty_field_warning_message}
    When the user enters text to a text field    id=organisationSearchName    abcd
    And the user clicks the button/link    jQuery=.button:contains("Search")
    Then the user should see the text in the page    Sorry we couldn't find any results.
    When the user enters text to a text field    id=organisationSearchName    !!
    And the user clicks the button/link    jQuery=.button:contains("Search")
    Then the user should see the text in the page    Please enter valid characters

Academic organisation (accept invitation flow)
    [Documentation]    INFUND-1166
    ...
    ...    INFUND-917
    ...    INFUND-2450
    [Tags]    HappyPath    Email
    When the user enters text to a text field    id=organisationSearchName    Liv
    And the user clicks the button/link    jQuery=.button:contains("Search")
    Then the user should see the text in the page    University of Liverpool
    When the user clicks the button/link    link= University of Liverpool
    And the user should see the text in the page    University (HEI)
    When the user clicks the button/link    jQuery=button:contains("Enter address manually")
    And the user enters text to a text field    id=addressForm.selectedPostcode.addressLine1    The East Wing
    And the user enters text to a text field    id=addressForm.selectedPostcode.addressLine2    Popple Manor
    And the user enters text to a text field    id=addressForm.selectedPostcode.addressLine3    1, Popple Boulevard
    And the user enters text to a text field    id=addressForm.selectedPostcode.town    Poppleton
    And the user enters text to a text field    id=addressForm.selectedPostcode.county    Poppleshire
    And the user enters text to a text field    id=addressForm.selectedPostcode.postcode    POPPS123
    And the user clicks the button/link    jQuery=.button:contains("Save organisation and continue")
    And the user clicks the button/link    jQuery=.button:contains("Save")
    And the user fills the create account form    Steven    Gerrard
    And the user opens the mailbox and verifies the email from
    And the user clicks the button/link    jQuery=.button:contains("Sign in")
    And guest user log-in    worth.email.test+inviteacademics@gmail.com    Passw0rd123
    When the user clicks the button/link    link=${OPEN_COMPETITION_LINK}
    And the user clicks the button/link    link=Your finances
    Then the user should see the text in the page    TSB reference
    And the user should not see the text in the page    Labour
    And the user should not see an error in the page

*** Keywords ***
