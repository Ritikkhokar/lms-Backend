package com.example.lmsProject.service;

import com.example.lmsProject.dto.AverageMarks;
import com.example.lmsProject.dto.SubmissionDto;
import com.example.lmsProject.entity.Submission;

import java.io.IOException;
import java.util.List;

public interface SubmissionService {
    List<Submission> getAllSubmissions();
    Submission getSubmissionById(Integer id);
    Submission createSubmission(SubmissionDto submissionDto) throws IOException;
    Submission updateSubmissionForStudent(Integer id, SubmissionDto submissionDto);
    Submission updateSubmissionForTeacher(Integer id, SubmissionDto submissionDto);
    void deleteSubmission(Integer id);
    List<Submission> getAllSubmissionsByUserId(Integer userId);
    List<Submission> getAllSubmissionsByAssignmentId(Integer assignmentId);
    void deleteSubmissionsForAssignment(Integer assignmentId);
}
