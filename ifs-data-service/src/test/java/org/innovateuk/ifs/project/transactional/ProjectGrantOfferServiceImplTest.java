package org.innovateuk.ifs.project.transactional;

import org.apache.commons.lang3.tuple.Pair;
import org.innovateuk.ifs.BaseServiceUnitTest;
import org.innovateuk.ifs.address.builder.AddressBuilder;
import org.innovateuk.ifs.address.domain.Address;
import org.innovateuk.ifs.application.domain.Application;
import org.innovateuk.ifs.commons.error.CommonErrors;
import org.innovateuk.ifs.commons.error.CommonFailureKeys;
import org.innovateuk.ifs.commons.rest.LocalDateResource;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.competition.domain.Competition;
import org.innovateuk.ifs.file.domain.FileEntry;
import org.innovateuk.ifs.file.resource.FileEntryResource;
import org.innovateuk.ifs.finance.resource.ApplicationFinanceResource;
import org.innovateuk.ifs.project.builder.PartnerOrganisationBuilder;
import org.innovateuk.ifs.project.domain.PartnerOrganisation;
import org.innovateuk.ifs.project.domain.Project;
import org.innovateuk.ifs.project.domain.ProjectUser;
import org.innovateuk.ifs.project.finance.resource.FinanceCheckSummaryResource;
import org.innovateuk.ifs.project.gol.YearlyGOLProfileTable;
import org.innovateuk.ifs.project.resource.ApprovalType;
import org.innovateuk.ifs.project.resource.ProjectOrganisationCompositeId;
import org.innovateuk.ifs.project.resource.ProjectResource;
import org.innovateuk.ifs.project.resource.SpendProfileTableResource;
import org.innovateuk.ifs.user.domain.*;
import org.innovateuk.ifs.user.resource.OrganisationResource;
import org.innovateuk.ifs.user.resource.OrganisationTypeEnum;
import org.innovateuk.ifs.user.resource.UserResource;
import org.innovateuk.ifs.user.resource.UserRoleType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Supplier;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.innovateuk.ifs.address.builder.AddressBuilder.newAddress;
import static org.innovateuk.ifs.application.builder.ApplicationBuilder.newApplication;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.competition.builder.CompetitionBuilder.newCompetition;
import static org.innovateuk.ifs.file.builder.FileEntryBuilder.newFileEntry;
import static org.innovateuk.ifs.file.builder.FileEntryResourceBuilder.newFileEntryResource;
import static org.innovateuk.ifs.finance.builder.ApplicationFinanceResourceBuilder.newApplicationFinanceResource;
import static org.innovateuk.ifs.finance.resource.cost.FinanceRowType.LABOUR;
import static org.innovateuk.ifs.invite.domain.ProjectParticipantRole.PROJECT_MANAGER;
import static org.innovateuk.ifs.invite.domain.ProjectParticipantRole.PROJECT_PARTNER;
import static org.innovateuk.ifs.project.builder.CostCategoryResourceBuilder.newCostCategoryResource;
import static org.innovateuk.ifs.project.builder.PartnerOrganisationBuilder.newPartnerOrganisation;
import static org.innovateuk.ifs.project.builder.ProjectBuilder.newProject;
import static org.innovateuk.ifs.project.builder.ProjectUserBuilder.newProjectUser;
import static org.innovateuk.ifs.project.finance.builder.FinanceCheckSummaryResourceBuilder.newFinanceCheckSummaryResource;
import static org.innovateuk.ifs.project.transactional.ProjectGrantOfferServiceImpl.GRANT_OFFER_LETTER_DATE_FORMAT;
import static org.innovateuk.ifs.user.builder.OrganisationBuilder.newOrganisation;
import static org.innovateuk.ifs.user.builder.OrganisationResourceBuilder.newOrganisationResource;
import static org.innovateuk.ifs.user.builder.ProcessRoleBuilder.newProcessRole;
import static org.innovateuk.ifs.user.builder.RoleBuilder.newRole;
import static org.innovateuk.ifs.user.builder.UserBuilder.newUser;
import static org.innovateuk.ifs.user.builder.UserResourceBuilder.newUserResource;
import static org.innovateuk.ifs.user.resource.OrganisationTypeEnum.ACADEMIC;
import static org.innovateuk.ifs.user.resource.OrganisationTypeEnum.BUSINESS;
import static org.innovateuk.ifs.util.MapFunctions.asMap;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;


public class ProjectGrantOfferServiceImplTest extends BaseServiceUnitTest<ProjectGrantOfferService> {

    private Long projectId = 123L;
    private Long applicationId = 456L;
    private Long userId = 7L;

    private Application application;
    private List<Organisation> organisations;
    private Organisation nonAcademicUnfunded;
    private Role leadApplicantRole;
    private User user;
    private ProcessRole leadApplicantProcessRole;
    private ProjectUser leadPartnerProjectUser;
    private Project project;
    private List<OrganisationResource> organisationResources;
    private SpendProfileTableResource table;
    private SpendProfileTableResource tableZero;

    private List<BigDecimal> monthlyTotalsZero = asList(
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO) ;

    private ApplicationFinanceResource applicationFinanceResourceZero = newApplicationFinanceResource().withGrantClaimPercentage(0)
            .withApplication(456L).withOrganisation(3L)
            .build();

    private List<BigDecimal> yearlyCosts = asList(
            BigDecimal.valueOf(500),
            BigDecimal.valueOf(100),
            BigDecimal.valueOf(200));

    private Address address;

    @Captor
    ArgumentCaptor<Map<String, Object> > templateArgsCaptor;

    @Captor
    ArgumentCaptor<String> templateCaptor;

    @Captor
    ArgumentCaptor<FileEntryResource> fileEntryResCaptor;

    @Captor
    ArgumentCaptor<Supplier<InputStream>> supplierCaptor;

