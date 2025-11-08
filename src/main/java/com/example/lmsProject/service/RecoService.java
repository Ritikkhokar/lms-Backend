package com.example.lmsProject.service;

import com.example.lmsProject.dto.RecoItem;
import com.example.lmsProject.dto.RecoResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RecoService {

    private final WebSearchProvider provider;
    private final int topNDefault;

    public RecoService(
            java.util.List<WebSearchProvider> providers,
            @Value("${reco.provider:SERPAPI}") String providerName,
            @Value("${reco.topNDefault:5}") int topNDefault
    ) {
        this.topNDefault = topNDefault;
        this.provider = providers.stream()
                .filter(p -> p.name().equalsIgnoreCase(providerName))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No provider matched: " + providerName));
    }

    public RecoResponse recommend(java.util.List<String> concepts, Integer topN) {
        if (concepts == null || concepts.isEmpty()) return new RecoResponse(List.of());
        int k = (topN == null || topN <= 0) ? topNDefault : topN;

        List<RecoItem> all = new ArrayList<>();
        for (String concept : concepts) {
            String q = buildQuery(concept);
            var results = provider.search(q, k * 2);

            for (var r : results) {
                double score = score(concept, r.title(), r.snippet(), r.url());
                String why = (score >= 0.6)
                        ? "High match to concept with reputable source"
                        : "Relevant tutorial/guide for the concept";
                all.add(new RecoItem(concept, r.title(), r.url(), r.snippet(), r.source(), score, why));
            }
        }

        Map<String, List<RecoItem>> grouped = new LinkedHashMap<>();
        for (var item : all) grouped.computeIfAbsent(item.concept(), c -> new ArrayList<>()).add(item);

        List<RecoItem> finalList = new ArrayList<>();
        grouped.forEach((concept, items) -> {
            items.sort(java.util.Comparator.comparingDouble(RecoItem::score).reversed());
            finalList.addAll(items.stream().limit(k).toList());
        });

        return new RecoResponse(finalList);
    }

    private String buildQuery(String concept) {
        return concept + " tutorial OR guide OR course OR exercises";
    }

    private double score(String concept, String title, String snippet, String url) {
        String c = concept == null ? "" : concept.toLowerCase();
        String t = title == null ? "" : title.toLowerCase();
        String s = snippet == null ? "" : snippet.toLowerCase();
        String u = url == null ? "" : url.toLowerCase();

        double score = 0.0;
        if (t.contains(c)) score += 0.35;
        if (s.contains(c)) score += 0.25;

        if (u.contains("khanacademy") || u.contains("coursera") || u.contains("edx.org") ||
                u.contains("stanford.edu") || u.contains("mit.edu") || u.contains("geeksforgeeks") ||
                u.contains("freecodecamp") || u.contains("oracle.com") || u.contains("docs."))
            score += 0.3;

        if (u.contains("stackoverflow") || u.contains("reddit")) score -= 0.1;

        return Math.max(0.0, Math.min(1.0, score));
    }
}
