package com.worth.ifs.finance.service;

import com.worth.ifs.commons.service.BaseRestService;
import com.worth.ifs.finance.domain.Cost;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static java.util.Arrays.asList;

/**
 * CostRestServiceImpl is a utility for CRUD operations on {@link Cost}.
 * This class connects to the {@link com.worth.ifs.finance.controller.CostController}
 * through a REST call.
 */
@Service
public class CostRestServiceImpl extends BaseRestService implements CostRestService {
    private final Log log = LogFactory.getLog(getClass());

    @Value("${ifs.data.service.rest.cost}")
    String costRestURL;

    @Override
    public void add(Long applicationFinanceId, Long questionId) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.put(dataRestServiceURL + costRestURL + "/add/"+applicationFinanceId + "/" + questionId, applicationFinanceId, questionId);
    }

    @Override
    public List<Cost> getCosts(Long applicationFinanceId) {
        return asList(restGet(costRestURL + "/get/"+applicationFinanceId, Cost[].class));
    }

    @Override
    public void update(Cost cost) {
        RestTemplate restTemplate = new RestTemplate();
        String url = dataRestServiceURL + costRestURL + "/update/" +cost.getId();

        //set your entity to send
        HttpEntity<Cost> entity = new HttpEntity<>(cost, getJSONHeaders());

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity
                , String.class);
    }

    @Override
    public void delete(Long costId) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.delete(dataRestServiceURL + costRestURL + "/delete/"+costId);
    }

    @Override
    public Cost findById(Long id) {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Cost> responseEntity = restTemplate.getForEntity(dataRestServiceURL + costRestURL + "/findById/"+id, Cost.class);
        return responseEntity.getBody();
    }
}
