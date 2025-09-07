package com.uplix.hackathon.Dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class GetWaitlistDTO {
    private List<com.uplix.hackathon.Dto.WaitlistEntryDTO> data;

    @JsonProperty("total_count")
    private int totalCount;
}
