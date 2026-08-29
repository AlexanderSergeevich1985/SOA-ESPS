package com.soaesps.auth.service;

import com.soaesps.auth.repository.UserDetailsRepository;
import com.soaesps.core.DataModels.security.BaseUserDetails;
import com.soaesps.core.exception.UserAlreadyExistAuthException;
import com.soaesps.core.security.checker.BaseUserDetailsChecker;
import com.soaesps.core.security.util.SecurityHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserDetailsService Test Suite")
public class UserDetailsServiceTest {

    // Enable experimental support for Java 25 before Mockito initializes Byte Buddy
    static {
        System.setProperty("net.bytebuddy.experimental", "true");
    }

    @Mock
    private UserDetailsRepository userDetailsRepository;

    @Mock
    private BaseUserDetailsChecker baseUserDetailsChecker; // Required dependency mock

    @InjectMocks // Mockito injects mocks into the concrete implementation class
    private BaseUserDetailsServiceImpl baseUserDetailsService;

    private BaseUserDetails testUser;
    private MockedStatic<SecurityHelper> mockedSecurityHelper; // Used for mocking SecurityHelper static methods

    @BeforeEach
    void setUp() {
        testUser = new BaseUserDetails();
        testUser.setId(1L); // Explicitly set ID as service layer performs type casting
        testUser.setUsername("testUser");
        testUser.setPassword("password");
    }

    @AfterEach
    void tearDown() {
        if (mockedSecurityHelper != null) {
            mockedSecurityHelper.close(); // Mandatory cleanup for static mocks
        }
    }

    @Nested
    @DisplayName("User Lookup (loadUserByUsername)")
    class LoadUserByUsernameTests {

        @Test
        @DisplayName("Should throw exception when user is not found")
        void shouldThrowExceptionWhenUserNotFound() {
            when(userDetailsRepository.findByUsername("unknownUser"))
                    .thenReturn(Optional.empty());

            assertThrows(UsernameNotFoundException.class, () -> {
                baseUserDetailsService.loadUserByUsername("unknownUser");
            });
        }

        @Test
        @DisplayName("Should successfully return user details when user exists")
        void shouldReturnUserDetailsWhenUserExists() {
            when(userDetailsRepository.findByUsername("testUser"))
                    .thenReturn(Optional.of(testUser));

            UserDetails result = baseUserDetailsService.loadUserByUsername("testUser");

            assertNotNull(result);
            assertEquals("testUser", result.getUsername());
        }
    }

    @Nested
    @DisplayName("Account Management (CRUD - Set 1)")
    class AccountManagementTests {

        @Test
        @DisplayName("Should successfully create an account")
        void shouldCreateUserAccount() {
            when(userDetailsRepository.findByUsername("testUser")).thenReturn(Optional.empty());
            when(userDetailsRepository.save(any(BaseUserDetails.class))).thenReturn(testUser);

            Long id = baseUserDetailsService.createUserAccount(testUser);

            assertEquals(1L, id);
            verify(userDetailsRepository, times(1)).save(testUser);
        }

        @Test
        @DisplayName("Should throw exception during creation if profile already exists")
        void shouldThrowExceptionWhenProfileExists() {
            when(userDetailsRepository.findByUsername("testUser")).thenReturn(Optional.of(testUser));

            assertThrows(IllegalArgumentException.class, () -> {
                baseUserDetailsService.createUserAccount(testUser);
            });
        }

        @Test
        @DisplayName("Should successfully update an account if it exists")
        void shouldUpdateUserAccount() {
            when(userDetailsRepository.findByUsername("testUser"))
                    .thenReturn(Optional.of(testUser));

            boolean isUpdated = baseUserDetailsService.updateUserAccount("testUser", testUser);

            assertTrue(isUpdated);
            verify(userDetailsRepository, times(1)).save(testUser);
        }

        @Test
        @DisplayName("Should successfully delete an existing account")
        void shouldDeleteUserAccount() {
            when(userDetailsRepository.findByUsername("testUser"))
                    .thenReturn(Optional.of(testUser));

            boolean isDeleted = baseUserDetailsService.deleteUserAccount("testUser");

            assertTrue(isDeleted);
            verify(userDetailsRepository, times(1)).delete(testUser);
        }
    }

    @Nested
    @DisplayName("Standard Spring Security Methods (CRUD - Set 2)")
    class SpringSecurityCrudTests {

        @Test
        @DisplayName("createUser: should throw exception if user already exists")
        void shouldThrowExceptionWhenUserAlreadyExists() {
            doNothing().when(baseUserDetailsChecker).check(any());
            when(userDetailsRepository.findByUsername("testUser")).thenReturn(Optional.of(testUser));

            assertThrows(UserAlreadyExistAuthException.class, () -> {
                baseUserDetailsService.createUser(testUser);
            });
        }

        @Test
        @DisplayName("deleteUser: should delete by ID if user is found")
        void shouldDeleteUserById() {
            when(userDetailsRepository.findByUsername("testUser")).thenReturn(Optional.of(testUser));

            baseUserDetailsService.deleteUser("testUser");

            verify(userDetailsRepository, times(1)).deleteById(1L);
        }
    }

    @Nested
    @DisplayName("Security and Verifications")
    class SecurityAndChecksTests {

        @Test
        @DisplayName("Should successfully change password for the current user")
        void shouldChangePassword() {
            // Initialize static mock for Spring Security utility class
            mockedSecurityHelper = mockStatic(SecurityHelper.class);
            mockedSecurityHelper.when(SecurityHelper::getCurrentLogin).thenReturn("testUser");

            when(userDetailsRepository.findByUsername("testUser")).thenReturn(Optional.of(testUser));

            baseUserDetailsService.changePassword("oldPassword", "newTestPassword");

            assertEquals("newTestPassword", testUser.getPassword());
            verify(userDetailsRepository, times(1)).save(testUser);
        }

        @Test
        @DisplayName("Should return true if user exists")
        void shouldReturnTrueWhenUserExists() {
            when(userDetailsRepository.findByUsername("testUser"))
                    .thenReturn(Optional.of(testUser));

            boolean exists = baseUserDetailsService.userExists("testUser");

            assertTrue(exists);
        }
    }
}