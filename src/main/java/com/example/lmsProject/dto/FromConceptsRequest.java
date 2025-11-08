package com.example.lmsProject.dto;
import java.util.List;
public record FromConceptsRequest(List<String> concepts, Integer topN) {}
