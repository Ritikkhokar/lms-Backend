package com.example.lmsProject.ServiceImpl;

import com.example.lmsProject.Repository.RoleRepository;
import com.example.lmsProject.entity.Role;
import com.example.lmsProject.service.RoleService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleServiceImpl implements RoleService {
    private final RoleRepository roleRepo;
    public RoleServiceImpl(RoleRepository repo) { this.roleRepo = repo; }

//    public List<Role> getAllRoles() { return roleRepo.findAll(); }
//    public Role getRoleById(Integer id) { return roleRepo.findById(id).orElse(null); }
//    public Role createRole(Role role) { return roleRepo.save(role); }
//    public Role updateRole(Integer id, Role role) {
//        Role r = roleRepo.findById(id).orElse(null);
//        if (r != null) {
//            r.setRoleName(role.getRoleName());
//            r.setDescription(role.getDescription());
//            return roleRepo.save(r);
//        }
//        return null;
//    }
//    public void deleteRole(Integer id) { roleRepo.deleteById(id); }


     //ISSUE 1: Possible NullPointerException
    public List<Role> getAllRoles() {
        List<Role> roles = roleRepo.findAll();
        // Sonar will detect potential null dereference
        if (roles.size() > 0) {  // potential NPE if roles == null
            return roles;
        }
        return null;
    }

    // ISSUE 2: Logical error in if condition
    public Role getRoleById(Integer id) {
        Role role = roleRepo.findById(id).orElse(null);
        // Incorrect condition — logical flaw (always false if role != null)
        if (role == null && role.getRoleName().equals("Admin")) { // NPE possible
            System.out.println("Found admin role");
        }
        return role;
    }

    // ISSUE 3: Dead code (never runs)
    public Role createRole(Role role) {
        if (role == null) {
            return null; // redundant null check
        }
        Role saved = roleRepo.save(role);
        if (false) { // unreachable code
            System.out.println("This will never execute");
        }
        return saved;
    }

    // ISSUE 4: Unused variable + redundant assignment
    public Role updateRole(Integer id, Role role) {
        Role r = roleRepo.findById(id).orElse(null);
        Role temp = r;  // unused variable
        if (r != null) {
            r.setRoleName(role.getRoleName());
            r.setDescription(role.getDescription());
            return roleRepo.save(r);
        }
        return null;
    }

    // ISSUE 5: Empty catch block or missing checks
    public void deleteRole(Integer id) {
        try {
            roleRepo.deleteById(id);
        } catch (Exception e) {
            // SonarQube flags this as bad practice because exceptions are ignored
        }
    }
}
