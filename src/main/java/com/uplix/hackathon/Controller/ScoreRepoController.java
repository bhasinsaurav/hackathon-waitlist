package com.uplix.hackathon.Controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.uplix.hackathon.Dto.RequestScoringDTO;
import com.uplix.hackathon.Service.RepoScoringProducer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("hackathon")
@AllArgsConstructor
@Slf4j
public class ScoreRepoController {
    private final RepoScoringProducer producer;

    @PostMapping("/score")
    public ResponseEntity<String> scoreRepo(@RequestBody RequestScoringDTO dto) throws JsonProcessingException {
        String userEmail = "harkaransohal@gmail.com";
        producer.requestScoring(userEmail, dto.getRepoUrl());
        return ResponseEntity.ok("Scoring request sent for repo: " + dto.getRepoUrl());
    }
}
