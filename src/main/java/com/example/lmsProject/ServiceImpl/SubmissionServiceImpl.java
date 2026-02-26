package com.example.lmsProject.ServiceImpl;

import com.example.lmsProject.Repository.SubmissionRepository;
import com.example.lmsProject.dto.SubmissionDto;
import com.example.lmsProject.entity.*;
import com.example.lmsProject.service.*;
import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDate;
import java.util.List;

@Service
public class SubmissionServiceImpl implements SubmissionService {

    private static final Logger logger = LoggerFactory.getLogger(SubmissionServiceImpl.class);
    private final SubmissionRepository submissionRepository;
    private final UserService userService;
    private final StorageService storageService;
    private final AssignmentService assignmentService;
    private final EmailService emailService;

    public SubmissionServiceImpl(
            SubmissionRepository repo, EnrollmentService enrollmentService, UserService userService, StorageService storageService, AssignmentService assignmentService, EmailService emailService
    ) {
        this.submissionRepository = repo;
        this.userService = userService;
        this.storageService = storageService;
        this.assignmentService = assignmentService;
        this.emailService = emailService;
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
        Assignment assignment = assignmentService.getAssignmentById(dto.getAssignmentId());
        if(assignment == null){
            throw new RuntimeException("Assignment not found");
        }
        if(assignment.getDueDate().isBefore(ChronoLocalDate.from(LocalDateTime.now()))){
            throw new RuntimeException("Assignment can not be submitted after due date");
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
        submission.setMaximumGrade(BigDecimal.valueOf(100));
        return submissionRepository.save(submission);
    }

    @Override
    public Submission updateSubmissionForStudent(Integer id, SubmissionDto submissionDto) {
        Assignment assignment = assignmentService.getAssignmentById(submissionDto.getAssignmentId());
        if(assignment == null){
            throw new RuntimeException("Assignment not found");
        }
        if(assignment.getDueDate().isBefore(ChronoLocalDate.from(LocalDateTime.now()))){
            throw new RuntimeException("Assignment can not be submitted after due date");
        }
        return submissionRepository.findById(id).map(existing -> {
            if(submissionDto.getFile() != null) {
                String key = "submissions/" + submissionDto.getUserId() + "/" + submissionDto.getAssignmentId() + "/"
                        + System.currentTimeMillis() + "_" + submissionDto.getFile().getOriginalFilename();
                String s3Key = null;
                try {
                    s3Key = storageService.uploadFile(
                            key,
                            submissionDto.getFile().getInputStream(),
                            submissionDto.getFile().getSize(),
                            submissionDto.getFile().getContentType()
                    );
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                existing.setSubmissionUrl(s3Key);
            }
            existing.setSubmittedAt(LocalDateTime.now());
            return submissionRepository.save(existing);
        }).orElse(null);
    }

    @Override
    public Submission updateSubmissionForTeacher(Integer id, SubmissionDto submissionDto) {
        return submissionRepository.findById(id).map(existing -> {
            existing.setIs_graded(Boolean.TRUE);
            if(submissionDto.getFeedback() != null){
                existing.setFeedback(submissionDto.getFeedback());
            }
            if(submissionDto.getGrades() != null){
                existing.setGrade(submissionDto.getGrades());
            }
            Submission submission = submissionRepository.save(existing);
            emailService.sendGradeNotification(
                        submission.getStudent().getEmail(),
                        submission.getAssignment().getTitle(),
                        String.valueOf(submission.getGrade()),
                        submission.getStudent().getFullName()
            );

            return  submission;
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

    @Override
    public List<Submission> getAllSubmissionsByAssignmentId(Integer assignmentId) {
        return submissionRepository.findByAssignment_AssignmentId(assignmentId);
    }

    @Override
    public void deleteSubmissionsForAssignment(Integer assignmentId){
        List<Submission> submissions = getAllSubmissionsByAssignmentId(assignmentId);
        for(Submission submission : submissions){
            deleteSubmission(submission.getSubmissionId());
        }
    }

}
