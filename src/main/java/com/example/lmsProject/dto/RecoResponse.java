package com.example.lmsProject.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** Response for POST /api/reco/from-concepts */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecoResponse {
    private List<RecoItem> items;
}
