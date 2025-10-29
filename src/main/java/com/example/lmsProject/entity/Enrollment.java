package com.example.lmsProject.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(
        name = "enrollments",
        uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "course_id"})
)
public class Enrollment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer enrollmentId;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "enrolled_at")
    private LocalDateTime enrolledAt;

    @ManyToOne
    @JoinColumn(name = "enrolled_by")
    private User enrolledBy; // Can be null (optional)
}
