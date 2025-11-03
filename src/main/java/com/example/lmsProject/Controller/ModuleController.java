package com.example.lmsProject.Controller;

import com.example.lmsProject.service.ModuleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.util.List;
import com.example.lmsProject.entity.Module;

@RestController
@RequestMapping("/api/modules")
public class ModuleController {

    private static final Logger logger = LoggerFactory.getLogger(ModuleController.class);
    private final ModuleService moduleService;

    public ModuleController(ModuleService service) {
        this.moduleService = service;
    }

    @GetMapping
    public List<Module> getAllModules() {
        return moduleService.getAllModules();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Module> getModuleById(@PathVariable Integer id) {
        Module module = moduleService.getModuleById(id);
        if (module == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(module);
    }

    @PostMapping
    public ResponseEntity<Module> createModule(@RequestBody Module module) {
        Module created = moduleService.createModule(module);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Module> updateModule(@PathVariable Integer id, @RequestBody Module module) {
        Module updated = moduleService.updateModule(id, module);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteModule(@PathVariable Integer id) {
        moduleService.deleteModule(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/getModulesByCourseId/{id}")
    public ResponseEntity<List<Module>> getModulesByCourseId(@PathVariable Integer id){
        return ResponseEntity.ok( moduleService.getModulesByCourseId(id));
    }
}
