package com.example.lmsProject.integration;

import com.example.lmsProject.Controller.EnrollmentController;
import com.example.lmsProject.entity.Course;
import com.example.lmsProject.entity.Enrollment;
import com.example.lmsProject.entity.User;
import com.example.lmsProject.service.EnrollmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@ExtendWith(MockitoExtension.class)
class EnrollmentInt {

    private MockMvc mvc;

    @Mock
    private EnrollmentService enrollmentService;

    private EnrollmentController enrollmentController;

    @BeforeEach
    void setup() {
        enrollmentController = new EnrollmentController(enrollmentService);
        mvc = MockMvcBuilders.standaloneSetup(enrollmentController).build();
    }


    @Test
    void createEnrollment_ok_returnsCreatedEntity() throws Exception {
        var student = new User();
        student.setUserId(1002);
        var course = new Course();
        course.setCourseId(301);
        var admin = new User();
        admin.setUserId(1001);

        var created = new Enrollment();
        created.setEnrollmentId(601);
        created.setStudent(student);
        created.setCourse(course);
        created.setEnrolledBy(admin);

        when(enrollmentService.createEnrollment(any(Enrollment.class))).thenReturn(created);

        mvc.perform(post("/api/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                  {"student":{"userId":1002},"course":{"courseId":301},"enrolledBy":{"userId":1001}}
                                """))
                .andDo(print())
                // If your controller returns 200 OK, change to .isOk()
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.enrollmentId").value(601))
                .andExpect(jsonPath("$.student.userId").value(1002))
                .andExpect(jsonPath("$.course.courseId").value(301))
                .andExpect(jsonPath("$.enrolledBy.userId").value(1001));
    }

    @Test
    void getEnrollmentById_notFound_returns404() throws Exception {
        when(enrollmentService.getEnrollmentById(eq(999999))).thenReturn(null);

        mvc.perform(get("/api/enrollments/{id}", 999999))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
}