    @Before
    public void setUp() {

        ApplicationFinanceResource applicationFinanceResource = newApplicationFinanceResource().withGrantClaimPercentage(30)
                .withApplication(456L).withOrganisation(3L)
                .build();
        table = new SpendProfileTableResource();
        table.setMarkedAsComplete(true);
        table.setMonthlyCostsPerCategoryMap(asMap(
                1L, asList(new BigDecimal("30.44"), new BigDecimal("30"), new BigDecimal("40")),
                2L, asList(new BigDecimal("70"), new BigDecimal("50.10"), new BigDecimal("60")),
                3L, asList(new BigDecimal("50"), new BigDecimal("5"), new BigDecimal("10.31"))));

        table.setCostCategoryGroupMap(asMap(
                LABOUR,  asList(asMap(
                        1L, asList(new BigDecimal("30.44"), new BigDecimal("30"), new BigDecimal("40")),
                        2L, asList(new BigDecimal("70"), new BigDecimal("50.10"), new BigDecimal("60")),
                        3L, asList(new BigDecimal("50"), new BigDecimal("5"), new BigDecimal("10.31")))
                )));

        table.setEligibleCostPerCategoryMap(asMap(
                1L, asList(new BigDecimal("30.44"), new BigDecimal("30"), new BigDecimal("40")),
                2L, asList(new BigDecimal("70"), new BigDecimal("50.10"), new BigDecimal("60")),
                3L, asList(new BigDecimal("50"), new BigDecimal("5"), new BigDecimal("10.31"))
        ));
        table.setCostCategoryResourceMap(asMap(
                1L, newCostCategoryResource().withName("Labour").build(),
                2L, newCostCategoryResource().withName("Materials").build(),
                3L, newCostCategoryResource().withName("Other").build()
        ));
        table.setMonths(asList(
                new LocalDateResource(1, 3, 2019),new LocalDateResource(1, 4, 2019)));

        tableZero = new SpendProfileTableResource();
        tableZero.setMarkedAsComplete(true);
        tableZero.setMonthlyCostsPerCategoryMap(asMap(
                1L, asList(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO),
                2L, asList(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO),
                3L, asList(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO)));

        tableZero.setCostCategoryGroupMap(asMap(
                LABOUR,  asList(asMap(
                        1L, asList(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO),
                        2L, asList(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO),
                        3L, asList(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO)
                ))));

        tableZero.setEligibleCostPerCategoryMap(asMap(
                1L, asList(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO),
                2L, asList(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO),
                3L, asList(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO)
        ));
        tableZero.setCostCategoryResourceMap(asMap(
                1L, newCostCategoryResource().withName("Labour").build(),
                2L, newCostCategoryResource().withName("Materials").build(),
                3L, newCostCategoryResource().withName("Other").build()
        ));
        tableZero.setMonths(asList(
                new LocalDateResource(1, 3, 2019),new LocalDateResource(1, 4, 2019)));

        organisations = newOrganisation().withOrganisationType(ACADEMIC).build(3);
        nonAcademicUnfunded = newOrganisation().withOrganisationType(BUSINESS).build();
        organisationResources = newOrganisationResource().build(4);

        Competition competition = newCompetition().build();

        address = newAddress().withAddressLine1("test1")
                .withAddressLine2("test2")
                .withPostcode("PST")
                .withTown("town").build();

        leadApplicantRole = newRole(UserRoleType.LEADAPPLICANT).build();

        user = newUser().
                withId(userId).
                build();

        leadApplicantProcessRole = newProcessRole().
                withOrganisation(organisations.get(0)).
                withRole(leadApplicantRole).
                withUser(user).
                build();

        leadPartnerProjectUser = newProjectUser().
                withOrganisation(this.organisations.get(0)).
                withRole(PROJECT_PARTNER).
                withUser(user).
                build();

        application = newApplication().
                withId(applicationId).
                withCompetition(competition).
                withProcessRoles(leadApplicantProcessRole).
                withName("My Application").
                withDurationInMonths(5L).
                withStartDate(LocalDate.of(2017, 3, 2)).
                build();

        PartnerOrganisation partnerOrganisation = newPartnerOrganisation().withOrganisation(organisations.get(0)).build();
        PartnerOrganisation partnerOrganisation2 = newPartnerOrganisation().withOrganisation(organisations.get(1)).build();
        PartnerOrganisation partnerOrganisation3 = newPartnerOrganisation().withOrganisation(organisations.get(2)).build();

        List<PartnerOrganisation> partnerOrganisations = new ArrayList<>();
        partnerOrganisations.add(partnerOrganisation);
        partnerOrganisations.add(partnerOrganisation2);
        partnerOrganisations.add(partnerOrganisation3);

        project = newProject().
                withId(projectId).
                withPartnerOrganisations(partnerOrganisations).
                withAddress(address).
                withApplication(application).
                withProjectUsers(singletonList(leadPartnerProjectUser)).
                build();

        FinanceCheckSummaryResource financeCheckSummaryResource = newFinanceCheckSummaryResource()
                .withTotalPercentageGrant(BigDecimal.valueOf(25))
                .build();

        List<BigDecimal> monthlyTotals = asList(
                BigDecimal.valueOf(50),
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(20));

        List<BigDecimal> yearlyCosts = asList(
                BigDecimal.valueOf(500),
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(200));

        when(projectRepositoryMock.findOne(projectId)).thenReturn(project);
        when(organisationRepositoryMock.findOne(organisations.get(0).getId())).thenReturn(organisations.get(0));
        when(organisationRepositoryMock.findOne(organisations.get(1).getId())).thenReturn(organisations.get(1));
        when(organisationRepositoryMock.findOne(organisations.get(2).getId())).thenReturn(organisations.get(2));
        when(organisationMapperMock.mapToResource(organisations.get(0))).thenReturn(organisationResources.get(0));
        when(organisationMapperMock.mapToResource(organisations.get(1))).thenReturn(organisationResources.get(1));
        when(organisationMapperMock.mapToResource(organisations.get(2))).thenReturn(organisationResources.get(2));
        when(organisationFinanceDelegateMock.isUsingJesFinances(ACADEMIC.name())).thenReturn(true);
        when(financeRowServiceMock.financeDetails(project.getApplication().getId(), organisations.get(0).getId())).thenReturn(ServiceResult.serviceSuccess(applicationFinanceResource));
        when(financeRowServiceMock.financeDetails(project.getApplication().getId(), organisations.get(1).getId())).thenReturn(ServiceResult.serviceSuccess(applicationFinanceResource));
        when(financeRowServiceMock.financeDetails(project.getApplication().getId(), organisations.get(2).getId())).thenReturn(ServiceResult.serviceSuccess(applicationFinanceResource));
        when(projectFinanceServiceMock.getSpendProfileTable(any(ProjectOrganisationCompositeId.class)))
                .thenReturn(ServiceResult.serviceSuccess(table));
        when(spendProfileTableCalculatorMock.calculateMonthlyTotals(table.getMonthlyCostsPerCategoryMap(),
                table.getMonths().size())).thenReturn(monthlyTotals);
        when(spendProfileTableCalculatorMock.calculateEligibleCostPerYear(any(ProjectResource.class), any(List.class), any(List.class))).thenReturn(yearlyCosts);
        when(financeCheckServiceMock.getFinanceCheckSummary(project.getId())).thenReturn(ServiceResult.serviceSuccess(financeCheckSummaryResource));
    }

