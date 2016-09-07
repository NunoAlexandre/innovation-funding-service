*** Settings ***
Documentation     INFUND-3010 As a partner I want to be able to supply bank details for my business so that Innovate UK can verify its suitability for funding purposes
...
...               INFUND-3282 As a partner I want to be able to supply an existing or new address for my bank account to support the bank details verification process
...
...               INFUND-2621 As a contributor I want to be able to review the current Project Setup status of all partners in my project so I can get an indication of the overall status of the consortium
Suite Setup       Log in as user    steve.smith@empire.com    Passw0rd
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

*** Test Cases ***

Bank details page
    [Documentation]    INFUND-3010
    [Tags]    HappyPath
    Given guest user log-in  steve.smith@empire.com    Passw0rd
    When the user clicks the button/link    link=00000001: best riffs
    And the user clicks the button/link    link=Bank details
    Then the user should see the element    jQuery=.button:contains("Submit bank account details")
    And the user should see the text in the page    Bank account

Bank details server side validations
    [Documentation]    INFUND-3010
    [Tags]
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
    [Tags]    HappyPath
    When the user selects the radio button    addressType    ADD_NEW
    And the user enters text to a text field    name=addressForm.postcodeInput    ${EMPTY}
    And the user clicks the button/link    jQuery=.button:contains("Find UK address")
    Then the user should see the element    css=.form-label .error-message
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
    # Please note that the bank details for these Experian tests are dummy data specfically chosen to elicit certain responses from the stub.
    When the user submits the bank account details    12345673    000003
    Then the user should see the text in the page    Bank account details are incorrect, please check and try again

Bank details submission
    [Documentation]    INFUND-3010, INFUND-2621
    [Tags]    Experian    HappyPath
    # Please note that the bank details for these Experian tests are dummy data specfically chosen to elicit certain responses from the stub.
    When the user enters text to a text field    name=accountNumber    51406795
    And the user enters text to a text field    name=sortCode    404745
    When the user clicks the button/link    jQuery=.button:contains("Submit bank account details")
    And the user clicks the button/link    jquery=button:contains("Cancel")
    And the user should not see the text in the page    Bank account details below are now being reviewed
    When the user clicks the button/link    jQuery=.button:contains("Submit bank account details")
    And the user clicks the button/link    jquery=button:contains("Submit")
    And the user should see the text in the page    Bank account details below are now being reviewed
    And the user should see the element    css=.success-alert
    Then the user navigates to the page    ${project_in_setup_page}
    And the user should see the element    jQuery=ul li.waiting:nth-child(4)
    When the user clicks the button/link    link=What's the status of each of my partners?
    Then the user navigates to the page             ${project_in_setup_page}/team-status
    And the user should see the text in the page    Project team status
    And the user should see the element             jQuery=#table-project-status tr:nth-of-type(1) td.status.waiting:nth-of-type(3)
    [Teardown]    logout as user

Bank details for non-lead partner
    [Documentation]    INFUND-3010, INFUND-2621
    [Tags]    Experian    HappyPath
    # Please note that the bank details for these Experian tests are dummy data specfically chosen to elicit certain responses from the stub.
    Given guest user log-in    pete.tom@egg.com    Passw0rd
    And the user clicks the button/link    link=00000001: best riffs
    And the user clicks the button/link    link=Bank details
    When the user enters text to a text field    name=accountNumber    51406795
    And the user enters text to a text field    name=sortCode    404745
    When the user selects the radio button    addressType    ADD_NEW
    And the user enters text to a text field    id=addressForm.postcodeInput    BS14NT
    And the user clicks the button/link    id=postcode-lookup
    Then the user should see the element    css=#select-address-block
    And the user clicks the button/link    css=#select-address-block > button
    And the address fields should be filled
    When the user clicks the button/link    jQuery=.button:contains("Submit bank account details")
    And the user clicks the button/link    jquery=button:contains("Cancel")
    And the user should not see the text in the page    Bank details below are now being reviewed
    When the user clicks the button/link    jQuery=.button:contains("Submit bank account details")
    And the user clicks the button/link    jquery=button:contains("Submit")
    And the user should see the text in the page    Bank account details below are now being reviewed
    And the user should see the element    css=.success-alert
    Then the user navigates to the page    ${project_in_setup_page}
    And the user should see the element    jQuery=ul li.complete:nth-child(2)
    # TODO uncomment below when INFUND-4735 is done
#    When the user clicks the button/link    link=What's the status of each of my partners?
#    Then the user navigates to the page     ${project_in_setup_page}/team-status
#    And the user should see the text in the page    Project team status
#    And the user should see the element             jQuery=#table-project-status tr:nth-of-type(3) td.status.waiting:nth-of-type(3)
    [Teardown]    logout as user

Bank details don't show for partner with no finance details
    [Documentation]    INFUND-3010
    [Tags]
    Given guest user log-in  jessica.doe@ludlow.co.uk    Passw0rd
    When the user clicks the button/link    link=00000001: best riffs
    Then the user should not see the element    link=Bank details
    And the user should see the text in the page    Bank details (not required)




*** Keywords ***
the user moves focus away from the element
    [Arguments]    ${element}
    mouse out    ${element}
    focus    jQuery=.button:contains("Submit bank account details")

the user submits the bank account details
    [Arguments]    ${account_number}    ${sort_code}
    the user enters text to a text field    name=accountNumber    ${account_number}
    the user enters text to a text field    name=sortCode    ${sort_code}
    the user clicks the button/link    jQuery=.button:contains("Submit bank account details")
    the user clicks the button/link    jQuery=.button:contains("Submit")
