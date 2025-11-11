package com.example.lmsProject.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public interface WebSearchProvider {
    String name();
    List<SearchResult> search(String query, int max);

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    class SearchResult {
        private String title;
        private String url;
        private String snippet;
        private String source;
    }
}