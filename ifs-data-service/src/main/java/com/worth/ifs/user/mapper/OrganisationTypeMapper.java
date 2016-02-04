package com.worth.ifs.user.mapper;

import com.worth.ifs.user.domain.OrganisationType;
import com.worth.ifs.user.repository.OrganisationTypeRepository;
import com.worth.ifs.user.resource.OrganisationTypeResource;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(
    componentModel = "spring",
    uses = {

    }
)
public abstract class OrganisationTypeMapper {

    @Autowired
    private OrganisationTypeRepository repository;

    public abstract OrganisationTypeResource mapOrganisationTypeToResource(OrganisationType object);

    public abstract OrganisationType resourceToOrganisationType(OrganisationTypeResource resource);

    public Long mapOrganisationTypeToId(OrganisationType object) {
        if (object == null) {
            return null;
        }
        return object.getId();
    }

    public OrganisationType mapIdToOrganisationType(Long id) {
        if(id != null){
            return repository.findOne(id);
        }
        return null;
    }
}