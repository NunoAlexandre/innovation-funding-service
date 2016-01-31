package com.worth.ifs.transactional;

import static com.worth.ifs.transactional.RestResult.restSuccess;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.OK;

/**
 *
 */
public class RestResults {

    public static <T> RestResult<T> ok() {
        return restSuccess(OK);
    }

    public static <T> RestResult<T> accepted() {
        return restSuccess(ACCEPTED);
    }
}
