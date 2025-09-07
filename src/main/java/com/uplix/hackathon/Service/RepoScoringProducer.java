package com.uplix.hackathon.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class RepoScoringProducer {
    private final JmsTemplate jmsTemplate;

    public void requestScoring(String userEmail, String repoUrl) throws JsonProcessingException {
        Map<String, Object> payload = Map.of(
                "userEmail", userEmail,
                "repoUrl", repoUrl
        );
        String message = new ObjectMapper().writeValueAsString(payload);

//        jmsTemplate.convertAndSend("uplix/repo/request/readme", message);
//        jmsTemplate.convertAndSend("uplix/repo/request/code", message);
//        jmsTemplate.convertAndSend("uplix/repo/request/innovation", message);
        jmsTemplate.convertAndSend("uplix/repo/request/lighthouse", message);
    }
}