*** Settings ***
Documentation     INFUND-1188 As an assessor I want to be able to review my assessments from one place so that I can work in my favoured style when reviewing
Suite Setup       Log in as user    email=paul.plum@gmail.com    password=Passw0rd
Suite Teardown    TestTeardown User closes the browser
Force Tags        Assessor
Resource          ../../../resources/GLOBAL_LIBRARIES.robot
Resource          ../../../resources/variables/GLOBAL_VARIABLES.robot
Resource          ../../../resources/variables/User_credentials.robot
Resource          ../../../resources/keywords/Login_actions.robot
Resource          ../../../resources/keywords/User_actions.robot

*** Variables ***
@{database}       pymysql    ${database_name}    ${database_user}    ${database_password}    ${database_host}    ${database_port}

*** Test Cases ***
Competition link should navigate to the applications
    [Documentation]    INFUND-3716
    [Tags]    HappyPath
    [Setup]
    When The user clicks the button/link    link=Juggling Craziness
    Then The user should see the text in the page    Applications for Assessment

Calculation of the applications for assessment should be correct
    Then the calculation should be correct    Applications for Assessment    //div/form/ul/li

When the deadline has passed the assessment should not be visible
    [Documentation]    INFUND-1188
    [Tags]    MySQL    Pending
    #TODO Pending INFUND-5380
    When The assessment deadline for the Juggling Craziness changes to the past
    Then The user should not see the element    link=Juggling is fun
    [Teardown]    execute sql string    UPDATE `ifs`.`milestone` SET `DATE`='2016-12-31 00:00:00' WHERE `id`='21';

*** Keywords ***
the calculation should be correct
    [Arguments]    ${TEXT}    ${Section_Xpath}
    [Documentation]    This keyword uses 2 arguments. The first one is about the page's text (competition or application) and the second is about the Xpath selector.
    ${NO_OF_COMP_OR_APPL}=    Get Matching Xpath Count    ${Section_Xpath}
    Page Should Contain    ${TEXT} (${NO_OF_COMP_OR_APPL})

The assessment deadline for the Juggling Craziness changes to the past
    ${today}=    get time
    ${yesterday} =    Subtract Time From Date    ${today}    1 day
    When execute sql string    UPDATE `ifs`.`milestone` SET `DATE`='${yesterday}' WHERE `id`='21';
    And reload page
