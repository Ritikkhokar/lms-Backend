package com.example.lmsProject.Repository;

import com.example.lmsProject.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubmissionRepository extends JpaRepository<Submission, Integer> {}

