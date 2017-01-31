package org.innovateuk.ifs.publiccontent.service;

import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.competition.publiccontent.resource.PublicContentResource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.innovateuk.ifs.commons.rest.RestResult.restSuccess;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

/**
 * Created by luke.harper on 25/01/2017.
 */
@RunWith(MockitoJUnitRunner.class)
public class PublicContentServiceImplTest {

    private static final Long COMPETITION_ID = 1L;

    @InjectMocks
    private PublicContentServiceImpl target;

    @Mock
    private PublicContentRestService publicContentRestService;


    @Test
    public void testGetCompetitionById() {
        PublicContentResource resource = new PublicContentResource();
        when(publicContentRestService.getByCompetitionId(COMPETITION_ID)).thenReturn(restSuccess(resource));

        PublicContentResource result = target.getCompetitionById(COMPETITION_ID);

        assertThat(result, equalTo(resource));
    }

    @Test
    public void testPublishByCompetitionId() {
        when(publicContentRestService.publishByCompetitionId(COMPETITION_ID)).thenReturn(restSuccess());

        ServiceResult<Void> result = target.publishByCompetitionId(COMPETITION_ID);

        assertTrue(result.isSuccess());
    }
}
