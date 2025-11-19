package com.example.lmsProject.Repository;

import com.example.lmsProject.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubmissionRepository extends JpaRepository<Submission, Integer> {
    List<Submission> findByStudent_UserId(Integer userId);
    List<Submission> findByAssignment_AssignmentId(Integer userId);
    List<Submission> findByAssignment_AssignmentIdAndStudent_UserId(Integer assignmentId, Integer userId);
}


