package com.example.lmsProject.service;
import com.example.lmsProject.entity.Course;
import java.util.List;

public interface CourseService {
    List<Course> getAllCourses();
    Course getCourseById(Integer id);
    Course createCourse(Course course);
    Course updateCourse(Integer id, Course course);
    void deleteCourse(Integer id);
}
