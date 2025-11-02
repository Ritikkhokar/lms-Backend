package com.example.lmsProject.ServiceImpl;

import com.example.lmsProject.Controller.AuthController;
import com.example.lmsProject.Repository.ModuleRepository;
import com.example.lmsProject.service.ModuleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.example.lmsProject.entity.Module;
import java.util.List;

@Service
public class ModuleServiceImpl implements ModuleService {

    private static final Logger logger = LoggerFactory.getLogger(ModuleServiceImpl.class);
    private final ModuleRepository moduleRepository;

    public ModuleServiceImpl(ModuleRepository repo) {
        this.moduleRepository = repo;
    }

    @Override
    public List<Module> getAllModules() {
        return moduleRepository.findAll();
    }

    @Override
    public Module getModuleById(Integer id) {
        return moduleRepository.findById(id).orElse(null);
    }

    @Override
    public Module createModule(Module module) {
        return moduleRepository.save(module);
    }

    @Override
    public Module updateModule(Integer id, Module module) {
        return moduleRepository.findById(id).map(existingModule -> {
            existingModule.setTitle(module.getTitle());
            existingModule.setDescription(module.getDescription());
            existingModule.setCourse(module.getCourse());
            return moduleRepository.save(existingModule);
        }).orElse(null);
    }

    @Override
    public void deleteModule(Integer id) {
        moduleRepository.deleteById(id);
    }
}
