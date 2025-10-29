package com.example.lmsProject.ServiceImpl;

import com.example.lmsProject.Repository.EnrollmentRepository;
import com.example.lmsProject.entity.Enrollment;
import com.example.lmsProject.service.EnrollmentService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class EnrollmentServiceImpl implements EnrollmentService {

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
        return enrollmentRepository.save(enrollment);
    }

    @Override
    public Enrollment updateEnrollment(Integer id, Enrollment enrollment) {
        return enrollmentRepository.findById(id)
                .map(existing -> {
                    existing.setStudent(enrollment.getStudent());
                    existing.setCourse(enrollment.getCourse());
                    existing.setEnrolledAt(enrollment.getEnrolledAt());
                    existing.setEnrolledBy(enrollment.getEnrolledBy());
                    return enrollmentRepository.save(existing);
                }).orElse(null);
    }

    @Override
    public void deleteEnrollment(Integer id) {
        enrollmentRepository.deleteById(id);
    }
}
