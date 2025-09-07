package com.uplix.hackathon.Dto;

import java.util.List;

public record ProjectReviewResponse(int project_structure_score, String project_structure_reasoning, List<BusinessLogicFile> recommended_business_logic_files) {
    public record BusinessLogicFile(String file, String justification) {

    }
}
