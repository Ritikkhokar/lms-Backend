package com.example.lmsProject.integration;


import com.example.lmsProject.Controller.CourseController;
import com.example.lmsProject.entity.Course;
import com.example.lmsProject.service.CourseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@ExtendWith(MockitoExtension.class)
class CourseInt {

    private MockMvc mvc;

    @Mock
    private CourseService courseService;

    private CourseController courseController;

    @BeforeEach
    void setup() {
        courseController = new CourseController(courseService);
        mvc = MockMvcBuilders.standaloneSetup(courseController).build();
    }

    @Test
    void createCourse_ok_returnsCreatedEntity() throws Exception {
        var created = new Course();
        created.setCourseId(301);
        created.setTitle("Java Fundamentals");
        created.setDescription("Learn Java basics.");
        created.setCreatedAt(LocalDateTime.now());

        when(courseService.createCourse(any(Course.class))).thenReturn(created);

        mvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                  {"title":"Java Fundamentals","description":"Learn Java basics."}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.courseId").value(301))
                .andExpect(jsonPath("$.title").value("Java Fundamentals"))
                .andExpect(jsonPath("$.description").value("Learn Java basics."));
    }


    @Test
    void getCourseById_notFound_returns404() throws Exception {
        when(courseService.getCourseById(eq(999))).thenReturn(null);

        mvc.perform(get("/api/courses/{id}", 999))
                .andExpect(status().isNotFound());
    }
}
