package com.soaesps.core.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

public class JwtTokenConverterFactory implements ConverterFactory<Jwt, AbstractAuthenticationToken> {
    private final String resourceId;

    public JwtTokenConverterFactory(String resourceId) {
        this.resourceId = resourceId;
    }
    @Override
    public <T extends AbstractAuthenticationToken> Converter<Jwt, T> getConverter(Class<T> tClass) {
        return new JwtTokenConverterFactory.JwtToAuthTokenConverter<>(resourceId, tClass);
    }

    private static class JwtToAuthTokenConverter<T extends AbstractAuthenticationToken> implements Converter<Jwt, T> {
        private static final String ROLE_PREFIX = "ROLE_";

        private final String resourceId;

        private Class<T> tClass;

        public JwtToAuthTokenConverter(String resourceId, Class<T> tClass) {
            this.resourceId = resourceId;
            this.tClass = tClass;
        }

        @Override
        public T convert(Jwt token) {
            Collection<GrantedAuthority> authorities = extractResourceRoles(token, resourceId)
                    .stream()
                    .map(r -> (GrantedAuthority)r)
                    .collect(Collectors.toList());

            return createToken(authorities);
        }

        protected T createToken(Collection<GrantedAuthority> authorities) {
            T authToken = null;
            try {
                authToken = tClass.getConstructor(new Class[] {authorities.getClass()}).newInstance(authorities);
                authToken.setAuthenticated(true);
            } catch (Exception ex) {
            }

            return authToken;
        }

        private static Collection<? extends GrantedAuthority> extractResourceRoles(final Jwt jwt, final String resourceId) {
            Map<String, Map<String, Collection<String>>> resourceAccess = jwt.getClaim("resource_access");
            Map<String, Collection<String>> resource;
            Collection<String> resourceRoles;
            if (resourceAccess != null && (resource = resourceAccess.get(resourceId)) != null &&
                    (resourceRoles = resource.get("roles")) != null) {
                return resourceRoles.stream()
                        .map(x -> new SimpleGrantedAuthority(ROLE_PREFIX + x))
                        .collect(Collectors.toSet());
            }

            return Collections.emptySet();
        }
    }
}