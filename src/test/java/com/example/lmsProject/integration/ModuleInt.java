package com.example.lmsProject.integration;

import com.example.lmsProject.Controller.ModuleController;
import com.example.lmsProject.entity.Course;
import com.example.lmsProject.entity.Module;
import com.example.lmsProject.service.ModuleService;
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
class ModuleInt {

    private MockMvc mvc;

    @Mock private ModuleService moduleService;

    private ModuleController moduleController;

    @BeforeEach
    void setup() {
        moduleController = new ModuleController(moduleService);
        mvc = MockMvcBuilders.standaloneSetup(moduleController).build();
    }


    @Test
    void createModule_ok_returnsCreatedEntity_nestedCourse() throws Exception {

        var course = new Course();
        course.setCourseId(301);
        var created = new Module();
        created.setModuleId(401);
        created.setTitle("Week 1 - Intro");
        created.setDescription("Basics of Java");
        created.setCourse(course);
        when(moduleService.createModule(any(Module.class))).thenReturn(created);

        mvc.perform(post("/api/modules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
              {"title":"Week 1 - Intro","description":"Basics of Java","course":{"courseId":301}}
            """))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.moduleId").value(401))
                .andExpect(jsonPath("$.title").value("Week 1 - Intro"))
                .andExpect(jsonPath("$.description").value("Basics of Java"))
                .andExpect(jsonPath("$.course.courseId").value(301)); // nested shape
    }


    @Test
    void getModuleById_notFound_returns404() throws Exception {
        when(moduleService.getModuleById(eq(9999))).thenReturn(null);

        mvc.perform(get("/api/modules/{id}", 9999))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
}
