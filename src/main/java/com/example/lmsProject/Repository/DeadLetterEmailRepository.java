package com.example.lmsProject.Repository;

import com.example.lmsProject.entity.DeadLetterEmail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeadLetterEmailRepository extends JpaRepository<DeadLetterEmail, Long> {
}
