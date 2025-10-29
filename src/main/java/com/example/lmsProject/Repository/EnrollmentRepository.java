package com.example.lmsProject.Repository;

import com.example.lmsProject.entity.Assignment;
import com.example.lmsProject.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Integer> {}

