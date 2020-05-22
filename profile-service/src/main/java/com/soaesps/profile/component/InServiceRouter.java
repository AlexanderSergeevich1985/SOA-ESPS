package com.soaesps.profile.component;

import com.soaesps.core.DataModels.security.BaseUserDetails;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import java.io.IOException;

@Component
public interface InServiceRouter {
    UserDetails getUserDetailsByName(@PathVariable String name) throws IOException;

    boolean createNewUser(@Valid @RequestBody BaseUserDetails userDetails);

    boolean removeUser(@PathVariable String name);
}