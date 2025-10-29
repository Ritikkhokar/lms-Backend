package com.example.lmsProject.service;

import com.example.lmsProject.entity.Assignment;
import java.util.List;

public interface AssignmentService {
    List<Assignment> getAllAssignments();
    Assignment getAssignmentById(Integer id);
    Assignment createAssignment(Assignment assignment);
    Assignment updateAssignment(Integer id, Assignment assignment);
    void deleteAssignment(Integer id);
}
