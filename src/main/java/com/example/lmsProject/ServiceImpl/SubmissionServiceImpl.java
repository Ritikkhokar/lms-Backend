package com.example.lmsProject.ServiceImpl;

import com.example.lmsProject.Repository.SubmissionRepository;
import com.example.lmsProject.entity.Submission;
import com.example.lmsProject.service.SubmissionService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SubmissionServiceImpl implements SubmissionService {

    private final SubmissionRepository submissionRepository;

    public SubmissionServiceImpl(SubmissionRepository repo) {
        this.submissionRepository = repo;
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
    public Submission createSubmission(Submission submission) {
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
}
