package com.example.lmsProject.ServiceImpl;

import com.example.lmsProject.Repository.EnrollmentRepository;
import com.example.lmsProject.entity.Enrollment;
import com.example.lmsProject.service.EnrollmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EnrollmentServiceImpl implements EnrollmentService {

    private static final Logger logger = LoggerFactory.getLogger(EnrollmentServiceImpl.class);
    private final EnrollmentRepository enrollmentRepository;

    public EnrollmentServiceImpl(EnrollmentRepository repo) {
        this.enrollmentRepository = repo;
    }

    @Override
    public List<Enrollment> getAllEnrollments() {
        return enrollmentRepository.findAll();
    }

    @Override
    public Enrollment getEnrollmentById(Integer id) {
        return enrollmentRepository.findById(id).orElse(null);
    }

    @Override
    public Enrollment createEnrollment(Enrollment enrollment) {
        enrollment.setEnrolledAt(LocalDateTime.now());
        return enrollmentRepository.save(enrollment);
    }

    @Override
    public Enrollment updateEnrollment(Integer id, Enrollment enrollment) {
        return enrollmentRepository.findById(id)
                .map(existing -> {
                    existing.setStudent(enrollment.getStudent());
                    existing.setCourse(enrollment.getCourse());
                    existing.setEnrolledBy(enrollment.getEnrolledBy());
                    return enrollmentRepository.save(existing);
                }).orElse(null);
    }

    @Override
    public void deleteEnrollment(Integer id) {
        enrollmentRepository.deleteById(id);
    }

    @Override
    public List<Enrollment> getAllEnrollmentsByCourseId(Integer courseId) {
        return enrollmentRepository.findByCourse_CourseId(courseId);
    }
}