    @Test
    public void testCreateSignedGrantOfferLetterFileEntry() {
        assertCreateFile(
                project::getSignedGrantOfferLetter,
                (fileToCreate, inputStreamSupplier) ->
                        service.createSignedGrantOfferLetterFileEntry(123L, fileToCreate, inputStreamSupplier));
    }

    @Test
    public void testCreateGrantOfferLetterFileEntry() {
        assertCreateFile(
                project::getGrantOfferLetter,
                (fileToCreate, inputStreamSupplier) ->
                        service.createGrantOfferLetterFileEntry(123L, fileToCreate, inputStreamSupplier));
    }


    @Test
    public void testCreateAdditionalContractFileEntry() {
        assertCreateFile(
                project::getAdditionalContractFile,
                (fileToCreate, inputStreamSupplier) ->
                        service.createAdditionalContractFileEntry(123L, fileToCreate, inputStreamSupplier));
    }


    @Test
    public void testGetAdditionalContractFileEntryDetails() {
        assertGetFileDetails(
                project::setAdditionalContractFile,
                () -> service.getAdditionalContractFileEntryDetails(123L));
    }

    @Test
    public void testGetGrantOfferLetterFileEntryDetails() {
        assertGetFileDetails(
                project::setGrantOfferLetter,
                () -> service.getGrantOfferLetterFileEntryDetails(123L));
    }

    @Test
    public void testGetSignedGrantOfferLetterFileEntryDetails() {
        assertGetFileDetails(
                project::setSignedGrantOfferLetter,
                () -> service.getSignedGrantOfferLetterFileEntryDetails(123L));
    }

    @Test
    public void testGetAdditionalContractFileContents() {
        assertGetFileContents(
                project::setAdditionalContractFile,
                () -> service.getAdditionalContractFileAndContents(123L));
    }

    @Test
    public void testGetGrantOfferLetterFileContents() {
        assertGetFileContents(
                project::setGrantOfferLetter,
                () -> service.getGrantOfferLetterFileAndContents(123L));
    }

    @Test
    public void testGetSignedGrantOfferLetterFileContents() {
        assertGetFileContents(
                project::setSignedGrantOfferLetter,
                () -> service.getSignedGrantOfferLetterFileAndContents(123L));
    }

    @Test
    public void testUpdateSignedGrantOfferLetterFileEntry() {
        when(golWorkflowHandlerMock.isSent(any())).thenReturn(Boolean.TRUE);
        assertUpdateFile(
                project::getSignedGrantOfferLetter,
                (fileToUpdate, inputStreamSupplier) ->
                        service.updateSignedGrantOfferLetterFile(123L, fileToUpdate, inputStreamSupplier));
    }

    @Test
    public void testUpdateSignedGrantOfferLetterFileEntryGolNotSent() {

        FileEntryResource fileToUpdate = newFileEntryResource().build();
        Supplier<InputStream> inputStreamSupplier = () -> null;

        when(golWorkflowHandlerMock.isSent(any())).thenReturn(Boolean.FALSE);

        ServiceResult<Void> result = service.updateSignedGrantOfferLetterFile(123L, fileToUpdate, inputStreamSupplier);
        assertTrue(result.isFailure());
        assertEquals(result.getErrors().get(0).getErrorKey(), CommonFailureKeys.GRANT_OFFER_LETTER_MUST_BE_SENT_BEFORE_UPLOADING_SIGNED_COPY.toString());

    }

    @Test
    public void testSubmitGrantOfferLetterFailureNoSignedGolFile() {

        ServiceResult<Void> result = service.submitGrantOfferLetter(projectId);

        assertTrue(result.getFailure().is(CommonFailureKeys.SIGNED_GRANT_OFFER_LETTER_MUST_BE_UPLOADED_BEFORE_SUBMIT));
        Assert.assertThat(project.getOfferSubmittedDate(), nullValue());
    }

    @Test
    public void testSubmitGrantOfferLetterFailureCannotReachSignedState() {
        project.setSignedGrantOfferLetter(mock(FileEntry.class));

        when(golWorkflowHandlerMock.sign(any())).thenReturn(Boolean.FALSE);

        ServiceResult<Void> result = service.submitGrantOfferLetter(projectId);

        assertTrue(result.getFailure().is(CommonFailureKeys.GRANT_OFFER_LETTER_CANNOT_SET_SIGNED_STATE));
        Assert.assertThat(project.getOfferSubmittedDate(), nullValue());
    }

