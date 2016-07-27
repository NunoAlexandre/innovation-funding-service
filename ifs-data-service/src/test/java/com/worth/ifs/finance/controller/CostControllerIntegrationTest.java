package com.worth.ifs.finance.controller;

import com.worth.ifs.BaseControllerIntegrationTest;
import com.worth.ifs.application.constant.ApplicationStatusConstants;
import com.worth.ifs.application.domain.Application;
import com.worth.ifs.application.domain.ApplicationStatus;
import com.worth.ifs.commons.error.Error;
import com.worth.ifs.commons.rest.RestResult;
import com.worth.ifs.commons.rest.ValidationMessages;
import com.worth.ifs.finance.domain.ApplicationFinance;
import com.worth.ifs.finance.domain.Cost;
import com.worth.ifs.finance.repository.CostRepository;
import com.worth.ifs.finance.resource.cost.*;
import com.worth.ifs.user.domain.ProcessRole;
import com.worth.ifs.user.domain.User;
import com.worth.ifs.user.mapper.UserMapper;
import com.worth.ifs.user.resource.OrganisationSize;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.validation.BindingResult;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.worth.ifs.commons.error.Error.fieldError;
import static com.worth.ifs.security.SecuritySetter.swapOutForUser;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.*;

@Rollback
public class CostControllerIntegrationTest extends BaseControllerIntegrationTest<CostController> {

    private GrantClaim grantClaim;
    private Materials materials;
    private LabourCost labourCost;
    private LabourCost labourCostDaysPerYear;

    private CapitalUsage capitalUsage;
    private SubContractingCost subContractingCost;
    private TravelCost travelCost;
    private OtherCost otherCost;
    private OtherFunding otherFunding;
    private OtherFunding otherFundingCost;
    private Overhead overhead;

    private String overMaxAllowedTextSize;

    @Mock
    private BindingResult bindingResult;

    @Autowired
    private CostRepository costRepository;
    private Cost grandClaimCost;
    private ApplicationFinance applicationFinance;
    private long leadApplicantId;
    private long leadApplicantProcessRole;
    public static final long APPLICATION_ID = 1L;

    @Autowired
    private UserMapper userMapper;

    @Override
    @Autowired
    protected void setControllerUnderTest(CostController controller) {
        this.controller = controller;
    }

    @Before
    public void prepare(){
        loginSteveSmith();
        grandClaimCost = costRepository.findOne(48L);
        applicationFinance = grandClaimCost.getApplicationFinance();

        grantClaim = (GrantClaim) controller.get(48L).getSuccessObject();
        materials = (Materials) controller.get(12L).getSuccessObject();
        labourCost = (LabourCost) controller.get(4L).getSuccessObject();
        labourCostDaysPerYear = (LabourCost) controller.get(1L).getSuccessObject();

        otherFunding = (OtherFunding) controller.get(54L).getSuccessObject();

        overhead =  (Overhead) controller.get(51L).getSuccessObject();
        ValidationMessages capitalUsageResult = controller.add(applicationFinance.getId(), 31L, null).getSuccessObject();
        capitalUsage = (CapitalUsage) controller.get(capitalUsageResult.getObjectId()).getSuccessObject();
        ValidationMessages subConstractingCostResult = controller.add(applicationFinance.getId(), 32L, new SubContractingCost()).getSuccessObject();
        subContractingCost = (SubContractingCost) controller.get(subConstractingCostResult.getObjectId()).getSuccessObject();
        ValidationMessages travelCostResult = controller.add(applicationFinance.getId(), 33L, new TravelCost()).getSuccessObject();
        travelCost = (TravelCost) controller.get(travelCostResult.getObjectId()).getSuccessObject();
        ValidationMessages otherCostResult = controller.add(applicationFinance.getId(), 34L, null).getSuccessObject();
        otherCost = (OtherCost) controller.get(otherCostResult.getObjectId()).getSuccessObject();
        ValidationMessages otherFundingResult = controller.add(applicationFinance.getId(), 35L, null).getSuccessObject();
        otherFundingCost = (OtherFunding) controller.get(otherFundingResult.getObjectId()).getSuccessObject();

        leadApplicantId = 1L;
        leadApplicantProcessRole = 1L;

        overMaxAllowedTextSize = StringUtils.repeat("<ifs_test>", 30);

        List<ProcessRole> proccessRoles = new ArrayList<>();
        proccessRoles.add(
                new ProcessRole(
                        leadApplicantProcessRole,
                        null,
                        new Application(
                                APPLICATION_ID,
                                "",
                                new ApplicationStatus(
                                        ApplicationStatusConstants.CREATED.getId(),
                                        ApplicationStatusConstants.CREATED.getName()
                                )
                        ),
                        null,
                        null
                )
        );
        User user = new User(leadApplicantId, "steve", "smith", "steve.smith@empire.com", "", proccessRoles, "123abc");
        proccessRoles.get(0).setUser(user);
        swapOutForUser(userMapper.mapToResource(user));
    }

