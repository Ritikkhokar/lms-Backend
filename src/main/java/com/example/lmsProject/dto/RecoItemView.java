package com.example.lmsProject.dto;
public record RecoItemView(
        String concept, String title, String url, String snippet,
        String source, Integer confidencePct, String rationale
) {}
