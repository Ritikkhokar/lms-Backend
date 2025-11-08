package com.example.lmsProject.integration;

import com.example.lmsProject.Controller.AssignmentController;
import com.example.lmsProject.entity.Assignment;
import com.example.lmsProject.entity.Course;
import com.example.lmsProject.service.AssignmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AssignmentInt {

    private MockMvc mvc;

    @Mock
    private AssignmentService assignmentService;

    private AssignmentController assignmentController;

    @BeforeEach
    void setup() {
        assignmentController = new AssignmentController(assignmentService);
        mvc = MockMvcBuilders.standaloneSetup(assignmentController).build();
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
        created.setFileUrl("https://example.com/a1.pdf");
        created.setCourse(course);

        when(assignmentService.createAssignment(any(Assignment.class))).thenReturn(created);

        mvc.perform(post("/api/assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                  {
                                    "title":"Assignment 1",
                                    "description":"Basics of Java",
                                    "dueDate":"2025-11-30",
                                    "fileUrl":"https://example.com/a1.pdf",
                                    "course":{"courseId":301}
                                  }
                                """))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.assignmentId").value(701))
                .andExpect(jsonPath("$.title").value("Assignment 1"))
                .andExpect(jsonPath("$.description").value("Basics of Java"))
                .andExpect(jsonPath("$.dueDate").value("2025-11-30"))
                .andExpect(jsonPath("$.fileUrl").value("https://example.com/a1.pdf"))
                .andExpect(jsonPath("$.course.courseId").value(301));
    }


    @Test
    void getAssignmentById_notFound_returns404() throws Exception {
        when(assignmentService.getAssignmentById(eq(999_999))).thenReturn(null);

        mvc.perform(get("/api/assignments/{id}", 999_999))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
}
