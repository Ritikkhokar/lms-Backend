package com.example.lmsProject.dto;

import com.example.lmsProject.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private Integer userId;
    private String fullName;
    private String email;
    private String roleName;
}
