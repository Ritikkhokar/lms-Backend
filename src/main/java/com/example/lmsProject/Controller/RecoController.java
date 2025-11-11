package com.example.lmsProject.Controller;

import com.example.lmsProject.dto.FromConceptsRequest;
import com.example.lmsProject.dto.RecoResponse;
import com.example.lmsProject.service.RecoService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reco")
public class RecoController {

    private final RecoService recoService;

    public RecoController(RecoService recoService) {
        this.recoService = recoService;
    }

    @PostMapping("/from-concepts")
    public RecoResponse fromConcepts(@RequestBody FromConceptsRequest req) {
        Integer topN = (req.getTopN() == null || req.getTopN() <= 0) ? null : req.getTopN();
        return recoService.recommend(req.getConcepts(), topN);
    }
}
