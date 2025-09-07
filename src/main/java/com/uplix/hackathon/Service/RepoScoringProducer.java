package com.uplix.hackathon.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uplix.hackathon.Dto.GitJmsListener;
import com.uplix.hackathon.Dto.LiveJmsListener;
import com.uplix.hackathon.Dto.RequestScoringDTO;
import com.uplix.hackathon.Entity.JobScore;
import com.uplix.hackathon.Repository.JobScoreRepo;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@AllArgsConstructor
public class RepoScoringProducer {
    private final JmsTemplate jmsTemplate;
    private final JobScoreRepo jobScoreRepo;

    public UUID requestScoring(RequestScoringDTO dto) throws JsonProcessingException {

        JobScore jobScore = new JobScore();
        jobScore.setEmail(dto.getEmail());
        jobScore.setGithubUrl(dto.getRepoUrl());
        jobScore.setLiveUrl(dto.getLiveUrl());
        JobScore save = jobScoreRepo.save(jobScore);
        UUID jobId = save.getJobId();
        GitJmsListener gitJmsListener = new GitJmsListener(dto.getRepoUrl(), jobId);

        LiveJmsListener liveJmsListener = new LiveJmsListener(dto.getLiveUrl(), jobId);

        jmsTemplate.convertAndSend("uplix/request/score/readme", gitJmsListener);
        jmsTemplate.convertAndSend("uplix/request/score/repo", gitJmsListener);
        jmsTemplate.convertAndSend("uplix/repo/request/lighthouse", liveJmsListener);
        return jobId;
    }
}