    @Test
    public void testSubmitGrantOfferLetterSuccess() {
        project.setSignedGrantOfferLetter(mock(FileEntry.class));

        when(golWorkflowHandlerMock.sign(any())).thenReturn(Boolean.TRUE);

        ServiceResult<Void> result = service.submitGrantOfferLetter(projectId);

        assertTrue(result.isSuccess());
        Assert.assertThat(project.getOfferSubmittedDate(), notNullValue());
    }

    @Test
    public void testGenerateGrantOfferLetter() {
        assertGenerateFile(
                fileEntryResource ->
                        service.generateGrantOfferLetter(123L, fileEntryResource));
    }

    @Test
    public void testRemoveGrantOfferLetterFileEntry() {

        UserResource internalUserResource = newUserResource().build();
        User internalUser = newUser().withId(internalUserResource.getId()).build();
        setLoggedInUser(internalUserResource);

        FileEntry existingGOLFile = newFileEntry().build();
        project.setGrantOfferLetter(existingGOLFile);

        when(userRepositoryMock.findOne(internalUserResource.getId())).thenReturn(internalUser);
        when(golWorkflowHandlerMock.removeGrantOfferLetter(project, internalUser)).thenReturn(true);
        when(fileServiceMock.deleteFile(existingGOLFile.getId())).thenReturn(serviceSuccess(existingGOLFile));

        ServiceResult<Void> result = service.removeGrantOfferLetterFileEntry(123L);

        assertTrue(result.isSuccess());
        assertNull(project.getGrantOfferLetter());

        verify(golWorkflowHandlerMock).removeGrantOfferLetter(project, internalUser);
        verify(fileServiceMock).deleteFile(existingGOLFile.getId());
    }