    /* Labour Section Tests */

    @Rollback
    @Test
    public void testValidationLabour(){

        assertEquals(new BigDecimal("129.31034"), labourCost.getRate(labourCostDaysPerYear.getLabourDays()).setScale(5, BigDecimal.ROUND_HALF_EVEN));
        assertEquals(new BigDecimal("90000"), (labourCost.getTotal(labourCostDaysPerYear.getLabourDays())).setScale(0, BigDecimal.ROUND_HALF_EVEN) );

        RestResult<ValidationMessages> validationMessages = controller.update(labourCost.getId(), labourCost);
        assertTrue(validationMessages.isSuccess());
        assertTrue(validationMessages.getSuccessObject().getErrors().isEmpty());
    }

    @Rollback
    @Test
    public void testValidationLabourUpdateIncorrectValues(){
        labourCost.setRole("");
        labourCost.setLabourDays(-50);
        labourCost.setGrossAnnualSalary(new BigDecimal("-500000"));

        RestResult<ValidationMessages> validationMessages = controller.update(labourCost.getId(), labourCost);
        assertTrue(validationMessages.isSuccess());
        assertNotNull(validationMessages.getOptionalSuccessObject().get());

        ValidationMessages messages = validationMessages.getSuccessObject();
        assertEquals(3, messages.getErrors().size());
        assertEquals(labourCost.getId(), messages.getObjectId());
        assertEquals("costItem", messages.getObjectName());

        List<Error> expectedErrors = asList(
                fieldError("labourDays", -50, "This field should be 1 or higher"),
                fieldError("grossAnnualSalary", new BigDecimal("-500000"), "This field should be 1 or higher"),
                fieldError("role", "", "This field cannot be left blank"));

        assertEquals(expectedErrors.size(), messages.getErrors().size());
        expectedErrors.forEach(error -> assertTrue(messages.getErrors().contains(error)));
    }

    @Rollback
    @Test
    public void testValidationLabourUpdateIncorrectMaxValues() {

        labourCost.setRole(overMaxAllowedTextSize);
        labourCost.setLabourDays(400);
        labourCost.setGrossAnnualSalary(new BigDecimal("100000"));

        RestResult<ValidationMessages> validationMessages = controller.update(labourCost.getId(), labourCost);

        assertTrue(validationMessages.isSuccess());
        assertTrue(validationMessages.getSuccessObject().getErrors().isEmpty());
    }

    /* Overhead Section Tests */

    @Rollback
    @Test
    public void testValidationOverhead() {

        BigDecimal overheadTotal =  overhead.getTotal();
        assertEquals(0, overheadTotal.intValue());
        overheadTotal = new BigDecimal("50000");
        assertEquals(11500, overhead.getRate() * overheadTotal.intValue() / 100);

        RestResult<ValidationMessages> validationMessages = controller.update(overhead.getId(), overhead);
        assertTrue(validationMessages.isSuccess());
        assertTrue(validationMessages.getSuccessObject().getErrors().isEmpty());
    }

    @Rollback
    @Test
    public void testValidationOverheadUpdateMinRate(){

        overhead.setRate(-10);
        assertEquals(-100, overhead.getRate() * 1000/100);

        RestResult<ValidationMessages> validationMessages = controller.update(overhead.getId(), overhead);
        ValidationMessages messages = validationMessages.getSuccessObject();

        assertEquals(1, messages.getErrors().size());
        assertEquals(overhead.getId(), messages.getObjectId());
        assertEquals("costItem", messages.getObjectName());

        List<Error> expectedErrors = singletonList(
                fieldError("rate", -10, "This field should be 1 or higher"));

        assertEquals(expectedErrors.size(), messages.getErrors().size());
        expectedErrors.forEach(error -> assertTrue(messages.getErrors().contains(error)));
    }

