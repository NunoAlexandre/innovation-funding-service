*** Settings ***
Documentation     INFUND-736: As an applicant I want to be able to add all the finance details for all the sections so I can sent in all the info necessary to apply
...
...               INFUND-438: As an applicant and I am filling in the finance details I want a fully working Other funding section
...
...               INFUND-45: As an applicant and I am on the application form on an open application, I expect the form to help me fill in financial details, so I can have a clear overview and less chance of making mistakes
Suite Setup       Run keywords    log in and create new application if there is not one already
...               AND    Applicant navigates to the finances of the Robot application
Suite Teardown    TestTeardown User closes the browser
Force Tags        HappyPath    Finances
Resource          ../../../../resources/GLOBAL_LIBRARIES.robot
Resource          ../../../../resources/variables/GLOBAL_VARIABLES.robot
Resource          ../../../../resources/variables/User_credentials.robot
Resource          ../../../../resources/keywords/Login_actions.robot
Resource          ../../../../resources/keywords/User_actions.robot
Resource          ../../../../resources/keywords/SUITE_SET_UP_ACTIONS.robot

*** Variables ***
${OTHER_FUNDING_SOURCE}    Alice
${OTHER_FUNDING_AMOUNT}    10000
${OTHER_FUNDING_DATE}    12-2008

*** Test Cases ***
Labour
    [Documentation]    INFUND-192
    ...
    ...    Acceptance tests for the Labour section calculations
    ...
    ...    INFUND-736
    ...
    ...    INFUND-1256
    [Tags]
    When the Applicant fills in the Labour costs for two rows
    Then Totals should be correct    css=#section-total-9    £ 104,348    css=[data-mirror="#section-total-9"]    £ 104,348
    And the user clicks the button/link    name=remove_cost
    The row should be removed    css=.labour-costs-table tr:nth-of-type(3) td:nth-of-type(4) input
    And the user reloads the page
    Then Totals should be correct    css=#section-total-9    £ 52,174    css=[data-mirror="#section-total-9"]    £ 52,174
    And the applicant edits the working days field
    Then Totals should be correct    css=#section-total-9    £ 48,000    css=[data-mirror="#section-total-9"]    £ 48,000
    [Teardown]    the user clicks the button/link    jQuery=button:contains("Labour")

Administration support costs
    [Documentation]    INFUND-192
    ...
    ...    Acceptance tests for the Administration support costs section calculations
    ...
    ...    INFUND-736z
    When the user clicks the button/link    jQuery=button:contains("Administration support costs")
    And the user clicks the button/link    jQuery=label:contains("20% of labour costs")
    Then admin costs total should be correct    id=section-total-10-default    £ 9,600
    And user selects the admin costs    overheads-type-29    CUSTOM_RATE
    And the user enters text to a text field    css=[id$="customRate"]    30
    Then admin costs total should be correct    id=section-total-10-custom    £ 14,400
    [Teardown]    the user clicks the button/link    jQuery=button:contains("Administration support costs")

Materials
    [Documentation]    INFUND-192
    ...
    ...    INFUND-736
    [Tags]
    When the Applicant fills the Materials fields
    Then Totals should be correct    css=#section-total-11    £ 2,000    css=[data-mirror="#section-total-11"]    £ 2,000
    And the user clicks the button/link    css=#material-costs-table tbody tr:nth-child(1) button
    And the user reloads the page
    Then Totals should be correct    css=#section-total-11    £ 1,000    css=[data-mirror="#section-total-11"]    £ 1,000
    [Teardown]    the user clicks the button/link    jQuery=button:contains("Materials")

Capital usage
    [Documentation]    INFUND-736
    [Tags]
    When the applicant fills the 'capital usage' field
    Then Totals should be correct    css=#section-total-12    £ 200    css=[data-mirror="#section-total-12"]    £ 200
    And the user clicks the button/link    css=#capital_usage [data-repeatable-row]:nth-child(1) button
    And the user reloads the page
    Then Totals should be correct    css=#section-total-12    £ 100    css=[data-mirror="#section-total-12"]    £ 100
    And the user clicks the button/link    css=#capital_usage [data-repeatable-row]:nth-child(1) button
    [Teardown]    the user clicks the button/link    jQuery=button:contains("Capital usage")

