package com.example.lmsProject.service;
import com.example.lmsProject.entity.Role;
import java.util.List;

public interface RoleService {
    List<Role> getAllRoles();
    Role getRoleById(Integer id);
    Role createRole(Role role);
    Role updateRole(Integer id, Role role);
    void deleteRole(Integer id);
}
