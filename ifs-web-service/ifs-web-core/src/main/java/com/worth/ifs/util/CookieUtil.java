package com.worth.ifs.util;

import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.util.StringUtils;
import org.springframework.web.util.WebUtils;

import javax.annotation.PostConstruct;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Optional;

@Configurable
public final class CookieUtil {
    private static final Log LOG = LogFactory.getLog(CookieUtil.class);

    private static final Integer COOKIE_LIFETIME = 3600;

    private static CookieUtil cookieUtilHelper;

    private TextEncryptor encryptor;

    @Value("${server.session.cookie.secure}")
    private Boolean cookieSecure;

    @Value("${server.session.cookie.http-only}")
    private Boolean cookieHttpOnly;

    @Value("${ifs.web.security.csrf.encryption.password}")
    private String encryptionPassword;

    @Value("${ifs.web.security.csrf.encryption.salt}")
    private String encryptionSalt;

    @PostConstruct
    public void init() {
        encryptor = Encryptors.text(encryptionPassword, encryptionSalt);
    }

    public static CookieUtil getInstance() {
        if(null ==  cookieUtilHelper) cookieUtilHelper = new CookieUtil();

        return cookieUtilHelper;
    }

    public void saveToCookie(HttpServletResponse response, String fieldName, String fieldValue) {
        if (StringUtils.hasText(fieldName)) {
            Cookie cookie = null;
            try {
                cookie = new Cookie(fieldName, encodeCookieValue(fieldValue));
            } catch (UnsupportedEncodingException e) {
                LOG.error(e);
                return;
            }
            cookie.setSecure(cookieSecure);
            cookie.setHttpOnly(cookieHttpOnly);
            cookie.setPath("/");
            cookie.setMaxAge(COOKIE_LIFETIME);
            response.addCookie(cookie);
        }
    }

    public void removeCookie(HttpServletResponse response, String fieldName) {
        if (StringUtils.hasText(fieldName)) {
            Cookie cookie = new Cookie(fieldName, "");
            cookie.setSecure(cookieSecure);
            cookie.setHttpOnly(cookieHttpOnly);
            cookie.setPath("/");
            cookie.setMaxAge(0);
            response.addCookie(cookie);
        }
    }

    public Optional<Cookie> getCookie(HttpServletRequest request, String fieldName) {
        return Optional.ofNullable(WebUtils.getCookie(request, fieldName));
    }

    public String getCookieValue(HttpServletRequest request, String fieldName) {
        Optional<Cookie> cookie = getCookie(request, fieldName);
        if (cookie.isPresent()) {
            try {
                return decodeCookieValue(cookie.get().getValue());
            } catch (UnsupportedEncodingException ignore) {
                LOG.error(ignore);
                //Do nothing
            }
        }
        return "";
    }

    private String encodeCookieValue(String value) throws UnsupportedEncodingException {
        return encryptor.encrypt(URLEncoder.encode(value, CharEncoding.UTF_8));
    }

    private String decodeCookieValue(String encodedValue) throws UnsupportedEncodingException {
        return URLDecoder.decode(encryptor.decrypt(encodedValue), CharEncoding.UTF_8);
    }
}