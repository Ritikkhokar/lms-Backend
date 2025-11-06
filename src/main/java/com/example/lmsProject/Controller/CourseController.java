package com.example.lmsProject.Controller;

import com.example.lmsProject.dto.CoursePerformance;
import com.example.lmsProject.entity.Course;
import com.example.lmsProject.service.CourseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private static final Logger logger = LoggerFactory.getLogger(CourseController.class);
    private final CourseService courseService;

    public CourseController(CourseService service) {
        this.courseService = service;
    }

    @GetMapping
    public List<Course> getAllCourses() {
        return courseService.getAllCourses();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Course> getCourseById(@PathVariable Integer id) {
        Course course = courseService.getCourseById(id);
        if (course == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(course);
    }

    @PostMapping
    public ResponseEntity<Course> createCourse(@RequestBody Course course) {
        Course created = courseService.createCourse(course);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Course> updateCourse(@PathVariable Integer id, @RequestBody Course course) {
        Course updated = courseService.updateCourse(id, course);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Integer id) {
        courseService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/coursesByUserId/{id}")
    public ResponseEntity<List<Course>> getCoursesByUserId(@PathVariable Integer id){
       return ResponseEntity.ok( courseService.getCoursesByUserId(id));
    }

    @GetMapping("/coursePerformanceByCourseId/{id}/{threshold}")
    public ResponseEntity<CoursePerformance> coursePerformanceByCourseId(
            @PathVariable Integer id, @PathVariable Integer threshold
    ){
        return ResponseEntity.ok(courseService.getCoursePerformance(id, threshold));
    }

}
