package com.worth.ifs.organisation.transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.worth.ifs.address.resource.AddressResource;
import com.worth.ifs.commons.service.AbstractRestTemplateAdaptor;
import com.worth.ifs.commons.service.ServiceResult;
import com.worth.ifs.organisation.resource.OrganisationSearchResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriUtils;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.worth.ifs.commons.error.CommonErrors.internalServerErrorError;
import static com.worth.ifs.commons.service.ServiceResult.serviceFailure;
import static com.worth.ifs.commons.service.ServiceResult.serviceSuccess;
import static java.util.Optional.ofNullable;

/**
 * This class communicates with the Company House API.
 * This is used to get information abouts companies.
 *
 * @see <a href="https://developer.companieshouse.gov.uk/api/docs/">Company House API site</a>
 */
@Service
public class CompanyHouseApiServiceImpl implements CompanyHouseApiService {

    private static final Log LOG = LogFactory.getLog(CompanyHouseApiServiceImpl.class);

    @Value("${ifs.data.company-house.url}")
    private final String COMPANY_HOUSE_API = null;

    private static final int SEARCH_ITEMS_MAX = 10;

    @Autowired
    @Qualifier("companyhouse_adaptor")
    private AbstractRestTemplateAdaptor adaptor;

    @Override
    public ServiceResult<List<OrganisationSearchResult>> searchOrganisations(String encodedSearchText) {

        return decodeString(encodedSearchText).andOnSuccess(decodedSearchText -> {

            // encoded in the web-services.
            JsonNode companiesResources = restGet("search/companies?items_per_page=" + SEARCH_ITEMS_MAX + "&q=" + decodedSearchText, JsonNode.class);
            JsonNode companyItems = companiesResources.path("items");
            List<OrganisationSearchResult> results = new ArrayList<>();
            companyItems.forEach(i -> results.add(companySearchMapper(i)));
            return serviceSuccess(results);
        });
    }

    @Override
    public ServiceResult<OrganisationSearchResult> getOrganisationById(String id) {
        LOG.debug("getOrganisationById " + id);

        return ofNullable(restGet("company/" + id, JsonNode.class)).
            map(jsonNode -> serviceSuccess(companyProfileMapper(jsonNode))).
            orElse(serviceFailure(internalServerErrorError("No response from Companies House")));
    }

    protected <T> T restGet(String path, Class<T> c) {
        return adaptor.restGetEntity(COMPANY_HOUSE_API + path, c).getBody();
    }

    private OrganisationSearchResult companyProfileMapper(JsonNode jsonNode) {
        AddressResource officeAddress = getAddress(jsonNode, "registered_office_address");
        ObjectMapper mapper = new ObjectMapper();

        OrganisationSearchResult org = new OrganisationSearchResult(jsonNode.path("company_number").asText(), jsonNode.path("company_name").asText());
        org.setExtraAttributes(mapper.convertValue(jsonNode, Map.class));
        org.setOrganisationAddress(officeAddress);
        return org;
    }

    private AddressResource getAddress(JsonNode jsonNode, String path) {
        AddressResource address = new AddressResource(
                jsonNode.path(path).path("address_line_1").asText(),
                jsonNode.path(path).path("address_line_2").asText(),
                jsonNode.path(path).path("address_line_3").asText(),
                jsonNode.path(path).path("locality").asText(),
                jsonNode.path(path).path("region").asText(),
                jsonNode.path(path).path("postal_code").asText()
                );
        return address;
    }

    private ServiceResult<String> decodeString(String encodedSearchText) {
        try {
            return serviceSuccess(UriUtils.decode(encodedSearchText, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            LOG.error("Unable to decode search string " + encodedSearchText, e);
            return serviceFailure(internalServerErrorError("Unable to decode search string"));
        }
    }

    private OrganisationSearchResult companySearchMapper(JsonNode jsonNode) {
        AddressResource officeAddress = getAddress(jsonNode, "address");

        OrganisationSearchResult org = new OrganisationSearchResult(jsonNode.path("company_number").asText(), jsonNode.path("title").asText());
        Map<String, Object> extras = new HashMap<>();
        extras.put("company_type", jsonNode.path("company_type").asText());
        extras.put("date_of_creation", jsonNode.path("date_of_creation").asText());
        extras.put("description", jsonNode.path("description").asText());
        org.setExtraAttributes(extras);
        org.setOrganisationAddress(officeAddress);
        return org;
    }
}
