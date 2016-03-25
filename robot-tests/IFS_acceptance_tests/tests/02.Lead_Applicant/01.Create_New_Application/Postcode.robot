*** Settings ***
Documentation     INFUND-890 : As an applicant I want to use UK postcode lookup function to look up and enter my business address details as they won't necessarily be the same as the address held by Companies House, so that the system has accurate record of my contact details
Suite Setup       The guest user opens the browser
Suite Teardown    TestTeardown User closes the browser
Resource          ../../../resources/GLOBAL_LIBRARIES.robot
Resource          ../../../resources/variables/GLOBAL_VARIABLES.robot
Resource          ../../../resources/variables/User_credentials.robot
Resource          ../../../resources/keywords/Login_actions.robot
Resource          ../../../resources/keywords/User_actions.robot

*** Test Cases ***
Enter Valid Postcode and the results should be displayed in the dropdown
    [Documentation]    INFUND-890    # note that this will only work for the dev server for now, since postcode lookup isn't implemented on our local machines
    [Tags]    HappyPath    FailingForLocal
    Given the user navigates to the page    ${COMPETITION_DETAILS_URL}
    When the user clicks the button/link    jQuery=.column-third .button:contains("Apply now")
    And the user clicks the button/link    jQuery=.button:contains("Sign in to apply")
    And the user clicks the button/link    jQuery=.button:contains("Create")
    And the user enters text to a text field    id=org-name    Innovate
    And the user clicks the button/link    id=org-search
    And the user clicks the button/link    LINK=INNOVATE LTD
    When the user enters text to a text field    css=#postcode-check    BS14NT
    And the user clicks the button/link    id=postcode-lookup
    Then the user should see the element    css=#select-address-block
    And the user clicks the button/link    css=#select-address-block > button
    And the address fields should be filled

Empty Postcode field
    [Documentation]    INFUND-890
    [Tags]
    Given the user navigates to the page    ${POSTCODE_LOOKUP_URL}
    When the user enters text to a text field    css=#postcode-check    ${EMPTY}
    And the user clicks the button/link    id=postcode-lookup
    Then the user should see the element    css=.form-label .error-message

Same Operating address
    [Documentation]    INFUND-890
    [Tags]    HappyPath
    Given the user navigates to the page    ${POSTCODE_LOOKUP_URL}
    When the user selects the checkbox "The registered test is the same as the operating address"
    Then the user should not see the element    css=#postcode-check
    And the user unselects the checkbox "The registered test is the same as the operating address"
    And the user should see the element    css=#postcode-check

*** Keywords ***
the user selects the checkbox "The registered test is the same as the operating address"
    SLeep    1s
    Select Checkbox    id=address-same

the user unselects the checkbox "The registered test is the same as the operating address"
    Unselect Checkbox    id=address-same

the address fields should be filled
    # postcode lookup implemented on dev but not on our local machines, so check which is running:
    Run Keyword If    '${RUNNING_ON_DEV}' != ''    the address fields should be filled with valid data
    Run Keyword If    '${RUNNING_ON_DEV}' == ''    the address fields should be filled with dummy data

the address fields should be filled with valid data
    Textfield Should Contain    id=street    Am Reprographics
    Textfield Should Contain    id=street-2    King William House
    Textfield Should Contain    id=street-3    13 Queen Square
    Textfield Should Contain    id=town    Bristol
    Textfield Should Contain    id=county    City of Bristol
    Textfield Should Contain    id=postcode    BS1 4NT

the address fields should be filled with dummy data
    Textfield Should Contain    id=street    Montrose House 1
    Textfield Should Contain    id=street-2    Clayhill Park
    Textfield Should Contain    id=street-3    Cheshire West and Chester
    Textfield Should Contain    id=town    Neston
    Textfield Should Contain    id=county    Cheshire
    Textfield Should Contain    id=postcode    CH64 3RU
