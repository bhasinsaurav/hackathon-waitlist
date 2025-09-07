package com.uplix.hackathon.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScoreResponse {
    private String status;
    private int repoScore;
    private int codeScore;
    private int readmeScore;
    private int accessibilityScore;
    private int seoScore;
    private int bestPracticeScore;
    private int performanceScore;
    private String repoReasoning;
    private String codeReasoning;
    private String readmeReasoning;
}