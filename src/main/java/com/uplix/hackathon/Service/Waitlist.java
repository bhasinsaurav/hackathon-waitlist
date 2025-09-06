package com.uplix.hackathon.Service;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service

public class Waitlist {

    @Value("${clerk.secret.key}")
    private String CLERK_SECRET_KEY;


    public boolean sendEmailToClerk(HashMap<String, String> emailMap) throws UnirestException {
        String userEmail = emailMap.get("email");
        if (userEmail == null || userEmail.isBlank()) {
            log.warn("email is null/blank");
            return false;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("email_address", userEmail);
        body.put("notify", true);

        final String API_VERSION = "2025-03-12"; // use a valid Clerk API version
        String jsonBody = String.format("{\"email_address\":\"%s\",\"notify\":true}", userEmail);


        HttpResponse<String> string = Unirest.post("https://api.clerk.com/v1/waitlist_entries")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer "+ CLERK_SECRET_KEY)
                .body(jsonBody)
                .asString();

        log.info("string: {}", string.getBody());

        return true;
    }




}
