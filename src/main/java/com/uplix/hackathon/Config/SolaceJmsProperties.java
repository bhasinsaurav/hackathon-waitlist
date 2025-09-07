package com.uplix.hackathon.Config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "solace.jms")
public class SolaceJmsProperties {
    private String host;
    private String vpn;
    private String username;
    private String password;
}
