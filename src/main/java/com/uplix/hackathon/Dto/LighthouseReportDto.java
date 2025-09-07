package com.uplix.hackathon.Dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LighthouseReportDto {
    private String url;            // finalUrl
    private Integer performance;   // 0–100
    private Integer accessibility; // 0–100
    private Integer bestPractices; // 0–100
    private Integer seo;           // 0–100
    private Integer pwa;           // 0–100 or null when absent

    private Integer fcpMs;         // first-contentful-paint (ms)
    private Integer lcpMs;         // largest-contentful-paint (ms)
    private Integer ttiMs;         // interactive (ms)
    private Integer speedIndexMs;  // speed-index (ms)
    private Integer tbtMs;         // total-blocking-time (ms)
    private Double  cls;           // cumulative-layout-shift

    /**
     * Build a summary directly from the raw Lighthouse report JSON.
     */
    public static LighthouseReportDto fromReport(JsonNode root) {
        JsonNode categories = root.path("categories");
        JsonNode audits     = root.path("audits");

        return LighthouseReportDto.builder()
                .url(root.path("finalUrl").asText(null))
                .performance(pct(categories, "performance"))
                .accessibility(pct(categories, "accessibility"))
                .bestPractices(pct(categories, "best-practices"))
                .seo(pct(categories, "seo"))
                .pwa(pct(categories, "pwa")) // stays null if not present
                .fcpMs(ms(audits, "first-contentful-paint"))
                .lcpMs(ms(audits, "largest-contentful-paint"))
                .ttiMs(ms(audits, "interactive"))
                .speedIndexMs(ms(audits, "speed-index"))
                .tbtMs(ms(audits, "total-blocking-time"))
                .cls(dbl(audits, "cumulative-layout-shift"))
                .build();
    }

    private static Integer pct(JsonNode categories, String key) {
        JsonNode score = categories.path(key).path("score");
        if (!score.isNumber()) return null;
        return (int) Math.round(score.asDouble() * 100.0);
    }

    private static Integer ms(JsonNode audits, String key) {
        JsonNode nv = audits.path(key).path("numericValue");
        if (!nv.isNumber()) return null;
        return (int) Math.round(nv.asDouble());
    }

    private static Double dbl(JsonNode audits, String key) {
        JsonNode nv = audits.path(key).path("numericValue");
        return nv.isNumber() ? nv.asDouble() : null;
    }
}
