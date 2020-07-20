package com.soaesps.core.security.util;

import com.soaesps.core.DataModels.security.AuthAudit;
import com.soaesps.core.Utils.DateTimeHelper;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SecurityHelper {
    private SecurityHelper() {}

    public static String getCurrentLogin() {
        final Authentication authentication = getUserAuthentication();
        String login = null;
        if(authentication != null && !(authentication instanceof AnonymousAuthenticationToken)) {
            final Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails) {
                login = ((UserDetails)principal).getUsername();
            } else {
                login = principal.toString();
            }
        }

        return login;
    }

    public static Authentication getUserAuthentication() {
        final SecurityContext context = SecurityContextHolder.getContext();

        return context.getAuthentication();
    }

    public static Collection<? extends GrantedAuthority> getGrantedAuthorities() {
        return getUserAuthentication().getAuthorities();
    }

    public static Collection<String> getCurrentUserRoles() {
        return getGrantedAuthorities().stream()
                .filter(Objects::nonNull)
                .map(Object::toString)
                .filter(s -> !s.trim().isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }

    public static boolean userHasRoles(final String... roles) {
        final Collection<String> userRoles = getCurrentUserRoles();

        return Stream.of(roles).anyMatch(userRoles::contains);
    }

    public static AuthAudit initAuthAudit() {
        final AuthAudit authAudit = new AuthAudit();
        authAudit.setActionDate(DateTimeHelper.getLocalCurrentTime());
        authAudit.setLogonName(getCurrentLogin());

        return authAudit;
    }
}