package com.example.lmsProject.Repository;

import com.example.lmsProject.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Integer> {}

