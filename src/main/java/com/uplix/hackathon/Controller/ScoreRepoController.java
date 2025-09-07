package com.uplix.hackathon.Controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.uplix.hackathon.Dto.GetAllUserProjects;
import com.uplix.hackathon.Dto.RequestScoringDTO;
import com.uplix.hackathon.Dto.ScoreResponse;
import com.uplix.hackathon.Service.RepoScoringProducer;
import com.uplix.hackathon.Service.ScoreResponseService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("hackathon")
@AllArgsConstructor
@Slf4j
public class ScoreRepoController {
    private final RepoScoringProducer producer;
    private final ScoreResponseService scoreResponseService;

    @PostMapping("/score")
    public ResponseEntity<Map<String, String>> scoreRepo(@RequestBody RequestScoringDTO dto) throws JsonProcessingException {
        UUID uuid = producer.requestScoring(dto);
        return ResponseEntity.ok(Map.of("jobId", uuid.toString()));
    }

    @GetMapping("/score/{jobId}")
    public ResponseEntity<ScoreResponse> scoreResponse(@PathVariable UUID jobId)
    {
        ScoreResponse response = scoreResponseService.getResponse(jobId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/getall/{email}")
    public ResponseEntity<List<GetAllUserProjects>> getAllUserProjects(@PathVariable String email) {
        List<GetAllUserProjects> allUserProjects = scoreResponseService.getAllUserProjects(email);
        return new ResponseEntity<>(allUserProjects, HttpStatus.OK);
    }
}
