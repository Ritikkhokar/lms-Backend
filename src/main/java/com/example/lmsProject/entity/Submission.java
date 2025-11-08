package com.example.lmsProject.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "submissions")
public class Submission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer submissionId;

    @ManyToOne
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Column(name = "submission_url", nullable = false)
    private String submissionUrl;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(precision = 5, scale = 2)
    private BigDecimal grade;

    @Column(precision = 5, scale = 2)
    private BigDecimal maximumGrade;

    @Column(name = "is_graded")
    private Boolean is_graded = false;

    private String feedback;
}
