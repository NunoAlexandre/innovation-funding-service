*** Settings ***
Documentation     -INFUND-1095: The creator of the application gets the Lead applicant role
Suite Setup       The guest user opens the browser
Suite Teardown    TestTeardown User closes the browser
Resource          ../../../resources/GLOBAL_LIBRARIES.robot
Resource          ../../../resources/variables/GLOBAL_VARIABLES.robot
Resource          ../../../resources/variables/User_credentials.robot
Resource          ../../../resources/keywords/Login_actions.robot
Resource          ../../../resources/keywords/Applicant_actions.robot

*** Variables ***

*** Test Cases ***
Do nothing at all
    [Documentation]    Test not yet implemented
    Don't do anything

*** Keywords ***
Don't do anything
    the user navigates to the page    ${application_overview_url}
