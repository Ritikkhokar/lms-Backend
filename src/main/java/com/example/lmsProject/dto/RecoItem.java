package com.example.lmsProject.dto;
public record RecoItem(
        String concept, String title, String url, String snippet,
        String source, double score, String rationale
) {}
