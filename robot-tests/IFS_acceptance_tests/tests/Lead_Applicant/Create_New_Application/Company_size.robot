*** Settings ***
Documentation     INFUND-891: As an Applicant I want to be able to add the organisation size against my company profile during initial registration so I can be correctly aligned with eligibility criteria for the competition
Suite Setup        The guest user opens the browser
Suite Teardown     TestTeardown User closes the browser
Resource          ../../../resources/GLOBAL_LIBRARIES.robot
Resource          ../../../resources/variables/GLOBAL_VARIABLES.robot
Resource          ../../../resources/variables/User_credentials.robot
Resource          ../../../resources/keywords/Login_actions.robot
Resource          ../../../resources/keywords/Applicant_actions.robot

*** Variables ***
${CREATE_YOUR_ACCOUNT_PAGE}    ${SERVER}/application/create/selected-business/05493105
${REGISTRATION_PAGE}    ${SERVER}registration/register?organisationId=13#

*** Test Cases ***
Enter address manually
    [Documentation]    INFUND-891
    [Tags]    Applicant    New application    Company size
    Given the applicant is in the 'create your account' page
    When the applicant clicks the "Enter address manually"
    Then the address details form should be visible


EU Definition link should navigate to the correct page
    [Documentation]    INFUND-891
    [Tags]    Applicant    New application    Company size   Pending
    # The company size functionality has now moved to the finances section
    # so this is pending, ready to be deleted on review
    Given the applicant is in the 'create your account' page
    When the applicant clicks the 'EU definition' link
    Then the applicant should navigate to the correct page


The applicant should be able to choose different organisation sizes
    [Documentation]    INFUND-891
    [Tags]    Applicant    Company Size    New application  Pending
    # Tagged as pending since the choose organisation size is no longer on this page
    Given the applicant is in the 'create your account' page
    And the applicant selects the Micro size and saves the page
    And the Applicant should redirect to the create account page
    When the applicant selects the Medium size and saves the page
    And the Applicant should redirect to the create account page
    Then the applicant selects the Large size and saves the page
    And the Applicant should redirect to the create account page

*** Keywords ***
the applicant is in the 'create your account' page
    go to    ${CREATE_YOUR_ACCOUNT_PAGE}

the applicant clicks the 'EU definition' link
    click link    link=EU Definition

The applicant should navigate to the correct page
    Location Should Be    http://ec.europa.eu/growth/smes/business-friendly-environment/sme-definition/index_en.htm

the applicant selects the Micro size and saves the page
    Select Radio Button    organisationSize    SMALL
    Click Element    css=#content > form > button

the Applicant should redirect to the create account page
    Page Should Contain    The profile that you are creating will be linked to the following organisation
    Page Should Contain    INNOVATE LTD

the applicant selects the Medium size and saves the page
    go to    ${CREATE_YOUR_ACCOUNT_PAGE}
    Select Radio Button    organisationSize    MEDIUM
    Click Element    css=#content > form > button

the applicant selects the Large size and saves the page
    go to    ${CREATE_YOUR_ACCOUNT_PAGE}
    Select Radio Button    organisationSize    LARGE
    Click Element    css=#content > form > button

the applicant clicks the "Enter address manually"
    Click Element    css=#manual-company-input > fieldset:nth-child(1) > button

the address details form should be visible
    Element Should Be Visible    css=#address-details
    Element Should Be Visible    id=street
