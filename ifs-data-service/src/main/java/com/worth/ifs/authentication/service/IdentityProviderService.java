package com.worth.ifs.authentication.service;

import com.worth.ifs.security.NotSecured;
import com.worth.ifs.transactional.ServiceResult;

/**
 * Represents an endpoint attached to the (external) Authentication service
 */
public interface IdentityProviderService {

    /**
     * Creates a user record in the Identity Provider's database and returns a unique id for that user record
     *
     * @param emailAddress
     * @param phoneNumber
     * @return
     */
    @NotSecured("TODO - implement when permissions matrix defined")
    ServiceResult<String> createUserRecordWithUid(String emailAddress, String password);

    /**
     * Update a user record in the Identity Provider's database and returns the User's unique id
     *
     * @param uid
     * @param password
     * @return
     */
    @NotSecured("TODO - implement when permissions matrix defined")
    ServiceResult<String> updateUserPassword(String uid, String password);
}
