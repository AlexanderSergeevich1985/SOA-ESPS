package com.soaesps.core.filter;

import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class BaseSecurityContextPersistenceFilter implements Filter {
    private String rolePrefix = "ROLE_";

    private MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();

    private Set<String> endpoints = new HashSet<>();

    private Set<String> allowedRoles = new HashSet<>();

    private String redirectUrl;

    private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    public void setEndpoints(final Set<String> endpoints) {
        if(endpoints == null || endpoints.isEmpty()) return;
        this.endpoints = endpoints.stream().filter(e -> (e != null && e.matches("^(/)?([^/\\s]+(/)?)+$"))).collect(Collectors.toSet());
    }

    public void setAllowedRoles(final Set<String> allowedRoles) {
        if(allowedRoles == null || allowedRoles.isEmpty()) return;
        this.allowedRoles = allowedRoles.stream().filter(r -> isValidRole(r)).collect(Collectors.toSet());
    }

    public boolean isValidRole(final String role) {
        return role != null && role.startsWith(this.rolePrefix);
    }

    public void setRedirectUrl(final String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public void setRedirectStrategy(final RedirectStrategy redirectStrategy) {
        this.redirectStrategy = redirectStrategy;
    }

    @Override
    public void init(FilterConfig fc) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        if (req == null || res == null || chain == null) {
            throw new IllegalArgumentException("Cannot pass null values to constructor");
        }

        if (endpoints.isEmpty() || allowedRoles.isEmpty()) {
            chain.doFilter(req, res);
        }

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        final String contextPath = request.getContextPath();

        if (endpoints.stream().anyMatch(e -> e.equalsIgnoreCase(contextPath))) {
            final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null) {
                this.onCredentialsNotFound(this.messages.getMessage("AbstractSecurityInterceptor.authenticationNotFound", "An Authentication object was not found in the SecurityContext"), null, null);
            }

            Collection<? extends GrantedAuthority> roles = authentication.getAuthorities();
            if (!allowedRoles.stream().anyMatch(roles::contains)) {
                this.onAccessDenied(this.messages.getMessage("AbstractAccessDecisionManager.accessDenied", "Access is denied"));
            }
        }
        chain.doFilter(req, res);
    }

    @Override
    public void destroy() {
    }

    private void onCredentialsNotFound(final String reason, Object secureObject, Collection<ConfigAttribute> configAttribs) {
        AuthenticationCredentialsNotFoundException exception = new AuthenticationCredentialsNotFoundException(reason);
        //AuthenticationCredentialsNotFoundEvent event = new AuthenticationCredentialsNotFoundEvent(secureObject, configAttribs, exception);
        throw exception;
    }

    private void onAccessDenied(final String reason) throws AccessDeniedException {
        throw new AccessDeniedException(this.messages.getMessage("AbstractAccessDecisionManager.accessDenied", "Access is denied"));
    }
}