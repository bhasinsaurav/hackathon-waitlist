package com.uplix.hackathon.Dto;

import java.io.Serializable;
import java.util.UUID;

public record GitJmsListener(String repoUrl, UUID jobId){
}
