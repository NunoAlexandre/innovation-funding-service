package com.worth.ifs.application.resourceAssembler;

import com.worth.ifs.application.controller.ApplicationController;
import com.worth.ifs.application.domain.Application;
import com.worth.ifs.application.resource.ApplicationResource;
import com.worth.ifs.commons.resource.ExtendedLink;
import com.worth.ifs.user.domain.ProcessRole;
import com.worth.ifs.user.resourceAssembler.ProcessRoleResourceAssembler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Service
public class ApplicationResourceAssembler extends ResourceAssemblerSupport<Application, ApplicationResource> {

    private Class<ApplicationController> controllerClass = ApplicationController.class;

    @Autowired
    private ProcessRoleResourceAssembler processRoleResourceAssembler;

    public ApplicationResourceAssembler() {
        super(ApplicationController.class, ApplicationResource.class);
    }

    @Override
    public ApplicationResource toResource(Application application) {
        final ApplicationResource resource = createResourceWithId(application.getId(), application);
        // Add (multiple) links to processRoles
        for(ProcessRole role : application.getProcessRoles()) {
            resource.add( processRoleResourceAssembler.linkToSingleResource(role).withRel("roles") );
        }

        return resource;
    }

    public ExtendedLink linkToSingleResource(Application application) {
        Link link = linkTo(methodOn(controllerClass).getApplicationById(application.getId())).withSelfRel();
        return new ExtendedLink(link).withTitle(application.getName());
    }

    @Override
    protected ApplicationResource instantiateResource(Application application) {
        return new ApplicationResource(application.getId(),
            application.getName(),
            application.getStartDate(),
            application.getDurationInMonths(),
            application.getProcessRoles(),
            application.getApplicationStatus(),
            application.getCompetition()
        );
    }

    public Resources<ApplicationResource> toEmbeddedList(Iterable<Application> entities) {
        final List<ApplicationResource> resources = toResources(entities);
        return new Resources<>(resources, linkTo(controllerClass).withSelfRel());
    }
}