Subcontracting costs
    [Documentation]    INFUND-192
    ...    INFUND-736
    ...    INFUND-2303
    [Tags]
    When the applicant edits the Subcontracting costs section
    Then Totals should be correct    css=#section-total-13    £ 200    css=[aria-controls="collapsible-4"] [data-mirror]    £ 200
    And the user clicks the button/link    css=#subcontracting [data-repeatable-row]:nth-child(1) button
    And the user reloads the page
    Then Totals should be correct    css=#section-total-13    £ 100    css=[aria-controls="collapsible-4"] [data-mirror]    £ 100
    [Teardown]    the user clicks the button/link    jQuery=button:contains("Subcontracting costs")

Travel and subsistence
    [Documentation]    INFUND-736
    [Tags]
    When the Applicant fills the Travel fields
    Then Totals should be correct    css=#section-total-14    £ 2,000    css=[data-mirror="#section-total-14"]    £ 2,000
    And the user clicks the button/link    css=#travel-costs-table [data-repeatable-row]:nth-child(1) button
    And the user reloads the page
    Then Totals should be correct    css=#section-total-14    £ 1,000    css=[data-mirror="#section-total-14"]    £ 1,000
    [Teardown]    the user clicks the button/link    jQuery=button:contains("Travel and subsistence")

Other costs
    [Documentation]    INFUND-736
    [Tags]
    When the applicant adds one row for the other costs
    Then Totals should be correct    id=section-total-15    £ 200    css=[data-mirror="#section-total-15"]    £ 200
    Then the user reloads the page
    Then Totals should be correct    id=section-total-15    £ 200    css=[data-mirror="#section-total-15"]    £ 200
    [Teardown]    the user clicks the button/link    jQuery=button:contains("Other Costs")

Other Funding
    [Documentation]    INFUND-438, INFUND-2257, INFUND-3196
    [Tags]
    Given the user clicks the button/link    jQuery=#otherFundingShowHideToggle label:contains(No) input
    Then the user should not see the element    jQuery=button:contains('Add another source of funding')
    And the applicant selects 'Yes' and fills two rows
    Then the total of the other funding should be correct
    And the applicant can leave the 'Your finances' page but the details are still saved
    And the user clicks the button/link    jQuery=#otherFundingShowHideToggle label:contains(No) input
    Then the user should not see the element    jQuery=button:contains('Add another source of funding')
    And the applicant cannot see the 'other funding' details
    Then the user reloads the page
    Given the user clicks the button/link    jQuery=#otherFundingShowHideToggle label:contains(Yes) input
    Then the total of the other funding should be correct

Funding level
    [Tags]
    Then auto-save should work for the "Grant" field
    And the grant value should be correct in the finance summary page

*** Keywords ***
the Applicant fills in the Labour costs for two rows
    the user clicks the button/link    jQuery=button:contains("Labour")
    the user should see the element    css=.labour-costs-table tr:nth-of-type(1) td:nth-of-type(2) input
    Clear Element Text    css=[name^="labour-labourDaysYearly"]
    Input Text    css=[name^="labour-labourDaysYearly"]    230
    Input Text    css=.labour-costs-table tr:nth-of-type(1) td:nth-of-type(1) input    test
    Input Text    css=.labour-costs-table tr:nth-of-type(1) td:nth-of-type(2) input    120000
    Input Text    css=.labour-costs-table tr:nth-of-type(1) td:nth-of-type(4) input    100
    mouse out    css=input
    Focus    jQuery=button:contains('Add another role')
    the user clicks the button/link    jQuery=button:contains('Add another role')
    the user should see the element    css=.labour-costs-table tr:nth-of-type(3) td:nth-of-type(4) input
    Input Text    css=.labour-costs-table tr:nth-of-type(3) td:nth-of-type(2) input    120000
    Input Text    css=.labour-costs-table tr:nth-of-type(3) td:nth-of-type(4) input    100
    Input Text    css=.labour-costs-table tr:nth-of-type(3) td:nth-of-type(1) input    test
    Mouse Out    css=.labour-costs-table tr:nth-of-type(3) td:nth-of-type(1) input
    Mouse Out    css=input
    focus    css=.app-submit-btn

