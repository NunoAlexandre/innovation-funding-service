package com.worth.ifs.config;

import com.worth.ifs.interceptors.GoogleAnalyticsHandlerInterceptor;
import com.worth.ifs.interceptors.MenuLinksHandlerInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.springframework.web.servlet.resource.ContentVersionStrategy;
import org.springframework.web.servlet.resource.VersionResourceResolver;

import java.util.Locale;

@Configuration
public class IFSWebConfiguration extends WebMvcConfigurerAdapter {
    public static final int CACHE_PERIOD = 60 * 60 * 24 * 60;

    @Autowired
    Environment env;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        super.addInterceptors(registry);
        registry.addInterceptor(getMenuLinksHandlerInterceptor());
        registry.addInterceptor(getGoogleAnalyticsHandlerInterceptor());
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        if(env.acceptsProfiles("uat", "prod")) {
            VersionResourceResolver versionResourceResolver = new VersionResourceResolver()
                    .addVersionStrategy(new ContentVersionStrategy(), "/**");

            registry.addResourceHandler("/js/**", "/css/**", "/images/**", "/favicon.ico")
                    .addResourceLocations(
                            "classpath:static/js/", "static/js/",
                            "classpath:static/css/", "static/css/",
                            "classpath:static/images/", "static/images/"
                    )
                    .setCachePeriod(CACHE_PERIOD)
                    .resourceChain(true)
                    .addResolver(versionResourceResolver);
        }else{
            registry.addResourceHandler("/js/**", "/css/**", "/images/**", "/favicon.ico")
                    .addResourceLocations(
                            "classpath:static/js/", "static/js/",
                            "classpath:static/css/", "static/css/",
                            "classpath:static/images/", "static/images/"
                    )
                    .resourceChain(true);
        }
        super.addResourceHandlers(registry);
    }

    @Bean
    public HandlerInterceptor getMenuLinksHandlerInterceptor() {
        return new MenuLinksHandlerInterceptor();
    }
    @Bean
    public HandlerInterceptor getGoogleAnalyticsHandlerInterceptor() {
        return new GoogleAnalyticsHandlerInterceptor();
    }

    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver slr = new SessionLocaleResolver();
        slr.setDefaultLocale(Locale.UK);
        return slr;
    }
}
