package com.soaesps.auth.controller;

import com.soaesps.auth.service.BaseUserDetailsService;
import com.soaesps.core.DataModels.security.BaseUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
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

	@PreAuthorize("#oauth2.clientHasRole('admin') and #oauth2.hasScope('server')")
	@GetMapping("/{name}")
	public UserDetails getUserDetailsByName(@PathVariable String name) {
		return userDetailsService.loadUserByUsername(name);
	}

	@GetMapping("/current")
	public UserDetails getCurrentUser(Principal principal) {
		return userDetailsService.loadUserByUsername(principal.getName());
	}

	@PutMapping("/current")
	public void updateCurrentUser(Principal principal, @Valid @RequestBody BaseUserDetails account) {
		userDetailsService.updateUserAccount(principal.getName(), account);
	}

	@PreAuthorize("#oauth2.clientHasRole('admin') and #oauth2.hasScope('server')")
	@PostMapping("/creation")
	public void createNewUser(@Valid @RequestBody BaseUserDetails userDetails) {
		userDetailsService.createUserAccount(userDetails);
	}

	@PreAuthorize("#oauth2.clientHasRole('admin') and #oauth2.hasScope('server')")
	@PostMapping("/{name}/removing")
	public void createNewUser(@PathVariable String name) {
		userDetailsService.deleteUserAccount(name);
	}
}