    @Rollback
    @Test
    public void testValidationOverheadUpdateMaxRate(){
        overhead.setRate(150);
        assertEquals(1500, overhead.getRate() * 1000/100);

        RestResult<ValidationMessages> validationMessages = controller.update(overhead.getId(), overhead);
        ValidationMessages messages = validationMessages.getSuccessObject();

        assertEquals(1, messages.getErrors().size());
        assertEquals(overhead.getId(), messages.getObjectId());
        assertEquals("costItem", messages.getObjectName());

        List<Error> expectedErrors = singletonList(
                fieldError("rate", 150, "This field should be 100 or lower"));

        assertEquals(expectedErrors.size(), messages.getErrors().size());
        expectedErrors.forEach(error -> assertTrue(messages.getErrors().contains(error)));
    }


    /* Material Section Tests */

    @Rollback
    @Test
    public void testValidationMaterial(){
        assertEquals(new BigDecimal("2000"), materials.getTotal());

        RestResult<ValidationMessages> validationMessages = controller.update(materials.getId(), materials);
        assertTrue(validationMessages.isSuccess());
        assertTrue(validationMessages.getSuccessObject().getErrors().isEmpty());
    }

    @Rollback
    @Test
    public void testValidationMaterialUpdateInvalidValues(){
        materials.setCost(new BigDecimal("-5"));

        materials.setItem("");
        materials.setQuantity(-5);

        RestResult<ValidationMessages> validationMessages = controller.update(materials.getId(), materials);
        ValidationMessages messages = validationMessages.getSuccessObject();

        assertEquals(materials.getId(), messages.getObjectId());
        assertEquals("costItem", messages.getObjectName());

        List<Error> expectedErrors = asList(
                fieldError("item", "", "This field cannot be left blank"),
                fieldError("quantity", -5, "This field should be 1 or higher"),
                fieldError("cost", new BigDecimal("-5"), "This field should be 1 or higher"));

        assertEquals(expectedErrors.size(), messages.getErrors().size());
        expectedErrors.forEach(error -> assertTrue(messages.getErrors().contains(error)));
    }

    @Rollback
    @Test
    public void testValidationMaterialUpdateMaxValues() {

        materials.setCost(new BigDecimal("1000"));
        materials.setItem(overMaxAllowedTextSize);
        materials.setQuantity(1000);

        RestResult<ValidationMessages> validationMessages = controller.update(materials.getId(), materials);
        assertTrue(validationMessages.isSuccess());
        assertTrue(validationMessages.getSuccessObject().getErrors().isEmpty());

    }

    /* Capital Usage Section Tests */

    @Rollback
    @Test
    public void testValidationCapitalUsageUpdateSuccess() {

        capitalUsage.setDescription(overMaxAllowedTextSize);
        capitalUsage.setExisting("New");
        capitalUsage.setDeprecation(5);
        capitalUsage.setResidualValue(new BigDecimal("1000"));
        capitalUsage.setNpv(new BigDecimal("10000"));
        capitalUsage.setUtilisation(99);

        assertEquals(new BigDecimal("8910.00"), capitalUsage.getTotal());

        RestResult<ValidationMessages> validationMessages = controller.update(capitalUsage.getId(), capitalUsage);
        assertTrue(validationMessages.isSuccess());
        assertTrue(validationMessages.getSuccessObject().getErrors().isEmpty());
    }

    @Rollback
    @Test
    public void testValidationCapitalUsageUpdateIncorrectValues(){
        capitalUsage.setDescription("");
        capitalUsage.setExisting("");
        capitalUsage.setDeprecation(-5);
        capitalUsage.setResidualValue(new BigDecimal("-100000"));
        capitalUsage.setNpv(new BigDecimal("-10000"));
        capitalUsage.setUtilisation(-5);

        RestResult<ValidationMessages> validationMessages = controller.update(capitalUsage.getId(), capitalUsage);
        ValidationMessages messages = validationMessages.getSuccessObject();

        assertEquals(capitalUsage.getId(), messages.getObjectId());
        assertEquals("costItem", messages.getObjectName());

        List<Error> expectedErrors = asList(
                fieldError("description", "", "This field cannot be left blank"),
                fieldError("existing", "", "This field cannot be left blank"),
                fieldError("deprecation", -5, "This field should be 1 or higher"),
                fieldError("residualValue", new BigDecimal("-100000"), "This field should be 0 or higher"),
                fieldError("npv", new BigDecimal("-10000"), "This field should be 1 or higher"),
                fieldError("utilisation", -5, "This field should be 0 or higher"));

        assertEquals(expectedErrors.size(), messages.getErrors().size());
        expectedErrors.forEach(error -> assertTrue(messages.getErrors().contains(error)));
    }