    @Test
    public void testRemoveGrantOfferLetterFileEntryButWorkflowRejected() {

        UserResource internalUserResource = newUserResource().build();
        User internalUser = newUser().withId(internalUserResource.getId()).build();
        setLoggedInUser(internalUserResource);

        FileEntry existingGOLFile = newFileEntry().build();
        project.setGrantOfferLetter(existingGOLFile);

        when(userRepositoryMock.findOne(internalUserResource.getId())).thenReturn(internalUser);
        when(golWorkflowHandlerMock.removeGrantOfferLetter(project, internalUser)).thenReturn(false);

        ServiceResult<Void> result = service.removeGrantOfferLetterFileEntry(123L);

        assertTrue(result.isFailure());
        assertTrue(result.getFailure().is(CommonFailureKeys.GRANT_OFFER_LETTER_CANNOT_BE_REMOVED));
        assertEquals(existingGOLFile, project.getGrantOfferLetter());

        verify(golWorkflowHandlerMock).removeGrantOfferLetter(project, internalUser);
        verify(fileServiceMock, never()).deleteFile(existingGOLFile.getId());
    }
    private final Organisation organisation(OrganisationTypeEnum type, String name) {
        return newOrganisation()
                .withOrganisationType(type)
                .withName(name)
                .build();
    }
    @Test
    public void testGenerateGrantOfferLetterIfReadySuccess() { //here
        FileEntryResource fileEntryResource = newFileEntryResource().
                withFilesizeBytes(1024).
                withMediaType("application/pdf").
                withName("grant_offer_letter").
                build();

        FileEntry createdFile = newFileEntry().build();
        Pair<File, FileEntry> fileEntryPair = Pair.of(new File("blah"), createdFile);

        StringBuilder stringBuilder = new StringBuilder();
        String htmlFile = stringBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
                .append("<html dir=\"ltr\" lang=\"en\">\n")
                .append("<head>\n")
                .append("<meta charset=\"UTF-8\"></meta>\n")
                .append("</head>\n")
                .append("<body>\n")
                .append("<p>\n")
                .append("${LeadContact}<br/>\n")
                .append("</p>\n")
                .append("</body>\n")
                .append("</html>\n").toString();

        Competition comp = newCompetition()
                .withName("Test Comp")
                .build();
        Organisation o1 = organisation(BUSINESS, "Org1");
        Organisation o2 = organisation(BUSINESS, "Org2");
        Organisation o3 = organisation(BUSINESS, "Org3");

        Role leadAppRole = newRole(UserRoleType.LEADAPPLICANT)
                .build();
        User u = newUser()
                .withFirstName("ab")
                .withLastName("cd")
                .build();
        ProcessRole leadAppProcessRole = newProcessRole()
                .withOrganisation(o1)
                .withUser(u)
                .withRole(leadAppRole)
                .build();
        Application app = newApplication()
                .withCompetition(comp)
                .withProcessRoles(leadAppProcessRole)
                .withId(3L)
                .build();
        ProjectUser pm = newProjectUser()
                .withRole(PROJECT_MANAGER)
                .withOrganisation(o1)
                .build();

        PartnerOrganisation po = newPartnerOrganisation()
                .withOrganisation(o1)
                .withLeadOrganisation(true)
                .build();

        PartnerOrganisation po2 = newPartnerOrganisation()
                .withOrganisation(o2)
                .withLeadOrganisation(false)
                .build();

        PartnerOrganisation po3 = newPartnerOrganisation()
                .withOrganisation(o3)
                .withLeadOrganisation(false)
                .build();


        Address address = newAddress()
                .withAddressLine1("InnovateUK")
                .withAddressLine2("Northstar House")
                .withTown("Swindon")
                .withPostcode("SN1 1AA")
                .build();
        Project project = newProject()
                .withOtherDocumentsApproved(ApprovalType.APPROVED)
                .withName("project 1")
                .withApplication(app)
                .withPartnerOrganisations(asList(po, po2, po3)) //add here
                .withProjectUsers(asList(pm))
                .withDuration(10L)
                .withAddress(address)
                .withTargetStartDate(LocalDate.now())
                .build();

        ApplicationFinanceResource applicationFinanceResource = newApplicationFinanceResource()
                .withGrantClaimPercentage(30)
                .withApplication(456L)
                .withOrganisation(3L)
                .build();
        FinanceCheckSummaryResource financeCheckSummaryResource = newFinanceCheckSummaryResource()
                .withTotalPercentageGrant(BigDecimal.valueOf(25))
                .build();

        Map<String, Object> templateArgs = new HashMap<String, Object>();
        templateArgs.put("SortedOrganisations", asList(o1.getName(), o2.getName(), o3.getName()));
        templateArgs.put("ProjectLength", 10L);
        templateArgs.put("ProjectTitle", "project 1");
        templateArgs.put("LeadContact", "ab cd");
        templateArgs.put("ApplicationNumber", 3L);
        templateArgs.put("LeadOrgName", "Org1");
        templateArgs.put("CompetitionName", "Test Comp");
        templateArgs.put("Address1", "InnovateUK");
        templateArgs.put("Address2", "Northstar House");
        templateArgs.put("Address3", "");
        templateArgs.put("TownCity", "Swindon");
        templateArgs.put("PostCode", "SN1 1AA");
        templateArgs.put("ProjectStartDate",project.getTargetStartDate().format(DateTimeFormatter.ofPattern(GRANT_OFFER_LETTER_DATE_FORMAT)));
        templateArgs.put("Date", LocalDateTime.now().toString());

        Map<String, Integer> organisationAndGrantPercentageMap = new HashMap<>();
        organisationAndGrantPercentageMap.put(o1.getName(), new Integer("30"));
        organisationAndGrantPercentageMap.put(o2.getName(), new Integer("30"));
        organisationAndGrantPercentageMap.put(o3.getName(), new Integer("30"));

        Map<String, List<String>> organisationYearsMap  = new HashMap<>();
        organisationYearsMap.put(o1.getName(), new LinkedList<>());
        organisationYearsMap.put(o2.getName(), new LinkedList<>());
        organisationYearsMap.put(o3.getName(), new LinkedList<>());

        Map<String, List<BigDecimal>> organisationEligibleCostTotal  = new HashMap<>();
        organisationEligibleCostTotal.put(o1.getName(), asList(new BigDecimal("500"), new BigDecimal("100"), new BigDecimal("200")));
        organisationEligibleCostTotal.put(o2.getName(), asList(new BigDecimal("500"), new BigDecimal("100"), new BigDecimal("200")));
        organisationEligibleCostTotal.put(o3.getName(), asList(new BigDecimal("500"), new BigDecimal("100"), new BigDecimal("200")));

        Map<String, List<BigDecimal>> organisationGrantAllocationTotal  = new HashMap<>();
        organisationGrantAllocationTotal.put(o1.getName(), new LinkedList<>());
        organisationGrantAllocationTotal.put(o2.getName(), new LinkedList<>());
        organisationGrantAllocationTotal.put(o3.getName(), new LinkedList<>());

        Map<String, BigDecimal> yearEligibleCostTotal  = new HashMap<>();
        yearEligibleCostTotal.put(o1.getName(), new BigDecimal("1"));
        yearEligibleCostTotal.put(o2.getName(), new BigDecimal("1"));
        yearEligibleCostTotal.put(o3.getName(), new BigDecimal("1"));

        Map<String, BigDecimal> yearGrantAllocationTotal  = new HashMap<>();
        YearlyGOLProfileTable expectedYearlyGOLProfileTable = new YearlyGOLProfileTable(organisationAndGrantPercentageMap, organisationYearsMap, organisationEligibleCostTotal, organisationGrantAllocationTotal, yearEligibleCostTotal, yearGrantAllocationTotal);

        when(organisationRepositoryMock.findOne(o1.getId())).thenReturn(o1);
        when(organisationRepositoryMock.findOne(o2.getId())).thenReturn(o2);
        when(organisationRepositoryMock.findOne(o3.getId())).thenReturn(o3);
        when(projectFinanceServiceMock.getSpendProfileStatusByProjectId(123L)).thenReturn(serviceSuccess(ApprovalType.APPROVED));
        when(projectFinanceServiceMock.getSpendProfileTable(any(ProjectOrganisationCompositeId.class))).thenReturn(serviceSuccess(table));
        when(projectRepositoryMock.findOne(123L)).thenReturn(project);
        when(rendererMock.renderTemplate(eq("common/grantoffer/grant_offer_letter.html"), any(Map.class))).thenReturn(ServiceResult.serviceSuccess(htmlFile));
        when(fileServiceMock.createFile(any(FileEntryResource.class), any(Supplier.class))).thenReturn(ServiceResult.serviceSuccess(fileEntryPair));
        when(fileEntryMapperMock.mapToResource(createdFile)).thenReturn(fileEntryResource);
        when(financeCheckServiceMock.getFinanceCheckSummary(project.getId())).thenReturn(ServiceResult.serviceSuccess(financeCheckSummaryResource));

        when(organisationFinanceDelegateMock.isUsingJesFinances(BUSINESS.name())).thenReturn(false);
        when(financeRowServiceMock.financeDetails(project.getApplication().getId(), o1.getId())).thenReturn(ServiceResult.serviceSuccess(applicationFinanceResource));
        when(financeRowServiceMock.financeDetails(project.getApplication().getId(), o2.getId())).thenReturn(ServiceResult.serviceSuccess(applicationFinanceResource));
        when(financeRowServiceMock.financeDetails(project.getApplication().getId(), o3.getId())).thenReturn(ServiceResult.serviceSuccess(applicationFinanceResource));

        Map<String, BigDecimal> eligibleCostTotal = new HashMap<>();
        eligibleCostTotal.put(o1.getName(), new BigDecimal("1"));
        eligibleCostTotal.put(o2.getName(), new BigDecimal("1"));
        eligibleCostTotal.put(o3.getName(), new BigDecimal("1"));
        when(spendProfileTableCalculatorMock.createYearlyEligibleCostTotal(any(ProjectResource.class), any(Map.class), any(List.class))).thenReturn(eligibleCostTotal);

        ServiceResult<Void> result = service.generateGrantOfferLetterIfReady(123L);

        verify(rendererMock).renderTemplate(templateCaptor.capture(), templateArgsCaptor.capture());
        verify(fileServiceMock).createFile(fileEntryResCaptor.capture(), supplierCaptor.capture());

        assertEquals(templateArgs.get("SortedOrganisations"), templateArgsCaptor.getAllValues().get(0).get("SortedOrganisations"));
        assertEquals(templateArgs.get("ProjectLength"), templateArgsCaptor.getAllValues().get(0).get("ProjectLength"));
        assertEquals(templateArgs.get("ProjectTitle"), templateArgsCaptor.getAllValues().get(0).get("ProjectTitle"));
        assertEquals(templateArgs.get("ProjectStartDate"), templateArgsCaptor.getAllValues().get(0).get("ProjectStartDate"));
        assertEquals(templateArgs.get("LeadContact"), templateArgsCaptor.getAllValues().get(0).get("LeadContact"));
        assertEquals(templateArgs.get("ApplicationNumber"), templateArgsCaptor.getAllValues().get(0).get("ApplicationNumber"));
        assertEquals(templateArgs.get("LeadOrgName"), templateArgsCaptor.getAllValues().get(0).get("LeadOrgName"));
        assertEquals(templateArgs.get("CompetitionName"), templateArgsCaptor.getAllValues().get(0).get("CompetitionName"));
        assertEquals(templateArgs.get("Address1"), templateArgsCaptor.getAllValues().get(0).get("Address1"));
        assertEquals(templateArgs.get("Address2"), templateArgsCaptor.getAllValues().get(0).get("Address2"));
        assertEquals(templateArgs.get("Address3"), templateArgsCaptor.getAllValues().get(0).get("Address3"));
        assertEquals(templateArgs.get("TownCity"), templateArgsCaptor.getAllValues().get(0).get("TownCity"));
        assertEquals(templateArgs.get("PostCode"), templateArgsCaptor.getAllValues().get(0).get("PostCode"));

        assertEquals(fileEntryResource.getMediaType(), fileEntryResCaptor.getAllValues().get(0).getMediaType());
        assertEquals(fileEntryResource.getName() + ".pdf", fileEntryResCaptor.getAllValues().get(0).getName());

        String startOfGeneratedFileString = null;
        try {
            int n = supplierCaptor.getAllValues().get(0).get().available();
            byte [] startOfGeneratedFile = new byte[n];
            supplierCaptor.getAllValues().get(0).get().read(startOfGeneratedFile, 0, n <9 ? n : 9);
            startOfGeneratedFileString = new String(startOfGeneratedFile, StandardCharsets.UTF_8);
        }
        catch(IOException e) {

        }
        String pdfHeader = "%PDF-1.4\n";
        assertEquals(pdfHeader, startOfGeneratedFileString.substring(0, pdfHeader.length()));
        assertTrue(result.isSuccess());
        assertTrue(compareYearlyGolProfileTable(expectedYearlyGOLProfileTable, (YearlyGOLProfileTable) templateArgsCaptor.getAllValues().get(0).get("TableData")));
    }

