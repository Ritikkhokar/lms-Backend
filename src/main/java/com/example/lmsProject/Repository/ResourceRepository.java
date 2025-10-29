package com.example.lmsProject.Repository;

import com.example.lmsProject.entity.Course;
import com.example.lmsProject.entity.Resource;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResourceRepository extends JpaRepository<Resource, Integer> {}

