package com.example.lmsProject.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Data
public class AssignmentDto {
    private Integer courseId;
    private String title;
    private String description;
    private LocalDate dueDate;
    private MultipartFile file;
}
