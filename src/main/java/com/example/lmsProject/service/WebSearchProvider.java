package com.example.lmsProject.service;

import java.util.List;

public interface WebSearchProvider {
    String name();
    List<SearchResult> search(String query, int max);

    record SearchResult(String title, String url, String snippet, String source) {}
}
