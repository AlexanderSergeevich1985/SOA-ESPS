package com.soaesps.auth.controller;

import com.soaesps.auth.dto.ChangePasswordRequest;
import com.soaesps.auth.service.BaseUserDetailsService;
import com.soaesps.core.DataModels.security.BaseUserDetails;

import com.soaesps.core.Utils.HttpUtils;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.security.Principal;

@RestController
@RequestMapping("/accounts")
public class AccountController {
	@Autowired
	private BaseUserDetailsService userDetailsService;

	@PreAuthorize("isAuthenticated()")
	@GetMapping("/load")
	public ResponseEntity<UserDetails> getCurrentUser(Principal principal) {
		UserDetails userDetail = userDetailsService.loadUserByUsername(principal.getName());
		return HttpUtils.onOk(userDetail);
	}

    @PreAuthorize("isAnonymous() or hasRole('ADMIN') or hasRole('SERVICE')")
    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createUserAccount(@Valid @RequestBody BaseUserDetails account) {
        Long id = userDetailsService.createUserAccount(account);
        return HttpUtils.onOk("entity_id", String.valueOf(id));
    }

	@PreAuthorize("isAuthenticated()")
	@PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
	public void updateCurrentUser(Principal principal, @Valid @RequestBody BaseUserDetails account) {
		userDetailsService.updateUserAccount(principal.getName(), account);
	}

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/change_password")
    public void changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userDetailsService.changePassword(request.oldPassword(), request.newPassword());
    }


    @PreAuthorize("hasRole('ADMIN') or hasRole('SERVICE') or (isAuthenticated() and #username == principal.username)")
    @DeleteMapping("/remove/{username}")
	public void removeUser(@PathVariable
                               @Size(min = 5, max = 50, message = "Username must be between 5 and 50 characters")
                               String username) {
		userDetailsService.deleteUserAccount(username);
	}

	@PreAuthorize("hasRole('ADMIN') or hasRole('SERVICE')")
	@GetMapping("/isExist/{username}")
	public Boolean userExists(@PathVariable @Size(min = 5, max = 25, message = "Username cannot be empty") String username) {
		return userDetailsService.userExists(username);
	}
}