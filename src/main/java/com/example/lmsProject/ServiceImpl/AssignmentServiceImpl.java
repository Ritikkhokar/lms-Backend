package com.example.lmsProject.ServiceImpl;

import com.example.lmsProject.Repository.AssignmentRepository;
import com.example.lmsProject.dto.AssignmentDto;
import com.example.lmsProject.entity.Assignment;
import com.example.lmsProject.entity.Course;
import com.example.lmsProject.service.AssignmentService;
import com.example.lmsProject.service.CourseService;
import com.example.lmsProject.service.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Timer;

@Service
public class AssignmentServiceImpl implements AssignmentService {

    private static final Logger logger = LoggerFactory.getLogger(AssignmentServiceImpl.class);
    private final AssignmentRepository assignmentRepository;
    private final StorageService storageService;
    private final CourseService courseService;


    public AssignmentServiceImpl(AssignmentRepository repo, StorageService storageService, CourseService courseService) {
        this.assignmentRepository = repo;
        this.storageService = storageService;
        this.courseService = courseService;
    }

    @Override
    public List<Assignment> getAllAssignments() {
        return assignmentRepository.findAll();
    }

    @Override
    public Assignment getAssignmentById(Integer id) {
        return assignmentRepository.findById(id).orElse(null);
    }

    @Override
    public Assignment createAssignment(AssignmentDto dto) throws IOException {
        String key = "assignments/" + dto.getCourseId() + "/" + dto.getTitle() + "/"
                + System.currentTimeMillis() + "_" + dto.getFile().getOriginalFilename();
        String s3Key = storageService.uploadFile(
                key,
                dto.getFile().getInputStream(),
                dto.getFile().getSize(),
                dto.getFile().getContentType()
        );
        Course course = courseService.getCourseById(dto.getCourseId());
        if(course == null){
            throw new RuntimeException("Course not found");
        }
        Assignment assignment = new Assignment();
        assignment.setCourse(course);
        assignment.setTitle(dto.getTitle());
        assignment.setDescription(dto.getDescription());
        assignment.setDueDate(dto.getDueDate());
        assignment.setFileKey(s3Key);
        assignment.setCreatedAt(LocalDateTime.now());
        return assignmentRepository.save(assignment);
    }

    @Override
    public Assignment updateAssignment(Integer id, AssignmentDto dto) {
        return assignmentRepository.findById(id).map(existing -> {
            if(dto.getTitle() != null){
                existing.setTitle(dto.getTitle());
            }
            if(dto.getDescription() != null){
                existing.setDescription(dto.getDescription());
            }
            if(dto.getDueDate() != null){
                existing.setDueDate(dto.getDueDate());
            }
            // Upload new file if present (file update is optional)
            MultipartFile file = dto.getFile();
            if (file != null && !file.isEmpty()) {
                String newKey = "assignments/" + dto.getCourseId() + "/" + dto.getTitle() + "/"
                        + System.currentTimeMillis() + "_" + dto.getFile().getOriginalFilename();
                try {
                    storageService.uploadFile(
                            newKey,
                            file.getInputStream(),
                            file.getSize(),
                            file.getContentType()
                    );
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                existing.setFileKey(newKey);
            }
            // Update timestamp (optional for updatedAt, not createdAt!)
//             existing.setCreatedAt(LocalDateTime.now()); // Do NOT change createdAt on update. Add an updatedAt instead if you want.
            return assignmentRepository.save(existing);
        }).orElseThrow(() -> new RuntimeException("Assignment not found"));
    }

    @Override
    public void deleteAssignment(Integer id) {
        assignmentRepository.deleteById(id);
    }

    @Override
    public List<Assignment> getAssignmentsByCourseId(Integer courseId) {
        return assignmentRepository.findByCourse_CourseId(courseId);
    }
}
