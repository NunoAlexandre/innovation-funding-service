package com.worth.ifs.bankdetails.security;

import com.worth.ifs.bankdetails.domain.BankDetails;
import com.worth.ifs.bankdetails.mapper.BankDetailsMapper;
import com.worth.ifs.bankdetails.repository.BankDetailsRepository;
import com.worth.ifs.bankdetails.resource.BankDetailsResource;
import com.worth.ifs.security.PermissionEntityLookupStrategies;
import com.worth.ifs.security.PermissionEntityLookupStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@PermissionEntityLookupStrategies
public class BankDetailsLookupStrategy {

    @Autowired
    private BankDetailsRepository bankDetailsRepository;

    @Autowired
    private BankDetailsMapper bankDetailsMapper;

    @PermissionEntityLookupStrategy
    public BankDetails getBankDetails(Long bankDetailsId) {
        return bankDetailsRepository.findOne(bankDetailsId);
    }

    @PermissionEntityLookupStrategy
    public BankDetailsResource getBankDetailsResource(Long bankDetailsId) {
        return bankDetailsMapper.mapToResource(bankDetailsRepository.findOne(bankDetailsId));
    }
}
