*** Settings ***
Documentation     INFUND-3010 As a partner I want to be able to supply bank details for my business so that Innovate UK can verify its suitability for funding purposes
...
...               INFUND-3282 As a partner I want to be able to supply an existing or new address for my bank account to support the bank details verification process

Suite Setup       Log in as user    steve.smith@empire.com       Passw0rd
Suite Teardown    the user closes the browser
Force Tags
Resource          ../../resources/GLOBAL_LIBRARIES.robot
Resource          ../../resources/variables/GLOBAL_VARIABLES.robot
Resource          ../../resources/variables/User_credentials.robot
Resource          ../../resources/keywords/Login_actions.robot
Resource          ../../resources/keywords/User_actions.robot
Resource          ../../resources/variables/EMAIL_VARIABLES.robot
Resource          ../../resources/keywords/SUITE_SET_UP_ACTIONS.robot

*** Variables ***


*** Test Cases ***

Bank details server side validations
    [Documentation]    INFUND-3010
    [Tags]
    Given guest user log-in    steve.smith@empire.com    Passw0rd
    And the user clicks the button/link    link=00000001: best riffs
    And the user clicks the button/link    link=Bank details
    When the user clicks the button/link    jQuery=.button:contains("Submit bank account details")
    Then the user should see an error    Please enter an account number
    And the user should see an error    Please enter a sort code
    And the user should see an error    You need to select a billing address before you can continue

Bank details client side validations
    [Documentation]    INFUND-3010
    [Tags]
    When the user enters text to a text field    name=accountNumber    1234567
    And the user moves focus away from the element    name=accountNumber
    Then the user should not see the text in the page    Please enter an account number
    And the user should see an error    Please enter a valid account number
    When the user enters text to a text field    name=accountNumber    12345679
    And the user moves focus away from the element    name=accountNumber
    Then the user should not see the text in the page    Please enter an account number
    And the user should not see the text in the page    Please enter a valid account number
    When the user enters text to a text field    name=sortCode    12345
    And the user moves focus away from the element    name=sortCode
    Then the user should see an error    Please enter a valid sort code
    When the user enters text to a text field    name=sortCode    123456
    And the user moves focus away from the element    name=sortCode
    Then the user should not see the text in the page    Please enter a sort code
    And the user should not see the text in the page    Please enter a valid sort code
    When the user selects the radio button    addressType    REGISTERED
    Then the user should not see the text in the page    You need to select a billing address before you can continue

Bank account postcode lookup
    [Documentation]    INFUND-3282
    [Tags]
    When the user selects the radio button    addressType    ADD_NEW
    When the user enters text to a text field    name=addressForm.postcodeInput    ${EMPTY}
    # TODO the following two steps have been commented out as they are
    # Pending due to INFUND-4043
    # And the user clicks the button/link    jQuery=.button:contains("Find UK address")
    # Then the user should see the element    css=.form-label .error-message
    When the user enters text to a text field    name=addressForm.postcodeInput    BS14NT/
    And the user clicks the button/link    jQuery=.button:contains("Find UK address")
    Then the user should see the element    name=addressForm.selectedPostcodeIndex
    When the user selects the radio button    addressType    ADD_NEW
    And the user enters text to a text field    id=addressForm.postcodeInput    BS14NT
    And the user clicks the button/link    id=postcode-lookup
    Then the user should see the element    css=#select-address-block
    And the user clicks the button/link    css=#select-address-block > button
    And the address fields should be filled

Bank details experian validations
    [Documentation]    INFUND-3010
    [Tags]    Experian
    When the user submits the bank account details     12345673      000003
    Then the user should see the text in the page     Modulus check has failed
    When the user submits the bank account details     22345616     000003
    Then the user should see the text in the page      Account does not support Direct Debit transactions
    When the user submits the bank account details     22345632      000003
    Then the user should see the text in the page       Account does not support Direct Credit transactions
    When the user submits the bank account details      22345624     000003
    Then the user should see the text in the page       Collection account requires a reference or roll account number
    When the user submits the bank account details      22345683    000003
    Then the user should see the text in the page       Account does not support AUDDIS transactions
    When the user submits the bank account details     22345610     000004
    Then the user should see the text in the page      Alternate information is available for this account




Bank details submission
    [Documentation]    INFUND-3010
    [Tags]     Experian
    When the user enters text to a text field      name=accountNumber         12345677
    And the user enters text to a text field       name=sortCode              000003
    When the user clicks the button/link    jQuery=.button:contains("Submit bank account details")
    And the user clicks the button/link    jquery=button:contains("Cancel")
    And the user should not see the text in the page    Your bank details have been approved
    When the user clicks the button/link    jQuery=.button:contains("Submit bank account details")
    And the user clicks the button/link    jquery=button:contains("Submit")
    And the user should see the text in the page    Your bank details have been approved
    And the user should see the element    css=.success-alert
    Then the user navigates to the page    ${project_in_setup_page}
    And the user should see the element    jQuery=ul li.complete:nth-child(2)


*** Keywords ***

the user moves focus away from the element
    [Arguments]    ${element}
    mouse out    ${element}
    focus    jQuery=.button:contains("Submit bank account details")

the user submits the bank account details
   [Arguments]     ${account_number}      ${sort_code}
   the user enters text to a text field     name=accountNumber    ${account_number}
   the user enters text to a text field      name=sortCode        ${sort_code}
   the user clicks the button/link          jQuery=.button:contains("Submit bank account details")
   the user clicks the button/link          jQuery=.button:contains("Submit")