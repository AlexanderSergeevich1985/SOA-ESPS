package com.soaesps.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soaesps.auth.config.SecurityConfiguration;
import com.soaesps.auth.repository.OAuth2TokenRepository;
import com.soaesps.auth.service.BaseUserDetailsService;
import com.soaesps.auth.service.security.OtpVerificationService;
import com.soaesps.core.DataModels.security.BaseUserDetails;
import jakarta.persistence.EntityManagerFactory;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.validation.annotation.Validated;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Validated
@WebMvcTest(MfaRestController.class)
@Import({SecurityConfiguration.class, AuthenticationFlowTest.TestInfrastructureConfig.class})
@ActiveProfiles("TEST")
@EnableJpaRepositories(basePackages = {})
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false",
        "spring.cloud.config.import-check.enabled=false",
        "spring.main.allow-bean-definition-overriding=true",
        "spring.autoconfigure.exclude=" +
                "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
                "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration," +
                "org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration," +
                "org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration," +
                "com.soaesps.auth.config.HibernateConfiguration"
})
class AuthenticationFlowTest {

    @TestConfiguration
    static class TestConfig {
        @Bean(name = "hibernateConfiguration")
        public Object mockHibernateConfiguration() {
            return new Object();
        }

        @Bean(name = "authServerSecurityFilterChain")
        public Object overrideAuthServerSecurityFilterChain() {
            return new Object();
        }
    }

    @TestConfiguration
    public static class TestInfrastructureConfig {

        @Bean(name = "registeredClientRepository")
        @Primary
        public Object mockRegisteredClientRepository() {
            return Mockito.mock(RegisteredClientRepository.class);
        }

        @Bean(name = "inMemoryRegisteredClientRepository")
        public Object mockInMemoryRegisteredClientRepository() {
            return Mockito.mock(RegisteredClientRepository.class);
        }

        @Bean(name = "entityManagerFactory")
        @Primary
        public Object mockEntityManagerFactory() {
            return Mockito.mock(EntityManagerFactory.class);
        }

        @Bean(name = "transactionManager")
        @Primary
        public Object mockTransactionManager() {
            return Mockito.mock(PlatformTransactionManager.class);
        }
    }

    @MockitoBean(name = "jwtDecoder")
    private JwtDecoder jwtDecoder;

    @MockitoBean(name = "tokenGenerator")
    private OAuth2TokenGenerator<?> tokenGenerator;

    @MockitoBean
    private OAuth2TokenRepository tokenRepository;

    @MockitoBean
    @Qualifier("customAuthenticationProvider")
    private AuthenticationProvider provider;

    @MockitoBean(name = "authorizationService")
    private OAuth2AuthorizationService authorizationService;

    @MockitoBean
    @Qualifier("baseUserDetailsServiceImpl")
    private BaseUserDetailsService userDetailsService;

    @MockitoBean
    private OtpVerificationService otpService;

    @MockitoBean
    @Qualifier("successHandler")
    private AuthenticationSuccessHandler successHandler;

    @MockitoBean
    private AuthenticationFailureHandler failureHandler;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @BeforeEach
    void setup() {
        mapper.disable(com.fasterxml.jackson.databind.MapperFeature.USE_ANNOTATIONS);
        mapper.setVisibility(
                com.fasterxml.jackson.annotation.PropertyAccessor.FIELD,
                com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY
        );

        Authentication successfulAuth = new UsernamePasswordAuthenticationToken(
                "mfa_user",
                "password",
                AuthorityUtils.NO_AUTHORITIES
        );
        Mockito.when(provider.authenticate(any(Authentication.class)))
                .thenReturn(successfulAuth);
    }

    @Nested
    class FormLoginAuthenticationTests {

        //@Test
        void shouldReturnAcceptedStatus_WhenMfaIsEnabledForUser() throws Exception {
            BaseUserDetails mfaUser = new BaseUserDetails();
            mfaUser.setUsername("mfa_user");
            mfaUser.setMfaEnabled(true);

            Mockito.doReturn(mfaUser)
                    .when(userDetailsService).loadUserByUsername("mfa_user");

            mockMvc.perform(post("/login_security_check")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("username", "mfa_user")
                            .param("password", "anyPassword")
                            .with(csrf()))
                    .andExpect(status().isAccepted())
                    .andExpect(jsonPath("$.mfaRequired").value(true))
                    .andExpect(jsonPath("$.tempToken").exists());
        }
    }

    @Nested
    class OtpVerificationEndpointTests {

        @Test
        void shouldIssueOAuth2Tokens_WhenSessionAndOtpAreValid() throws Exception {
            MockHttpSession session = new MockHttpSession();
            org.springframework.security.core.Authentication preAuth =
                    Mockito.mock(org.springframework.security.core.Authentication.class);
            Mockito.when(preAuth.getName()).thenReturn("mfa_user");

            session.setAttribute("MFA_PRE_AUTH", preAuth);
            session.setAttribute("MFA_TEMP_TOKEN", "token-xyz-123");

            Mockito.doReturn(true)
                    .when(otpService).validateCode("mfa_user", "555666");

            OAuth2AccessToken mockToken = Mockito.mock(OAuth2AccessToken.class);
            Mockito.when(mockToken.getTokenValue()).thenReturn("valid-jwt-access-token");
            Mockito.doReturn(mockToken).when(tokenGenerator).generate(any());

            String payload = "{\"code\":\"555666\",\"tempToken\":\"token-xyz-123\"}";

            mockMvc.perform(post("/login/otp/verify")
                            .session(session)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload)
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.access_token").exists())
                    .andExpect(jsonPath("$.access_token").value(Matchers.matchesPattern("^[A-F0-9]{32}$")))
                    .andExpect(jsonPath("$.refresh_token").exists())
                    .andExpect(jsonPath("$.refresh_token").value(Matchers.instanceOf(String.class)))
                    .andExpect(jsonPath("$.refresh_token").value(Matchers.not(Matchers.emptyString())))
                    .andExpect(jsonPath("$.token_type").value("Bearer"));
        }
    }
}
