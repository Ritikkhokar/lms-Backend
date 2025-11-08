package com.example.lmsProject.Controller;

import com.example.lmsProject.dto.*;
import com.example.lmsProject.service.RecoService;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reco")
public class PerfRecoController {

    private final RecoService recoService;

    public PerfRecoController(RecoService recoService) { this.recoService = recoService; }

    @PostMapping("/performance-and-recos")
    public PerfRecoResponse perfAndRecos(@RequestBody PerfRecoRequest req) {
        int threshold = (req.threshold() == null) ? 70 : req.threshold();
        int topN = (req.topN() == null || req.topN() <= 0) ? 5 : req.topN();

        var cs = Optional.ofNullable(req.conceptScores()).orElse(List.of());
        int avg = cs.isEmpty() ? 0 :
                (int) Math.round(cs.stream().mapToInt(c -> c.score() == null ? 0 : c.score()).average().orElse(0));

        var perf = cs.stream().map(c ->
                new PerfItem(
                        c.concept(),
                        c.score(),
                        (c.score() != null && c.score() < threshold) ? "needs_improvement" : "on_track"
                )
        ).toList();

        var weakConcepts = cs.stream()
                .filter(c -> c.score() != null && c.score() < threshold)
                .map(ConceptScore::concept)
                .distinct()
                .toList();

        var recoResponse = recoService.recommend(weakConcepts, topN);

        var recoItems = recoResponse.items().stream().map(r ->
                new RecoItemView(
                        r.concept(), r.title(), r.url(), r.snippet(), r.source(),
                        (int) Math.round(r.score() * 100.0),
                        r.rationale()
                )
        ).collect(Collectors.toList());

        return new PerfRecoResponse(
                req.studentId(), req.courseId(), avg, threshold, perf, recoItems
        );
    }
}
