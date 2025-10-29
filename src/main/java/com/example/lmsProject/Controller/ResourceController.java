package com.example.lmsProject.Controller;

import com.example.lmsProject.entity.Resource;
import com.example.lmsProject.service.ResourceService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.util.List;

@RestController
@RequestMapping("/api/resources")
public class ResourceController {

    private final ResourceService resourceService;

    public ResourceController(ResourceService service) {
        this.resourceService = service;
    }

    @GetMapping
    public List<Resource> getAllResources() {
        return resourceService.getAllResources();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Resource> getResourceById(@PathVariable Integer id) {
        Resource resource = resourceService.getResourceById(id);
        if (resource == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(resource);
    }

    @PostMapping
    public ResponseEntity<Resource> createResource(@RequestBody Resource resource) {
        Resource created = resourceService.createResource(resource);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Resource> updateResource(@PathVariable Integer id, @RequestBody Resource resource) {
        Resource updated = resourceService.updateResource(id, resource);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResource(@PathVariable Integer id) {
        resourceService.deleteResource(id);
        return ResponseEntity.noContent().build();
    }
}
