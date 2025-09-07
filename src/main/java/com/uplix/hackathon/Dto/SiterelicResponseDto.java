package com.uplix.hackathon.Dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SiterelicResponseDto {
    private long timestamp;
    private String apiVersion;
    private String apiStatus;
    private int apiCode;
    private Meta meta;
    private String data;

    @Data
    @Builder
    public static class Meta {
        private String url;
        private String device;
        private boolean followRedirect;
        private String redirectedURL;
        private Test test;
    }

    @Data
    @Builder
    public static class Test {
        private String id;
    }
}
