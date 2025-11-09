package com.example.lmsProject.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class PerfRecoResponse {
    private Integer studentId;
    private Integer courseId;
    private Integer average;
    private Integer threshold;
    private List<PerfItem> performance;
    private List<RecoItemView> recommendations;
}