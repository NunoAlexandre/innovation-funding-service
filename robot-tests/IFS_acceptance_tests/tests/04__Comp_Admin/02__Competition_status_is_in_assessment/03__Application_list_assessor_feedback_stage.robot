*** Settings ***
Documentation     INFUND-2606 - As a competition administrator I want a view of all applications at the 'Assessor Feedback' stage so that I can publish their uploaded assessor feedback
Suite Setup       Log in as user    email=john.doe@innovateuk.test    password=Passw0rd
Suite Teardown    the user closes the browser
Force Tags        CompAdmin
Resource          ../../../resources/defaultResources.robot

*** Variables ***

*** Test Cases ***
Status and applications are correct
    [Documentation]    INFUND-2606
    [Tags]    HappyPath
    When the user navigates to the page    ${server}/management/competition/${FUNDERS_PANEL_COMPETITION}
    Then the user should see the text in the page    Assessor Feedback
    And the user should see the text in the page    Matter - Planning for Web
    And the user should see the text in the page    Sensing & Control network using the lighting infrastructure

Number of submitted and funded applications is correct
    When the user should see the text in the page    2 Applications submitted
    And the user should see the text in the page    1 Funded

The 'Fund project?' column title is now 'Funded' and isn't editable
    [Documentation]    INFUND-2606
    [Tags]
    When the user should see the text in the page    Funded
    And the user should not see the text in the page    Fund project?
    Then the user should not see the element    id=fund24
    And the user should not see the element    id=fund25

Publish assessor feedback button is now visible
    [Documentation]    INFUND-2606
    [Tags]    HappyPath
    When the user should not see the element    jQuery=.button:contains("Notify applicants")
    Then the user should see the element    jQuery=.button:contains("Publish assessor feedback")
    And publish assessor feedback button should be disabled

*** Keywords ***
publish assessor feedback button should be disabled
    Element Should Be Disabled    jQuery=.button:contains("Publish assessor feedback")