    @Rollback
    @Test
    public void testValidationCapitalUsageUpdateOverMaxAllowedValues(){
        capitalUsage.setDescription(overMaxAllowedTextSize);
        capitalUsage.setExisting(overMaxAllowedTextSize);
        capitalUsage.setDeprecation(1000);
        capitalUsage.setResidualValue(new BigDecimal("1000000"));
        capitalUsage.setNpv(new BigDecimal("1000"));
        capitalUsage.setUtilisation(200);

        RestResult<ValidationMessages> validationMessages = controller.update(capitalUsage.getId(), capitalUsage);
        ValidationMessages messages = validationMessages.getSuccessObject();

        assertEquals(capitalUsage.getId(), messages.getObjectId());
        assertEquals("costItem", messages.getObjectName());

        List<Error> expectedErrors = asList(
                fieldError("existing", overMaxAllowedTextSize, "This field cannot contain more than 255 characters"),
                fieldError("utilisation", 200, "This field should be 100 or lower"));

        assertEquals(expectedErrors.size(), messages.getErrors().size());
        expectedErrors.forEach(error -> assertTrue(messages.getErrors().contains(error)));
    }

    /* SubContracting Section Tests */

    @Rollback
    @Test
    public void testValidationSubContractingCostUpdateSuccess() {

        subContractingCost.setName("Tom Bloggs");
        subContractingCost.setCountry("UK");
        subContractingCost.setRole("Business Analyst");
        subContractingCost.setCost(new BigDecimal("10000"));

        RestResult<ValidationMessages> validationMessages = controller.update(subContractingCost.getId(), subContractingCost);
        assertTrue(validationMessages.isSuccess());
        assertTrue(validationMessages.getSuccessObject().getErrors().isEmpty());
    }

    @Rollback
    @Test
    public void testValidationSubContractingCostUpdateIncorrectValues(){
        subContractingCost.setName("");
        subContractingCost.setCountry("");
        subContractingCost.setRole("");
        subContractingCost.setCost(new BigDecimal("-5000"));

        RestResult<ValidationMessages> validationMessages = controller.update(travelCost.getId(), travelCost);
        ValidationMessages messages = validationMessages.getSuccessObject();

        assertEquals(travelCost.getId(), messages.getObjectId());
        assertEquals("costItem", messages.getObjectName());

        List<Error> expectedErrors = asList(
                fieldError("item", null, "This field cannot be left blank"),
                fieldError("cost", null, "may not be null"),
                fieldError("quantity", null, "may not be null"));

        assertEquals(expectedErrors.size(), messages.getErrors().size());
        expectedErrors.forEach(error -> assertTrue(messages.getErrors().contains(error)));
    }

    @Rollback
    @Test
    public void testValidationSubContractingCostUpdateOverMaxAllowedValues(){
        subContractingCost.setName(overMaxAllowedTextSize);
        subContractingCost.setCountry(overMaxAllowedTextSize);
        subContractingCost.setRole(overMaxAllowedTextSize);
        subContractingCost.setCost(new BigDecimal("1000000"));

        RestResult<ValidationMessages> validationMessages = controller.update(subContractingCost.getId(), subContractingCost);
        assertTrue(validationMessages.isSuccess());
        assertTrue(validationMessages.getSuccessObject().getErrors().isEmpty());
    }

    /* TravelCost Section Tests */

    @Rollback
    @Test
    public void testValidationTravelCostUpdateSuccess() {
        travelCost.setItem("Travel To Australia for research consultancy");
        travelCost.setCost(new BigDecimal("1000"));
        travelCost.setQuantity(100);

        assertEquals(new BigDecimal("100000"), travelCost.getTotal());

        RestResult<ValidationMessages> validationMessages = controller.update(travelCost.getId(), travelCost);
        assertTrue(validationMessages.isSuccess());
        assertTrue(validationMessages.getSuccessObject().getErrors().isEmpty());
    }

