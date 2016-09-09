*** Settings ***
Documentation     INFUND-3763 As a project finance team member I want to receive feedback from Experian regarding a partners' bank account details
...
...               INFUND-4054 As a Project Finance team member I want to be able to review and amend unverified partner bank details to ensure they are suitable for approval
Suite Setup       Log in as user    project.finance1@innovateuk.test    Passw0rd
Suite Teardown    the user closes the browser
Force Tags        Experian    Project Setup
Resource          ../../resources/GLOBAL_LIBRARIES.robot
Resource          ../../resources/variables/GLOBAL_VARIABLES.robot
Resource          ../../resources/variables/User_credentials.robot
Resource          ../../resources/keywords/Login_actions.robot
Resource          ../../resources/keywords/User_actions.robot
Resource          ../../resources/variables/EMAIL_VARIABLES.robot
Resource          ../../resources/keywords/SUITE_SET_UP_ACTIONS.robot

*** Variables ***

*** Test Cases ***
The user can see the company name with score
    [Documentation]    INFUND-3763
    [Tags]    HappyPath
    When the user navigates to the page    ${server}/project-setup-management/project/1/organisation/31/review-bank-details    # note that this user does not have a dashboard yet, so we need to browse to this page directly for now
    Then the user should see the text in the page    Vitruvius Stonework Limited
    And the user should see the element    css = tr:nth-child(1) .yes

The user can see the company number with status
    [Documentation]    INFUND-3763
    [Tags]
    Then the user should see the text in the page    Company Number
    And the user should see the text in the page    60674010
    And the user should see the element    css = tr:nth-child(2) .yes

The user can see the account number with status
    [Documentation]    INFUND-3763
    [Tags]
    Then the user should see the text in the page    Bank account number / Sort code
    And the user should see the text in the page    51406795 / 404745
    And the user should see the element    css = tr:nth-child(3) .yes

The user can see the address with score
    [Documentation]    INFUND-3763
    [Tags]
    Then the user should see the text in the page    Address
    And the user should see the element    css = tr:nth-child(4) .no

The user has the options to edit the details and to approve the bank details
    [Documentation]    INFUND-3763
    [Tags]
    Then the user should see the element    link=Change bank account details
    And the user should see the element    jQuery=.button:contains("Approve bank account details")


The user can change bank account details
    [Documentation]    INFUND-4054
    [Tags]    HappyPath
    Given the user clicks the button/link        link=Change bank account details
    Then the user navigates to the page          ${server}/project-setup-management/project/1/organisation/31/review-bank-details/change
    And the text box should be editable          id=company-name
    Then the user enters text to a text field    id=street    Montrose House 2
    And the user enters text to a text field     id=company-name    Vitruvius Stonework not LImited
    And the user enters text to a text field     id=companies-house-number    60674011

The user cancels bank details changes
    [Documentation]    INFUND-4054
    [Tags]
    When the user clicks the button/link          link=Cancel bank account changes
    Then the user navigates to the page           ${server}/project-setup-management/project/1/organisation/31/review-bank-details
    When the user clicks the button/link          link=Change bank account details
    Then the user should see the text in field    id=street    Montrose House 1

The user updates bank account details
    [Documentation]    INFUND-4054
    [Tags]    HappyPath
    When the user enters text to a text field     id=street    Montrose House 2
    And the user clicks the button/link           jQuery=.column-half.alignright .button:contains("Update bank account details")
    And the user clicks the button/link           jQuery=.modal-partner-change-bank-details .button:contains("Update bank account details")   #Due to popup
    Then the user navigates to the page           ${server}/project-setup-management/project/1/organisation/31/review-bank-details
    When the user clicks the button/link          link=Change bank account details
    Then the user should see the text in field    id=street    Montrose House 2
    [Teardown]    logout as user


Other internal users cannot access this page
    [Documentation]    INFUND-3763
    [Tags]
    [Setup]    guest user log-in    john.doe@innovateuk.test    Passw0rd
    the user navigates to the page and gets a custom error message    ${server}/project-setup-management/project/1/organisation/31/review-bank-details    You do not have the necessary permissions for your request
    [Teardown]    logout as user

Project partners cannot access this page
    [Documentation]    INFUND-3763
    [Tags]    Pending    # TODO  Pending due to INFUND-4680
    [Setup]    guest user log-in    steve.smith@empire.com    Passw0rd
    the user navigates to the page and gets a custom error message    ${server}/project-setup-management/project/1/organisation/31/review-bank-details    You do not have the necessary permissions for your your request
    [Teardown]    logout as user

*** Keywords ***
the text box should be editable
    [Arguments]    ${text_field}
    Element Should Be Enabled    ${text_field}

the user should see the text in field
    [Arguments]    ${text_field}    ${text}
    focus    ${text_field}
    textfield should contain    ${text_field}    ${text}