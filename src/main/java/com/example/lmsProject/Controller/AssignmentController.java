package com.example.lmsProject.Controller;

import com.example.lmsProject.dto.AssignmentDto;
import com.example.lmsProject.entity.Assignment;
import com.example.lmsProject.service.AssignmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/assignments")
public class AssignmentController {

    private static final Logger logger = LoggerFactory.getLogger(AssignmentController.class);
    private final AssignmentService assignmentService;

    public AssignmentController(AssignmentService service) {
        this.assignmentService = service;
    }

    @GetMapping
    public List<Assignment> getAllAssignments() {
        return assignmentService.getAllAssignments();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Assignment> getAssignmentById(@PathVariable Integer id) {
        Assignment assignment = assignmentService.getAssignmentById(id);
        if (assignment == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(assignment);
    }

    @PostMapping
    public ResponseEntity<Assignment> createAssignment(@ModelAttribute AssignmentDto dto) throws IOException {
        Assignment created = assignmentService.createAssignment(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Assignment> updateAssignment(@PathVariable Integer id, @RequestBody AssignmentDto assignment) {
        Assignment updated = assignmentService.updateAssignment(id, assignment);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAssignment(@PathVariable Integer id) {
        assignmentService.deleteAssignment(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/assignmentsByCourseId/{id}")
    public ResponseEntity<List<Assignment>> getAssignmentsByCourseId(@PathVariable Integer id){
        return ResponseEntity.ok( assignmentService.getAssignmentsByCourseId(id));
    }
}
