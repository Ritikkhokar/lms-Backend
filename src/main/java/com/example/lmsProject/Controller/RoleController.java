package com.example.lmsProject.Controller;

import com.example.lmsProject.entity.Role;
import com.example.lmsProject.service.RoleService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class RoleController {
    private final RoleService roleService;
    public RoleController(RoleService service) { this.roleService = service; }

    @GetMapping
    public List<Role> getAllRoles() { return roleService.getAllRoles(); }

    @GetMapping("/{id}")
    public Role getRole(@PathVariable Integer id) { return roleService.getRoleById(id); }

    @PostMapping
    public Role createRole(@RequestBody Role role) { return roleService.createRole(role); }

    @PutMapping("/{id}")
    public Role updateRole(@PathVariable Integer id, @RequestBody Role role) {
        return roleService.updateRole(id, role);
    }

    @DeleteMapping("/{id}")
    public void deleteRole(@PathVariable Integer id) { roleService.deleteRole(id); }
}
