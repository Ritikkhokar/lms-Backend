package com.example.lmsProject.user;



import com.example.lmsProject.dto.UserDto; // <-- ensure this import matches your package
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserDtoJsonContractTest {

    private final ObjectMapper om = new ObjectMapper();

    @Test
    void userDto_serializesAndDeserializes() throws Exception {
        UserDto dto = new UserDto();
        // TODO set fields to match your UserDto
        // dto.setId(1L);
        // dto.setFullName("Priya Sharma");
        // dto.setEmail("priya.teacher@example.com");
        // dto.setRole("TEACHER");

        String json = om.writeValueAsString(dto);
        assertNotNull(json);
        assertTrue(json.startsWith("{"), "Should serialize to JSON object");

        UserDto back = om.readValue(json, UserDto.class);
        assertNotNull(back);

        // TODO assert your actual fields
        // assertEquals(dto.getId(), back.getId());
        // assertEquals(dto.getFullName(), back.getFullName());
        // assertEquals(dto.getEmail(), back.getEmail());
        // assertEquals(dto.getRole(), back.getRole());
    }

    @Test
    void userDto_hasExpectedJsonKeys() throws Exception {
        UserDto dto = new UserDto();
        // dto.setFullName("Priya Sharma");
        // dto.setEmail("priya.teacher@example.com");

        String json = om.writeValueAsString(dto);
        // TODO: make these match your actual JSON property names
        // assertTrue(json.contains("\"fullName\""));
        // assertTrue(json.contains("\"email\""));
    }
}
