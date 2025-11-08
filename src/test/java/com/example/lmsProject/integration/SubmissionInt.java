package com.example.lmsProject.integration;

import com.example.lmsProject.Controller.SubmissionController;
import com.example.lmsProject.entity.Assignment;
import com.example.lmsProject.entity.Submission;
import com.example.lmsProject.entity.User;
import com.example.lmsProject.service.SubmissionService;
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
class SubmissionInt {

    private MockMvc mvc;

    @Mock
    private SubmissionService submissionService;

    private SubmissionController submissionController;

    @BeforeEach
    void setup() {
        submissionController = new SubmissionController(submissionService);
        mvc = MockMvcBuilders.standaloneSetup(submissionController).build();
    }


    @Test
    void createSubmission_ok_returnsCreatedEntity() throws Exception {
        var asg = new Assignment();
        asg.setAssignmentId(701);
        var student = new User();
        student.setUserId(1002);

        var created = new Submission();
        created.setSubmissionId(801);
        created.setAssignment(asg);
        created.setStudent(student);

        created.setSubmissionUrl("https://example.com/submissions/a1_u1002.pdf");

        when(submissionService.createSubmission(any(Submission.class))).thenReturn(created);

        mvc.perform(post("/api/submissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                  {
                                    "assignment": {"assignmentId": 701},
                                    "student": {"userId": 1002},
                                    "submissionUrl": "https://example.com/submissions/a1_u1002.pdf"
                                  }
                                """))
                .andDo(print())

                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.submissionId").value(801))
                .andExpect(jsonPath("$.assignment.assignmentId").value(701))
                .andExpect(jsonPath("$.student.userId").value(1002))
                .andExpect(jsonPath("$.submissionUrl").value("https://example.com/submissions/a1_u1002.pdf"));
    }

    @Test
    void getSubmissionById_notFound_returns404() throws Exception {
        when(submissionService.getSubmissionById(eq(999_999))).thenReturn(null);

        mvc.perform(get("/api/submissions/{id}", 999_999))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
}
