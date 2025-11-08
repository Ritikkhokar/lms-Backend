package com.example.lmsProject.dto;

import com.example.lmsProject.entity.Course;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CoursePerformance {
    private Course course;
    private Integer averageGradeOfStudentsInCourse;
    private Integer studentCountBelowThreshold;
}
