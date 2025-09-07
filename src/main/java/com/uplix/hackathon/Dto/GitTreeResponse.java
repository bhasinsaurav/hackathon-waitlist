package com.uplix.hackathon.Dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GitTreeResponse(List<GitTreeItem> tree) {
}
