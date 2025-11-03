package com.example.lmsProject.Repository;

import com.example.lmsProject.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Integer> {
    List<Course> findByCreatedBy_UserId(Integer userId);
}

