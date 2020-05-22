package com.soaesps.core.security.filter;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.token.Token;
import org.springframework.security.core.token.TokenService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CustomAuthenticationFilter extends OncePerRequestFilter {
    public static String XAUTH_TOKEN_HEADER_NAME = "Authorization";

    public static String HEADER_PREFIX = "Bearer:";

    static private final Logger logger;

    static {
        logger = Logger.getLogger(CustomAuthenticationFilter.class.getName());
        logger.setLevel(Level.SEVERE);
    }

    private final UserDetailsService userDetailsService;

    private final TokenService tokenService;

    public CustomAuthenticationFilter(final UserDetailsService userDetailsService,
                                      final TokenService tokenService) {
        this.userDetailsService = userDetailsService;
        this.tokenService = tokenService;
    }

    @Override
    protected void doFilterInternal(final HttpServletRequest request,
                                    final HttpServletResponse response,
                                    final FilterChain chain) throws ServletException, IOException {
        try {
            final String authTokenStr = extractTokenFromRequest(request);
            final Token token = tokenService.verifyToken(authTokenStr);

            if (token != null) {
                final UserDetails userDetails = userDetailsService
                        .loadUserByUsername(token.getKey());
                final UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails,
                                null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        catch (final Exception ex) {
            if(logger.isLoggable(Level.SEVERE)) {
                logger.log(Level.SEVERE,
                        "[CustomAuthenticationFilter/doFilterInternal]: authorization failed: ", ex);
            }
        }
        finally {
            chain.doFilter(request, response);
        }
    }

    private String extractTokenFromRequest(final HttpServletRequest request) {
        final String token = request.getHeader(XAUTH_TOKEN_HEADER_NAME);
        if (StringUtils.hasText(token) && token.startsWith(HEADER_PREFIX)) {
            return token.substring(HEADER_PREFIX.length(), token.length());
        }
        return null;
    }
}