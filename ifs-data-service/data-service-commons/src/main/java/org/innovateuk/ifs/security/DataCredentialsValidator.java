package org.innovateuk.ifs.security;

import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.commons.security.CredentialsValidator;
import org.innovateuk.ifs.user.domain.User;
import org.innovateuk.ifs.user.mapper.UserMapper;
import org.innovateuk.ifs.user.resource.UserResource;
import org.innovateuk.ifs.user.repository.UserRepository;
import org.innovateuk.ifs.user.transactional.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import static org.innovateuk.ifs.commons.error.CommonErrors.notFoundError;
import static org.innovateuk.ifs.util.EntityLookupCallbacks.find;

@Component
public class DataCredentialsValidator implements CredentialsValidator {

    @Autowired
    @Lazy
    private UserService userService;

    @Override
    public RestResult<UserResource> retrieveUserByUid(String uid) {
        return userService.getUserResourceByUid(uid).toGetResponse();
    }
}
