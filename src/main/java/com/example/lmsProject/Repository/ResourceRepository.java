package com.example.lmsProject.Repository;

import com.example.lmsProject.entity.Resource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResourceRepository extends JpaRepository<Resource, Integer> {
    List<Resource> findByModule_moduleId(Integer moduleId);
}

