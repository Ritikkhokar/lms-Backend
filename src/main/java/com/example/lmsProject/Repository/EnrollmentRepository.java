package com.example.lmsProject.Repository;

import com.example.lmsProject.entity.Course;
import com.example.lmsProject.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Integer> {
    List<Enrollment> findByCourse_CourseId(Integer userId);
}

