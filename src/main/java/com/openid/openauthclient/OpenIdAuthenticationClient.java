package com.openid.openauthclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;

@SpringBootApplication
public class OpenIdAuthenticationClient extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(OpenIdAuthenticationClient.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(OpenIdAuthenticationClient.class, args);
    }
}
