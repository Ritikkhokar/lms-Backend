package com.example.lmsProject.ServiceImpl;

import com.example.lmsProject.Controller.AuthController;
import com.example.lmsProject.Repository.SubmissionRepository;
import com.example.lmsProject.dto.AverageMarks;
import com.example.lmsProject.dto.UserDto;
import com.example.lmsProject.entity.Enrollment;
import com.example.lmsProject.entity.Submission;
import com.example.lmsProject.service.EnrollmentService;
import com.example.lmsProject.service.SubmissionService;
import com.example.lmsProject.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SubmissionServiceImpl implements SubmissionService {

    private static final Logger logger = LoggerFactory.getLogger(SubmissionServiceImpl.class);
    private final SubmissionRepository submissionRepository;
    private final EnrollmentService enrollmentService;
    private final UserService userService;

    public SubmissionServiceImpl(
            SubmissionRepository repo, EnrollmentService enrollmentService, UserService userService
    ) {
        this.submissionRepository = repo;
        this.enrollmentService = enrollmentService;
        this.userService = userService;
    }

    @Override
    public List<Submission> getAllSubmissions() {
        return submissionRepository.findAll();
    }

    @Override
    public Submission getSubmissionById(Integer id) {
        return submissionRepository.findById(id).orElse(null);
    }

    @Override
    public Submission createSubmission(Submission submission) {
        return submissionRepository.save(submission);
    }

    @Override
    public Submission updateSubmission(Integer id, Submission submission) {
        return submissionRepository.findById(id).map(existing -> {
            existing.setAssignment(submission.getAssignment());
            existing.setStudent(submission.getStudent());
            existing.setSubmissionUrl(submission.getSubmissionUrl());
            existing.setSubmittedAt(submission.getSubmittedAt());
            existing.setGrade(submission.getGrade());
            existing.setFeedback(submission.getFeedback());
            return submissionRepository.save(existing);
        }).orElse(null);
    }

    @Override
    public void deleteSubmission(Integer id) {
        submissionRepository.deleteById(id);
    }

    @Override
    public AverageMarks calculateAverageMarks(Integer userId) {
        try {
            List<Submission> submissions = getAllSubmissionsByUserId(userId);
            int totalMarks = 0;
            int marksObtained = 0;

            for (Submission submission : submissions) {
                if (Boolean.TRUE.equals(submission.getIs_graded())) {
                    totalMarks += (submission.getMaximumGrade() != null) ? submission.getMaximumGrade().intValue() : 100;
                    marksObtained += (submission.getGrade() != null) ? submission.getGrade().intValue() : 0;
                }
            }
            if (totalMarks == 0) {
                return new AverageMarks();
            }
            int averagePercentage = (int) (((double) marksObtained / totalMarks) * 100);
            return new AverageMarks(new UserDto(), totalMarks, marksObtained, averagePercentage);
        } catch (Exception e) {
            throw new RuntimeException("Error occurred", e);
        }
    }

    @Override
    public List<Submission> getAllSubmissionsByUserId(Integer userId) {
        return submissionRepository.findByStudent_UserId(userId);
    }

    @Override
    public List<AverageMarks> averageGradeOfEachStudentInACourse(Integer courseId) {
        List<Enrollment> enrollments = enrollmentService.getAllEnrollmentsByCourseId(courseId);
        List<AverageMarks> averageMarksOfStudents =  new ArrayList<>();
        for(Enrollment enrollment : enrollments){
            AverageMarks averageMarks = calculateAverageMarks(enrollment.getStudent().getUserId());
            if(averageMarks != null){
                averageMarks.setUserDto(userService.convertUserToUserDto(enrollment.getStudent()));
                averageMarksOfStudents.add(averageMarks);
            }
        }
        return averageMarksOfStudents;
    }
}
