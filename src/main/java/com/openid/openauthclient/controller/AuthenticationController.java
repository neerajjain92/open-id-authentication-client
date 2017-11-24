package com.openid.openauthclient.controller;

import com.openid.openauthclient.model.OpenIdConnectUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthenticationController {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    @RequestMapping("/authenticate")
    @ResponseBody
    public final String authenticate() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        OpenIdConnectUserDetails userDetails = (OpenIdConnectUserDetails) authentication.getPrincipal();
        logger.info("User Id {}",userDetails.getUserId());
        logger.info("User Name {}",userDetails.getUsername());
        logger.info("Access Token {}",userDetails.getToken());
        return "Welcome, " + userDetails.getUsername();
    }
}
