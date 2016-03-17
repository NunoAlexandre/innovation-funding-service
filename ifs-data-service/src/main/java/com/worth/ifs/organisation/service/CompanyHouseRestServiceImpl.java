package com.worth.ifs.organisation.service;

import com.worth.ifs.commons.service.BaseRestService;
import com.worth.ifs.organisation.resource.OrganisationSearchResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriUtils;

import java.io.UnsupportedEncodingException;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * Class to expose methods to communicate with the company house api.
 */
@Service
public class CompanyHouseRestServiceImpl  extends BaseRestService implements CompanyHouseRestService {
    private static final Log log = LogFactory.getLog(CompanyHouseRestServiceImpl.class);

    @Value("${ifs.data.service.rest.companyhouse}")
    String companyHouseRestUrl;
    @Override
    public List<OrganisationSearchResult> searchOrganisations(String searchText){
    	String searchTextEncoded;
        try {
        	searchTextEncoded = UriUtils.encode(searchText, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error(e);
            searchTextEncoded = searchText;
        }
        return asList(restGet(companyHouseRestUrl + "/searchCompanyHouse/"+searchTextEncoded, OrganisationSearchResult[].class));
    }
    @Override
    public OrganisationSearchResult getOrganisationById(String id){

        return restGet(companyHouseRestUrl + "/getCompanyHouse/"+id, OrganisationSearchResult.class);
    }
}
