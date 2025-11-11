package com.example.lmsProject.ServiceImpl;

import com.example.lmsProject.Repository.CourseRepository;
import com.example.lmsProject.Repository.SubmissionRepository;
import com.example.lmsProject.dto.AverageMarks;
import com.example.lmsProject.dto.CoursePerformance;
import com.example.lmsProject.dto.UserDto;
import com.example.lmsProject.entity.Course;
import com.example.lmsProject.entity.Enrollment;
import com.example.lmsProject.entity.Submission;
import com.example.lmsProject.service.CourseService;
import com.example.lmsProject.service.EnrollmentService;
import com.example.lmsProject.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CourseServiceImpl implements CourseService {

    private static final Logger logger = LoggerFactory.getLogger(CourseServiceImpl.class);
    private final CourseRepository courseRepository;
    private final EnrollmentService enrollmentService;
    private final SubmissionRepository submissionRepository;
    private final UserService userService;

    public CourseServiceImpl(CourseRepository repo, EnrollmentService enrollmentService, SubmissionRepository submissionRepository, UserService userService) {
        this.courseRepository = repo;
        this.enrollmentService = enrollmentService;
        this.submissionRepository = submissionRepository;
        this.userService = userService;
    }

    @Override
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    @Override
    public Course getCourseById(Integer id) {
        return courseRepository.findById(id).orElse(null);
    }

    @Override
    public Course createCourse(Course course) {
        return courseRepository.save(course);
    }

    @Override
    public Course updateCourse(Integer id, Course course) {
        return courseRepository.findById(id).map(existingCourse -> {
            existingCourse.setTitle(course.getTitle());
            existingCourse.setDescription(course.getDescription());
            existingCourse.setCreatedBy(course.getCreatedBy());
            existingCourse.setCreatedAt(course.getCreatedAt());
            return courseRepository.save(existingCourse);
        }).orElse(null);
    }

    @Override
    public void deleteCourse(Integer id) {
        courseRepository.deleteById(id);
    }

    @Override
    public List<Course> getCoursesByUserId(Integer userId) {
        return courseRepository.findByCreatedBy_UserId(userId);
    }

    @Override
    public CoursePerformance getCoursePerformance(Integer courseId, Integer threshold) {
        Course course = courseRepository.findById(courseId).orElse(null);
        if (course == null) {
            return null;
        }

        List<Enrollment> enrollments = enrollmentService.getAllEnrollmentsByCourseId(courseId);
        if (enrollments == null || enrollments.isEmpty()) {
            // Return zero averages if no enrollments found
            return new CoursePerformance(course, 0, 0);
        }

        int sumOfAverageGrades = 0;
        int countStudentsBelowThreshold = 0;

        for (Enrollment enrollment : enrollments) {
            AverageMarks avgMarks = calculateAverageMarks(enrollment.getStudent().getUserId());
            int averagePercentage = (avgMarks != null) ? avgMarks.getAveragePercentage() : 0;

            sumOfAverageGrades += averagePercentage;

            if (averagePercentage < threshold) {
                countStudentsBelowThreshold++;
            }
        }

        // Prevent division by zero by guarding enrollments size > 0
        int averageGradeOfClass = sumOfAverageGrades / enrollments.size();

        return new CoursePerformance(course, averageGradeOfClass, countStudentsBelowThreshold);
    }

    @Override
    public AverageMarks calculateAverageMarks(Integer userId) {
        try {
            List<Submission> submissions = submissionRepository.findByStudent_UserId(userId);
            if (submissions == null || submissions.isEmpty()) {
                return new AverageMarks();
            }

            int totalMaxMarks = 0;
            int totalMarksObtained = 0;

            for (Submission submission : submissions) {
                if (Boolean.TRUE.equals(submission.getIs_graded())) {
                    int maxGrade = (submission.getMaximumGrade() != null) ?
                            submission.getMaximumGrade().intValue() : 100;
                    int grade = (submission.getGrade() != null) ? submission.getGrade().intValue() : 0;

                    totalMaxMarks += maxGrade;
                    totalMarksObtained += grade;
                }
            }

            if (totalMaxMarks == 0) {
                return new AverageMarks();
            }

            int averagePercentage = (int) (((double) totalMarksObtained / totalMaxMarks) * 100);

            return new AverageMarks(new UserDto(), totalMaxMarks, totalMarksObtained, averagePercentage);
        } catch (Exception e) {
            throw new RuntimeException("Error occurred while calculating average marks for userId " + userId, e);
        }
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
