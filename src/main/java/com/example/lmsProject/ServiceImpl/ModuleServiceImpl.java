package com.example.lmsProject.ServiceImpl;

import com.example.lmsProject.Repository.ModuleRepository;
import com.example.lmsProject.entity.Module;
import com.example.lmsProject.exception.ResourceNotFoundException;
import com.example.lmsProject.service.ModuleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ModuleServiceImpl implements ModuleService {

    private static final Logger logger = LoggerFactory.getLogger(ModuleServiceImpl.class);
    private final ModuleRepository moduleRepository;

    public ModuleServiceImpl(ModuleRepository repo) {
        this.moduleRepository = repo;
    }

    @Override
    @Cacheable(cacheNames = "modules")
    public List<Module> getAllModules() {
        logger.info("Fetching all modules from database (cache miss)");
        return moduleRepository.findAll();
    }

    @Override
    @Cacheable(cacheNames = "moduleById", key = "#id")
    public Module getModuleById(Integer id) {
        logger.info("Fetching module by id {} from database (cache miss)", id);
        return moduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found with id: " + id));
    }

    @Override
    @Cacheable(cacheNames = "modulesByCourse", key = "#courseId")
    public List<Module> getModulesByCourseId(Integer courseId) {
        logger.info("Fetching modules by courseId {} from database (cache miss)", courseId);
        return moduleRepository.findByCourse_CourseId(courseId);
    }

    @Override
    @CacheEvict(cacheNames = { "modules", "moduleById", "modulesByCourse" }, allEntries = true)
    public Module createModule(Module module) {
        Module saved = moduleRepository.save(module);
        logger.info("Created module with id {}, evicted module caches", saved.getModuleId());
        return saved;
    }

    @Override
    @CacheEvict(cacheNames = { "modules", "moduleById", "modulesByCourse" }, allEntries = true)
    public Module updateModule(Integer id, Module module) {
        Module existingModule = moduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found with id: " + id));

        if (module.getTitle() != null) {
            existingModule.setTitle(module.getTitle());
        }
        if (module.getDescription() != null) {
            existingModule.setDescription(module.getDescription());
        }
        if (module.getCourse() != null) {
            existingModule.setCourse(module.getCourse());
        }

        Module updated = moduleRepository.save(existingModule);
        logger.info("Updated module with id {}, evicted module caches", id);
        return updated;
    }

    @Override
    @CacheEvict(cacheNames = { "modules", "moduleById", "modulesByCourse" }, allEntries = true)
    public void deleteModule(Integer id) {
        if (!moduleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Module not found with id: " + id);
        }
        moduleRepository.deleteById(id);
        logger.info("Deleted module with id {}, evicted module caches", id);
    }
}