    @Rollback
    @Test
    public void testValidationTravelCostUpdateIncorrectMinValues(){
        travelCost.setItem("");
        travelCost.setCost(new BigDecimal("-1000"));
        travelCost.setQuantity(-500);

        RestResult<ValidationMessages> validationMessages = controller.update(travelCost.getId(), travelCost);
        ValidationMessages messages = validationMessages.getSuccessObject();
        assertEquals(travelCost.getId(), messages.getObjectId());
        assertEquals("costItem", messages.getObjectName());

        List<Error> expectedErrors = asList(
                fieldError("item", "", "This field cannot be left blank"),
                fieldError("cost", new BigDecimal("-1000"), "This field should be 1 or higher"),
                fieldError("quantity", -500, "This field should be 1 or higher"));

        assertEquals(expectedErrors.size(), messages.getErrors().size());
        expectedErrors.forEach(error -> assertTrue(messages.getErrors().contains(error)));
    }

    @Rollback
    @Test
    public void testValidationTravelCostUpdateIncorrectMaxAndZeroValues(){

        travelCost.setItem(overMaxAllowedTextSize);
        travelCost.setCost(new BigDecimal("0"));
        travelCost.setQuantity(0);

        RestResult<ValidationMessages> validationMessages = controller.update(travelCost.getId(), travelCost);
        ValidationMessages messages = validationMessages.getSuccessObject();

        assertEquals(300, overMaxAllowedTextSize.length());

        assertEquals(travelCost.getId(), messages.getObjectId());
        assertEquals("costItem", messages.getObjectName());

        List<Error> expectedErrors = asList(
                fieldError("cost", new BigDecimal("0"), "This field should be 1 or higher"),
                fieldError("quantity", 0, "This field should be 1 or higher"));

        assertEquals(expectedErrors.size(), messages.getErrors().size());
        expectedErrors.forEach(error -> assertTrue(messages.getErrors().contains(error)));
    }

    /* Other Cost Section Tests */

    @Rollback
    @Test
    public void testValidationOtherCostUpdateSuccess() {
        otherCost.setCost(new BigDecimal("1000"));
        otherCost.setDescription("Additional Test Cost");

        RestResult<ValidationMessages> validationMessages = controller.update(otherCost.getId(), otherCost);
        assertTrue(validationMessages.isSuccess());
        assertTrue(validationMessages.getSuccessObject().getErrors().isEmpty());
    }

    @Rollback
    @Test
    public void testValidationOtherCostUpdateIncorrectCostValue() {

        otherCost.setCost(new BigDecimal("0"));
        otherCost.setDescription(overMaxAllowedTextSize);

        RestResult<ValidationMessages> validationMessages = controller.update(otherCost.getId(), otherCost);
        ValidationMessages messages = validationMessages.getSuccessObject();

        assertEquals(otherCost.getId(), messages.getObjectId());
        assertEquals("costItem", messages.getObjectName());

        List<Error> expectedErrors = singletonList(
                fieldError("cost", new BigDecimal("0"), "This field should be 1 or higher"));

        assertEquals(expectedErrors.size(), messages.getErrors().size());
        expectedErrors.forEach(error -> assertTrue(messages.getErrors().contains(error)));
    }

    @Rollback
    @Test
    public void testValidationOtherCostUpdateMinIncorrectValues() {

        otherCost.setCost(new BigDecimal("-1000"));
        otherCost.setDescription("");

        RestResult<ValidationMessages> validationMessages = controller.update(otherCost.getId(), otherCost);
        ValidationMessages messages = validationMessages.getSuccessObject();

        assertEquals(otherCost.getId(), messages.getObjectId());
        assertEquals("costItem", messages.getObjectName());

        List<Error> expectedErrors = asList(
                fieldError("description", "", "This field cannot be left blank"),
                fieldError("cost", new BigDecimal("-1000"), "This field should be 1 or higher"));

        assertEquals(expectedErrors.size(), messages.getErrors().size());
        expectedErrors.forEach(error -> assertTrue(messages.getErrors().contains(error)));
    }

     /* Other funding section Tests */

    @Rollback
    @Test
    public void testValidationOtherFundingUpdate(){

        assertEquals(new BigDecimal("0"), otherFunding.getTotal());
        assertEquals("Yes", otherFunding.getOtherPublicFunding());

        RestResult<ValidationMessages> validationMessages = controller.update(otherFunding.getId(), otherFunding);
        assertTrue(validationMessages.getSuccessObject().getErrors().isEmpty());
    }

