package com.example.lmsProject.integration;

import com.example.lmsProject.Controller.AssignmentController;
import com.example.lmsProject.entity.Assignment;
import com.example.lmsProject.entity.Course;
import com.example.lmsProject.service.AssignmentService;
import com.example.lmsProject.service.SubmissionService;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.mockito.Mock;

@ExtendWith(MockitoExtension.class)
class AssignmentInt {

    private MockMvc mvc;

    @Mock private AssignmentService assignmentService;
    @Mock private SubmissionService submissionService;

    private AssignmentController assignmentController;

    @BeforeEach
    void setup() {
        // Controller needs BOTH services
        assignmentController = new AssignmentController(assignmentService, submissionService);

        // Configure Jackson so LocalDate -> "yyyy-MM-dd" (string), not [yyyy,mm,dd]
        ObjectMapper om = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();

        mvc = MockMvcBuilders
                .standaloneSetup(assignmentController)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(om))
                .build();
    }

    @Test
    void createAssignment_ok_returnsCreatedEntity() throws Exception {
        var course = new Course();
        course.setCourseId(301);

        var created = new Assignment();
        created.setAssignmentId(701);
        created.setTitle("Assignment 1");
        created.setDescription("Basics of Java");
        created.setDueDate(LocalDate.parse("2025-11-30"));
        // Align with your entity/DTO field name
        created.setFileKey("https://example.com/a1.pdf");
        created.setCourse(course);

        // Use any() without class to avoid type inference issues (e.g., AssignmentDto vs Assignment)
        Mockito.when(assignmentService.createAssignment(Mockito.any())).thenReturn(created);

        mvc.perform(post("/api/assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "title": "Assignment 1",
                              "description": "Basics of Java",
                              "dueDate": "2025-11-30",
                              "fileKey": "https://example.com/a1.pdf",
                              "course": { "courseId": 301 }
                            }
                        """))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.assignmentId").value(701))
                .andExpect(jsonPath("$.title").value("Assignment 1"))
                .andExpect(jsonPath("$.description").value("Basics of Java"))
                // now serialized as a string thanks to Jackson config above
                .andExpect(jsonPath("$.dueDate").value("2025-11-30"))
                // assert fileKey (not fileUrl)
                .andExpect(jsonPath("$.fileKey").value("https://example.com/a1.pdf"))
                .andExpect(jsonPath("$.course.courseId").value(301));
    }

    @Test
    void getAssignmentById_notFound_returns404() throws Exception {
        Mockito.when(assignmentService.getAssignmentById(eq(999_999))).thenReturn(null);

        mvc.perform(get("/api/assignments/{id}", 999_999))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
}
