package com.worth.ifs.security;

import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Users accessing a page which they don't have access, will be forwarded to the specified url.
 */
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private String accessDeniedUrl;

    public CustomAuthenticationEntryPoint() {
    	// no-arg constructor

    }

    public CustomAuthenticationEntryPoint(String accessDeniedUrl) {
        this.accessDeniedUrl = accessDeniedUrl;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {

        if(authException instanceof InsufficientAuthenticationException) {
            response.sendRedirect(accessDeniedUrl);
        }
    }
}
