package com.example.lmsProject.service;

import com.example.lmsProject.dto.PerfRecoRequest;
import com.example.lmsProject.dto.PerfRecoResponse;
import com.example.lmsProject.dto.RecoResponse;

import java.util.List;

public interface RecoService {

    RecoResponse recommend(List<String> concepts, Integer topN);


    PerfRecoResponse perfAndRecos(PerfRecoRequest req);
}
