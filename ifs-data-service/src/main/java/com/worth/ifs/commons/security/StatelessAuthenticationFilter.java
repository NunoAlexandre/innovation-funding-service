package com.worth.ifs.commons.security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@Configurable
public class StatelessAuthenticationFilter extends GenericFilterBean {
    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    private UserAuthenticationService userAuthenticationService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        if(!ignoreRequest(httpRequest)) {
            Authentication authentication = userAuthenticationService.getAuthentication(httpRequest);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(request, response);
    }

    public boolean ignoreRequest(HttpServletRequest request) {
        RequestMatcher ignored = getIgnoredRequestMatchers();
        return ignored.matches(request);
    }

    /**
     * This methods returns a request matchers with all requests that don't have to be authenticate.
     * For example static resources.
     */
    public RequestMatcher getIgnoredRequestMatchers() {
        List<RequestMatcher> antPathRequestMatchers = new ArrayList<RequestMatcher>();
        antPathRequestMatchers.add(new AntPathRequestMatcher("/error"));
        antPathRequestMatchers.add(new AntPathRequestMatcher("/"));
        antPathRequestMatchers.add(new AntPathRequestMatcher("/css/**"));
        antPathRequestMatchers.add(new AntPathRequestMatcher("/js/**"));
        antPathRequestMatchers.add(new AntPathRequestMatcher("/assets/**"));
        antPathRequestMatchers.add(new AntPathRequestMatcher("/images/**"));
        antPathRequestMatchers.add(new AntPathRequestMatcher("/favicon.ico"));
        antPathRequestMatchers.add(new AntPathRequestMatcher("/docs/**"));
        antPathRequestMatchers.add(new AntPathRequestMatcher("/prototypes/**"));

        /****** For Application creation *******/
        antPathRequestMatchers.add(new AntPathRequestMatcher("/competition/**"));
        antPathRequestMatchers.add(new AntPathRequestMatcher("/application/create/**"));
        antPathRequestMatchers.add(new AntPathRequestMatcher("/application/createApplicationByName/**"));
        /****** For organisation creation *******/
        antPathRequestMatchers.add(new AntPathRequestMatcher("/organisation/create/**"));
        antPathRequestMatchers.add(new AntPathRequestMatcher("/organisation/save/**"));
        antPathRequestMatchers.add(new AntPathRequestMatcher("/organisation/saveResource/**"));
        antPathRequestMatchers.add(new AntPathRequestMatcher("/organisationtype/**"));
        antPathRequestMatchers.add(new AntPathRequestMatcher("/organisation/addAddress/**"));
        antPathRequestMatchers.add(new AntPathRequestMatcher("/companyhouse/**"));
        antPathRequestMatchers.add(new AntPathRequestMatcher("/organisationsearch/**"));
        /****** For Invites *******/
        antPathRequestMatchers.add(new AntPathRequestMatcher("/inviteorganisation/**"));
        antPathRequestMatchers.add(new AntPathRequestMatcher("/invite/getInviteByHash/**"));
        antPathRequestMatchers.add(new AntPathRequestMatcher("/invite/acceptInvite/**"));
        antPathRequestMatchers.add(new AntPathRequestMatcher("/invite/getInviteOrganisationByHash/**"));
        antPathRequestMatchers.add(new AntPathRequestMatcher("/accept-invite/**"));
        /****** For Registration *******/
        antPathRequestMatchers.add(new AntPathRequestMatcher("/registration/**"));
        return new OrRequestMatcher(antPathRequestMatchers);
    }

}
