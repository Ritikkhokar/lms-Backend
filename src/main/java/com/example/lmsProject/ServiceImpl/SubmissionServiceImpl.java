package com.example.lmsProject.ServiceImpl;

import com.example.lmsProject.Repository.SubmissionRepository;
import com.example.lmsProject.dto.SubmissionDto;
import com.example.lmsProject.entity.*;
import com.example.lmsProject.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class SubmissionServiceImpl implements SubmissionService {

    private static final Logger logger = LoggerFactory.getLogger(SubmissionServiceImpl.class);
    private final SubmissionRepository submissionRepository;
    private final EnrollmentService enrollmentService;
    private final UserService userService;
    private final StorageService storageService;
    private final AssignmentService assignmentService;

    public SubmissionServiceImpl(
            SubmissionRepository repo, EnrollmentService enrollmentService, UserService userService, StorageService storageService, AssignmentService assignmentService
    ) {
        this.submissionRepository = repo;
        this.enrollmentService = enrollmentService;
        this.userService = userService;
        this.storageService = storageService;
        this.assignmentService = assignmentService;
    }

    @Override
    public List<Submission> getAllSubmissions() {
        return submissionRepository.findAll();
    }

    @Override
    public Submission getSubmissionById(Integer id) {
        return submissionRepository.findById(id).orElse(null);
    }

    @Override
    public Submission createSubmission(SubmissionDto dto) throws IOException {
        String key = "submissions/" + dto.getUserId() + "/" + dto.getAssignmentId() + "/"
                + System.currentTimeMillis() + "_" + dto.getFile().getOriginalFilename();
        String s3Key = storageService.uploadFile(
                key,
                dto.getFile().getInputStream(),
                dto.getFile().getSize(),
                dto.getFile().getContentType()
        );
        System.out.println("Ritik -> " + s3Key);
        Assignment assignment = assignmentService.getAssignmentById(dto.getAssignmentId());
        if(assignment == null){
            throw new RuntimeException("Assignment not found");
        }
        User user = userService.getUserById(dto.getUserId());
        if(user == null){
            throw new RuntimeException("user not found");
        }
        Submission submission = new Submission();
        submission.setAssignment(assignment);
        submission.setStudent(user);
        submission.setSubmittedAt(LocalDateTime.now());
        submission.setSubmissionUrl(s3Key); // Store the key, not URL!
        return submissionRepository.save(submission);
    }

    @Override
    public Submission updateSubmission(Integer id, Submission submission) {
        return submissionRepository.findById(id).map(existing -> {
            existing.setAssignment(submission.getAssignment());
            existing.setStudent(submission.getStudent());
            existing.setSubmissionUrl(submission.getSubmissionUrl());
            existing.setSubmittedAt(submission.getSubmittedAt());
            existing.setGrade(submission.getGrade());
            existing.setFeedback(submission.getFeedback());
            return submissionRepository.save(existing);
        }).orElse(null);
    }

    @Override
    public void deleteSubmission(Integer id) {
        submissionRepository.deleteById(id);
    }

    @Override
    public List<Submission> getAllSubmissionsByUserId(Integer userId) {
        return submissionRepository.findByStudent_UserId(userId);
    }

}