    @Test
    public void testGenerateGrantOfferLetterFailureSpendProfilesNotApproved() {

        when(projectFinanceServiceMock.getSpendProfileStatusByProjectId(123L)).thenReturn(serviceSuccess(ApprovalType.REJECTED));

        ServiceResult<Void> result = service.generateGrantOfferLetterIfReady(123L);
        assertTrue(result.isSuccess());
    }


    @Test
    public void testGenerateGrantOfferLetterOtherDocsNotApproved() {

        Competition comp = newCompetition().withName("Test Comp").build();
        Organisation o1 = newOrganisation().withName("Org1").build();
        Role leadAppRole = newRole(UserRoleType.LEADAPPLICANT).build();
        User u = newUser().withFirstName("ab").withLastName("cd").build();
        ProcessRole leadAppProcessRole = newProcessRole().withOrganisation(o1).withUser(u).withRole(leadAppRole).build();
        Application app = newApplication().withCompetition(comp).withProcessRoles(leadAppProcessRole).withId(3L).build();
        ProjectUser pm = newProjectUser().withRole(PROJECT_MANAGER).withOrganisation(o1).build();
        PartnerOrganisation po = PartnerOrganisationBuilder.newPartnerOrganisation().withOrganisation(o1).withLeadOrganisation(true).build();
        Project project = newProject().withOtherDocumentsApproved(ApprovalType.REJECTED).withApplication(app).withPartnerOrganisations(asList(po)).withProjectUsers(asList(pm)).withDuration(10L).build();

        when(projectFinanceServiceMock.getSpendProfileStatusByProjectId(123L)).thenReturn(serviceSuccess(ApprovalType.APPROVED));
        when(projectRepositoryMock.findOne(123L)).thenReturn(project);

        ServiceResult<Void> result = service.generateGrantOfferLetterIfReady(123L);
        assertTrue(result.isSuccess());
    }

    @Test
    public void testGenerateGrantOfferLetterNoProject() {

        when(projectFinanceServiceMock.getSpendProfileStatusByProjectId(123L)).thenReturn(serviceSuccess(ApprovalType.APPROVED));
        when(projectRepositoryMock.findOne(123L)).thenReturn(null);

        ServiceResult<Void> result = service.generateGrantOfferLetterIfReady(123L);
        assertTrue(result.isFailure());
        assertTrue(result.getFailure().is(CommonErrors.notFoundError(Project.class, 123L)));
    }

