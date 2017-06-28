package org.innovateuk.ifs.invite.builder;

import org.innovateuk.ifs.invite.domain.RejectionReason;
import org.junit.Test;

import java.util.List;

import static org.innovateuk.ifs.invite.builder.RejectionReasonBuilder.newRejectionReason;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.junit.Assert.assertEquals;

public class RejectionReasonBuilderTest {

    @Test
    public void buildOne() {
        Long expectedId = 1L;
        String expectedReason = "Reason";
        Boolean expectedActive = TRUE;
        Integer expectedPriority = 1;

        RejectionReason rejectionReason = newRejectionReason()
                .withId(expectedId)
                .withReason(expectedReason)
                .withActive(expectedActive)
                .withPriority(expectedPriority)
                .build();

        assertEquals(expectedId, rejectionReason.getId());
        assertEquals(expectedReason, rejectionReason.getReason());
        assertEquals(expectedActive, rejectionReason.isActive());
        assertEquals(expectedPriority.intValue(), rejectionReason.getPriority());
    }

    @Test
    public void buildMany() {
        Long[] expectedIds = {1L, 2L};
        String[] expectedReasons = {"Reason 1", "Reason 2"};
        Boolean[] expectedActives = {TRUE, FALSE};
        Integer[] expectedPriorities = {1, 2};

        List<RejectionReason> rejectionReasons = newRejectionReason()
                .withId(expectedIds)
                .withReason(expectedReasons)
                .withActive(expectedActives)
                .withPriority(expectedPriorities)
                .build(2);

        RejectionReason first = rejectionReasons.get(0);
        assertEquals(expectedIds[0], first.getId());
        assertEquals(expectedReasons[0], first.getReason());
        assertEquals(expectedActives[0], first.isActive());
        assertEquals(expectedPriorities[0].intValue(), first.getPriority());

        RejectionReason second = rejectionReasons.get(1);
        assertEquals(expectedIds[1], second.getId());
        assertEquals(expectedReasons[1], second.getReason());
        assertEquals(expectedActives[1], second.isActive());
        assertEquals(expectedPriorities[1].intValue(), second.getPriority());
    }
}
