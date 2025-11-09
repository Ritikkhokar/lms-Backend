package com.example.lmsProject.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecoItem {
    private String concept;
    private String title;
    private String url;
    private String snippet;
    private String source;
    private Double score;
    private String rationale;
}