the applicant edits the working days field
    the user should see the element    css=[name^="labour-labourDaysYearly"]
    Clear Element Text    css=[name^="labour-labourDaysYearly"]
    Input Text    css=[name^="labour-labourDaysYearly"]    250
    Focus    css=.app-submit-btn
    Sleep    200ms

the Applicant fills the Materials fields
    the user clicks the button/link    jQuery=button:contains("Materials")
    the user should see the element    css=#material-costs-table tbody tr:nth-of-type(1) td:nth-of-type(2) input
    Input Text    css=#material-costs-table tbody tr:nth-of-type(1) td:nth-of-type(2) input    10
    Input Text    css=#material-costs-table tbody tr:nth-of-type(1) td:nth-of-type(3) input    100
    input text    css=#material-costs-table tbody tr:nth-of-type(1) td:nth-of-type(1) input    test
    Focus    jQuery=button:contains('Add another materials cost')
    the user clicks the button/link    jQuery=button:contains('Add another materials cost')
    the user should see the element    css=#material-costs-table tbody tr:nth-of-type(2) td:nth-of-type(2) input
    Input Text    css=#material-costs-table tbody tr:nth-of-type(2) td:nth-of-type(2) input    10
    Input Text    css=#material-costs-table tbody tr:nth-of-type(2) td:nth-of-type(3) input    100
    Input Text    css=#material-costs-table tbody tr:nth-of-type(2) td:nth-of-type(1) input    test
    mouse out    css=#material-costs-table tbody tr:nth-of-type(2) td:nth-of-type(1) input
    focus    css=.app-submit-btn

the applicant edits the Subcontracting costs section
    the user clicks the button/link    jQuery=button:contains("Subcontracting costs")
    the user should see the text in the page    Subcontractor name
    Input Text    css=#collapsible-4 .form-row:nth-child(1) input[id$=subcontractingCost]    100
    input text    css=.form-row:nth-child(1) [name^="subcontracting-name"]    test1
    input text    css=.form-row:nth-child(1) [name^="subcontracting-country-"]    test2
    input text    css=.form-row:nth-child(1) [name^="subcontracting-role"]    test3
    Mouse Out    css=input
    focus    jQuery=button:contains('Add another subcontractor')
    the user clicks the button/link    jQuery=button:contains('Add another subcontractor')
    the user should see the element    css=#collapsible-4 .form-row:nth-child(2)
    input text    css=.form-row:nth-child(2) [name^="subcontracting-name"]    test1
    input text    css=.form-row:nth-child(2) [name^="subcontracting-country-"]    test2
    input text    css=.form-row:nth-child(2) [name^="subcontracting-role"]    test3
    Input Text    css=#collapsible-4 .form-row:nth-child(2) input[id$=subcontractingCost]    100
    input text    css=#collapsible-4 .form-row:nth-child(1) input[id$=name]    test
    mouse out    css=#collapsible-4 .form-row:nth-child(1) input[id$=name]
    focus    css=.app-submit-btn

the applicant fills the 'capital usage' field
    the user clicks the button/link    jQuery=button:contains("Capital usage")
    Input Text    css=.form-row:nth-child(1) .form-finances-capital-usage-npv    1000
    Input Text    css=.form-row:nth-child(1) .form-finances-capital-usage-npv    1000
    input text    css=.form-row:nth-child(1) .form-finances-capital-usage-residual-value    900
    input text    css=.form-finances-capital-usage-utilisation    100
    input text    css=.form-finances-capital-usage-depreciation    11
    input text    css=.form-row:nth-child(1) [name^="capital_usage-description"]    Test
    the user clicks the button/link    jQuery=.form-row:nth-child(1) label:contains(Existing) input
    sleep    200ms
    focus    jQuery=button:contains('Add another asset')
    the user clicks the button/link    jQuery=button:contains('Add another asset')
    the user should see the element    css=.form-row:nth-child(2) .form-finances-capital-usage-npv
    Input Text    css=.form-row:nth-child(2) .form-finances-capital-usage-npv    1000
    input text    css=.form-row:nth-child(2) .form-finances-capital-usage-residual-value    900
    input text    css=.form-row:nth-child(2) .form-finances-capital-usage-utilisation    100
    Input Text    css=.form-row:nth-child(2) .form-finances-capital-usage-depreciation    10
    input text    css=.form-row:nth-child(2) [name^="capital_usage-description"]    Test
    the user clicks the button/link    jQuery=.form-row:nth-child(2) label:contains(Existing) input
    mouse out    css=.form-row:nth-child(2) [name^="capital_usage-description"]
    focus    css=.app-submit-btn