    @Rollback
    @Ignore
    @Test
    public void testValidationOtherFundingUpdateIncorrectValues() {

        assertEquals("Yes", otherFunding.getOtherPublicFunding());
        otherFundingCost.setOtherPublicFunding("Yes");
        otherFundingCost.setFundingSource("SomethingWrongHere");
        otherFundingCost.setSecuredDate("15-1000");
        otherFundingCost.setFundingAmount(new BigDecimal("0"));

        RestResult<ValidationMessages> validationMessages = controller.update(otherFundingCost.getId(), otherFundingCost);
        ValidationMessages messages = validationMessages.getSuccessObject();

        assertEquals(otherFundingCost.getId(), messages.getObjectId());
        assertEquals("costItem", messages.getObjectName());

        List<Error> expectedErrors = asList(
                fieldError("securedDate", "15-1000", "Invalid secured date.  Please use MM-YYYY format."),
                fieldError("fundingSource", "SomethingWrongHere", "Funding source cannot be blank."),
                fieldError("fundingAmount", new BigDecimal("0"), "This field should be 1 or higher"));

        assertEquals(expectedErrors.size(), messages.getErrors().size());
        expectedErrors.forEach(error -> assertTrue(messages.getErrors().contains(error)));
    }

    /* Grant Claim Section Tests - Small Organisation Size */

    @Rollback
    @Test
    public void testValidationGrantClaimUpdateSmallOrganisationSize(){

        assertEquals(OrganisationSize.SMALL, applicationFinance.getOrganisationSize());
        grantClaim.setGrantClaimPercentage(55);

        RestResult<ValidationMessages> validationMessages = controller.update(grantClaim.getId(), grantClaim);
        assertTrue(validationMessages.isSuccess());
        assertTrue(validationMessages.getSuccessObject().getErrors().isEmpty());
    }

    @Rollback
    @Test
    public void testValidationGrantClaimUpdateSmallOrganisationSizeHigherValue(){

        assertEquals(OrganisationSize.SMALL, applicationFinance.getOrganisationSize());
        grantClaim.setGrantClaimPercentage(71);

        RestResult<ValidationMessages> validationMessages = controller.update(grantClaim.getId(), grantClaim);
        ValidationMessages messages = validationMessages.getSuccessObject();

        assertEquals(grantClaim.getId(), messages.getObjectId());
        assertEquals("costItem", messages.getObjectName());

        List<Error> expectedErrors = singletonList(
                fieldError("grantClaimPercentage", 71, "This field should be 70% or lower"));

        assertEquals(expectedErrors.size(), messages.getErrors().size());
        expectedErrors.forEach(error -> assertTrue(messages.getErrors().contains(error)));
    }

    @Rollback
    @Test
    public void testValidationGrantClaimUpdateSmallOrganisationSizeNegativeValue() {

        assertEquals(OrganisationSize.SMALL, applicationFinance.getOrganisationSize());

        grantClaim.setGrantClaimPercentage(-1);

        RestResult<ValidationMessages> validationMessages = controller.update(grantClaim.getId(), grantClaim);
        ValidationMessages messages = validationMessages.getSuccessObject();

        assertEquals(grantClaim.getId(), messages.getObjectId());
        assertEquals("costItem", messages.getObjectName());

        List<Error> expectedErrors = singletonList(
                fieldError("grantClaimPercentage", -1, "This field should be 0% or higher"));

        assertEquals(expectedErrors.size(), messages.getErrors().size());
        expectedErrors.forEach(error -> assertTrue(messages.getErrors().contains(error)));
    }

     /* Grant Claim Section Tests - Medium Organisation Size */

    @Rollback
    @Test
    public void testValidationGrantClaimUpdateMediumOrganisationSize(){

        applicationFinance.setOrganisationSize(OrganisationSize.MEDIUM);

        assertEquals(OrganisationSize.MEDIUM, applicationFinance.getOrganisationSize());
        grantClaim.setGrantClaimPercentage(45);

        RestResult<ValidationMessages> validationMessages = controller.update(grantClaim.getId(), grantClaim);
        assertTrue(validationMessages.isSuccess());
        assertTrue(validationMessages.getSuccessObject().getErrors().isEmpty());
    }

