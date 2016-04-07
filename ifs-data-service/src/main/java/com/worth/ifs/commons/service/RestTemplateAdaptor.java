package com.worth.ifs.commons.service;

import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import static com.worth.ifs.commons.security.UidAuthenticationService.AUTH_TOKEN;

/**
 * The default Rest Template Adaptor that talks to the IFS API, passing the appropriate authentication tokens depending
 * upon who is logged in
 */
@Component
public class RestTemplateAdaptor extends AbstractInternalRestTemplateAdaptor {

    @Override
    protected void setAuthenticationToken(HttpHeaders headers) {
        if (SecurityContextHolder.getContext() != null &&
                SecurityContextHolder.getContext().getAuthentication() != null &&
                SecurityContextHolder.getContext().getAuthentication().getCredentials() != null) {
            headers.set(AUTH_TOKEN, SecurityContextHolder.getContext().getAuthentication().getCredentials().toString());
        }
    }
}