the Applicant fills the Travel fields
    the user clicks the button/link    jQuery=button:contains("Travel and subsistence")
    the user should see the element    css=#travel-costs-table tbody tr:nth-of-type(1) td:nth-of-type(2) input
    Input Text    css=#travel-costs-table tbody tr:nth-of-type(1) td:nth-of-type(2) input    10
    Input Text    css=#travel-costs-table tbody tr:nth-of-type(1) td:nth-of-type(3) input    100
    Input Text    css=#travel-costs-table tbody tr:nth-of-type(1) td:nth-of-type(1) input    test
    Mouse Out    css=input
    #sleep    1s
    focus    jQuery=button:contains('Add another travel cost')
    the user clicks the button/link    jQuery=button:contains('Add another travel cost')
    the user should see the element    css=#travel-costs-table tbody tr:nth-of-type(2) td:nth-of-type(2) input
    Input Text    css=#travel-costs-table tbody tr:nth-of-type(2) td:nth-of-type(2) input    10
    Input Text    css=#travel-costs-table tbody tr:nth-of-type(2) td:nth-of-type(3) input    100
    Input Text    css=#travel-costs-table tbody tr:nth-of-type(2) td:nth-of-type(1) input    test
    mouse out    css=#travel-costs-table tbody tr:nth-of-type(2) td:nth-of-type(1) input
    focus    css=.app-submit-btn

the applicant adds one row for the other costs
    the user clicks the button/link    jQuery=button:contains("Other Costs")
    #the user clicks the button/link    jQuery=button:contains('Add another cost')
    the user should see the element    css=#other-costs-table tbody tr:nth-of-type(1) td:nth-of-type(2) input
    Input Text    css=#other-costs-table tbody tr:nth-of-type(1) td:nth-of-type(2) input    100
    Input Text    css=#other-costs-table tbody tr:nth-of-type(1) td:nth-of-type(1) textarea    test
    the user clicks the button/link    jQuery=button:contains('Add another cost')
    the user should see the element    css=#other-costs-table tbody tr:nth-of-type(2) td:nth-of-type(2) input
    Input Text    css=#other-costs-table tbody tr:nth-of-type(2) td:nth-of-type(1) textarea    test
    Input Text    css=#other-costs-table tbody tr:nth-of-type(2) td:nth-of-type(2) input    100
    Mouse Out    css=#other-costs-table tbody tr:nth-of-type(2) td:nth-of-type(2) input
    focus    css=.app-submit-btn

the user reloads the page
    Reload page
    sleep    500ms

the total of the other funding should be correct
    Textfield Value Should Be    id=other-funding-total    £ 20,000

The applicant cannot see the 'other funding' details
    the user should not see the text in the page    ${OTHER_FUNDING_SOURCE}
    the user should not see the text in the page    ${OTHER_FUNDING_DATE}
    the user should not see the text in the page    ${OTHER_FUNDING_AMOUNT}

The applicant can leave the 'Your finances' page but the details are still saved
    Execute Javascript    jQuery('form').attr('data-test','true');
    the user reloads the page
    the user should see the element    css=#other-funding-table tbody tr:nth-of-type(1) td:nth-of-type(1) input
    Textfield Should Contain    css=#other-funding-table tbody tr:nth-of-type(1) td:nth-of-type(1) input    ${OTHER_FUNDING_SOURCE}
    Textfield Should Contain    css=#other-funding-table tbody tr:nth-of-type(1) td:nth-of-type(2) input    ${OTHER_FUNDING_DATE}

