package com.worth.ifs.user.controller;

import com.worth.ifs.BaseControllerIntegrationTest;
import com.worth.ifs.address.domain.Address;
import com.worth.ifs.address.domain.AddressType;
import com.worth.ifs.address.repository.AddressRepository;
import com.worth.ifs.address.resource.AddressResource;
import com.worth.ifs.user.domain.Organisation;
import com.worth.ifs.user.domain.OrganisationSize;
import com.worth.ifs.user.domain.OrganisationType;
import com.worth.ifs.user.repository.OrganisationTypeRepository;
import com.worth.ifs.user.resource.OrganisationResource;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;

import java.util.Collections;
import java.util.Set;

import static com.worth.ifs.user.builder.OrganisationResourceBuilder.newOrganisationResource;
import static com.worth.ifs.util.CollectionFunctions.simpleMap;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class OrganisationControllerIntegrationTest extends BaseControllerIntegrationTest<OrganisationController> {

    @Autowired
    private OrganisationTypeRepository organisationTypeRepository;

    @Autowired
    private AddressRepository addressRepository;

    private static final String companyHouseId = "0123456789";
    private static final String companyName = "CompanyName1";

    @Before
    public void setLoggedInUserOnThread() {
        loginSteveSmith();
    }

    @Override
    @Autowired
    protected void setControllerUnderTest(OrganisationController controller) {
        this.controller = controller;
    }

    @Rollback
    @Test
    public void findByIdShouldReturnOrganisation() throws Exception {

        OrganisationResource org = controller.findById(2L).getSuccessObject();
        assertEquals("Worth Internet Systems", org.getName());

        loginPeteTom();
        org = controller.findById(6L).getSuccessObject();
        assertEquals("EGGS", org.getName());
    }

    @Rollback
    @Test
    public void testOrganisationType() throws Exception {

        OrganisationResource org = controller.findById(2L).getSuccessObject();
        OrganisationType organisationType = organisationTypeRepository.findOne(org.getOrganisationType());

        assertEquals("Business", organisationType.getName());

        loginPeteTom();
        org = controller.findById(6L).getSuccessObject();
        organisationType = organisationTypeRepository.findOne(org.getOrganisationType());
        assertEquals("University (HEI)", organisationType.getName());
        assertEquals("Research", organisationType.getParentOrganisationType().getName());
    }

    @Rollback
    @Test
    public void testFindByApplicationId() throws Exception {
        Set<OrganisationResource> organisations = controller.findByApplicationId(1L).getSuccessObject();
        assertEquals("There should be 5 organisation in this application", 5, organisations.size());

        OrganisationResource organisation = controller.findById(2L).getSuccessObject();
        assertTrue("One of the organisations should be Worth Internet Systems", simpleMap(organisations, OrganisationResource::getId).contains(organisation.getId()));
    }

    private OrganisationResource createOrganisation(){
        OrganisationResource organisation = newOrganisationResource().
                withName(companyName).withCompanyHouseNumber(companyHouseId).build();
        return controller.create(organisation).getSuccessObject();
    }

    @Rollback
    @Test
    public void testCreate() throws Exception {
        OrganisationResource organisationResource = createOrganisation();


        assertEquals(companyHouseId, organisationResource.getCompanyHouseNumber());
        assertEquals(companyName, organisationResource.getName());

        OrganisationResource org = controller.findById(organisationResource.getId()).getSuccessObject();
        assertEquals(companyHouseId, org.getCompanyHouseNumber());
        assertEquals(companyName, org.getName());
    }

    @Rollback
    @Test
    public void testCreateSecondConstructor() throws Exception {
        OrganisationResource organisation = newOrganisationResource().
                withName(companyName).withCompanyHouseNumber(companyHouseId).withOrganisationSize(OrganisationSize.LARGE).
                withUsers(singletonList(getSteveSmith())).build();

        OrganisationResource organisationResource = controller.create(organisation).getSuccessObject();

        flushAndClearSession();

        assertEquals(companyHouseId, organisationResource.getCompanyHouseNumber());
        assertEquals(companyName, organisationResource.getName());
        assertEquals(OrganisationSize.LARGE, organisationResource.getOrganisationSize());

        OrganisationResource org = controller.findById(organisationResource.getId()).getSuccessObject();
        assertEquals(companyHouseId, org.getCompanyHouseNumber());
        assertEquals(companyName, org.getName());
        assertEquals(OrganisationSize.LARGE, org.getOrganisationSize());
    }

    @Rollback
    @Test
    public void testAddAddress() throws Exception {
        OrganisationResource organisationResource = createOrganisation();
        AddressResource addressResource = new AddressResource("Line1", "Line2",  "Line3", "town", "county", "postcode");
        controller.addAddress(organisationResource.getId(), AddressType.OPERATING, addressResource);

        flushAndClearSession();

        OrganisationResource cleanOrganisation = controller.findById(organisationResource.getId()).getSuccessObject();
        assertEquals(1, cleanOrganisation.getAddresses().size());
        Long addressId = cleanOrganisation.getAddresses().get(0);
        Address address = addressRepository.findOne(addressId);

        assertEquals("Line1", address.getAddressLine1());
        assertEquals("Line2", address.getAddressLine2());
        assertEquals("Line3", address.getAddressLine3());
        assertEquals("town", address.getTown());
        assertEquals("postcode", address.getPostcode());
        assertEquals("county", address.getCounty());
    }
}
