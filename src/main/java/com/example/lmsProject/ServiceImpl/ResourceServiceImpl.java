package com.example.lmsProject.ServiceImpl;

import com.example.lmsProject.Repository.ResourceRepository;
import com.example.lmsProject.dto.ResourceDto;
import com.example.lmsProject.entity.Course;
import com.example.lmsProject.entity.Module;
import com.example.lmsProject.entity.Resource;
import com.example.lmsProject.entity.User;
import com.example.lmsProject.exception.ResourceNotFoundException;
import com.example.lmsProject.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ResourceServiceImpl implements ResourceService {

    private static final Logger logger = LoggerFactory.getLogger(ResourceServiceImpl.class);
    private final ResourceRepository resourceRepository;
    private final StorageService storageService;
    private final UserService userService;
    private final CourseService courseService;
    private final ModuleService moduleService;

    public ResourceServiceImpl(
            ResourceRepository repo,
            StorageService storageService,
            UserService userService,
            CourseService courseService,
            ModuleService moduleService
    ) {
        this.resourceRepository = repo;
        this.storageService = storageService;
        this.userService = userService;
        this.courseService = courseService;
        this.moduleService = moduleService;
    }

    @Override
    @Cacheable(cacheNames = "resources")
    public List<Resource> getAllResources() {
        logger.info("Fetching all resources from database (cache miss)");
        return resourceRepository.findAll();
    }

    @Override
    @Cacheable(cacheNames = "resourceById", key = "#id")
    public Resource getResourceById(Integer id) {
        logger.info("Fetching resource by id {} from database (cache miss)", id);
        return resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with id: " + id));
    }

    @Override
    @Cacheable(cacheNames = "resourcesByModule", key = "#moduleId")
    public List<Resource> getResourcesByModuleId(Integer moduleId) {
        logger.info("Fetching resources by moduleId {} from database (cache miss)", moduleId);
        return resourceRepository.findByModule_moduleId(moduleId);
    }

    @Override
    @CacheEvict(cacheNames = { "resources", "resourceById", "resourcesByModule" }, allEntries = true)
    public Resource createResource(ResourceDto dto) throws IOException {
        Resource resource = new Resource();

        if (dto.getFile() != null) {
            String key = "resource/" + dto.getCourseId() + "/" + dto.getModuleId() + "/" + dto.getTitle() + "/"
                    + System.currentTimeMillis() + "_" + dto.getFile().getOriginalFilename();

            String s3Key = storageService.uploadFile(
                    key,
                    dto.getFile().getInputStream(),
                    dto.getFile().getSize(),
                    dto.getFile().getContentType()
            );
            resource.setFileUrl(s3Key);
        }

        User user = userService.getUserById(dto.getUploadedBy());
        Course course = courseService.getCourseById(dto.getCourseId());
        Module module = moduleService.getModuleById(dto.getModuleId());
        // These calls will throw ResourceNotFoundException if not found, so no manual null checks needed.

        resource.setCourse(course);
        resource.setUploadedBy(user);
        resource.setTitle(dto.getTitle());
        resource.setModule(module);
        resource.setUploadDate(LocalDateTime.now());

        Resource saved = resourceRepository.save(resource);
        logger.info("Created resource with id {}, evicted resource caches", saved.getResourceId());
        return saved;
    }

    @Override
    @CacheEvict(cacheNames = { "resources", "resourceById", "resourcesByModule" }, allEntries = true)
    public Resource updateResource(Integer id, ResourceDto dto) {
        Resource existingResource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with id: " + id));

        if (dto.getTitle() != null) {
            existingResource.setTitle(dto.getTitle());
        }

        if (dto.getFile() != null) {
            String key = "resource/" + dto.getCourseId() + "/" + dto.getModuleId() + "/" + dto.getTitle() + "/"
                    + System.currentTimeMillis() + "_" + dto.getFile().getOriginalFilename();

            try {
                String s3Key = storageService.uploadFile(
                        key,
                        dto.getFile().getInputStream(),
                        dto.getFile().getSize(),
                        dto.getFile().getContentType()
                );
                existingResource.setFileUrl(s3Key);
            } catch (IOException e) {
                throw new RuntimeException("Error uploading resource file to storage", e);
            }
        }

        existingResource.setUploadDate(LocalDateTime.now());

        if (dto.getUploadedBy() != null) {
            User user = userService.getUserById(dto.getUploadedBy());
            existingResource.setUploadedBy(user);
        }

        Resource updated = resourceRepository.save(existingResource);
        logger.info("Updated resource with id {}, evicted resource caches", id);
        return updated;
    }

    @Override
    @CacheEvict(cacheNames = { "resources", "resourceById", "resourcesByModule" }, allEntries = true)
    public void deleteResource(Integer id) {
        if (!resourceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Resource not found with id: " + id);
        }
        resourceRepository.deleteById(id);
        logger.info("Deleted resource with id {}, evicted resource caches", id);
    }
}
