package com.uplix.hackathon.Dto;

import java.io.Serializable;
import java.util.UUID;

public record LiveJmsListener(String liveUrl, UUID jobId){
}
