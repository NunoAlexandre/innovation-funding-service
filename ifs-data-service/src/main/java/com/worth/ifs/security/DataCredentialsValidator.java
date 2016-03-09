package com.worth.ifs.security;

import com.worth.ifs.commons.rest.RestResult;
import com.worth.ifs.commons.security.CredentialsValidator;
import com.worth.ifs.user.domain.User;
import com.worth.ifs.user.transactional.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class DataCredentialsValidator implements CredentialsValidator {

    @Autowired
    private UserService userService;

    @Override
    public RestResult<User> retrieveUserByUid(String uid) {
        return userService.getUserByUid(uid);
    }
}
