package com.uplix.hackathon.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uplix.hackathon.Dto.LiveJmsListener;
import com.uplix.hackathon.Dto.SiterelicResponseDto;
import com.uplix.hackathon.Dto.LighthouseReportDto;
import com.uplix.hackathon.Entity.JobScore;
import com.uplix.hackathon.Enum.ServiceStatus;
import com.uplix.hackathon.Repository.JobScoreRepo;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Service
public class LighthouseService {

    @Value("${spring.lighthouse.api-key}")
    private String apiKey;

    @Value("${spring.lighthouse.url}")
    private String lighthouseUrl;

    private final JobScoreRepo jobScoreRepo;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    public LighthouseService(JobScoreRepo jobScoreRepo) {
        this.jobScoreRepo = jobScoreRepo;
    }

    // 🔔 Solace listener
    @Transactional
    @JmsListener(destination = "uplix/repo/request/lighthouse", containerFactory = "jmsListenerContainerFactory")
    public void handleLighthouseRequest(String json) throws JsonProcessingException {
        try {
            LiveJmsListener liveJmsListener = new ObjectMapper().readValue(json, LiveJmsListener.class);
            String liveUrl = liveJmsListener.liveUrl();

            log.info("🔦 Lighthouse request received for repoUrl: {}", liveUrl);

            // 1. Trigger Siterelic Lighthouse run
            SiterelicResponseDto response = getLighthouseScore(liveUrl);
            log.info("📊 Lighthouse API triggered: {}", response);

            if (response != null && response.getData() != null) {
                String csvUrl = response.getData();
                log.info("📥 Fetching Lighthouse report from: {}", csvUrl);

                // 2. Download + parse report
                LighthouseReportDto report = getLighthouseReport(csvUrl);
                log.info("✅ Lighthouse report parsed: {}", report);
                JobScore referenceById = jobScoreRepo.getReferenceById(liveJmsListener.jobId());
                referenceById.setAccessibilityScore(report.getAccessibility());
                referenceById.setBestPracticesScore(report.getBestPractices());
                referenceById.setSeoScore(report.getSeo());
                referenceById.setPerformanceScore(report.getPerformance());
                referenceById.setLiveScoreStatus(ServiceStatus.COMPLETED);
                jobScoreRepo.save(referenceById);
            } else {
                log.warn("⚠️ No Lighthouse data returned for {}", liveUrl);
            }

        } catch (Exception e) {
            log.error("❌ Error handling Lighthouse request", e);
        }
    }

    // 🔹 Call Siterelic Lighthouse API
    public SiterelicResponseDto getLighthouseScore(String url) {
        log.info("Getting report...");
        Map<String, Object> payload = new HashMap<>();
        payload.put("url", url);
        payload.put("device", "desktop");
        payload.put("proxyCountry", "us");
        payload.put("followRedirect", true);
        payload.put("parameters", List.of(
                "--only-categories=performance",
                "--only-categories=seo",
                "--only-categories=accessibility",
                "--only-categories=best-practices",
                "--only-categories=pwa",
                "--output=csv"));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", apiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
        return restTemplate.postForObject(lighthouseUrl, entity, SiterelicResponseDto.class);
    }

    // 🔹 Fetch & parse Lighthouse report
    public LighthouseReportDto getLighthouseReport(String dataUrl) {
        ResponseEntity<byte[]> resp = restTemplate.exchange(dataUrl, HttpMethod.GET, null, byte[].class);

        MediaType ct = resp.getHeaders().getContentType();
        byte[] bodyBytes = resp.getBody();
        if (bodyBytes == null || bodyBytes.length == 0) {
            log.warn("⚠️ Empty body from {}", dataUrl);
            return new LighthouseReportDto();
        }

        String body = new String(bodyBytes, StandardCharsets.UTF_8);
        boolean looksJson = body.trim().startsWith("{") || (ct != null && ct.includes(MediaType.APPLICATION_JSON));
        boolean looksCsv = body.contains(",") || body.contains("\n")
                || (ct != null && ct.includes(MediaType.TEXT_PLAIN))
                || dataUrl.toLowerCase().endsWith(".csv");

        return (looksJson && !looksCsv) ? parseJson(body)
                : (looksCsv && !looksJson) ? parseCsv(body)
                : (body.trim().startsWith("{") ? parseJson(body) : parseCsv(body));
    }

