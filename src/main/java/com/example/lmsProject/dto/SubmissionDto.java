package com.example.lmsProject.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class SubmissionDto {
    private Integer userId;
    private Integer assignmentId;
    private MultipartFile file;
}