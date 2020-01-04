package com.soaesps.core.component.security;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CustomAuthenticationEntryPoint extends BasicAuthenticationEntryPoint {
    public static Logger logger;

    static {
        logger = Logger.getLogger(CustomAuthenticationEntryPoint.class.getName());
        logger.setLevel(Level.INFO);
    }

    public void commence(final HttpServletRequest request, final HttpServletResponse response,
                         final AuthenticationException authEx) throws IOException {
        if (logger.isLoggable(Level.INFO)) {
            logger.log(Level.INFO, "[CustomAuthenticationEntryPoint/commence]: ", authEx);
        }

        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Access Denied");
    }
}