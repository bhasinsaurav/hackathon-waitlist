package com.uplix.hackathon.Repository;

import com.uplix.hackathon.Entity.JobScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JobScoreRepo extends JpaRepository<JobScore, UUID> {
    List<JobScore> findAllByEmail(String email);
}
