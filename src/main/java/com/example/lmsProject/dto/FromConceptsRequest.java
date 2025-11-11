package com.example.lmsProject.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FromConceptsRequest {
    private List<String> concepts;
    private Integer topN; // optional; default handled in service
}
