package org.innovateuk.ifs.thread.security;

import org.innovateuk.ifs.BasePermissionRulesTest;
import org.innovateuk.ifs.finance.domain.ProjectFinance;
import org.innovateuk.ifs.project.finance.service.ProjectFinanceNotesService;
import org.innovateuk.ifs.threads.security.ProjectFinanceNotePermissionRules;
import org.innovateuk.ifs.threads.security.ProjectFinanceQueryPermissionRules;
import org.innovateuk.ifs.user.domain.Organisation;
import org.innovateuk.ifs.user.resource.UserResource;
import org.innovateuk.threads.resource.FinanceChecksSectionType;
import org.innovateuk.threads.resource.NoteResource;
import org.innovateuk.threads.resource.PostResource;
import org.innovateuk.threads.resource.QueryResource;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.innovateuk.ifs.finance.domain.builder.ProjectFinanceBuilder.newProjectFinance;
import static org.innovateuk.ifs.user.builder.RoleBuilder.newRole;
import static org.innovateuk.ifs.user.builder.RoleResourceBuilder.newRoleResource;
import static org.innovateuk.ifs.user.builder.UserBuilder.newUser;
import static org.innovateuk.ifs.user.builder.UserResourceBuilder.newUserResource;
import static org.innovateuk.ifs.user.resource.UserRoleType.FINANCE_CONTACT;
import static org.innovateuk.ifs.user.resource.UserRoleType.PROJECT_FINANCE;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class ProjectFinanceNotePermissionRulesTest extends BasePermissionRulesTest<ProjectFinanceNotePermissionRules> {
    private NoteResource noteResource;
    private UserResource projectFinanceUserOne;
    private UserResource projectFinanceUserTwo;
    private UserResource intrusor;

    @Before
    public void setUp() throws Exception {
        projectFinanceUserOne = projectFinanceUser();
        projectFinanceUserTwo = newUserResource().withId(1993L).withRolesGlobal(newRoleResource()
                .withType(PROJECT_FINANCE).build(1)).build();
        intrusor = getUserWithRole(FINANCE_CONTACT);
        noteResource = sampleNote();
    }

    private NoteResource sampleNote() {
        return sampleNote(asList(new PostResource(null, projectFinanceUserOne, null, null, null)));
    }

    private NoteResource sampleNoteWithoutPosts() {
        return sampleNote(null);
    }

    private NoteResource sampleNote(List<PostResource> posts) {
        return noteResource = new NoteResource(3L, 22L, posts,null, null);
    }

    @Override
    protected ProjectFinanceNotePermissionRules supplyPermissionRulesUnderTest() {
        return new ProjectFinanceNotePermissionRules();
    }

    @Test
    public void testThatOnlyInternalProjectFinanceUsersCanCreateNotes() throws Exception {
        assertTrue(rules.onlyInternalUsersCanCreateNotesWithInitialPostAndIsAuthor(noteResource, projectFinanceUserOne));
        assertFalse(rules.onlyInternalUsersCanCreateNotesWithInitialPostAndIsAuthor(noteResource, intrusor));
    }

    @Test
    public void testThatNoteCreationRequiresTheInitialPost() throws Exception {
        assertTrue(rules.onlyInternalUsersCanCreateNotesWithInitialPostAndIsAuthor(noteResource, projectFinanceUserOne));
        assertFalse(rules.onlyInternalUsersCanCreateNotesWithInitialPostAndIsAuthor(sampleNoteWithoutPosts(), projectFinanceUserOne));
    }

    @Test
    public void testThatNoteCreationRequiresTheInitialPostAuthorToBeTheCurrentUser() throws Exception {
        assertTrue(rules.onlyInternalUsersCanCreateNotesWithInitialPostAndIsAuthor(noteResource, projectFinanceUserOne));
        assertFalse(rules.onlyInternalUsersCanCreateNotesWithInitialPostAndIsAuthor(noteResource, projectFinanceUserTwo));
    }

    @Test
    public void testThatOnlyProjectFinanceUserCanAddPostsToANote() throws Exception {
        assertTrue(rules.onlyInternalUsersCanAddPosts(noteResource, projectFinanceUserOne));
        assertTrue(rules.onlyInternalUsersCanAddPosts(noteResource, projectFinanceUserTwo));
        assertFalse(rules.onlyInternalUsersCanAddPosts(noteResource, intrusor));
    }

    @Test
    public void testThatOnlyInternalProjectFinanceUsersCanDeleteANote() throws Exception {
        assertTrue(rules.onlyInternalUsersCanDeleteNotes(noteResource, projectFinanceUserOne));
        assertTrue(rules.onlyInternalUsersCanDeleteNotes(noteResource, projectFinanceUserTwo));
        assertFalse(rules.onlyInternalUsersCanDeleteNotes(noteResource, intrusor));
    }

    @Test
    public void testThatOnlyInternalUsersViewNotes() {
        assertTrue(rules.onlyInternalUsersCanViewNotes(noteResource, projectFinanceUserOne));
        assertTrue(rules.onlyInternalUsersCanViewNotes(noteResource, projectFinanceUserTwo));
        assertFalse(rules.onlyInternalUsersCanViewNotes(noteResource, intrusor));
    }

}
