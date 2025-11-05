package com.example.lmsProject.ServiceImpl;

import com.example.lmsProject.Repository.CourseRepository;
import com.example.lmsProject.dto.CoursePerformance;
import com.example.lmsProject.entity.Course;
import com.example.lmsProject.entity.Enrollment;
import com.example.lmsProject.service.CourseService;
import com.example.lmsProject.service.EnrollmentService;
import com.example.lmsProject.service.SubmissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseServiceImpl implements CourseService {

    private static final Logger logger = LoggerFactory.getLogger(CourseServiceImpl.class);
    private final CourseRepository courseRepository;
    private final EnrollmentService enrollmentService;
    private final SubmissionService submissionService;

    public CourseServiceImpl(CourseRepository repo, EnrollmentService enrollmentService, SubmissionService submissionService) {
        this.courseRepository = repo;
        this.enrollmentService = enrollmentService;
        this.submissionService = submissionService;
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
        int sumOfAverageStudentGrades = 0;
        int studentLessThanThreshold = 0;
        if(course!= null){
         List<Enrollment> enrollments = enrollmentService.getAllEnrollmentsByCourseId(courseId);
         for(Enrollment enrollment : enrollments){
             sumOfAverageStudentGrades += submissionService.calculateAverageMarks(
                     enrollment.getStudent().getUserId()).getAveragePercentage();
             if(submissionService.calculateAverageMarks(
                     enrollment.getStudent().getUserId()).getAveragePercentage()<threshold){
                 studentLessThanThreshold++;
             }
         }
         int averageGradeOfClass = sumOfAverageStudentGrades/enrollments.size();
         return new CoursePerformance(course, averageGradeOfClass, studentLessThanThreshold);

        }
        return null;
    }
}
