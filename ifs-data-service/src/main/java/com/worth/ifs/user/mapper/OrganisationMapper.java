package com.worth.ifs.user.mapper;

import com.worth.ifs.commons.mapper.GlobalMapperConfig;
import com.worth.ifs.finance.mapper.ApplicationFinanceMapper;
import com.worth.ifs.organisation.mapper.AddressMapper;
import com.worth.ifs.organisation.mapper.OrganisationAddressMapper;
import com.worth.ifs.user.domain.Organisation;
import com.worth.ifs.user.repository.OrganisationRepository;
import com.worth.ifs.user.resource.OrganisationResource;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(
    config = GlobalMapperConfig.class,
    uses = {
        AddressMapper.class,
        ProcessRoleMapper.class,
        ApplicationFinanceMapper.class,
        OrganisationAddressMapper.class,
        OrganisationTypeMapper.class
    }
)
public abstract class OrganisationMapper {

    @Autowired
    private OrganisationRepository repository;

    @Mappings({
            @Mapping(target = "users", ignore = true )
    })
    public abstract OrganisationResource mapOrganisationToResource(Organisation object);

    @Mappings({
            @Mapping(target = "users", ignore = true )
    })
    public abstract Organisation resourceToOrganisation(OrganisationResource resource);

    public Long mapOrganisationToId(Organisation object) {
        if (object == null) {
            return null;
        }
        return object.getId();
    }

    public Organisation mapIdToOrganisation(Long id) {
        if (id != null){
            return repository.findOne(id);
        }
        return null;
    }
}