package com.example.lmsProject.integration;

import com.example.lmsProject.Controller.ResourceController;
import com.example.lmsProject.entity.Course;
import com.example.lmsProject.entity.Module;
import com.example.lmsProject.entity.Resource;
import com.example.lmsProject.entity.User;
import com.example.lmsProject.service.ResourceService;
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
class ResourceInt {

    private MockMvc mvc;

    @Mock
    private ResourceService resourceService;

    private ResourceController resourceController;

    @BeforeEach
    void setup() {
        resourceController = new ResourceController(resourceService);
        mvc = MockMvcBuilders.standaloneSetup(resourceController).build();
    }


    @Test
    void createResource_ok_returnsCreatedEntity_withRelations() throws Exception {
        var course = new Course();
        course.setCourseId(301);

        var module = new Module();
        module.setModuleId(401);

        var uploader = new User();
        uploader.setUserId(1001);

        var created = new Resource();
        created.setResourceId(501);
        created.setTitle("SRS.pdf");
        created.setFileUrl("https://example.com/docs/SRS.pdf");
        created.setCourse(course);
        created.setModule(module);
        created.setUploadedBy(uploader);

        when(resourceService.createResource(any(Resource.class))).thenReturn(created);

        mvc.perform(post("/api/resources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                  {
                                    "title":"SRS.pdf",
                                    "fileUrl":"https://example.com/docs/SRS.pdf",
                                    "course":{"courseId":301},
                                    "module":{"moduleId":401},
                                    "uploadedBy":{"userId":1001}
                                  }
                                """))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.resourceId").value(501))
                .andExpect(jsonPath("$.title").value("SRS.pdf"))
                .andExpect(jsonPath("$.fileUrl").value("https://example.com/docs/SRS.pdf"))
                .andExpect(jsonPath("$.course.courseId").value(301))
                .andExpect(jsonPath("$.module.moduleId").value(401))
                .andExpect(jsonPath("$.uploadedBy.userId").value(1001));
    }


    @Test
    void getResourceById_notFound_returns404() throws Exception {
        when(resourceService.getResourceById(eq(99999))).thenReturn(null);

        mvc.perform(get("/api/resources/{id}", 99999))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
}
