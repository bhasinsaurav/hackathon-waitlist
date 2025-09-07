package com.uplix.hackathon.Service;

import com.uplix.hackathon.Dto.GetAllUserProjects;
import com.uplix.hackathon.Dto.ScoreResponse;
import com.uplix.hackathon.Entity.JobScore;
import com.uplix.hackathon.Enum.ServiceStatus;
import com.uplix.hackathon.Repository.JobScoreRepo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ScoreResponseService {

    private final JobScoreRepo jobScoreRepo;

    public ScoreResponseService(JobScoreRepo jobScoreRepo) {
        this.jobScoreRepo = jobScoreRepo;

    }

    public ScoreResponse getResponse(UUID jobId) {
        Optional<JobScore> byId = jobScoreRepo.findById(jobId);
        ScoreResponse scoreResponse = new ScoreResponse();
        JobScore jobScore;
        if(byId.isPresent()) {
            jobScore=byId.get();
        }
        else{
            throw new IllegalStateException("Job Score not found");
        }
        ServiceStatus liveScoreStatus = jobScore.getLiveScoreStatus();
        ServiceStatus repoScoreStatus = jobScore.getRepoScoreStatus();
        ServiceStatus readmeScoreStatus = jobScore.getReadmeScoreStatus();
        if(liveScoreStatus != ServiceStatus.COMPLETED || repoScoreStatus != ServiceStatus.COMPLETED || readmeScoreStatus != ServiceStatus.COMPLETED) {
            scoreResponse.setStatus("in_progress");
            return scoreResponse;
        }
        else{
            scoreResponse.setStatus("completed");
        }

        scoreResponse.setAccessibilityScore(jobScore.getAccessibilityScore());
        scoreResponse.setCodeScore(jobScore.getCodeComplexityScore());
        scoreResponse.setReadmeScore(jobScore.getReadmeScore());
        scoreResponse.setPerformanceScore(jobScore.getPerformanceScore());
        scoreResponse.setSeoScore(jobScore.getSeoScore());
        scoreResponse.setBestPracticeScore(jobScore.getBestPracticesScore());
        scoreResponse.setCodeReasoning(jobScore.getCodeComplexityDescription());
        scoreResponse.setRepoReasoning(jobScore.getCodeStructureScoreDescription());
        scoreResponse.setReadmeReasoning(jobScore.getReadmeScoreDescription());
        scoreResponse.setRepoScore(jobScore.getCodeStructureScore());

        return scoreResponse;
    }

    public List<GetAllUserProjects> getAllUserProjects(String email){
        List<JobScore> allByEmail = jobScoreRepo.findAllByEmail(email);

        List<GetAllUserProjects> list = allByEmail.stream()
                .map(this::getResponse)  // method reference
                .toList();

        return list;


    }

    public GetAllUserProjects getResponse(JobScore jobScore) {

        ScoreResponse scoreResponse = new ScoreResponse();
        GetAllUserProjects getAllUserProjects = new GetAllUserProjects();
        getAllUserProjects.setEmail(jobScore.getEmail());
        getAllUserProjects.setGitUrl(jobScore.getGithubUrl());
        getAllUserProjects.setLiveUrl(jobScore.getLiveUrl());
        scoreResponse.setStatus("completed");
        scoreResponse.setAccessibilityScore(jobScore.getAccessibilityScore());
        getAllUserProjects.setJobId(jobScore.getJobId());
        scoreResponse.setCodeScore(jobScore.getCodeComplexityScore());
        scoreResponse.setReadmeScore(jobScore.getReadmeScore());
        scoreResponse.setPerformanceScore(jobScore.getPerformanceScore());
        scoreResponse.setSeoScore(jobScore.getSeoScore());
        scoreResponse.setBestPracticeScore(jobScore.getBestPracticesScore());
        scoreResponse.setCodeReasoning(jobScore.getCodeComplexityDescription());
        scoreResponse.setRepoReasoning(jobScore.getCodeStructureScoreDescription());
        scoreResponse.setReadmeReasoning(jobScore.getReadmeScoreDescription());
        scoreResponse.setRepoScore(jobScore.getCodeStructureScore());

        getAllUserProjects.setScore(scoreResponse);
        return getAllUserProjects;
    }
}
