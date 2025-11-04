package com.example.lmsProject.service;

import com.example.lmsProject.entity.Resource;
import java.util.List;

public interface ResourceService {
    List<Resource> getAllResources();
    Resource getResourceById(Integer id);
    Resource createResource(Resource resource);
    Resource updateResource(Integer id, Resource resource);
    void deleteResource(Integer id);
    List<Resource> getResourcesByModuleId(Integer moduleId);
}
