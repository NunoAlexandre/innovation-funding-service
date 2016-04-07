package com.worth.ifs.user.security;

import com.worth.ifs.BaseUnitTestMocksTest;
import com.worth.ifs.user.resource.UserResource;
import org.junit.Test;
import org.mockito.InjectMocks;

import static com.worth.ifs.user.builder.UserResourceBuilder.newUserResource;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Test for the lookup strategies employed by the permission system to look up entities based on keys
 */
public class UserLookupStrategiesTest extends BaseUnitTestMocksTest {

    @InjectMocks
    private UserLookupStrategies lookup;

    @Test
    public void testFindById() {
        UserResource user = newUserResource().build();
        when(userMapperMock.mapIdToResource(123L)).thenReturn(user);
        assertEquals(user, lookup.findById(123L));
    }
}
