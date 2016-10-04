package com.worth.ifs.user.builder;

import com.worth.ifs.address.domain.Address;
import com.worth.ifs.address.resource.AddressResource;
import com.worth.ifs.user.domain.Contract;
import com.worth.ifs.user.domain.Profile;
import com.worth.ifs.user.resource.BusinessType;
import com.worth.ifs.user.resource.ContractResource;
import com.worth.ifs.user.resource.ProfileResource;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.List;

import static com.worth.ifs.address.builder.AddressBuilder.newAddress;
import static com.worth.ifs.address.builder.AddressResourceBuilder.newAddressResource;
import static com.worth.ifs.user.builder.ContractBuilder.newContract;
import static com.worth.ifs.user.builder.ContractResourceBuilder.newContractResource;
import static com.worth.ifs.user.builder.ProfileBuilder.newProfile;
import static com.worth.ifs.user.builder.ProfileResourceBuilder.newProfileResource;
import static com.worth.ifs.user.resource.BusinessType.ACADEMIC;
import static com.worth.ifs.user.resource.BusinessType.BUSINESS;
import static org.junit.Assert.assertEquals;

public class ProfileResourceBuilderTest {
    @Test
    public void buildOne() {
        Long expectedId = 1L;
        AddressResource expectedAddress = newAddressResource().build();
        String expectedSkillsAreas = "skills areas";
        BusinessType expectedBusinessType = BUSINESS;
        ContractResource expectedContract = newContractResource().build();
        LocalDateTime expectedContractSignedDate = LocalDateTime.now();


        ProfileResource profile = newProfileResource()
                .withId(expectedId)
                .withAddress(expectedAddress)
                .withSkillsAreas(expectedSkillsAreas)
                .withBusinessType(expectedBusinessType)
                .withContract(expectedContract)
                .withContractSignedDate(expectedContractSignedDate)
                .build();

        assertEquals(expectedId, profile.getId());
        assertEquals(expectedAddress, profile.getAddress());
        assertEquals(expectedSkillsAreas, profile.getSkillsAreas());
        assertEquals(expectedBusinessType, profile.getBusinessType());
        assertEquals(expectedContract, profile.getContract());
        assertEquals(expectedContractSignedDate, profile.getContractSignedDate());
    }

    @Test
    public void buildMany() {
        Long[] expectedIds = { 1L, 2L };
        AddressResource[] expectedAddresses = newAddressResource().buildArray(2, AddressResource.class);
        String[] expectedSkillsAreas = { "skills areas 1", "skills areas 2" };
        BusinessType[] expectedBusinessTypes = { BUSINESS, ACADEMIC };
        ContractResource[] expectedContracts = newContractResource().buildArray(2, ContractResource.class);
        LocalDateTime[] expectedContractSignedDates = { LocalDateTime.now(), LocalDateTime.now().plusDays(1L) };

        List<ProfileResource> profiles = newProfileResource()
                .withId(expectedIds)
                .withAddress(expectedAddresses)
                .withSkillsAreas(expectedSkillsAreas)
                .withBusinessType(expectedBusinessTypes)
                .withContract(expectedContracts)
                .withContractSignedDate(expectedContractSignedDates)
                .build(2);

        ProfileResource first = profiles.get(0);

        assertEquals(expectedIds[0], first.getId());
        assertEquals(expectedAddresses[0], first.getAddress());
        assertEquals(expectedSkillsAreas[0], first.getSkillsAreas());
        assertEquals(expectedBusinessTypes[0], first.getBusinessType());
        assertEquals(expectedContracts[0], first.getContract());
        assertEquals(expectedContractSignedDates[0], first.getContractSignedDate());

        ProfileResource second = profiles.get(1);

        assertEquals(expectedIds[1], second.getId());
        assertEquals(expectedAddresses[1], second.getAddress());
        assertEquals(expectedSkillsAreas[1], second.getSkillsAreas());
        assertEquals(expectedBusinessTypes[1], second.getBusinessType());
        assertEquals(expectedContracts[1], second.getContract());
        assertEquals(expectedContractSignedDates[1], second.getContractSignedDate());
    }
}
