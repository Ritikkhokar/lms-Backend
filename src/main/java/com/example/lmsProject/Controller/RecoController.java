package com.example.lmsProject.Controller;

import com.example.lmsProject.dto.FromConceptsRequest;
import com.example.lmsProject.dto.RecoResponse;
import com.example.lmsProject.service.RecoService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reco")
public class RecoController {
    private final RecoService service;
    public RecoController(RecoService service) { this.service = service; }

    @PostMapping("/from-concepts")
    public RecoResponse fromConcepts(@RequestBody FromConceptsRequest req) {
        int topN = (req.topN() == null || req.topN() <= 0) ? 5 : req.topN();
        return service.recommend(req.concepts(), topN);
    }
}
