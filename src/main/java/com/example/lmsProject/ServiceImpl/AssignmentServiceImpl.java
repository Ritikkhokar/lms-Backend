package com.example.lmsProject.ServiceImpl;

import com.example.lmsProject.Controller.AuthController;
import com.example.lmsProject.Repository.AssignmentRepository;
import com.example.lmsProject.entity.Assignment;
import com.example.lmsProject.service.AssignmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AssignmentServiceImpl implements AssignmentService {

    private static final Logger logger = LoggerFactory.getLogger(AssignmentServiceImpl.class);
    private final AssignmentRepository assignmentRepository;

    public AssignmentServiceImpl(AssignmentRepository repo) {
        this.assignmentRepository = repo;
    }

    @Override
    public List<Assignment> getAllAssignments() {
        return assignmentRepository.findAll();
    }

    @Override
    public Assignment getAssignmentById(Integer id) {
        return assignmentRepository.findById(id).orElse(null);
    }

    @Override
    public Assignment createAssignment(Assignment assignment) {
        return assignmentRepository.save(assignment);
    }

    @Override
    public Assignment updateAssignment(Integer id, Assignment assignment) {
        return assignmentRepository.findById(id).map(existing -> {
            existing.setTitle(assignment.getTitle());
            existing.setDescription(assignment.getDescription());
            existing.setDueDate(assignment.getDueDate());
            existing.setFileUrl(assignment.getFileUrl());
            existing.setCreatedAt(assignment.getCreatedAt());
            existing.setCourse(assignment.getCourse());
            return assignmentRepository.save(existing);
        }).orElse(null);
    }

    @Override
    public void deleteAssignment(Integer id) {
        assignmentRepository.deleteById(id);
    }
}
