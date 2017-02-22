package org.innovateuk.ifs.competition.populator.publiccontent;


import org.innovateuk.ifs.competition.publiccontent.resource.PublicContentResource;
import org.innovateuk.ifs.competition.publiccontent.resource.PublicContentSectionResource;
import org.innovateuk.ifs.competition.publiccontent.resource.PublicContentSectionType;
import org.innovateuk.ifs.competition.viewmodel.publiccontent.AbstractPublicSectionContentViewModel;

/**
 * Abstract class to populate the generic fields needed in the view.
 * @param <M> the view model class.
 */
public abstract class AbstractPublicContentSectionViewModelPopulator<M extends AbstractPublicSectionContentViewModel>{

    public M populate(PublicContentResource publicContentResource) {
        M model = createInitial();
        model.setSectionType(getType());
        model.setPublished(publicContentResource.getPublishDate() != null);

        publicContentResource.getContentSections().stream()
                .filter(section -> getType().equals(section.getType()))
                .findAny()
                .ifPresent(publicContentSectionResource -> {
                    populateSection(model, publicContentResource, publicContentSectionResource);
                });


        return model;
    }

    protected abstract M createInitial();
    protected abstract void populateSection(M model, PublicContentResource publicContentResource, PublicContentSectionResource section);
    public abstract PublicContentSectionType getType();
}
