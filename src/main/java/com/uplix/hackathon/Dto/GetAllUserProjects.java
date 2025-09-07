package com.uplix.hackathon.Dto;

import lombok.Data;

import java.util.UUID;

@Data
public class GetAllUserProjects {

    private String email;
    private String liveUrl;
    private String gitUrl;
    private UUID jobId;
    private ScoreResponse score;
}
