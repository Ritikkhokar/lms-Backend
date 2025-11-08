package com.example.lmsProject.dto;
import java.util.List;


public record PerfRecoResponse(
        Integer studentId, Integer courseId, Integer average, Integer threshold,
        java.util.List<PerfItem> performance,
        java.util.List<RecoItemView> recommendations
) {}
