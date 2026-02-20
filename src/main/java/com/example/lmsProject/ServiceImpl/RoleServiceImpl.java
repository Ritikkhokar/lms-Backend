package com.example.lmsProject.ServiceImpl;

import com.example.lmsProject.Controller.AuthController;
import com.example.lmsProject.Controller.RoleController;
import com.example.lmsProject.Repository.RoleRepository;
import com.example.lmsProject.entity.Role;
import com.example.lmsProject.exception.ResourceNotFoundException;
import com.example.lmsProject.service.RoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleServiceImpl implements RoleService {

    private static final Logger logger = LoggerFactory.getLogger(RoleServiceImpl.class);
    private final RoleRepository roleRepo;
    public RoleServiceImpl(RoleRepository repo) { this.roleRepo = repo; }

    @Override
    @Cacheable(cacheNames = "roles")
    public List<Role> getAllRoles() {
        logger.info("Fetching all roles from database (cache miss)");
        return roleRepo.findAll();
    }

    @Override
    @Cacheable(cacheNames = "roleById", key = "#id")
    public Role getRoleById(Integer id) {
        logger.info("Fetching role by id {} from database (cache miss)", id);
        return roleRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));
    }

    @Override
    @CacheEvict(cacheNames = { "roles", "roleById" }, allEntries = true)
    public Role createRole(Role role) {
        Role created = roleRepo.save(role);
        logger.info("Created role with id {}, evicted role caches", created.getRoleId());
        return created;
    }

    @Override
    @CacheEvict(cacheNames = { "roles", "roleById" }, allEntries = true)
    public Role updateRole(Integer id, Role role) {
        Role existing = roleRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));

        if (role.getRoleName() != null) {
            existing.setRoleName(role.getRoleName());
        }
        if (role.getDescription() != null) {
            existing.setDescription(role.getDescription());
        }

        Role updated = roleRepo.save(existing);
        logger.info("Updated role with id {}, evicted role caches", id);
        return updated;
    }

    @Override
    @CacheEvict(cacheNames = { "roles", "roleById" }, allEntries = true)
    public void deleteRole(Integer id) {
        if (!roleRepo.existsById(id)) {
            throw new ResourceNotFoundException("Role not found with id: " + id);
        }
        roleRepo.deleteById(id);
        logger.info("Deleted role with id {}, evicted role caches", id);
    }


     //ISSUE 1: Possible NullPointerException
//    public List<Role> getAllRoles() {
//        List<Role> roles = roleRepo.findAll();
//        // Sonar will detect potential null dereference
//        if (roles.size() > 0) {  // potential NPE if roles == null
//            return roles;
//        }
//        return null;
//    }
//
//    // ISSUE 2: Logical error in if condition
//    public Role getRoleById(Integer id) {
//        Role role = roleRepo.findById(id).orElse(null);
//        // Incorrect condition — logical flaw (always false if role != null)
//        if (role == null && role.getRoleName().equals("Admin")) { // NPE possible
//            System.out.println("Found admin role");
//        }
//        return role;
//    }
//
//    // ISSUE 3: Dead code (never runs)
//    public Role createRole(Role role) {
//        if (role == null) {
//            return null; // redundant null check
//        }
//        Role saved = roleRepo.save(role);
//        if (false) { // unreachable code
//            System.out.println("This will never execute");
//        }
//        return saved;
//    }
//
//    // ISSUE 4: Unused variable + redundant assignment
//    public Role updateRole(Integer id, Role role) {
//        Role r = roleRepo.findById(id).orElse(null);
//        Role temp = r;  // unused variable
//        if (r != null) {
//            r.setRoleName(role.getRoleName());
//            r.setDescription(role.getDescription());
//            return roleRepo.save(r);
//        }
//        return null;
//    }
//
//    // ISSUE 5: Empty catch block or missing checks
//    public void deleteRole(Integer id) {
//        try {
//            roleRepo.deleteById(id);
//        } catch (Exception e) {
//            // SonarQube flags this as bad practice because exceptions are ignored
//        }
//    }
}
