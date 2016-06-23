*** Settings ***
Documentation     INFUND-1423 Going back from the 'create your account' page gives an error
Suite Setup       The guest user opens the browser
Suite Teardown
Test Teardown     The user closes the browser
Resource          ../../../resources/GLOBAL_LIBRARIES.robot
Resource          ../../../resources/variables/GLOBAL_VARIABLES.robot
Resource          ../../../resources/variables/User_credentials.robot
Resource          ../../../resources/keywords/Login_actions.robot
Resource          ../../../resources/keywords/User_actions.robot

*** Test Cases ***
Click the back button while on the create account page
    [Documentation]    INFUND-1423
    [Tags]    Create account    Back button     Pending
    # Pending due to INFUND-3690
    Given the user navigates to the page    ${LOGIN_URL}
    When the user follows the flow to register their organisation
    And the user goes back to the previous page
    Then the user should be redirected to the correct page    ${confirm_organisation_url}

The user logs in and visits the create account page
    [Documentation]    INFUND-1423
    [Tags]    Create account    Back button
    Given Guest user log-in    &{lead_applicant_credentials}
    When the user navigates to the page    ${ACCOUNT_CREATION_FORM_URL}
    Then the user should see the text in the page    Your Profile

*** Keywords ***