    @Test
    public void testUnfundedNonAcademicPartnerNotIncludedInGrantOfferLetter() {
        PartnerOrganisation partnerOrganisation = newPartnerOrganisation().withOrganisation(organisations.get(0)).build();
        PartnerOrganisation partnerOrganisation2 = newPartnerOrganisation().withOrganisation(organisations.get(1)).build();
        PartnerOrganisation partnerOrganisation3 = newPartnerOrganisation().withOrganisation(organisations.get(2)).build();
        PartnerOrganisation partnerOrganisation4 = newPartnerOrganisation().withOrganisation(nonAcademicUnfunded).build();

        List<PartnerOrganisation> partnerOrganisations = new ArrayList<>();
        partnerOrganisations.add(partnerOrganisation);
        partnerOrganisations.add(partnerOrganisation2);
        partnerOrganisations.add(partnerOrganisation3);
        partnerOrganisations.add(partnerOrganisation4);

        project = newProject().
                withId(projectId).
                withPartnerOrganisations(partnerOrganisations).
                withAddress(address).
                withApplication(application).
                withProjectUsers(singletonList(leadPartnerProjectUser)).
                build();

        when(projectRepositoryMock.findOne(123L)).thenReturn(project);
        when(organisationFinanceDelegateMock.isUsingJesFinances(BUSINESS.name())).thenReturn(false);
        when(financeRowServiceMock.financeDetails(project.getApplication().getId(), nonAcademicUnfunded.getId())).thenReturn(ServiceResult.serviceSuccess(applicationFinanceResourceZero));
        when(projectFinanceServiceMock.getSpendProfileTable(new ProjectOrganisationCompositeId(projectId, nonAcademicUnfunded.getId())))
                .thenReturn(ServiceResult.serviceSuccess(tableZero));
        when(spendProfileTableCalculatorMock.calculateMonthlyTotals(tableZero.getMonthlyCostsPerCategoryMap(),
                tableZero.getMonths().size())).thenReturn(monthlyTotalsZero);

        Map<String, BigDecimal> eligibleCostTotal = new HashMap<>();
        eligibleCostTotal.put(organisations.get(0).getName(), new BigDecimal("1"));
        eligibleCostTotal.put(organisations.get(1).getName(), new BigDecimal("2"));
        eligibleCostTotal.put(organisations.get(2).getName(), new BigDecimal("3"));
        eligibleCostTotal.put(nonAcademicUnfunded.getName(), BigDecimal.ZERO);
        when(spendProfileTableCalculatorMock.createYearlyEligibleCostTotal(any(ProjectResource.class), any(Map.class), any(List.class))).thenReturn(eligibleCostTotal);

        List<BigDecimal> yearlyCostsZero = asList(
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO);

        when(spendProfileTableCalculatorMock.calculateEligibleCostPerYear(any(ProjectResource.class), any(List.class), any(List.class))).thenReturn(yearlyCosts,yearlyCosts,yearlyCosts,yearlyCostsZero);

        FileEntryResource fileEntryResource = newFileEntryResource().
                withFilesizeBytes(1024).
                withMediaType("application/pdf").
                withName("grant_offer_letter").
                build();
        FileEntry createdFile = newFileEntry().build();
        Pair<File, FileEntry> fileEntryPair = Pair.of(new File("blah"), createdFile);
        StringBuilder stringBuilder = new StringBuilder();
        String htmlFile = stringBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
                .append("<html dir=\"ltr\" lang=\"en\">\n")
                .append("<head>\n")
                .append("<meta charset=\"UTF-8\"></meta>\n")
                .append("</head>\n")
                .append("<body>\n")
                .append("<p>\n")
                .append("${LeadContact}<br/>\n")
                .append("</p>\n")
                .append("</body>\n")
                .append("</html>\n").toString();

        Map<String, Integer> organisationAndGrantPercentageMap = new HashMap<>();
        organisationAndGrantPercentageMap.put(organisations.get(0).getName(), new Integer("100"));
        organisationAndGrantPercentageMap.put(organisations.get(1).getName(), new Integer("100"));
        organisationAndGrantPercentageMap.put(organisations.get(2).getName(), new Integer("100"));
        Map<String, List<String>> organisationYearsMap  = new HashMap<>();
        organisationYearsMap.put(organisations.get(0).getName(), new LinkedList<>());
        organisationYearsMap.put(organisations.get(1).getName(), new LinkedList<>());
        organisationYearsMap.put(organisations.get(2).getName(), new LinkedList<>());
        Map<String, List<BigDecimal>> organisationEligibleCostTotal  = new HashMap<>();
        organisationEligibleCostTotal.put(organisations.get(0).getName(), asList(new BigDecimal("500"), new BigDecimal("100"), new BigDecimal("200")));
        organisationEligibleCostTotal.put(organisations.get(1).getName(), asList(new BigDecimal("500"), new BigDecimal("100"), new BigDecimal("200")));
        organisationEligibleCostTotal.put(organisations.get(2).getName(), asList(new BigDecimal("500"), new BigDecimal("100"), new BigDecimal("200")));
        Map<String, List<BigDecimal>> organisationGrantAllocationTotal  = new HashMap<>();
        organisationGrantAllocationTotal.put(organisations.get(0).getName(), new LinkedList<>());
        organisationGrantAllocationTotal.put(organisations.get(1).getName(), new LinkedList<>());
        organisationGrantAllocationTotal.put(organisations.get(2).getName(), new LinkedList<>());
        Map<String, BigDecimal> yearEligibleCostTotal  = new HashMap<>();
        yearEligibleCostTotal.put(organisations.get(0).getName(), new BigDecimal("1"));
        yearEligibleCostTotal.put(organisations.get(1).getName(), new BigDecimal("2"));
        yearEligibleCostTotal.put(organisations.get(2).getName(), new BigDecimal("3"));
        Map<String, BigDecimal> yearGrantAllocationTotal  = new HashMap<>();

        YearlyGOLProfileTable expectedYearlyGOLProfileTable = new YearlyGOLProfileTable(organisationAndGrantPercentageMap, organisationYearsMap, organisationEligibleCostTotal, organisationGrantAllocationTotal, yearEligibleCostTotal, yearGrantAllocationTotal);

        when(rendererMock.renderTemplate(any(String.class), any(Map.class))).thenReturn(ServiceResult.serviceSuccess(htmlFile));

        when(fileEntryMapperMock.mapToResource(createdFile)).thenReturn(fileEntryResource);
        when(fileServiceMock.createFile(any(FileEntryResource.class), any(Supplier.class))).thenReturn(ServiceResult.serviceSuccess(fileEntryPair));

        service.generateGrantOfferLetter(123L, fileEntryResource);

        verify(rendererMock).renderTemplate(templateCaptor.capture(), templateArgsCaptor.capture());

        verify(projectRepositoryMock).findOne(123L);

        assertTrue(compareYearlyGolProfileTable(expectedYearlyGOLProfileTable, (YearlyGOLProfileTable) templateArgsCaptor.getAllValues().get(0).get("TableData")));
    }

