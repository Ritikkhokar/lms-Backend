package com.example.lmsProject.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@Data
public class SubmissionDto {
    private Integer userId;
    private Integer assignmentId;
    private MultipartFile file;
    private String feedback;
    private BigDecimal grades;
}