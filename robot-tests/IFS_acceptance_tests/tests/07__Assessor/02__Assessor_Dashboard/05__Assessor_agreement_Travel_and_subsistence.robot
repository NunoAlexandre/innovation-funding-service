*** Settings ***
Documentation     INFUND-1481 As an assessor I need to review and accept the Innovate UK Assessor agreement so that I am able to assess a competition
...
...               INFUND-5628 As an assessor I want to be able to monitor my agreement expiry so that I can be sure that I am eligible to assess competitions
...
...               INFUND-5645 As an assessor I want to be able to review annexes to the agreement from the same screen so that I have all the information I need about assessing competitions
...
...               INFUND-5432 As an assessor I want to receive an alert to complete my profile when I log into my dashboard so that I can ensure that it is complete.
...
...               INFUND-7061 As an assessor I can view the travel and subsistence rates so that I know how much I can claim
Suite Setup       The user logs-in in new browser  &{existing_assessor1_credentials}
Suite Teardown    The user closes the browser
Force Tags        Assessor
Resource          ../../../resources/defaultResources.robot

*** Test Cases ***
Travel and subsistence rates
    [Documentation]    INFUND-7061
    [Tags]
    Given The user should see the element    link=travel and subsistence rates
    When the user clicks the button/link    jQuery=a:contains("travel and subsistence rates")
    Then the user should see the text in the page    Day subsistence
    And the user should see the text in the page    24 hour / overnight subsistence
    And the user should see the text in the page    Public transport
    And the user should see the text in the page    Mileage rates
    And the user should see the text in the page    Please make sure your travel claims, receipts and tickets are all submitted.
    [Teardown]    And the user clicks the button/link    link=Assessor dashboard

Cancel returns you back to the dashboard
    [Documentation]    INFUND-8009
    [Tags]
    Given The user should see the element   link=your assessor agreement    #his checks the alert message on the top of the page
    And the user clicks the button/link    jQuery=a:contains("your assessor agreement")
    When the user clicks the button/link    jQuery=a:contains("Cancel")
    Then the user should be redirected to the correct page    ${assessor_dashboard_url}
    [Teardown]    the user clicks the button/link    jQuery=a:contains("your assessor agreement")

Back button takes you to the previous page
    [Documentation]    INFUND-8009
    [Tags]
    Given the user clicks the button/link    link=Assessor dashboard
    Then the user should be redirected to the correct page    ${assessor_dashboard_url}
    [Teardown]    the user clicks the button/link    jQuery=a:contains("your assessor agreement")

Assessor agreement
    [Documentation]    INFUND-1481
    [Tags]
    When The user opens the link in new window  Download assessor agreement
    And the user goes back to the previous tab

Client-side validations and Submit
    [Documentation]    INFUND-1481
    ...
    ...
    ...    INFUND-5432
    [Tags]    HappyPath
    [Setup]
    Given the user navigates to the page    ${Server}/assessment/profile/agreement
    When the user selects the checkbox    agreesToTerms1
    And the user should not see an error in the page
    And the user clicks the button/link    jQuery=button:contains("Save and return to assessor dashboard")
    Then the user should be redirected to the correct page    ${assessor_dashboard_url}
    And The user should not see the element    jQuery=.message-alert a:contains('your assessor agreement')    #his checks the alert message on the top od the page

Agreement Confirmation
    [Documentation]    INFUND-5628
    Then the user clicks the button/link    jQuery=a:contains("your assessor agreement")
    And the user should see the text in the page    You signed the assessor agreement on

Find out more about our travel and subsistence rates
    [Documentation]    INFUND-8806
    [Tags]
    [Setup]    The user navigates to the page    ${Server}/assessment/profile/agreement
    Given the user should see the text in the page    Find out more about our travel and subsistence rates
    When the user clicks the button/link    link=travel and subsistence rates
    Then the user should be redirected to the correct page    ${Server}/assessment/profile/travel
    And The user goes back to the previous page

