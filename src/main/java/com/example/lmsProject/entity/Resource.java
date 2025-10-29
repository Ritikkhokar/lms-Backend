package com.example.lmsProject.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "resources")
public class Resource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer resourceId;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    @ManyToOne
    @JoinColumn(name = "module_id")
    private Module module;

    @ManyToOne
    @JoinColumn(name = "uploaded_by", nullable = false)
    private User uploadedBy;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String fileUrl;

    @Column(name = "upload_date", columnDefinition = "TIMESTAMP")
    private LocalDateTime uploadDate;
}
