package com.uplix.hackathon.Dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GitTreeItem(String path, String sha, String url) {
}
