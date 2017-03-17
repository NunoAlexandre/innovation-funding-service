*** Settings ***
Documentation  INFUND-6923 Create new public Competition listings page for Applicants to view open and upcoming competitions
Suite Setup    The guest user opens the browser
Force Tags     Applicant
Resource       ../../../resources/defaultResources.robot


*** Test Cases ***
Guest user navigates to Front Door
    [Documentation]  INFUND-6923
    [Tags]
    [Setup]  the user navigates to the page  ${frontDoor}
    When the user should see the element     jQuery=h1:contains("Innovation competitions")
    And the user should see the element      jQuery=p:contains("Browse upcoming and live competitions.")
    When the user should see the element     css=#keywords
    Then the user should see the element     css=#innovation-area
    When the user clicks the button/link     link=Contact us
    Then the user should see the element     jQuery=h1:contains("Contact us")
    And the user should not see an error in the page

Guest user can see Competitions and their information
    [Documentation]  INFUND-6923
    [Tags]
    [Setup]  the user navigates to the page  ${frontDoor}
    Given the user should see the element    link=Home and industrial efficiency programme
    Then the user should see the element     jQuery=dt:contains("Eligibility") + dd:contains("UK based business of any size. Must involve at least one SME")
    And the user should see the element      jQuery=dt:contains("Opens") + dd:contains("15 April 2016")
    And the user should see the element      jQuery=dt:contains("Closes") + dd:contains("09 September 2067")

#Guest user can filter competitions by Keywords, this is tested in file 05__Public_content.robot

Guest user can filter competitions by Innovation area
    [Documentation]  INFUND-6923
    [Tags]  HappyPath
    [Setup]  the user navigates to the page  ${frontDoor}
    When the user selects the option from the drop-down menu  Space technology  id=innovation-area
    And the user clicks the button/link                       jQuery=button:contains("Update results")
    Then the user should see the element                      jQuery=a:contains("Transforming big data")
    And the user should not see the element                   jQuery=a:contains("Home and industrial efficiency programme")
    When the user selects the option from the drop-down menu  Any  id=innovation-area
    And the user clicks the button/link                       jQuery=button:contains("Update results")
    Then the user should see the element                      jQuery=a:contains("Home and industrial efficiency programme")

Guest user can see the public information of a competition
    [Documentation]  INFUND-6923
    [Tags]
    [Setup]  the user navigates to the page  ${frontDoor}
    Given the user clicks the button/link    link=Home and industrial efficiency programme
    Then the user should see the element     jQuery=h1:contains("Home and industrial efficiency programme")
    And the user should see the element      jQuery=strong:contains("Competition opens") + span:contains("Friday 15 Apr 2016")
    And the user should see the element      jQuery=li:contains("Competition closes")
    And the user should see the element      jQuery=li:contains("Friday 9 September 2067")
    And the user should see the text in the page      Or Sign in to continue an existing application
    And the user should see the element      jQuery=.button:contains("Start new application")

Guest user can see the public Summary of the competition
    [Documentation]  INFUND-6923
    [Tags]
    Given the user clicks the button/link    link=Summary
    Then the user should see the element     jQuery=h3:contains("Description")
    And the user should see the text in the page  Innovate UK is investing up to £15 million in innovation projects to stimulate the new products and services of tomorrow.
    When the user should see the element     jQuery=h3:contains("Funding type")
    Then the user should see the element     jQuery=p:contains("Grant")
    When the user should see the element     jQuery=h3:contains("Project size")
    Then the user should see the element     jQuery=p:contains("£15 million")

Guest user can see the public Eligibility of the competition
    [Documentation]  INFUND-6923
    [Tags]
    Given the user clicks the button/link         link=Eligibility
    Then the user should see the element          jQuery=h3:contains("Lead applicant eligibility")
    And the user should see the text in the page  one SME involved in your proposal carry out your project work, and intend to

Guest user can see the public Scope of the competition
    [Documentation]  INFUND-6923
    [Tags]
    Given the user clicks the button/link         link=Scope
    Then the user should see the element          jQuery=h3:contains("Project scope")
    And the user should see the text in the page  Projects will: harness E&E technologies across the economy develop and scale-up research and development to bring ideas,

Guest user can see the public Dates of the competition
    [Documentation]  INFUND-6923
    [Tags]
    Given the user clicks the button/link  link=Dates
    When the user should see the element   jQuery=dt:contains("15 April 2016") + dd:contains("Competition opens")
    And the user should see the element   jQuery=dt:contains("12 May 2016") + dd:contains("Briefing event in Belfast")
    And the user should see the element   jQuery=dt:contains("9 September 2067") + dd:contains("Competition closes")
    And the user should see the element   jQuery=dt:contains("20 July 2068") + dd:contains("Applicants notified")

Guest user can see the public How to apply of the competition
    [Documentation]  INFUND-6923
    [Tags]
    Given the user clicks the button/link  link=How to apply
    When the user should see the element   jQuery=h3:contains("How to apply")
    Then the user should see the text in the page  Collaborators will be sent a link,

Guest user can see the public Supporting information of the competition
    [Documentation]  INFUND-6923
    [Tags]
    Given the user clicks the button/link  link=Supporting information
    When the user should see the element   jQuery=h3:contains("Background and further information")
    Then the user should see the text in the page  However, we sometimes struggle to fully commercialise the opportunities.

Guest user can apply to a competition
    [Documentation]  INFUND-6923
    [Tags]  HappyPath
    [Setup]  the user navigates to the page  ${frontDoor}
    Given the user clicks the button/link    link=Home and industrial efficiency programme
    When the user clicks the button/link     link=Start new application
    Then the user should see the element     jQuery=.button:contains("Sign in")
    And the user should see the element      jQuery=.button:contains("Create")


*** Keywords ***
