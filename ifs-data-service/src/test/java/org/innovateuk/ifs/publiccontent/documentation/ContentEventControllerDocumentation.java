package org.innovateuk.ifs.publiccontent.documentation;

import org.innovateuk.ifs.BaseControllerMockMVCTest;
import org.innovateuk.ifs.competition.publiccontent.resource.ContentEventResource;
import org.innovateuk.ifs.publiccontent.controller.ContentEventController;
import org.innovateuk.ifs.publiccontent.transactional.ContentEventService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;

import java.util.List;

import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.documentation.ContentEventResourceDocs.contentEventResourceBuilder;
import static org.innovateuk.ifs.documentation.ContentEventResourceDocs.contentEventResourceFields;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ContentEventControllerDocumentation extends BaseControllerMockMVCTest<ContentEventController> {
    @Mock
    private ContentEventService contentEventService;
    private RestDocumentationResultHandler document;

    @Override
    protected ContentEventController supplyControllerUnderTest() {
        return new ContentEventController();
    }

    @Before
    public void setup() {
        this.document = document("public-content/events/{method-name}",
                preprocessResponse(prettyPrint()));
    }

    @Test
    public void resetAndSaveEvents() throws Exception {
        List<ContentEventResource> resources = contentEventResourceBuilder.build(2);

        when(contentEventService.resetAndSaveEvents(1L, resources)).thenReturn(serviceSuccess());

        mockMvc.perform(post("/public-content/events/reset-and-save-events/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsString(resources)))
                .andExpect(status().isOk())
                .andDo(this.document.snippets(
                        pathParameters(
                                parameterWithName("id").description("The id of the public content that's being reset")
                        ),
                        requestFields(fieldWithPath("[]").description("List of public content events"))
                                .andWithPrefix("[].", contentEventResourceFields)
                ));
    }
}
