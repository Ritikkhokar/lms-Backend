package com.example.lmsProject.Repository;

import com.example.lmsProject.entity.Module;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ModuleRepository extends JpaRepository<Module, Integer> {
    List<Module> findByCourse_CourseId(Integer courseId);
}

