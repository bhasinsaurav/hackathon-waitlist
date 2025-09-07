package com.uplix.hackathon.Entity;

import com.uplix.hackathon.Enum.ServiceStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "job_score")
@Data
public class JobScore {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "job_id", nullable = false, updatable = false)
    private UUID jobId;
    @Column(name = "project_name")
    private String projectName;
    @Column(name = "email")
    private String email;
    @Column(name = "github_url")
    private String githubUrl;

    private String liveUrl;

    private Integer accessibilityScore;
    private Integer seoScore;
    private Integer bestPracticesScore;
    private Integer performanceScore;

    private Integer codeStructureScore;

    private String codeStructureScoreDescription;
    @Column(name = "code_complexity_score")
    private Integer codeComplexityScore;
    private String codeComplexityDescription;
    @Column(name = "readme_score")
    private Integer readmeScore;
    private String readmeScoreDescription;
    @Enumerated(EnumType.STRING)
    @Column(name = "git_score_status")
    private ServiceStatus repoScoreStatus;
    @Enumerated(EnumType.STRING)
    @Column(name = "live_score_status")
    private ServiceStatus liveScoreStatus;
    @Enumerated(EnumType.STRING)
    @Column(name = "readme_score_status")
    private ServiceStatus readmeScoreStatus;



    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    @PrePersist
    void onCreate(){
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate(){
        this.updatedAt = LocalDateTime.now();
    }


}

