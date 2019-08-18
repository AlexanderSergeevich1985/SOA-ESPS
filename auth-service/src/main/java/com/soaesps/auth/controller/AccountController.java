package com.soaesps.auth.controller;

import com.soaesps.auth.domain.CustomAuthenticationToken;
import com.soaesps.auth.service.AuthenticationService;
import com.soaesps.auth.service.BaseUserDetailsService;
import com.soaesps.core.DataModels.security.BaseUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletRequest;
import javax.validation.Valid;
import java.security.Principal;

@RestController
@RequestMapping("/accounts")
public class AccountController {
	@Autowired
	private BaseUserDetailsService userDetailsService;

	@Autowired
	private AuthenticationService authenticationService;

	@PreAuthorize("permitAll()")
	@PostMapping(value = "/authenticate", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<OAuth2AccessToken> authenticate(ServletRequest servletRequest, @RequestBody CustomAuthenticationToken token) {
		final OAuth2AccessToken accessToken = authenticationService.authorize(token);

		return new ResponseEntity<>(accessToken, HttpStatus.OK);
	}


	@PreAuthorize("#oauth2.clientHasRole('admin') or #oauth2.hasScope('server')")
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

	@PreAuthorize("#oauth2.clientHasRole('admin') or #oauth2.hasScope('server')")
	@PostMapping("/creation")
	public void createNewUser(@Valid @RequestBody BaseUserDetails userDetails) {
		userDetailsService.createUserAccount(userDetails);
	}

	@PreAuthorize("#oauth2.clientHasRole('admin') or #oauth2.hasScope('server')")
	@DeleteMapping("/{name}/removing")
	public void removeUser(@PathVariable String name) {
		userDetailsService.deleteUserAccount(name);
	}
}