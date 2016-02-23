package com.worth.ifs.invite.mapper;

import com.worth.ifs.application.mapper.ApplicationMapper;
import com.worth.ifs.commons.mapper.BaseMapper;
import com.worth.ifs.invite.domain.Invite;
import com.worth.ifs.invite.resource.InviteResource;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(
    componentModel = "spring",
    uses = {
        ApplicationMapper.class,
        InviteOrganisationMapper.class
    }
)
public abstract class InviteMapper extends BaseMapper<Invite, InviteResource, Long> {



    @Mappings({
            @Mapping(source = "application.competition.name", target = "competitionName"),
            @Mapping(source = "application.competition.id", target = "competitionId"),
            @Mapping(source = "application.leadOrganisation.name", target = "leadOrganisation"),
            @Mapping(source = "application.leadApplicant.name", target = "leadApplicant"),
            @Mapping(source = "application.leadApplicant.email", target = "leadApplicantEmail"),
            @Mapping(source = "application.name", target = "applicationName"),
            @Mapping(source = "application.id", target = "application"),
            @Mapping(source = "inviteOrganisation.id", target = "inviteOrganisation"),
            @Mapping(source = "inviteOrganisation.organisationName", target = "inviteOrganisationName"),
    })
    public abstract InviteResource mapToResource(Invite domain);

    public Long mapInviteToId(Invite object) {
        if (object == null) {
            return null;
        }
        return object.getId();
    }
}