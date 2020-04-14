package com.soaesps.profile.component.Impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soaesps.core.DataModels.security.BaseUserDetails;
import com.soaesps.core.integration.IntegrationConstant;
import com.soaesps.profile.component.InServiceRouter;
import com.soaesps.profile.service.ProfileServiceImpl;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class InServiceRouterImpl implements InServiceRouter {
    static private final Logger logger;

    static {
        logger = Logger.getLogger(ProfileServiceImpl.class.getName());
        logger.setLevel(Level.INFO);
    }

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper mapper;

    @Override
    public UserDetails getUserDetailsByName(@PathVariable String name)
            throws IOException {
        final byte[] response = (byte []) rabbitTemplate.
                convertSendAndReceive(
                        IntegrationConstant.Exchanges.USER_DETAILS_OUEUE.getExchangeName(),
                        IntegrationConstant.Exchanges.USER_DETAILS_OUEUE.getRouteKey(),
                        name);
        if (response == null) {
            return null;
        }

        return mapper.readValue(response, UserDetails.class);
    }

    @Override
    public boolean createNewUser(@Valid @RequestBody BaseUserDetails userDetails) {
        final byte[] response = (byte []) rabbitTemplate.
                convertSendAndReceive(
                        IntegrationConstant.Exchanges.USER_DETAILS_OUEUE.getExchangeName(),
                        IntegrationConstant.Exchanges.USER_DETAILS_OUEUE.getRouteKey(),
                        userDetails);

        return response != null;
    }

    @Override
    public boolean removeUser(@PathVariable String name) {
        final byte[] response = (byte []) rabbitTemplate.
                convertSendAndReceive(
                        IntegrationConstant.Exchanges.USER_DETAILS_OUEUE.getExchangeName(),
                        IntegrationConstant.Exchanges.USER_DETAILS_OUEUE.getRouteKey(),
                        name);

        return response != null;
    }
}