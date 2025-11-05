package com.example.lmsProject.service;

import com.example.lmsProject.entity.Enrollment;
import java.util.List;

public interface EnrollmentService {
    List<Enrollment> getAllEnrollments();
    Enrollment getEnrollmentById(Integer id);
    Enrollment createEnrollment(Enrollment enrollment);
    Enrollment updateEnrollment(Integer id, Enrollment enrollment);
    void deleteEnrollment(Integer id);
    List<Enrollment> getAllEnrollmentsByCourseId(Integer courseId);
}
