package com.example.lmsProject.service;

import com.example.lmsProject.dto.ResourceDto;
import com.example.lmsProject.entity.Resource;

import java.io.IOException;
import java.util.List;

public interface ResourceService {
    List<Resource> getAllResources();
    Resource getResourceById(Integer id);
    Resource createResource(ResourceDto resourceDto) throws IOException;
    Resource updateResource(Integer id, ResourceDto resourceDto);
    void deleteResource(Integer id);
    List<Resource> getResourcesByModuleId(Integer moduleId);
}
