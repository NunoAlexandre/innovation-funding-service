package com.worth.ifs.finance.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.worth.ifs.commons.rest.RestResult;
import com.worth.ifs.commons.service.BaseRestService;
import com.worth.ifs.file.resource.FileEntryResource;
import com.worth.ifs.finance.controller.ApplicationFinanceController;
import com.worth.ifs.finance.domain.ApplicationFinance;
import com.worth.ifs.finance.resource.ApplicationFinanceResource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.worth.ifs.commons.rest.RestResult.restSuccess;
import static com.worth.ifs.commons.service.ParameterizedTypeReferences.applicationFinanceResourceListType;

/**
 * ApplicationFinanceRestServiceImpl is a utility for CRUD operations on {@link ApplicationFinance}.
 * This class connects to the {@link com.worth.ifs.finance.controller.ApplicationFinanceController}
 * through a REST call.
 */
@Service
public class ApplicationFinanceRestServiceImpl extends BaseRestService implements ApplicationFinanceRestService {

    @Value("${ifs.data.service.rest.applicationfinance}")
    String applicationFinanceRestURL;

    @Override
    public RestResult<ApplicationFinanceResource> getApplicationFinance(Long applicationId, Long organisationId) {
        if(applicationId == null || organisationId == null){
            return null;
        }
        return getWithRestResult(applicationFinanceRestURL + "/findByApplicationOrganisation/" + applicationId + "/" + organisationId, ApplicationFinanceResource.class);
    }

    @Override
    public RestResult<List<ApplicationFinanceResource>> getApplicationFinances(Long applicationId) {
        if(applicationId == null) {
            return null;
        }

        return getWithRestResult(applicationFinanceRestURL + "/findByApplication/" + applicationId, applicationFinanceResourceListType());
    }

    @Override
    public RestResult<ApplicationFinanceResource> addApplicationFinanceForOrganisation(Long applicationId, Long organisationId) {
        if(applicationId == null || organisationId == null) {
            return null;
        }
        return postWithRestResult(applicationFinanceRestURL + "/add/" + applicationId + "/" + organisationId, null, ApplicationFinanceResource.class);
    }

    @Override
    public RestResult<ApplicationFinanceResource> update(Long applicationFinanceId, ApplicationFinanceResource applicationFinance){
        return postWithRestResult(applicationFinanceRestURL + "/update/"+ applicationFinanceId, applicationFinance, ApplicationFinanceResource.class);
    }

    @Override
    public RestResult<ApplicationFinanceResource> getById(Long applicationFinanceId){
        return getWithRestResult(applicationFinanceRestURL + "/getById/" + applicationFinanceId, ApplicationFinanceResource.class);
    }

    // TODO DW - INFUND-1555 - remove usage of ObjectNode
    @Override
    public RestResult<Double> getResearchParticipationPercentage(Long applicationId){
        return getWithRestResult(applicationFinanceRestURL + "/getResearchParticipationPercentage/" + applicationId, ObjectNode.class).andOnSuccess(jsonNode -> {
            double percentage = jsonNode.get(ApplicationFinanceController.RESEARCH_PARTICIPATION_PERCENTAGE).asDouble();
            return restSuccess(percentage);
        });
    }

    @Override
    public RestResult<ApplicationFinanceResource> getFinanceDetails(Long applicationId, Long organisationId) {
        return getWithRestResult(applicationFinanceRestURL + "/financeDetails/" + applicationId + "/"+organisationId, ApplicationFinanceResource.class);
    }

    @Override
    public RestResult<List<ApplicationFinanceResource>> getFinanceTotals(Long applicationId) {
        return getWithRestResult(applicationFinanceRestURL + "/financeTotals/" + applicationId, applicationFinanceResourceListType());
    }

    @Override
    public RestResult<FileEntryResource> addFinanceDocument(Long applicationFinanceId, String contentType, long contentLength, String originalFilename, byte[] file) {
        String url = applicationFinanceRestURL + "/financeDocument" +
                "?applicationFinanceId=" + applicationFinanceId +
                "&filename=" + originalFilename;

        final HttpHeaders headers = createHeader(contentType,  contentLength);

        return postWithRestResult(url, file, headers, FileEntryResource.class);
    }

    @Override
    public RestResult<Void> removeFinanceDocument(Long applicationFinanceId) {
        String url = applicationFinanceRestURL + "/financeDocument" +
                "?applicationFinanceId=" + applicationFinanceId;

        return deleteWithRestResult(url, Void.class);
    }

    @Override
    public RestResult<ByteArrayResource> getFile(Long applicationFinanceId) {
        String url = applicationFinanceRestURL + "/financeDocument" +
                "?applicationFinanceId=" + applicationFinanceId;

        return getWithRestResult(url, ByteArrayResource.class);
    }

    @Override
    public RestResult<FileEntryResource> getFileDetails(Long applicationFinanceId) {
        String url = applicationFinanceRestURL + "/financeDocument/fileentry" +
                "?applicationFinanceId=" + applicationFinanceId;

        return getWithRestResult(url, FileEntryResource.class);
    }

    private HttpHeaders createHeader(String contentType, long contentLength){
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentLength(contentLength);
        return headers;
    }
}
