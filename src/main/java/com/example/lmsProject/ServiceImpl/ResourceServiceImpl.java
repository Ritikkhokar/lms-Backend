package com.example.lmsProject.ServiceImpl;

import com.example.lmsProject.Repository.ResourceRepository;
import com.example.lmsProject.dto.ResourceDto;
import com.example.lmsProject.entity.Course;
import com.example.lmsProject.entity.Module;
import com.example.lmsProject.entity.Resource;
import com.example.lmsProject.entity.User;
import com.example.lmsProject.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    public ResourceServiceImpl(ResourceRepository repo, StorageService storageService, UserService userService, CourseService courseService, ModuleService moduleService) {
        this.resourceRepository = repo;
        this.storageService = storageService;
        this.userService = userService;
        this.courseService = courseService;
        this.moduleService = moduleService;
    }

    @Override
    public List<Resource> getAllResources() {
        return resourceRepository.findAll();
    }

    @Override
    public Resource getResourceById(Integer id) {
        return resourceRepository.findById(id).orElse(null);
    }

    @Override
    public Resource createResource(ResourceDto dto) throws IOException {
        Resource resource = new Resource();
        User user = userService.getUserById(dto.getUploadedBy());
        if (user == null){
            throw new RuntimeException("user not found");
        }
        Course course = courseService.getCourseById(dto.getCourseId());
        if (course == null){
            throw new RuntimeException("course not found");
        }
        Module module = moduleService.getModuleById(dto.getModuleId());
        if (module == null){
            throw new RuntimeException("module not found");
        }
        resource.setCourse(course);
        resource.setUploadedBy(user);
        resource.setTitle(dto.getTitle());
        resource.setModule(module);
        resource.setUploadDate(LocalDateTime.now());
        return resourceRepository.save(resource);
    }

    @Override
    public Resource updateResource(Integer id, ResourceDto dto) {
        return resourceRepository.findById(id).map(existingResource -> {
            if(dto.getTitle() != null){
                existingResource.setTitle(dto.getTitle());
            }
            if (dto.getFile() != null) {
                String key = "submissions/" + dto.getCourseId() + "/" + dto.getModuleId() + "/" + dto.getTitle() + "/"
                        + System.currentTimeMillis() + "_" + dto.getFile().getOriginalFilename();
                String s3Key = null;
                try {
                    s3Key = storageService.uploadFile(
                            key,
                            dto.getFile().getInputStream(),
                            dto.getFile().getSize(),
                            dto.getFile().getContentType()
                    );
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                existingResource.setFileUrl(s3Key);
            }
            existingResource.setUploadDate(LocalDateTime.now());
            User user = userService.getUserById(dto.getUploadedBy());
            if (user == null){
                throw new RuntimeException("user not found");
            }
            existingResource.setUploadedBy(user);
            return resourceRepository.save(existingResource);
        }).orElse(null);
    }

    @Override
    public void deleteResource(Integer id) {
        resourceRepository.deleteById(id);
    }

    @Override
    public List<Resource> getResourcesByModuleId(Integer moduleId) {
        return resourceRepository.findByModule_moduleId(moduleId);
    }
}
