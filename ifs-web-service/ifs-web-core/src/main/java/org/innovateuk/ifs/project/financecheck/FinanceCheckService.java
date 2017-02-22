package org.innovateuk.ifs.project.financecheck;

import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.file.resource.FileEntryResource;
import org.innovateuk.ifs.project.finance.resource.FinanceCheckEligibilityResource;
import org.innovateuk.ifs.project.finance.resource.FinanceCheckOverviewResource;
import org.innovateuk.ifs.project.finance.resource.FinanceCheckResource;
import org.innovateuk.ifs.project.finance.resource.FinanceCheckSummaryResource;
import org.innovateuk.ifs.project.finance.workflow.financechecks.resource.FinanceCheckProcessResource;
import org.innovateuk.ifs.project.resource.ProjectOrganisationCompositeId;
import org.innovateuk.threads.resource.NoteResource;
import org.innovateuk.threads.resource.PostResource;
import org.innovateuk.threads.resource.QueryResource;
import org.springframework.core.io.ByteArrayResource;

import java.util.List;
import java.util.Optional;

public interface FinanceCheckService {

    FinanceCheckResource getByProjectAndOrganisation(ProjectOrganisationCompositeId key);

    ServiceResult<Void> update(FinanceCheckResource toUpdate);

    ServiceResult<FinanceCheckSummaryResource> getFinanceCheckSummary(Long projectId);

    ServiceResult<FinanceCheckOverviewResource> getFinanceCheckOverview(Long projectId);

    ServiceResult<Void> approveFinanceCheck(Long projectId, Long organisationId);

    FinanceCheckProcessResource getFinanceCheckApprovalStatus(Long projectId, Long organisationId);

    FinanceCheckEligibilityResource getFinanceCheckEligibilityDetails(Long projectId, Long organisationId);

    ServiceResult<FileEntryResource> uploadFile(String contentType, long contentLength, String originalFilename, byte[] bytes);

    ServiceResult<Void> deleteFile(Long fileId);

    ServiceResult<Optional<ByteArrayResource>> downloadFile(Long fileId);

    ServiceResult<FileEntryResource> getFileInfo(Long fileId);

    ServiceResult<Long> saveQuery(QueryResource query);

    ServiceResult<Void> saveQueryPost(PostResource post, long threadId);

    ServiceResult<List<QueryResource>> loadQueries(Long projectFinanceId);

    ServiceResult<Long> saveNote(NoteResource note);

    ServiceResult<List<NoteResource>> loadNotes(Long projectFinanceId);

    ServiceResult<Void> saveNotePost(PostResource post, long noteId);
}
