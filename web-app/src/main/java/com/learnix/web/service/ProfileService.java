package com.learnix.web.service;

import com.learnix.web.dto.TeacherProfileCourse;
import com.learnix.web.dto.TeacherStatsResponse;
import com.learnix.web.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;

    public TeacherStatsResponse getTeacherStatsSp(Integer idTeacher) {
        return profileRepository.getTeacherStats(idTeacher);
    }

    public List<TeacherProfileCourse> getTeacherProfileCoursesSp(Integer idTeacher) {
        return profileRepository.getTeacherProfileCourses(idTeacher);
    }
}