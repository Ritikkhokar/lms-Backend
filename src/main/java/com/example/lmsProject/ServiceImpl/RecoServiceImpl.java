package com.example.lmsProject.ServiceImpl;


import com.example.lmsProject.dto.*;
import com.example.lmsProject.service.RecoService;
import com.example.lmsProject.service.WebSearchProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RecoServiceImpl implements RecoService {

    private final WebSearchProvider provider;
    private final int topNDefault;

    public RecoServiceImpl(
            List<WebSearchProvider> providers,
            @Value("${reco.provider:SERPAPI}") String providerName,
            @Value("${reco.topNDefault:5}") int topNDefault
    ) {
        this.topNDefault = topNDefault;
        this.provider = providers.stream()
                .filter(p -> p.name().equalsIgnoreCase(providerName))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No provider matched: " + providerName));
    }


    @Override
    public RecoResponse recommend(List<String> concepts, Integer topN) {
        if (concepts == null || concepts.isEmpty()) {
            return new RecoResponse(Collections.emptyList());
        }
        int k = (topN == null || topN <= 0) ? topNDefault : topN;

        List<RecoItem> all = new ArrayList<>();
        for (String concept : concepts) {
            String q = buildQuery(concept);
            var results = provider.search(q, k * 2);

            for (var r : results) {
                double score = score(concept, r.getTitle(), r.getSnippet(), r.getUrl());
                String why = (score >= 0.6)
                        ? "High match to concept with reputable source"
                        : "Relevant tutorial/guide for the concept";

                RecoItem item = new RecoItem();
                item.setConcept(concept);
                item.setTitle(r.getTitle());
                item.setUrl(r.getUrl());
                item.setSnippet(r.getSnippet());
                item.setSource(r.getSource());
                item.setScore(score);
                item.setRationale(why);

                all.add(item);
            }
        }


        Map<String, List<RecoItem>> grouped = new LinkedHashMap<>();
        for (var item : all) {
            grouped.computeIfAbsent(item.getConcept(), c -> new ArrayList<>()).add(item);
        }

        List<RecoItem> finalList = new ArrayList<>();
        grouped.forEach((concept, items) -> {
            items.sort(Comparator.comparingDouble(RecoItem::getScore).reversed());
            finalList.addAll(items.stream().limit(k).toList());
        });

        return new RecoResponse(finalList);
    }


    @Override
    public PerfRecoResponse perfAndRecos(PerfRecoRequest req) {
        int threshold = (req.getThreshold() == null) ? 70 : req.getThreshold();
        int topN = (req.getTopN() == null || req.getTopN() <= 0) ? topNDefault : req.getTopN();

        var cs = Optional.ofNullable(req.getConceptScores()).orElse(List.of());

        int avg = cs.isEmpty()
                ? 0
                : (int) Math.round(
                cs.stream()
                        .map(ConceptScore::getScore)
                        .mapToInt(s -> s == null ? 0 : s)
                        .average()
                        .orElse(0)
        );


        List<PerfItem> perf = cs.stream()
                .map(c -> new PerfItem(
                        c.getConcept(),
                        c.getScore(),
                        (c.getScore() != null && c.getScore() < threshold) ? "needs_improvement" : "on_track"
                ))
                .toList();


        List<String> weakConcepts = cs.stream()
                .filter(c -> c.getScore() != null && c.getScore() < threshold)
                .map(ConceptScore::getConcept)
                .distinct()
                .toList();

        RecoResponse recoResponse = recommend(weakConcepts, topN);

        List<RecoItemView> recoItems = recoResponse.getItems().stream()
                .map(r -> new RecoItemView(
                        r.getConcept(),
                        r.getTitle(),
                        r.getUrl(),
                        r.getSnippet(),
                        r.getSource(),
                        (int) Math.round((r.getScore() == null ? 0.0 : r.getScore()) * 100.0),
                        r.getRationale()
                ))
                .toList();

        return new PerfRecoResponse(
                req.getStudentId(),
                req.getCourseId(),
                avg,
                threshold,
                perf,
                recoItems
        );
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

        if (u.contains("khanacademy") || u.contains("coursera") || u.contains("edx.org")
                || u.contains("stanford.edu") || u.contains("mit.edu") || u.contains("geeksforgeeks")
                || u.contains("freecodecamp") || u.contains("oracle.com") || u.contains("docs.")) {
            score += 0.3;
        }

        if (u.contains("stackoverflow") || u.contains("reddit")) score -= 0.1;

        return Math.max(0.0, Math.min(1.0, score));
    }
}
