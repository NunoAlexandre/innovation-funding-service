package org.innovateuk.ifs.address.service;

import org.innovateuk.ifs.BaseRestServiceUnitTest;
import org.innovateuk.ifs.address.resource.AddressResource;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


import static org.innovateuk.ifs.commons.service.ParameterizedTypeReferences.addressResourceListType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AddressRestServiceMocksTest extends BaseRestServiceUnitTest<AddressRestServiceImpl> {
    private static final String addressRestURL = "/address";

    @Override
    protected AddressRestServiceImpl registerRestServiceUnderTest() {
        AddressRestServiceImpl addressRestService = new AddressRestServiceImpl();
        return addressRestService;
    }

    @Test
    public void testDoLookup() throws Exception{
        String postcode = "BS348XU";
        String expectedUrl = addressRestURL + "/doLookup?lookup=" + postcode;
        List<AddressResource> returnedAddresses = Arrays.asList(1,2,3,4).stream().map(i -> new AddressResource()).collect(Collectors.toList());// newAddressResource().build(4);
        setupGetWithRestResultExpectations(expectedUrl, addressResourceListType(), returnedAddresses);

        // now run the method under test
        List<AddressResource> addresses = service.doLookup(postcode).getSuccessObject();
        assertNotNull(addresses);
        assertEquals(returnedAddresses, addresses);
    }

    @Test
    public void testGetById(){
        AddressResource addressResource = new AddressResource();;
        String url = addressRestURL + "/" + addressResource.getId();
        setupGetWithRestResultExpectations(url, AddressResource.class, addressResource);

        AddressResource returnedAddressResource = service.getById(addressResource.getId()).getSuccessObject();
        assertNotNull(returnedAddressResource);
        Assert.assertEquals(returnedAddressResource, addressResource);
    }
}
