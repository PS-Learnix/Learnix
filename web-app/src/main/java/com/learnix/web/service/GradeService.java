package com.learnix.web.service;

import com.learnix.web.dto.ActivityDetailResponse;
import com.learnix.web.dto.ActivityStatsResponse;
import com.learnix.web.dto.StudentGradeResponse;
import com.learnix.web.repository.GradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GradeService {

    private final JdbcTemplate jdbcTemplate;
    private final GradeRepository gradeRepository;

    public ActivityDetailResponse getActivityDetailSp(Integer idActivity) {
        String sql = "SELECT a.id_activity, a.name AS act_name, c.name AS cur_name " +
                "FROM activities a " +
                "JOIN course_periods cp ON a.id_course_period = cp.id_course_period " +
                "JOIN courses c ON cp.id_course = c.id_course WHERE a.id_activity = ?";
        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> new ActivityDetailResponse(
                rs.getInt("id_activity"),
                rs.getString("act_name"),
                rs.getString("cur_name")
        ), idActivity);
    }

    public List<StudentGradeResponse> getActivityGradesSp(Integer idActivity) {
        return gradeRepository.getActivityGrades(idActivity);
    }

    public ActivityStatsResponse getActivityStatsSp(Integer idActivity) {
        return gradeRepository.getActivityStats(idActivity);
    }

    @Transactional
    public void saveStudentGradeSp(Integer idStudent, Integer idActivity, String value) {
        gradeRepository.saveStudentGrade(idStudent, idActivity, value);
    }
}