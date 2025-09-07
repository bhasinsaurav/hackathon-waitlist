package com.uplix.hackathon.Dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WaitlistEntryDTO {
    private String id;

    @JsonProperty("email_address")
    private String emailAddress;

    private String status;

    @JsonProperty("is_locked")
    private boolean isLocked;

    @JsonProperty("created_at")
    private long createdAt;

    @JsonProperty("updated_at")
    private long updatedAt;

    private Object invitation; // can refine later
}
