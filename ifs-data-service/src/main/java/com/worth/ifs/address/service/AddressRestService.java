package com.worth.ifs.address.service;

import com.worth.ifs.commons.rest.RestResult;
import com.worth.ifs.address.resource.AddressResource;

import java.util.List;

public interface AddressRestService {
    RestResult<AddressResource> findOne(Long id);
    RestResult<List<AddressResource>> doLookup(String lookup);
}