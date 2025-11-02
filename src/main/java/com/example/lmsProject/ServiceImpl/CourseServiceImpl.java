package com.example.lmsProject.ServiceImpl;

import com.example.lmsProject.Controller.AuthController;
import com.example.lmsProject.Repository.CourseRepository;
import com.example.lmsProject.entity.Course;
import com.example.lmsProject.service.CourseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseServiceImpl implements CourseService {

    private static final Logger logger = LoggerFactory.getLogger(CourseServiceImpl.class);
    private final CourseRepository courseRepository;

    public CourseServiceImpl(CourseRepository repo) {
        this.courseRepository = repo;
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
}
