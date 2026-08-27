package com.soaesps.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soaesps.auth.config.SecurityConfiguration;
import com.soaesps.auth.dto.ChangePasswordRequest;
import com.soaesps.auth.repository.OAuth2TokenRepository;
import com.soaesps.auth.service.BaseUserDetailsService;
import com.soaesps.core.DataModels.security.BaseUserDetails;
import com.soaesps.core.Utils.JsonUtil;
import com.soaesps.core.security.repository.AuthAuditRepository;

import jakarta.persistence.EntityManagerFactory;
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
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.validation.annotation.Validated;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Validated
@WebMvcTest(AccountController.class)
@Import({SecurityConfiguration.class, AccountControllerTest.TestInfrastructureConfig.class})
@ActiveProfiles("TEST")
@EnableJpaRepositories(basePackages = {})
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false",
        "spring.cloud.config.import-check.enabled=false",
        "spring.main.allow-bean-definition-overriding=true",
        //"spring.context.checkpoint=false",
        "spring.autoconfigure.exclude=" +
                "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
                "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration," +
                "org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration," +
                "org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration," +
                "com.soaesps.auth.config.HibernateConfiguration"
})
class AccountControllerTest {
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

    @MockitoBean
    private org.springframework.security.oauth2.jwt.JwtDecoder jwtDecoder;

    @MockitoBean
    private AuthAuditRepository authAuditRepository;

    @MockitoBean
    @Qualifier("baseUserDetailsServiceImpl")
    private BaseUserDetailsService userDetailsService;

    @MockitoBean
    private OAuth2TokenRepository tokenRepository;

    @MockitoBean
    @Qualifier("customAuthenticationProvider")
    private AuthenticationProvider provider;

    @MockitoBean
    private AuthenticationFailureHandler failureHandler;

