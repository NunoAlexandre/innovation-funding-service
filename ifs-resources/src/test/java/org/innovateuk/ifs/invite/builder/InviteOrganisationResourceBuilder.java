package org.innovateuk.ifs.invite.builder;

import org.innovateuk.ifs.BaseBuilder;
import org.innovateuk.ifs.invite.resource.ApplicationInviteResource;
import org.innovateuk.ifs.invite.resource.InviteOrganisationResource;

import java.util.List;
import java.util.function.BiConsumer;

import static java.util.Collections.emptyList;
import static org.innovateuk.ifs.base.amend.BaseBuilderAmendFunctions.setField;
import static org.innovateuk.ifs.base.amend.BaseBuilderAmendFunctions.uniqueIds;

public class InviteOrganisationResourceBuilder extends BaseBuilder<InviteOrganisationResource, InviteOrganisationResourceBuilder> {

    private InviteOrganisationResourceBuilder(List<BiConsumer<Integer, InviteOrganisationResource>> multiActions) {
        super(multiActions);
    }

    public static InviteOrganisationResourceBuilder newInviteOrganisationResource() {
        return new InviteOrganisationResourceBuilder(emptyList()).with(uniqueIds());
    }

    @Override
    protected InviteOrganisationResourceBuilder createNewBuilderWithActions(List<BiConsumer<Integer, InviteOrganisationResource>> actions) {
        return new InviteOrganisationResourceBuilder(actions);
    }

    public InviteOrganisationResourceBuilder withId(Long... ids) {
        return withArray((id, inviteResource) -> setField("id", id, inviteResource), ids);
    }

    public InviteOrganisationResourceBuilder withOrganisationName(String... organisationNames) {
        return withArray((organisationName, inviteResource) -> setField("organisationName", organisationName, inviteResource), organisationNames);
    }

    public InviteOrganisationResourceBuilder withOrganisationNameConfirmed(String... organisationNameConfirmeds) {
        return withArray((organisationNameConfirmed, inviteResource) -> setField("organisationNameConfirmed", organisationNameConfirmed, inviteResource), organisationNameConfirmeds);
    }

    public InviteOrganisationResourceBuilder withInviteResources(List<ApplicationInviteResource>... inviteResourceLists) {
        return withArray((inviteResources, inviteResource) -> setField("inviteResources", inviteResources, inviteResource), inviteResourceLists);
    }

    public InviteOrganisationResourceBuilder withOrganisation(Long... organisationIds) {
        return withArray((organisationId, inviteResource) -> setField("organisation", organisationId, inviteResource), organisationIds);
    }

    @Override
    protected InviteOrganisationResource createInitial() {
        return new InviteOrganisationResource();
    }
}
