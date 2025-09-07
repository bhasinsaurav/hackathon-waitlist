package com.uplix.hackathon.Config;

import com.mashape.unirest.http.Unirest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient getRestClient() {
        return RestClient.builder()
                .baseUrl("https://api.github.com/")
                .build();
    }
}
