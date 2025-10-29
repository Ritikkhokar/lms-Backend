package com.example.lmsProject.Repository;

import com.example.lmsProject.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {}

