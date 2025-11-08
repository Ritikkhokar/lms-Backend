package com.example.lmsProject.service;

import com.example.lmsProject.dto.AverageMarks;
import com.example.lmsProject.entity.Submission;

import java.util.List;

public interface SubmissionService {
    List<Submission> getAllSubmissions();
    Submission getSubmissionById(Integer id);
    Submission createSubmission(Submission submission);
    Submission updateSubmission(Integer id, Submission submission);
    void deleteSubmission(Integer id);
    AverageMarks calculateAverageMarks(Integer id);
    List<Submission> getAllSubmissionsByUserId(Integer userId);
    List<AverageMarks> averageGradeOfEachStudentInACourse(Integer courseId);
}
