package com.example.lmsProject.dto;

import com.example.lmsProject.entity.Course;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AverageMarks {
    private UserDto userDto;
    private Integer totalMArks;
    private Integer marksObtained;
    private Integer averagePercentage;
}
