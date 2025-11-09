package com.example.lmsProject.Controller;

import com.example.lmsProject.dto.AssignmentDto;
import com.example.lmsProject.dto.AverageMarks;
import com.example.lmsProject.dto.SubmissionDto;
import com.example.lmsProject.entity.Submission;
import com.example.lmsProject.service.SubmissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/submissions")
public class SubmissionController {

    private static final Logger logger = LoggerFactory.getLogger(SubmissionController.class);
    private final SubmissionService submissionService;

    public SubmissionController(SubmissionService service) {
        this.submissionService = service;
    }

    @GetMapping
    public List<Submission> getAllSubmissions() {
        return submissionService.getAllSubmissions();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Submission> getSubmissionById(@PathVariable Integer id) {
        Submission submission = submissionService.getSubmissionById(id);
        if (submission == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(submission);
    }

    @PostMapping
    public ResponseEntity<Submission> createSubmission(@ModelAttribute SubmissionDto dto) throws IOException {
        Submission created = submissionService.createSubmission(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/forStudent/{id}")
    public ResponseEntity<Submission> updateSubmissionForStudent(@PathVariable Integer id, @ModelAttribute SubmissionDto dto) {
        Submission updated = submissionService.updateSubmissionForStudent(id, dto);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubmission(@PathVariable Integer id) {
        submissionService.deleteSubmission(id);
        return ResponseEntity.noContent().build();
    }

}