    @Override
    protected ProjectGrantOfferService supplyServiceUnderTest() {
        return new ProjectGrantOfferServiceImpl();
    }

    private boolean compareYearlyGolProfileTable(YearlyGOLProfileTable a, YearlyGOLProfileTable b) {
        boolean result = true;
        if(a.getOrganisationAndGrantPercentageMap().entrySet().size() != b.getOrganisationAndGrantPercentageMap().entrySet().size()) {
            return false;
        }
        for(int i = 0; i < a.getOrganisationAndGrantPercentageMap().keySet().size(); i++) {
            Object k = a.getOrganisationAndGrantPercentageMap().keySet().toArray()[i];
            if(!b.getOrganisationAndGrantPercentageMap().containsKey(k) || !a.getOrganisationAndGrantPercentageMap().get(k).equals(b.getOrganisationAndGrantPercentageMap().get(k)))
            {
                result &= false;
            }
        }

        if(a.getOrganisationYearsMap().entrySet().size() != b.getOrganisationYearsMap().entrySet().size()) {
            return false;
        }
        for(int i = 0; i < a.getOrganisationYearsMap().keySet().size(); i++) {
            Object k = a.getOrganisationYearsMap().keySet().toArray()[i];
            if(!b.getOrganisationYearsMap().containsKey(k) || a.getOrganisationYearsMap().get(k).size() != b.getOrganisationYearsMap().get(k).size()) {
                result &= false;
            } else {
                for(int j = 0; j < a.getOrganisationYearsMap().get(k).size(); j++) {
                    if(!a.getOrganisationYearsMap().get(k).get(j).equals(b.getOrganisationYearsMap().get(k).get(j))) {
                        result &= false;
                    }
                }
            }
        }
        if(a.getOrganisationEligibleCostTotal().entrySet().size() != b.getOrganisationEligibleCostTotal().entrySet().size()) {
            return false;
        }
        for(int i = 0; i < a.getOrganisationEligibleCostTotal().keySet().size(); i++) {
            Object k = a.getOrganisationEligibleCostTotal().keySet().toArray()[i];
            if(!b.getOrganisationEligibleCostTotal().containsKey(k) || a.getOrganisationEligibleCostTotal().get(k).size() != b.getOrganisationEligibleCostTotal().get(k).size()) {
                result &= false;
            } else {
                for(int j = 0; j < a.getOrganisationEligibleCostTotal().get(k).size(); j++) {
                    if(!a.getOrganisationEligibleCostTotal().get(k).get(j).equals(b.getOrganisationEligibleCostTotal().get(k).get(j))) {
                        result &= false;
                    }
                }
            }
        }
        if(a.getOrganisationGrantAllocationTotal().entrySet().size() != b.getOrganisationGrantAllocationTotal().entrySet().size()) {
            return false;
        }
        for(int i = 0; i < a.getOrganisationGrantAllocationTotal().keySet().size(); i++) {
            Object k = a.getOrganisationGrantAllocationTotal().keySet().toArray()[i];
            if(!b.getOrganisationGrantAllocationTotal().containsKey(k) || a.getOrganisationGrantAllocationTotal().get(k).size() != b.getOrganisationGrantAllocationTotal().get(k).size()) {
                result &= false;
            } else {
                for(int j = 0; j < a.getOrganisationGrantAllocationTotal().get(k).size(); j++) {
                    if(!a.getOrganisationGrantAllocationTotal().get(k).get(j).equals(b.getOrganisationGrantAllocationTotal().get(k).get(j))) {
                        result &= false;
                    }
                }
            }
        }

        if(a.getYearEligibleCostTotal().entrySet().size() != b.getYearEligibleCostTotal().entrySet().size()) {
            return false;
        }
        for(int i = 0; i < a.getYearEligibleCostTotal().keySet().size(); i++) {
            Object k = a.getYearEligibleCostTotal().keySet().toArray()[i];
            if(!b.getYearEligibleCostTotal().containsKey(k) || !a.getYearEligibleCostTotal().get(k).equals(b.getYearEligibleCostTotal().get(k)))
            {
                result &= false;
            }
        }

        if(a.getYearGrantAllocationTotal().entrySet().size() != b.getYearGrantAllocationTotal().entrySet().size()) {
            return false;
        }
        for(int i = 0; i < a.getYearGrantAllocationTotal().keySet().size(); i++) {
            Object k = a.getYearGrantAllocationTotal().keySet().toArray()[i];
            if(!b.getYearGrantAllocationTotal().containsKey(k) || !a.getYearGrantAllocationTotal().get(k).equals(b.getYearGrantAllocationTotal().get(k)))
            {
                result &= false;
            }
        }
        result &= a.getEligibleCostGrandTotal() == b.getEligibleCostGrandTotal();
        return result;
    }
}