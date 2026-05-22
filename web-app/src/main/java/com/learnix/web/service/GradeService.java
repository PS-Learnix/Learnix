package com.learnix.web.service;

import com.learnix.web.dto.ActivityDetailResponse;
import com.learnix.web.dto.ActivityStatsResponse;
import com.learnix.web.dto.StudentGradeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GradeService {

    private final JdbcTemplate jdbcTemplate;

    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
    public List<StudentGradeResponse> getActivityGradesSp(Integer idActivity) {
        return jdbcTemplate.execute((ConnectionCallback<List<StudentGradeResponse>>) connection -> {
            CallableStatement cs = connection.prepareCall("CALL public.sp_get_activity_grades(?, ?)");
            cs.setInt(1, idActivity);
            cs.registerOutParameter(2, Types.OTHER);
            cs.execute();

            try (ResultSet rs = (ResultSet) cs.getObject(2)) {
                List<StudentGradeResponse> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(new StudentGradeResponse(
                            rs.getInt("id_student"), rs.getString("first_name"),
                            rs.getString("last_name"), rs.getString("grade_value")
                    ));
                }
                return list;
            }
        });
    }

    @Transactional(readOnly = true)
    public ActivityStatsResponse getActivityStatsSp(Integer idActivity) {
        return jdbcTemplate.execute((ConnectionCallback<ActivityStatsResponse>) connection -> {
            CallableStatement cs = connection.prepareCall("CALL public.sp_stats_registro_notas(?, ?, ?, ?)");
            cs.setInt(1, idActivity);
            cs.registerOutParameter(2, Types.INTEGER);
            cs.registerOutParameter(3, Types.INTEGER);
            cs.registerOutParameter(4, Types.INTEGER);
            cs.execute();

            return new ActivityStatsResponse(cs.getInt(2), cs.getInt(3), cs.getInt(4));
        });
    }

    @Transactional
    public void saveStudentGradeSp(Integer idStudent, Integer idActivity, String value) {
        jdbcTemplate.execute((ConnectionCallback<Object>) connection -> {
            CallableStatement cs = connection.prepareCall("CALL public.sp_save_student_grade(?, ?, ?)");
            cs.setInt(1, idStudent);
            cs.setInt(2, idActivity);
            cs.setString(3, value);
            cs.execute();
            return null;
        });
    }
}