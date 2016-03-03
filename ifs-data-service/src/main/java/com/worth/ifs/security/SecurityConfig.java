package com.worth.ifs.security;

import com.worth.ifs.commons.security.StatelessAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;

/**
 * Every request is stateless and is checked if the user has access to requested resource.
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Order(1)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Bean
    public StatelessAuthenticationFilter getStatelessAuthenticationFilter() {
        return new StatelessAuthenticationFilter();
    }


    public SecurityConfig() {
        super(true);
    }


    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
                .anonymous()
            .and()
                .authorizeRequests()
                // allow anonymous resource requests
                .requestMatchers(getStatelessAuthenticationFilter().getIgnoredRequestMatchers()).permitAll()
                .antMatchers("/user/email/*/password/*").permitAll()
                .antMatchers("/user/verifyEmail/*").permitAll()
                .antMatchers("/user/createLeadApplicantForOrganisation/*").permitAll()
                .antMatchers("/user/findByEmail/*/").permitAll()
                .antMatchers("/user/token/*").permitAll()
                .antMatchers("/organisation/findById/*").permitAll()
                .antMatchers("/address/doLookup/*").permitAll()
                .antMatchers("/browser/**").permitAll()
                .anyRequest().authenticated()
                .and()
                .exceptionHandling()
                .and()
                .addFilterBefore(getStatelessAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .headers().cacheControl();
    }


    private CsrfTokenRepository csrfTokenRepository() {
        HttpSessionCsrfTokenRepository repository = new HttpSessionCsrfTokenRepository();
        repository.setHeaderName("X-XSRF-TOKEN");
        return repository;
    }

}