    @MockitoBean
    private AuthenticationSuccessHandler successHandler;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        Mockito.doReturn(getTestUserDetails())
                .when(userDetailsService).loadUserByUsername(Mockito.anyString());
    }

    /* =========================================================================
       1. GET /accounts/load
       ========================================================================= */
    @Nested
    class GetCurrentUserTests {
        @Test
        void shouldReturnUserDetails_WhenAdminAuthenticated() throws Exception {
            mockMvc.perform(get("/accounts/load")
                            .with(user("testUser").roles("ADMIN")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value("test_user"));
        }

        @Test
        void shouldReturnUnauthorized_WhenUserNotAuthenticated() throws Exception {
            mockMvc.perform(get("/accounts/load")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void shouldRedirectToLogin_WhenBrowserNotAuthenticated() throws Exception {
            mockMvc.perform(get("/accounts/load")
                            .accept(MediaType.TEXT_HTML))
                    .andExpect(status().isFound())
                    .andExpect(redirectedUrlPattern("**/login"));
        }
    }

    /* =========================================================================
       2. POST /accounts/create
       ========================================================================= */
    @Nested
    class CreateUserAccountTests {

        @Test
        void shouldReturnOk_WhenAnonymousUserRegisters() throws Exception {
            Mockito.doReturn(1L).when(userDetailsService).createUserAccount(Mockito.any());
            String content = JsonUtil.toString(getTestUserDetails());

            mockMvc.perform(post("/accounts/create")
                            .with(anonymous())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(content))
                    .andDo(print())
                    .andExpect(status().isOk());
        }

        @Test
        void shouldReturnOk_WhenAdminCreatesUser() throws Exception {
            BaseUserDetails account = new BaseUserDetails();
            account.setUsername("operatorX");

            Mockito.when(userDetailsService.createUserAccount(any(BaseUserDetails.class))).thenReturn(200L);

            mockMvc.perform(post("/accounts/create")
                            .with(user("adminUser").roles("ADMIN"))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(account)))
                    .andExpect(status().isOk());
        }

        @Test
        void shouldReturnOk_WhenMicroserviceRegistersViaMtls() throws Exception {
            BaseUserDetails account = new BaseUserDetails();
            account.setUsername("systemUser");

            Mockito.when(userDetailsService.createUserAccount(any(BaseUserDetails.class))).thenReturn(300L);

            mockMvc.perform(post("/accounts/create")
                            .with(user("payments-service").roles("SERVICE"))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(account)))
                    .andExpect(status().isOk());
        }

        @Test
        void shouldReturnForbidden_WhenRegularAuthenticatedUserTriesToRegister() throws Exception {
            BaseUserDetails account = new BaseUserDetails();
            account.setUsername("illegalDouble");

            mockMvc.perform(post("/accounts/create")
                            .with(user("regularUser").roles("USER"))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(account)))
                    .andExpect(status().isForbidden());
        }
    }

    /* =========================================================================
       3. PUT /accounts/update
       ========================================================================= */
    @Nested
    class UpdateCurrentUserTests {

        @Test
        void shouldReturnOk_WhenAdminAuthenticated() throws Exception {
            String validBody = "{\"username\":\"testUser\",\"email\":\"test@soa-esps.ru\"}";

            mockMvc.perform(put("/accounts/update")
                            .with(user("testUser").roles("ADMIN"))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validBody))
                    .andExpect(status().isOk());
        }

        @Test
        void shouldReturnOk_WhenRegularUserAuthenticated() throws Exception {
            String validBody = "{\"username\":\"testUser\",\"email\":\"test@soa-esps.ru\"}";

            mockMvc.perform(put("/accounts/update")
                            .with(user("testUser").roles("USER"))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validBody))
                    .andExpect(status().isOk());
        }

        @Test
        void shouldReturnUnauthorized_WhenUserIsNotAuthenticated() throws Exception {
            String validBody = "{\"username\":\"testUser\",\"email\":\"test@soa-esps.ru\"}";

            mockMvc.perform(put("/accounts/update")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validBody))
                    .andExpect(status().isUnauthorized());
        }
    }

    /* =========================================================================
       4. PUT /accounts/change_password
       ========================================================================= */
    @Nested
    class ChangePasswordTests {
        @Test
        void shouldReturnOk_WhenHeadersAreProvidedByAdmin() throws Exception {
            ChangePasswordRequest request = new ChangePasswordRequest("oldPassword", "newPassword");

            mockMvc.perform(put("/accounts/change_password")
                            .with(user("testUser").roles("ADMIN"))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }

        @Test
        void shouldReturnBadRequest_WhenRequestBodyIsMissing() throws Exception {
            mockMvc.perform(put("/accounts/change_password")
                            .with(user("testUser").roles("ADMIN"))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }
    }

    /* =========================================================================
       5. DELETE /accounts/remove
       ========================================================================= */
    @Nested
    class RemoveUserTests {

        @Test
        void shouldReturnOk_WhenAdminAuthenticated() throws Exception {
            mockMvc.perform(delete("/accounts/remove/victimUser")
                            .with(user("adminUser").roles("ADMIN"))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        void shouldReturnOk_WhenRegularUserDeletesOwnAccount() throws Exception {
            mockMvc.perform(delete("/accounts/remove/testUser")
                            .with(user("testUser").roles("USER"))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        void shouldReturnUnauthorized_WhenUserIsNotAuthenticated() throws Exception {
            mockMvc.perform(delete("/accounts/remove")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void shouldReturnForbidden_WhenUserTriesToDeleteAnotherAccount() throws Exception {
            mockMvc.perform(delete("/accounts/remove/victimUser")
                            .with(user("attackerUser").roles("USER"))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

        @Test
        void shouldReturnBadRequest_WhenUsernameIsTooShort() throws Exception {
            mockMvc.perform(delete("/accounts/remove/user")
                            .with(user("adminUser").roles("ADMIN"))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturnBadRequest_WhenUsernameIsTooLong() throws Exception {
            String tooLongUsername = "a".repeat(51);

            mockMvc.perform(delete("/accounts/remove/" + tooLongUsername)
                            .with(user("adminUser").roles("ADMIN"))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }
    }

    /* =========================================================================
       6. GET /accounts/isExist/{username}
       ========================================================================= */
    @Nested
    class IsExistTests {
        @Test
        void shouldReturnOk_WhenUserIsAdmin() throws Exception {
            mockMvc.perform(get("/accounts/isExist/{username}", "testUser")
                            .with(user("testUser").roles("ADMIN")))
                    .andExpect(status().isOk());
        }

        @Test
        void shouldReturnForbidden_WhenUserHasNoAdminRole() throws Exception {
            mockMvc.perform(get("/accounts/isExist/{username}", "testUser")
                            .with(user("testUser").roles("USER")))
                    .andExpect(status().isForbidden());
        }

        @Test
        void shouldReturnBadRequest_WhenUsernameIsEmpty() throws Exception {
            mockMvc.perform(get("/accounts/isExist/{username}", " ")
                            .with(user("testUser").roles("ADMIN")))
                    .andExpect(status().isBadRequest());
        }
    }

    private BaseUserDetails getTestUserDetails() {
        final BaseUserDetails user = new BaseUserDetails();
        user.setUsername("test_user");
        user.setPassword("password");
        return user;
    }
}