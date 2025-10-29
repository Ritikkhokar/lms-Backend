package com.example.lmsProject.ServiceImpl;

import com.example.lmsProject.Repository.ResourceRepository;
import com.example.lmsProject.entity.Resource;
import com.example.lmsProject.service.ResourceService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ResourceServiceImpl implements ResourceService {

    private final ResourceRepository resourceRepository;

    public ResourceServiceImpl(ResourceRepository repo) {
        this.resourceRepository = repo;
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
    public Resource createResource(Resource resource) {
        return resourceRepository.save(resource);
    }

    @Override
    public Resource updateResource(Integer id, Resource resource) {
        return resourceRepository.findById(id).map(existingResource -> {
            existingResource.setTitle(resource.getTitle());
            existingResource.setFileUrl(resource.getFileUrl());
            existingResource.setUploadDate(resource.getUploadDate());
            existingResource.setCourse(resource.getCourse());
            existingResource.setModule(resource.getModule());
            existingResource.setUploadedBy(resource.getUploadedBy());
            return resourceRepository.save(existingResource);
        }).orElse(null);
    }

    @Override
    public void deleteResource(Integer id) {
        resourceRepository.deleteById(id);
    }
}