The applicant selects 'Yes' and fills two rows
    the user clicks the button/link    jQuery=label:contains(Yes) input
    Run Keyword And Ignore Error    Click element    jQuery=#other-funding-table button:contains("Remove")
    the user should see the element    css=#other-funding-table tbody tr:nth-of-type(1) td:nth-of-type(2) input
    the user should see the element    id=other-funding-table
    Input Text    css=#other-funding-table tbody tr:nth-of-type(1) td:nth-of-type(2) input    ${OTHER_FUNDING_DATE}
    Input Text    css=#other-funding-table tbody tr:nth-of-type(1) td:nth-of-type(3) input    ${OTHER_FUNDING_AMOUNT}
    Input Text    css=#other-funding-table tbody tr:nth-of-type(1) td:nth-of-type(1) input    ${OTHER_FUNDING_SOURCE}
    Focus    jQuery=button:contains('Add another source of funding')
    the user clicks the button/link    jQuery=button:contains('Add another source of funding')
    the user should see the element    css=#other-funding-table tbody tr:nth-of-type(2) td:nth-of-type(2) input
    the user clicks the button/link    css=#other-funding-table tbody tr:nth-of-type(2) td:nth-of-type(1) input
    Input Text    css=#other-funding-table tbody tr:nth-of-type(2) td:nth-of-type(2) input    ${OTHER_FUNDING_DATE}
    Input Text    css=#other-funding-table tbody tr:nth-of-type(2) td:nth-of-type(3) input    ${OTHER_FUNDING_AMOUNT}
    the user should see the element    css=#other-funding-table tbody tr:nth-of-type(2) td:nth-of-type(1) input
    Input Text    css=#other-funding-table tbody tr:nth-of-type(2) td:nth-of-type(1) input    ${OTHER_FUNDING_SOURCE}
    focus    css=.app-submit-btn

Totals should be correct
    [Arguments]    ${TOTAL_FIELD}    ${FIELD_VALUE}    ${TOTAL_COLLAPSIBLE}    ${COLLAPSIBLE_VALUE}
    Textfield Value Should Be    ${TOTAL_FIELD}    ${FIELD_VALUE}
    Element Should Contain    ${TOTAL_COLLAPSIBLE}    ${COLLAPSIBLE_VALUE}

User selects the admin costs
    [Arguments]    ${RADIO_BUTTON}    ${SELECTION}
    click element    xpath=//input[@type='radio' and starts-with(@name, '${RADIO_BUTTON}') and (@value='${SELECTION}' or @id='${SELECTION}')]
    focus    css=.app-submit-btn

Admin costs total should be correct
    [Arguments]    ${ADMIN_TOTAL}    ${ADMIN_VALUE}
    focus    css=.app-submit-btn
    the user should see the element    ${ADMIN_TOTAL}
    Textfield Value Should Be    ${ADMIN_TOTAL}    ${ADMIN_VALUE}
    Element Should Contain    jQuery=button:contains("Administration support costs")    ${ADMIN_VALUE}

the grant value should be correct in the finance summary page
    The user navigates to the next page
    Element Should Contain    css=.finance-summary tr:nth-of-type(1) td:nth-of-type(2)    25

auto-save should work for the "Grant" field
    Clear Element Text    id=cost-financegrantclaim
    focus    jQuery= button:contains('complete')
    Sleep    500ms
    Input Text    id=cost-financegrantclaim    25
    focus    jQuery= button:contains('complete')
    Sleep    300ms
    Reload Page
    focus    jQuery= button:contains('complete')
    ${input_value} =    Get Value    id=cost-financegrantclaim
    Should Be Equal As Strings    ${input_value}    25

The user navigates to the next page
    The user clicks the button/link    css=.next .pagination-label
    Run Keyword And Ignore Error    confirm action

The row should be removed
    [Arguments]    ${ROW}
    Wait Until Element Is Not Visible    ${ROW}
