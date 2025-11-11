package com.example.lmsProject.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Data
public class ResourceDto {
    private Integer courseId;
    private Integer ModuleId;
    private Integer uploadedBy;
    private String title;
    private MultipartFile file;
    private LocalDateTime uploadDate;
}
