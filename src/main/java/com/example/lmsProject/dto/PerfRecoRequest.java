package com.example.lmsProject.dto;
import java.util.List;
public record PerfRecoRequest(
        Integer studentId, Integer courseId, Integer threshold, Integer topN,
        java.util.List<ConceptScore> conceptScores
) {}
