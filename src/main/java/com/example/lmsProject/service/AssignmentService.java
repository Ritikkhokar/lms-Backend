package com.example.lmsProject.service;

import com.example.lmsProject.dto.AssignmentDto;
import com.example.lmsProject.entity.Assignment;

import java.io.IOException;
import java.util.List;

public interface AssignmentService {
    List<Assignment> getAllAssignments();
    Assignment getAssignmentById(Integer id);
    Assignment createAssignment(AssignmentDto assignment) throws IOException;
    Assignment updateAssignment(Integer id, AssignmentDto assignment);
    void deleteAssignment(Integer id);
    List<Assignment> getAssignmentsByCourseId(Integer courseId);
}
