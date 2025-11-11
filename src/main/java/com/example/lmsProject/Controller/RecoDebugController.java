package com.example.lmsProject.Controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/reco")
public class RecoDebugController {

    @Value("${reco.provider:}")
    private String provider;

    @Value("${reco.serpapi.apiKey:}")
    private String serpKey;  // empty string if not set

    @GetMapping("/debug")
    public Map<String, Object> debug() {
        boolean present = (serpKey != null && !serpKey.isBlank());
        String unmasked= present?serpKey:"";
        String masked = present
                ? serpKey.substring(0, Math.min(6, serpKey.length())) + "*****"
                : "";

        return Map.of(
                "provider", provider,
                "serpapiKeyPresent", present,
                "serpapiKeyPreview", unmasked
        );
    }
}
