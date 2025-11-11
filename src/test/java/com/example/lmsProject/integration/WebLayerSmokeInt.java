package com.example.lmsProject.integration;

import com.example.lmsProject.Controller.AuthController;
import com.example.lmsProject.Controller.RoleController;
import com.example.lmsProject.Controller.UserController;
import com.example.lmsProject.entity.Role;
import com.example.lmsProject.entity.User;
import com.example.lmsProject.service.RoleService;
import com.example.lmsProject.service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@ExtendWith(MockitoExtension.class)
class WebLayerSmokeInt {

    private MockMvc mvc;

    @Mock
    private UserService userService;
    @Mock
    private RoleService roleService;

    private AuthController authController;
    private UserController userController;
    private RoleController roleController;

    @BeforeEach
    void setup() {
        authController = new AuthController(userService);
        userController = new UserController(userService);
        roleController = new RoleController(roleService);

        mvc = MockMvcBuilders
                .standaloneSetup(authController, userController, roleController)
                .build();
    }

    @Test
    void auth_login_ok_returnsTokenAndUser() throws Exception {
        var role = new Role();
        role.setRoleId(1);
        role.setRoleName("Admin");

        var userEntity = new User();
        userEntity.setUserId(1001);
        userEntity.setFullName("Admin One");
        userEntity.setEmail("admin@test.edu");
        userEntity.setPasswordHash("Admin@123");
        userEntity.setRole(role);
        userEntity.setCreatedAt(LocalDateTime.now());


        when(userService.findByEmail(eq("admin@test.edu"))).thenReturn(userEntity);

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                  {"email":"admin@test.edu","password":"Admin@123"}
                                """))
                .andExpect(status().isOk())

                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.user.userId").value(1001))
                .andExpect(jsonPath("$.user.email").value("admin@test.edu"))
                .andExpect(jsonPath("$.user.roleName").value("Admin"));
    }


    @Test
    void users_create_ok_returnsUserEntity() throws Exception {
        var teacherRole = new Role();
        teacherRole.setRoleId(2);
        teacherRole.setRoleName("Teacher");

        var createdUser = new User();
        createdUser.setUserId(2002);
        createdUser.setFullName("Priya Sharma");
        createdUser.setEmail("priya.teacher@example.com");
        createdUser.setPasswordHash("Teach@123");
        createdUser.setRole(teacherRole);
        createdUser.setCreatedAt(LocalDateTime.now());

        when(userService.createUser(any(User.class))).thenReturn(createdUser);

        mvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                  {"fullName":"Priya Sharma","email":"priya.teacher@example.com","passwordHash":"Teach@123","role":{"roleId":2}}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(2002))
                .andExpect(jsonPath("$.email").value("priya.teacher@example.com"))
                .andExpect(jsonPath("$.fullName").value("Priya Sharma"))
                .andExpect(jsonPath("$.role.roleName").value("Teacher"));
    }


    @Test
    void roles_list_ok_returnsArray() throws Exception {
        var r1 = new Role();
        r1.setRoleId(1);
        r1.setRoleName("Admin");
        var r2 = new Role();
        r2.setRoleId(2);
        r2.setRoleName("Teacher");
        var r3 = new Role();
        r3.setRoleId(3);
        r3.setRoleName("Student");

        when(roleService.getAllRoles()).thenReturn(List.of(r1, r2, r3));

        mvc.perform(get("/api/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].roleName").value("Admin"))
                .andExpect(jsonPath("$[1].roleName").value("Teacher"))
                .andExpect(jsonPath("$[2].roleName").value("Student"));
    }
}
