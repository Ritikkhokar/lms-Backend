package com.example.lmsProject.Controller;

import com.example.lmsProject.dto.PerfRecoRequest;
import com.example.lmsProject.dto.PerfRecoResponse;
import com.example.lmsProject.service.RecoService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reco")
public class PerfRecoController {

    private final RecoService recoService;

    public PerfRecoController(RecoService recoService) {
        this.recoService = recoService;
    }

    @PostMapping("/performance-and-recos")
    public PerfRecoResponse perfAndRecos(@RequestBody PerfRecoRequest req) {
        return recoService.perfAndRecos(req);
    }
}
