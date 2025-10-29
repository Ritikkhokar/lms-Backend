package com.example.lmsProject.service;
import com.example.lmsProject.entity.Module;
import java.util.List;

public interface ModuleService {
    List<Module> getAllModules();
    Module getModuleById(Integer id);
    Module createModule(Module module);
    Module updateModule(Integer id, Module module);
    void deleteModule(Integer id);
}
