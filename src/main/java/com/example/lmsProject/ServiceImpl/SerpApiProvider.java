package com.example.lmsProject.ServiceImpl;

import com.example.lmsProject.service.WebSearchProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class SerpApiProvider implements WebSearchProvider {
    private static final Logger log = LoggerFactory.getLogger(SerpApiProvider.class);

    private final String apiKey;
    private final String endpoint;
    private final OkHttpClient http = new OkHttpClient();
    private final ObjectMapper om = new ObjectMapper();

    public SerpApiProvider(
            @Value("${reco.serpapi.apiKey:}") String apiKey,
            @Value("${reco.serpapi.endpoint:https://serpapi.com/search.json}") String endpoint
    ) {
        this.apiKey = apiKey;
        this.endpoint = endpoint;
    }

    @Override
    public String name() { return "SERPAPI"; }

    @Override
    public List<SearchResult> search(String query, int max) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("SerpAPI key missing; returning no results.");
            return List.of();
        }

        int count = Math.min(100, Math.max(1, max));
        HttpUrl url = HttpUrl.parse(endpoint).newBuilder()
                .addQueryParameter("engine", "google")
                .addQueryParameter("q", query)
                .addQueryParameter("num", String.valueOf(count))
                .addQueryParameter("api_key", apiKey)
                .build();

        Request req = new Request.Builder().url(url).get().build();

        try (Response resp = http.newCall(req).execute()) {
            if (!resp.isSuccessful() || resp.body() == null) {
                log.warn("SerpAPI HTTP {} for query '{}'", resp.code(), query);
                return List.of();
            }

            byte[] bodyBytes = resp.body().bytes();
            String bodyPreview = new String(bodyBytes, StandardCharsets.UTF_8);
            JsonNode root = om.readTree(bodyBytes);

            if (root.has("error")) {
                log.warn("SerpAPI error for '{}': {}", query, root.get("error").asText());
                return List.of();
            }

            JsonNode organic = root.path("organic_results");
            if (!organic.isArray() || organic.isEmpty()) {
                log.info("SerpAPI returned zero organic_results for '{}'. Raw preview: {}",
                        query, bodyPreview.length() > 500 ? bodyPreview.substring(0, 500) + "..." : bodyPreview);
                return List.of();
            }

            List<SearchResult> out = new ArrayList<>();
            for (JsonNode n : organic) {
                out.add(new SearchResult(
                        n.path("title").asText(""),
                        n.path("link").asText(""),
                        n.path("snippet").asText(""),
                        "serpapi"
                ));
            }
            return out;

        } catch (Exception e) {
            log.error("SerpAPI call failed for '{}': {}", query, e.toString());
            return List.of();
        }
    }
}
