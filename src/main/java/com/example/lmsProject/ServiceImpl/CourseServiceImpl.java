package com.example.lmsProject.ServiceImpl;

import com.example.lmsProject.Repository.AssignmentRepository;
import com.example.lmsProject.Repository.CourseRepository;
import com.example.lmsProject.Repository.SubmissionRepository;
import com.example.lmsProject.dto.AverageMarks;
import com.example.lmsProject.dto.CoursePerformance;
import com.example.lmsProject.dto.UserDto;
import com.example.lmsProject.entity.Assignment;
import com.example.lmsProject.entity.Course;
import com.example.lmsProject.entity.Enrollment;
import com.example.lmsProject.entity.Submission;
import com.example.lmsProject.exception.ResourceNotFoundException;
import com.example.lmsProject.service.CourseService;
import com.example.lmsProject.service.EnrollmentService;
import com.example.lmsProject.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CourseServiceImpl implements CourseService {

    private static final Logger logger = LoggerFactory.getLogger(CourseServiceImpl.class);
    private final CourseRepository courseRepository;
    private final EnrollmentService enrollmentService;
    private final SubmissionRepository submissionRepository;
    private final UserService userService;
    private final AssignmentRepository assignmentRepository;

    public CourseServiceImpl(CourseRepository repo, EnrollmentService enrollmentService, SubmissionRepository submissionRepository, UserService userService, AssignmentRepository assignmentRepository) {
        this.courseRepository = repo;
        this.enrollmentService = enrollmentService;
        this.submissionRepository = submissionRepository;
        this.userService = userService;
        this.assignmentRepository = assignmentRepository;
    }

    @Override
    @Cacheable(cacheNames = "courses")
    public List<Course> getAllCourses() {
        logger.info("Fetching all courses from database (cache miss)");
        return courseRepository.findAll();
    }

    @Override
    @Cacheable(cacheNames = "courseById", key = "#id")
    public Course getCourseById(Integer id) {
        logger.info("Fetching course by id {} from database (cache miss)", id);
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
    }

    @Override
    @CacheEvict(cacheNames = { "courses", "courseById", "coursesByUser" }, allEntries = true)
    public Course createCourse(Course course) {
        course.setCreatedAt(LocalDateTime.now());
        Course saved = courseRepository.save(course);
        logger.info("Created course with id {}, evicted course caches", saved.getCourseId());
        return saved;
    }

    @Override
    @CacheEvict(cacheNames = { "courses", "courseById", "coursesByUser" }, allEntries = true)
    public Course updateCourse(Integer id, Course course) {
        Course existingCourse = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));

        if (course.getTitle() != null) {
            existingCourse.setTitle(course.getTitle());
        }
        if (course.getDescription() != null) {
            existingCourse.setDescription(course.getDescription());
        }
        if (course.getCreatedBy() != null) {
            existingCourse.setCreatedBy(course.getCreatedBy());
        }

        Course updated = courseRepository.save(existingCourse);
        logger.info("Updated course with id {}, evicted course caches", id);
        return updated;
    }

    @Override
    @CacheEvict(cacheNames = { "courses", "courseById", "coursesByUser" }, allEntries = true)
    public void deleteCourse(Integer id) {
        if (!courseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Course not found with id: " + id);
        }
        courseRepository.deleteById(id);
        logger.info("Deleted course with id {}, evicted course caches", id);
    }

    @Override
    @Cacheable(cacheNames = "coursesByUser", key = "#userId")
    public List<Course> getCoursesByUserId(Integer userId) {
        logger.info("Fetching courses by creator userId {} from database (cache miss)", userId);
        return courseRepository.findByCreatedBy_UserId(userId);
    }

    @Override
    public CoursePerformance getCoursePerformance(Integer courseId, Integer threshold) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

        List<Enrollment> enrollments = enrollmentService.getAllEnrollmentsByCourseId(courseId);
        if (enrollments == null || enrollments.isEmpty()) {
            return new CoursePerformance(course, 0, 0);
        }

        int sumOfAverageGrades = 0;
        int countStudentsBelowThreshold = 0;

        for (Enrollment enrollment : enrollments) {
            AverageMarks avgMarks = calculateAverageMarksInACourse(enrollment.getStudent().getUserId(), courseId);
            int averagePercentage = (avgMarks != null) ? avgMarks.getAveragePercentage() : 0;

            sumOfAverageGrades += averagePercentage;

            if (averagePercentage < threshold) {
                countStudentsBelowThreshold++;
            }
        }

        int averageGradeOfClass = sumOfAverageGrades / enrollments.size();
        return new CoursePerformance(course, averageGradeOfClass, countStudentsBelowThreshold);
    }

    @Override
    public AverageMarks calculateAverageMarksInACourse(Integer userId, Integer courseId) {
        try {
            List<Assignment> assignments = assignmentRepository.findByCourse_CourseId(courseId);
            if (assignments == null || assignments.isEmpty()) {
                return new AverageMarks();
            }

            List<Submission> totalSubmissions = new ArrayList<>();
            for (Assignment assignment : assignments) {
                List<Submission> submissions = submissionRepository
                        .findByAssignment_AssignmentIdAndStudent_UserId(
                                assignment.getAssignmentId(), userId
                        );

                if (submissions != null && !submissions.isEmpty()) {
                    totalSubmissions.addAll(submissions);
                }
            }

            int totalMaxMarks = 0;
            int totalMarksObtained = 0;

            for (Submission submission : totalSubmissions) {
                if (Boolean.TRUE.equals(submission.getIs_graded())) {
                    int maxGrade = (submission.getMaximumGrade() != null)
                            ? submission.getMaximumGrade().intValue()
                            : 100;
                    int grade = (submission.getGrade() != null)
                            ? submission.getGrade().intValue()
                            : 0;

                    totalMaxMarks += maxGrade;
                    totalMarksObtained += grade;
                }
            }

            if (totalMaxMarks == 0) {
                return new AverageMarks();
            }

            int averagePercentage =
                    (int) (((double) totalMarksObtained / totalMaxMarks) * 100);

            return new AverageMarks(new UserDto(), totalMaxMarks, totalMarksObtained, averagePercentage);
        } catch (Exception e) {
            throw new RuntimeException("Error occurred while calculating average marks for userId " + userId, e);
        }
    }

    @Override
    public List<AverageMarks> averageGradeOfEachStudentInACourse(Integer courseId) {
        List<Enrollment> enrollments = enrollmentService.getAllEnrollmentsByCourseId(courseId);
        List<AverageMarks> averageMarksOfStudents = new ArrayList<>();
        for (Enrollment enrollment : enrollments) {
            AverageMarks averageMarks =
                    calculateAverageMarksInACourse(enrollment.getStudent().getUserId(), courseId);
            if (averageMarks != null) {
                averageMarks.setUserDto(
                        userService.convertUserToUserDto(enrollment.getStudent())
                );
                averageMarksOfStudents.add(averageMarks);
            }
        }
        return averageMarksOfStudents;
    }
}
