package com.soaesps.profile.client;

import com.soaesps.core.DataModels.security.BaseUserDetails;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@FeignClient(name = "auth-service")
public interface AuthServiceClient {
    @RequestMapping(method = RequestMethod.GET, value = "/accounts/{name}", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    UserDetails getUserDetailsByName(@PathVariable String name);

    @RequestMapping(method = RequestMethod.POST, value = "/accounts/creation", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    void createNewUser(@Valid @RequestBody BaseUserDetails userDetails);

    @RequestMapping(method = RequestMethod.DELETE, value = "/accounts/{name}/removing", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    void removeUser(@PathVariable String name);
}