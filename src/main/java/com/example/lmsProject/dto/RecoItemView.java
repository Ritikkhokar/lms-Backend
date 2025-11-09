package com.example.lmsProject.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecoItemView {
    private String concept;
    private String title;
    private String url;
    private String snippet;
    private String source;         // e.g., "serpapi"
    private Integer confidencePct; // 0..100
    private String rationale;
}