    @Rollback
    @Test
    public void testValidationGrantClaimUpdateMediumOrganisationSizeHigherValue(){

        applicationFinance.setOrganisationSize(OrganisationSize.MEDIUM);

        assertEquals(OrganisationSize.MEDIUM, applicationFinance.getOrganisationSize());
        grantClaim.setGrantClaimPercentage(61);

        RestResult<ValidationMessages> validationMessages = controller.update(grantClaim.getId(), grantClaim);
        ValidationMessages messages = validationMessages.getSuccessObject();

        assertEquals(grantClaim.getId(), messages.getObjectId());
        assertEquals("costItem", messages.getObjectName());

        List<Error> expectedErrors = singletonList(
                fieldError("grantClaimPercentage", 61, "This field should be 60% or lower"));

        assertEquals(expectedErrors.size(), messages.getErrors().size());
        expectedErrors.forEach(error -> assertTrue(messages.getErrors().contains(error)));
    }

    @Rollback
    @Test
    public void testValidationGrantClaimUpdateMediumOrganisationSizeNegativeValue() {

        applicationFinance.setOrganisationSize(OrganisationSize.MEDIUM);

        assertEquals(OrganisationSize.MEDIUM, applicationFinance.getOrganisationSize());
        grantClaim.setGrantClaimPercentage(-1);

        RestResult<ValidationMessages> validationMessages = controller.update(grantClaim.getId(), grantClaim);
        ValidationMessages messages = validationMessages.getSuccessObject();

        assertEquals(grantClaim.getId(), messages.getObjectId());
        assertEquals("costItem", messages.getObjectName());

        List<Error> expectedErrors = singletonList(
                fieldError("grantClaimPercentage", -1, "This field should be 0% or higher"));

        assertEquals(expectedErrors.size(), messages.getErrors().size());
        expectedErrors.forEach(error -> assertTrue(messages.getErrors().contains(error)));
    }

    /* Grant Claim Section Tests - Large Organisation Size */

    @Rollback
    @Test
    public void testValidationGrantClaimUpdateLargeOrganisationSize(){

        applicationFinance.setOrganisationSize(OrganisationSize.LARGE);

        assertEquals(OrganisationSize.LARGE, applicationFinance.getOrganisationSize());
        grantClaim.setGrantClaimPercentage(45);

        RestResult<ValidationMessages> validationMessages = controller.update(grantClaim.getId(), grantClaim);
        assertTrue(validationMessages.isSuccess());
        assertTrue(validationMessages.getSuccessObject().getErrors().isEmpty());
    }

    @Rollback
    @Test
    public void testValidationGrantClaimUpdateLargeOrganisationSizeHigherValue(){

        applicationFinance.setOrganisationSize(OrganisationSize.LARGE);

        assertEquals(OrganisationSize.LARGE, applicationFinance.getOrganisationSize());
        grantClaim.setGrantClaimPercentage(51);

        RestResult<ValidationMessages> validationMessages = controller.update(grantClaim.getId(), grantClaim);
        ValidationMessages messages = validationMessages.getSuccessObject();

        assertEquals(grantClaim.getId(), messages.getObjectId());
        assertEquals("costItem", messages.getObjectName());

        List<Error> expectedErrors = singletonList(
                fieldError("grantClaimPercentage", 51, "This field should be 50% or lower"));

        assertEquals(expectedErrors.size(), messages.getErrors().size());
        expectedErrors.forEach(error -> assertTrue(messages.getErrors().contains(error)));
    }

    @Rollback
    @Test
    public void testValidationGrantClaimUpdateLargeOrganisationSizeNegativeValue() {

        applicationFinance.setOrganisationSize(OrganisationSize.LARGE);

        assertEquals(OrganisationSize.LARGE, applicationFinance.getOrganisationSize());
        grantClaim.setGrantClaimPercentage(-1);

        RestResult<ValidationMessages> validationMessages = controller.update(grantClaim.getId(), grantClaim);
        ValidationMessages messages = validationMessages.getSuccessObject();

        assertEquals(1, messages.getErrors().size());
        assertEquals(grantClaim.getId(), messages.getObjectId());
        assertEquals("costItem", messages.getObjectName());

        List<Error> expectedErrors = singletonList(
                fieldError("grantClaimPercentage", -1, "This field should be 0% or higher"));

        assertEquals(expectedErrors.size(), messages.getErrors().size());
        expectedErrors.forEach(error -> assertTrue(messages.getErrors().contains(error)));
    }
}
