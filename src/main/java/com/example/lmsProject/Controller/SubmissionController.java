package com.example.lmsProject.Controller;

import com.example.lmsProject.entity.Submission;
import com.example.lmsProject.service.SubmissionService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.util.List;

@RestController
@RequestMapping("/api/submissions")
public class SubmissionController {

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
    public ResponseEntity<Submission> createSubmission(@RequestBody Submission submission) {
        Submission created = submissionService.createSubmission(submission);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Submission> updateSubmission(@PathVariable Integer id, @RequestBody Submission submission) {
        Submission updated = submissionService.updateSubmission(id, submission);
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
