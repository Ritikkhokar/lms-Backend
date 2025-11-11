package com.example.lmsProject.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class PerfRecoRequest {
    private Integer studentId;
    private Integer courseId;
    private Integer threshold;
    private Integer topN;
    private List<ConceptScore> conceptScores;
}
