package com.example.lmsProject.ServiceImpl;

import com.example.lmsProject.Repository.EnrollmentRepository;
import com.example.lmsProject.entity.Enrollment;
import com.example.lmsProject.exception.ResourceNotFoundException;
import com.example.lmsProject.service.EnrollmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
    @Cacheable(cacheNames = "enrollments")
    public List<Enrollment> getAllEnrollments() {
        logger.info("Fetching all enrollments from database (cache miss)");
        return enrollmentRepository.findAll();
    }

    @Override
    @Cacheable(cacheNames = "enrollmentById", key = "#id")
    public Enrollment getEnrollmentById(Integer id) {
        logger.info("Fetching enrollment by id {} from database (cache miss)", id);
        return enrollmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found with id: " + id));
    }

    @Override
    @CacheEvict(cacheNames = { "enrollments", "enrollmentById", "enrollmentsByCourse" }, allEntries = true)
    public Enrollment createEnrollment(Enrollment enrollment) {
        enrollment.setEnrolledAt(LocalDateTime.now());
        Enrollment saved = enrollmentRepository.save(enrollment);
        logger.info("Created enrollment with id {}, evicted enrollment caches", saved.getEnrollmentId());
        return saved;
    }

    @Override
    @CacheEvict(cacheNames = { "enrollments", "enrollmentById", "enrollmentsByCourse" }, allEntries = true)
    public Enrollment updateEnrollment(Integer id, Enrollment enrollment) {
        Enrollment existing = enrollmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found with id: " + id));

        if (enrollment.getStudent() != null) {
            existing.setStudent(enrollment.getStudent());
        }
        if (enrollment.getCourse() != null) {
            existing.setCourse(enrollment.getCourse());
        }
        if (enrollment.getEnrolledBy() != null) {
            existing.setEnrolledBy(enrollment.getEnrolledBy());
        }
        existing.setEnrolledAt(LocalDateTime.now());

        Enrollment updated = enrollmentRepository.save(existing);
        logger.info("Updated enrollment with id {}, evicted enrollment caches", id);
        return updated;
    }

    @Override
    @CacheEvict(cacheNames = { "enrollments", "enrollmentById", "enrollmentsByCourse" }, allEntries = true)
    public void deleteEnrollment(Integer id) {
        if (!enrollmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Enrollment not found with id: " + id);
        }
        enrollmentRepository.deleteById(id);
        logger.info("Deleted enrollment with id {}, evicted enrollment caches", id);
    }

    @Override
    @Cacheable(cacheNames = "enrollmentsByCourse", key = "#courseId")
    public List<Enrollment> getAllEnrollmentsByCourseId(Integer courseId) {
        logger.info("Fetching enrollments by courseId {} from database (cache miss)", courseId);
        return enrollmentRepository.findByCourse_CourseId(courseId);
    }
}
