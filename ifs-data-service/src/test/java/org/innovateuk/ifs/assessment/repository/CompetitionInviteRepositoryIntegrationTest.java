package org.innovateuk.ifs.assessment.repository;

import org.innovateuk.ifs.BaseRepositoryIntegrationTest;
import org.innovateuk.ifs.category.domain.InnovationArea;
import org.innovateuk.ifs.category.repository.InnovationAreaRepository;
import org.innovateuk.ifs.competition.domain.Competition;
import org.innovateuk.ifs.competition.repository.CompetitionRepository;
import org.innovateuk.ifs.invite.domain.CompetitionInvite;
import org.innovateuk.ifs.invite.repository.CompetitionInviteRepository;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

import static com.google.common.collect.ImmutableSet.of;
import static org.innovateuk.ifs.assessment.builder.CompetitionInviteBuilder.newCompetitionInvite;
import static org.innovateuk.ifs.base.amend.BaseBuilderAmendFunctions.id;
import static org.innovateuk.ifs.category.builder.InnovationAreaBuilder.newInnovationArea;
import static org.innovateuk.ifs.competition.builder.CompetitionBuilder.newCompetition;
import static org.innovateuk.ifs.invite.constant.InviteStatus.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.data.domain.Sort.Direction.*;

public class CompetitionInviteRepositoryIntegrationTest extends BaseRepositoryIntegrationTest<CompetitionInviteRepository> {

    private Competition competition;

    private InnovationArea innovationArea;

    @Autowired
    private CompetitionRepository competitionRepository;

    @Autowired
    private InnovationAreaRepository innovationAreaRepository;

    @Autowired
    @Override
    protected void setRepository(CompetitionInviteRepository repository) {
        this.repository = repository;
    }

    @Before
    public void setup() {
        competition = competitionRepository.save(newCompetition().withName("competition").build());
        innovationArea = innovationAreaRepository.save(newInnovationArea().withName("innovation area").build());
    }

    @Test
    public void findAll() {
        repository.save(new CompetitionInvite("name1", "tom@poly.io", "hash", competition, innovationArea));
        repository.save(new CompetitionInvite("name2", "tom@2poly.io", "hash2", competition, innovationArea));

        Iterable<CompetitionInvite> invites = repository.findAll();

        assertEquals(2, invites.spliterator().getExactSizeIfKnown());
    }

    @Test
    public void getByHash() {
        CompetitionInvite invite = new CompetitionInvite("name", "tom@poly.io", "hash", competition, innovationArea);
        CompetitionInvite saved = repository.save(invite);

        flushAndClearSession();

        CompetitionInvite retrievedInvite = repository.getByHash("hash");
        assertNotNull(retrievedInvite);

        assertEquals("name", retrievedInvite.getName());
        assertEquals("tom@poly.io", retrievedInvite.getEmail());
        assertEquals("hash", retrievedInvite.getHash());
        assertEquals(saved.getTarget().getId(), retrievedInvite.getTarget().getId());
    }

    @Test
    public void getByEmailAndCompetitionId() {
        CompetitionInvite invite = new CompetitionInvite("name", "tom@poly.io", "hash", competition, innovationArea);
        CompetitionInvite saved = repository.save(invite);

        CompetitionInvite retrievedInvite = repository.getByEmailAndCompetitionId("tom@poly.io", competition.getId());
        assertNotNull(retrievedInvite);

        assertEquals(saved, retrievedInvite);
    }

    @Test
    public void getByCompetitionIdAndStatus() {
        Competition otherCompetition = newCompetition().build();

        repository.save(newCompetitionInvite()
                .with(id(null))
                .withCompetition(competition, otherCompetition, competition, otherCompetition, competition, otherCompetition)
                .withEmail("john@example.com", "dave@example.com", "richard@example.com", "oliver@example.com", "michael@example.com", "rachel@example.com")
                .withHash("1dc914e2-d076-4b15-9fa6-99ee5b711613", "bddd15e6-9e9d-42e8-88b0-42f3abcbb26e", "0253e4b9-8f76-4a55-b40b-689a025a9129",
                        "cba968ac-d792-4f41-b3d2-8a92980d54ce", "9e6032a5-39d5-4ec8-a8b2-883f5956a809", "75a615bf-3e5a-4ae3-9479-1753ae438108")
                .withName("John Barnes", "Dave Smith", "Richard Turner", "Oliver Romero", "Michael King", "Rachel Fish")
                .withStatus(CREATED, CREATED, OPENED, OPENED, SENT, SENT)
                .build(6));

        Pageable pageable = new PageRequest(0, 20, new Sort(ASC, "name"));

        Page<CompetitionInvite> pageResult = repository.getByCompetitionIdAndStatus(competition.getId(), CREATED, pageable);

        List<CompetitionInvite> retrievedInvites = pageResult.getContent();

        assertEquals(1, retrievedInvites.size());

        assertEquals(competition, retrievedInvites.get(0).getTarget());
        assertEquals("john@example.com", retrievedInvites.get(0).getEmail());
        assertEquals("1dc914e2-d076-4b15-9fa6-99ee5b711613", retrievedInvites.get(0).getHash());
        assertEquals("John Barnes", retrievedInvites.get(0).getName());
        assertEquals(CREATED, retrievedInvites.get(0).getStatus());
    }

    @Test
    public void countByCompetitionIdAndStatus() {
        Competition otherCompetition = newCompetition().build();

        repository.save(newCompetitionInvite()
                .with(id(null))
                .withCompetition(competition, otherCompetition, competition, otherCompetition, competition, otherCompetition)
                .withEmail("john@example.com", "dave@example.com", "richard@example.com", "oliver@example.com", "michael@example.com", "rachel@example.com")
                .withHash("1dc914e2-d076-4b15-9fa6-99ee5b711613", "bddd15e6-9e9d-42e8-88b0-42f3abcbb26e", "0253e4b9-8f76-4a55-b40b-689a025a9129",
                        "cba968ac-d792-4f41-b3d2-8a92980d54ce", "9e6032a5-39d5-4ec8-a8b2-883f5956a809", "75a615bf-3e5a-4ae3-9479-1753ae438108")
                .withName("John Barnes", "Dave Smith", "Richard Turner", "Oliver Romero", "Michael King", "Rachel Fish")
                .withStatus(CREATED, CREATED, OPENED, OPENED, SENT, SENT)
                .build(6));

        long count = repository.countByCompetitionIdAndStatusIn(competition.getId(), of(CREATED, OPENED));
        assertEquals(2, count);
    }

    @Test
    public void save() {
        CompetitionInvite invite = new CompetitionInvite("name", "tom@poly.io", "hash", competition, innovationArea);
        repository.save(invite);

        flushAndClearSession();

        long id = invite.getId();

        CompetitionInvite retrievedInvite = repository.findOne(id);

        assertEquals("name", retrievedInvite.getName());
        assertEquals("tom@poly.io", retrievedInvite.getEmail());
        assertEquals("hash", retrievedInvite.getHash());
        assertEquals(competition.getId(), retrievedInvite.getTarget().getId());
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void save_duplicateHash() {
        repository.save(new CompetitionInvite("name1", "tom@poly.io", "sameHash", competition, innovationArea));
        repository.save(new CompetitionInvite("name2", "tom@2poly.io", "sameHash", competition, innovationArea));
    }
}
