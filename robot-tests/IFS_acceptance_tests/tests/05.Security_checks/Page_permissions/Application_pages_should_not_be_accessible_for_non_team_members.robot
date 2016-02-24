*** Settings ***
Documentation     INFUND-1683 As a user of IFS application, if I attempt to perform an action that I am not authorised perform, I am redirected to authorisation failure page with appropriate message
Test Teardown     TestTeardown User closes the browser
Resource          ../../../resources/GLOBAL_LIBRARIES.robot
Resource          ../../../resources/variables/GLOBAL_VARIABLES.robot
Resource          ../../../resources/variables/User_credentials.robot
Resource          ../../../resources/keywords/Login_actions.robot
Resource          ../../../resources/keywords/Applicant_actions.robot

*** Variables ***
${APPLICATION_7_OVERVIEW_PAGE}    ${SERVER}/application/7
${APPLICATION_7_FORM}    ${SERVER}/application/7/form/question/9

*** Test Cases ***
Guest user can't access overview page
    [Documentation]    INFUND-1683
    Given the guest user opens the browser
    When User navigates to the page    ${APPLICATION_7_OVERVIEW_PAGE}
    Then user should be redirected to the correct page    ${LOGIN_URL}

Guest user can't be able to access application form
    [Documentation]    INFUND-1683
    Given the guest user opens the browser
    When User navigates to the page    ${APPLICATION_7_FORM}
    Then user should be redirected to the correct page    ${LOGIN_URL}

Applicant who is not team member can't access overview page
    [Documentation]    INFUND-1683
    Given guest user log-in    &{collaborator2_credentials}
    When User navigates to the page    ${APPLICATION_7_OVERVIEW_PAGE}
    Then User should get an error page    Oops, something went wrong

Applicant who is not team member can't access application form page
    [Documentation]    INFUND-1683
    Given Guest user log-in    &{collaborator2_credentials}
    When User navigates to the page    ${APPLICATION_7_FORM}
    Then User should get an error page    Oops, something went wrong

Assessor can't access the overview page
    [Documentation]    INFUND-1683
    [Setup]    Guest user log-in    &{assessor_credentials}
    When User navigates to the page    ${APPLICATION_7_OVERVIEW_PAGE}
    Then User should get an error page    Oops, something went wrong

Assessor can't access the application form
    [Documentation]    INFUND-1683
    [Setup]    Guest user log-in    &{assessor_credentials}
    When User navigates to the page    ${APPLICATION_7_FORM}
    Then User should get an error page    Oops, something went wrong

*** Keywords ***
