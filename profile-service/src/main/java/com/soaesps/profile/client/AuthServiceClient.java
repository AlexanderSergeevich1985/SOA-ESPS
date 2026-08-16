package com.soaesps.profile.client;

import com.soaesps.core.DataModels.security.BaseUserDetails;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * Feign client for communicating with the auth-service microservice.
 */
@FeignClient(name = "auth-service")
public interface AuthServiceClient {
    @GetMapping(value = "/accounts/{name}", produces = MediaType.APPLICATION_JSON_VALUE)
    BaseUserDetails getUserDetailsByName(@PathVariable("name") String name);

    @PostMapping(value = "/accounts/creation", consumes = MediaType.APPLICATION_JSON_VALUE)
    void createNewUser(@Valid @RequestBody BaseUserDetails userDetails);

    @DeleteMapping(value = "/accounts/{name}/removing")
    void removeUser(@PathVariable("name") String name);
}