package com.soaesps.auth.controller;

import com.soaesps.auth.service.BaseUserDetailsService;
import com.soaesps.core.DataModels.security.BaseUserDetails;

import com.soaesps.core.Utils.HttpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
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
		ResponseEntity<UserDetails> resp = HttpUtils.onOk(userDetail);

		return HttpUtils.onOk(userDetail);
	}

	@PreAuthorize("permitAll()")
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
	public void changePassword(@RequestHeader("old_password") String oldPassword, @RequestHeader("new_password") String newPassword) {
		userDetailsService.changePassword(oldPassword, newPassword);
	}

	@PreAuthorize("isAuthenticated()")
	@DeleteMapping("/remove")
	public void removeUser(Principal principal) {
		userDetailsService.deleteUserAccount(principal.getName());
	}

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/isExist/{username}")
	public Boolean userExists(@PathVariable String username) {
		return userDetailsService.userExists(username);
	}
}