    // 🔹 Parse JSON Lighthouse report
    private LighthouseReportDto parseJson(String body) {
        try {
            JsonNode root = objectMapper.readTree(body);
            return LighthouseReportDto.fromReport(root);
        } catch (Exception e) {
            log.warn("Failed to parse Lighthouse JSON", e);
            return new LighthouseReportDto();
        }
    }

    // 🔹 Parse CSV Lighthouse report
    private LighthouseReportDto parseCsv(String csvText) {
        try (CSVParser parser = CSVParser.parse(csvText, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            LighthouseReportDto dto = new LighthouseReportDto();

            String url = null;
            Integer perf = null, a11y = null, bp = null, seo = null, pwa = null;
            Integer fcp = null, lcp = null, tti = null, si = null, tbt = null;
            Double cls = null;

            for (CSVRecord r : parser) {
                String finalUrl = get(r, "finalUrl");
                if (url == null && finalUrl != null && !finalUrl.isBlank()) url = finalUrl;

                String name = norm(get(r, "name"));
                String scoreStr = get(r, "score");
                if (scoreStr == null || scoreStr.isBlank()) continue;

                if (name != null && name.endsWith("-score")) {
                    Integer pct = toPct(scoreStr);
                    if (pct != null) {
                        switch (name) {
                            case "performance-score" -> perf = pct;
                            case "accessibility-score" -> a11y = pct;
                            case "best-practices-score", "bestpractices-score" -> bp = pct;
                            case "seo-score" -> seo = pct;
                            case "pwa-score" -> pwa = pct;
                        }
                        continue;
                    }
                }

                Integer pct = toPct(scoreStr);
                if (pct != null) {
                    switch (name) {
                        case "first-contentful-paint" -> fcp = pct;
                        case "largest-contentful-paint" -> lcp = pct;
                        case "interactive", "time-to-interactive" -> tti = pct;
                        case "speed-index", "speedindex" -> si = pct;
                        case "total-blocking-time", "tbt", "totalblockingtime" -> tbt = pct;
                        case "cumulative-layout-shift" -> cls = toDouble(scoreStr);
                    }
                }
            }

            dto.setUrl(url);
            dto.setPerformance(defaultInt(perf, 0));
            dto.setAccessibility(defaultInt(a11y, 0));
            dto.setBestPractices(defaultInt(bp, 0));
            dto.setSeo(defaultInt(seo, 0));
            dto.setPwa(defaultInt(pwa, 0));
            dto.setFcpMs(defaultInt(fcp, 0));
            dto.setLcpMs(defaultInt(lcp, 0));
            dto.setTtiMs(defaultInt(tti, 0));
            dto.setSpeedIndexMs(defaultInt(si, 0));
            dto.setTbtMs(defaultInt(tbt, 0));
            dto.setCls(cls == null ? 0.0 : cls);

            return dto;
        } catch (Exception e) {
            log.warn("Failed to parse Lighthouse CSV", e);
            return new LighthouseReportDto();
        }
    }

    // 🔹 Helpers
    private static int defaultInt(Integer v, int def) { return v == null ? def : v; }
    private static String get(CSVRecord r, String header) { return r.isMapped(header) ? r.get(header) : null; }
    private static String norm(String s) { return s == null ? null : s.trim().toLowerCase().replaceAll("[^a-z0-9]+", "-"); }
    private static Integer toPct(String v) { Double d = toDouble(v); return d == null ? null : (d <= 1.0 ? (int) Math.round(d * 100) : (int) Math.round(d)); }
    private static Double toDouble(String v) { try { return v == null ? null : Double.parseDouble(v.replaceAll("[, ]", "").replaceAll("ms$", "")); } catch (Exception ignored) { return null; } }
}
